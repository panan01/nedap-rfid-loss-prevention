(function() {
    /*
     * Basic error messages used throughout the command parser
     */
    const ERRORS = {
        invalid_argument_format: "Argument name may not begin with '-$'",
        invalid_value: (name) => `Invalid value '-${name}'`,
        unknown_command: (name) => `Unknown command '${name}'`,
        unknown_argument: (name) => `Invalid argument '-${name}'`,
        unknown_main_argument: `No main parameters allowed`,
        mismatched_param_count: (name, expected, found) => {
            if (name === "$main")
                return `Mismatched parameters for command, expected ${expected}, found ${found}`;
            return `Mismatched parameters for '-${name}', expected ${expected}, found ${found}`;
        },
        double_argument: (name) => `Double argument '-${name}'`,
        nan: (num) => `Not a number '${num}'`
    };

    /**
     * (Internal)
     * A token reader is used to read tokens from in `preParseCommand`, and can
     * eventually be used to render the command as highlighted HTML.
     */
    class TokenReader {
        constructor(tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        /**
         * Checks if there is another token available for reading
         */
        hasNext() {
            return this.pos < this.tokens.length;
        }

        /**
         * Looks up the token at the current position and increases the position
         */
        read() {
            return this.tokens[this.pos ++];
        }

        /**
         * Looks up the token at the current position without increasing the position
         */
        peek() {
            return this.tokens[this.pos];
        }

        /**
         * Sets the current position back to 0
         */
        reset() {
            this.pos = 0;
        }

        /**
         * Renders a highlighted HTML element using the types assigned to tokens
         */
        renderHighlighted() {
            let html = document.createElement("span");
            html.className = "highlighted-command";
            
            for (let token of this.tokens) {
                let span = document.createElement("span");
                for (let type of token.types) {
                    span.className += " highlight-" + type;
                }
                if (token.errors.length > 0) {
                    span.className += " highlight-error";
                }
                span.innerText = token.rawText;
                html.appendChild(span);
            }

            return html;
        }
    }

    /**
     * Determines the structure of the command read from the given command context. It
     * groups arguments with their parameter values, but does not validate arguments nor
     * argument values.
     * @param {TokenReader} ctx The command token context
     * @returns {{
     *  command: string,
     *  firstToken: Object,
     *  foundArgs: {main: boolean, token: Object, name: string, values: string[]}[]
     * }} The parsed command data
     */
    function preParseCommand(ctx) {
        // Commands look like this
        // /command mainvalue mainvalue -arg argvalue -arg2 arg2value arg2value

        // First token is command name
        let firstToken = ctx.read();
        firstToken.type = ["command"];

        // Read arguments and values
        let foundArgs = [];

        let lastArg = { // Initial argument
            main: true,
            token: firstToken,
            name: "$main",
            values: []
        };

        while (ctx.hasNext()) {
            let arg = ctx.read();

            if (arg.text[0] === '-') {
                // Argument (all arguments start with a dash)
                arg.type = ["argument"];

                if (lastArg) {
                    foundArgs.push(lastArg);
                }

                let argname = arg.text.substring(1); // strip argument dash

                lastArg = {
                    main: false,
                    token: arg,
                    name: argname,
                    values: []
                };
            } else {
                // Value
                arg.type = ["value"];
                
                // This is an argument input
                arg.type.push("argv");
                lastArg.values.push(arg);
            }
        }
        
        foundArgs.push(lastArg);

        return {
            command: firstToken.text,
            firstToken: firstToken,
            foundArgs: foundArgs
        };
    }

    /**
     * Creates a parser function for an argument that only accepts the given literals as values.
     * @param {string[]} literals The literals
     * @returns {function(string): {match: boolean, value?: any, error?: string}}
     */
    function createLiteralParser(literals) {
        return function(str) {
            if (literals.includes(str)) {
                return {match: true, value: str};
            } else {
                return {match: false, error: ERRORS.invalid_value(str)};
            }
        }
    }

    /**
     * Creates a suggestor function for an argument that only accepts the given literals as values.
     * @param {string[]} literals The literals
     * @returns {function(string): string[]}
     */
    function createLiteralSuggestor(literals) {
        return function(str) {
            let match = [];
            for (let literal of literals) {
                if (literal.length >= str.length && literal.substring(0, str.length) === str) {
                    match.push(literal);
                }
            }

            return match;
        }
    }

    /**
     * An instance of an argument definition.
     */
    class Argument {
        constructor(o) {
            this.name = o.name;
            this.params = [];
            this.required = o.required || false;
            this.multi = o.multi || false;

            const pars = o.params || (o.param ? [o.param] : []);
            for (let param of pars) {
                if (Array.isArray(param)) {
                    this.params.push({
                        parser: createLiteralParser(param),
                        suggestor: createLiteralSuggestor(param)
                    });
                } else {
                    if (Array.isArray(param.literals)) {
                        this.params.push({
                            parser: createLiteralParser(param.literals),
                            suggestor: createLiteralSuggestor(param.literals)
                        });
                    } else {
                        if (typeof param.parser !== "function") {
                            throw new Error(`Parameter for argument '${this.name}' has no parser function`);
                        }
                        let suggestor = param.suggestor;
                        if (typeof suggestor !== "function") {
                            suggestor = () => [];
                        }

                        this.params.push({
                            parser: param.parser,
                            suggestor: suggestor
                        });
                    }
                }
            }
        }

        /**
         * Validates the parsed argument data against this definition.
         * @param {Object} arg The argument data, generated by `preParseCommand`
         * @param {function(string, Object): void} genError Function that consumes errors
         * @returns {{error: boolean, result?: any[]}} The parse result
         */
        parse(arg, genError) {
            if (arg.values.length !== this.params.length) {
                genError(ERRORS.mismatched_param_count(arg.name, this.params.length, arg.values.length), arg.token);
                return {error: true};
            }

            let result = [];

            for (let i = 0, l = this.params.length; i < l; i ++) {
                let val = arg.values[i];
                let def = this.params[i];

                let res = def.parser(val.text);
                if (!res.match) {
                    genError(res.error, val);
                    return {error: true};
                }

                result.push(res.value);
            }

            return {
                error: false,
                result: result
            };
        }
    }

    /**
     * An instance of a command definition.
     */
    class Command {
        constructor(o) {
            const aliases = o.aliases || (o.alias ? [o.alias] : []);
            const args = o.args || [];

            if (aliases.length === 0) {
                throw new ReferenceError("Command with no aliases");
            }

            this.aliases = aliases;
            this.args = [];
            this.requiredArgs = [];
            this.argsByName = {};
            if (typeof o.trigger === "function") {
                this.trigger = o.trigger
            }

            for (let arg of args) {
                let inst = new Argument(arg);
                this.args.push(inst);
                this.argsByName[inst.name] = inst;

                if (inst.name !== "$main") {
                    this.requiredArgs.push(inst.name);
                }
            }

            if (!this.argsByName.$main) {
                let inst = new Argument({name: "$main"});
                this.args.push(inst);
                this.argsByName.$main = inst;
            }
        }

        /**
         * Triggers the command. Method is overwritten by constructor.
         * @param {CommandResult} result The command result, to read arguments from
         * @return {any} Any preferred value returned by the command trigger
         */
        trigger() {
        }

        /**
         * Matches and validates the given preparsed command against this definition.
         * @param {{
         *  command: string,
         *  firstToken: Object,
         *  foundArgs: {main: boolean, token: Object, name: string, values: string[]}[]
         * }} preparsed The pre-parsed command data
         * 
         * @returns {{match: boolean, errors?: string[], argData?: Object}}
         */
        match(preparsed) {
            if (!this.aliases.includes(preparsed.command)) {
                return {
                    match: false
                };
            }

            let errors = [];
            let argData = {};

            let required = this.requiredArgs.slice()

            function generateError(error, token) {
                errors.push(error);
                token.errors.push(error);
            }

            for (let arg of preparsed.foundArgs) {
                if (!arg.main && arg.name[0] === "$") {
                    generateError(ERRORS.invalid_argument_format, arg.token);
                    continue;
                }

                let definition = this.argsByName[arg.name];
                if (!definition) {
                    generateError(ERRORS.unknown_argument(arg.name), arg.token);
                    continue;
                }

                let parsed = definition.parse(arg, generateError);
                if (!parsed.error) {
                    let data = argData[arg.name] || {};

                    if (!Array.isArray(data.values) || data.values.length === 0) {
                        data.values = [parsed.result];
                    } else if (!definition.multi) {
                        generateError(ERRORS.double_argument(arg.name), arg.token);
                        continue;
                    } else {
                        data.values.push(parsed.result);
                    }

                    argData[arg.name] = data;

                    
                    for (let i = 0; i < required.length; i++) { 
                        if (required[i] === arg.name) { 
                            required.splice(i, 1); 
                            i --;
                        }
                    }
                }
            }

            if (required.length > 0) {
                // TODO: Errors
            }

            return {
                match: true,
                errors: errors,
                argData: argData
            };
        }
    }

    /**
     * Tokenizes a command input string.
     * @param {string} command The command string
     * @returns {Object[]} The tokenized command
     */
    function tokenizeCommand(command) {
        var tokens = [];

        let pos = 0;
        let len = command.length;

        let start = -1;
        let rawStart = 0;
        let quotes = null;
        while (pos < len) {
            let char = command[pos];

            if (quotes) {
                if (char === quotes) {
                    tokens.push({
                        text: command.substring(start, pos),
                        rawText: command.substring(rawStart, pos + 1),
                        start: start,
                        end: pos,
                        type: ["raw"],
                        errors: []
                    });

                    start = -1;
                    rawStart = pos + 1;
                    quotes = null;
                }
            } else {
                if (char === ' ') {
                    if (start >= 0) {
                        tokens.push({
                            text: command.substring(start, pos),
                            rawText: command.substring(rawStart, pos),
                            start: start,
                            end: pos,
                            type: ["raw"],
                            errors: []
                        });
    
                        start = -1;
                        rawStart = pos;
                    }
                } else if (char === '\'' || char === '"') {
                    quotes = char;
                    start = pos + 1;
                } else if (start < 0) {
                    start = pos;
                }
            }

            pos ++;
        }

        if (start >= 0) {
            tokens.push({
                text: command.substring(start, pos),
                start: start,
                end: pos,
                type: ["raw"],
                errors: []
            });
        }

        return tokens;
    }

    /**
     * The result object passed into command triggers.
     */
    class CommandResult {
        constructor(argData) {
            this.argData = argData;
        }

        /**
         * Returns whether a parameter value is present for the argument of the given name.
         * Can be useful when defining flags, to see if a certain flag is set.
         * @param {string} name The argument name
         * @returns {boolean} True if a value is present
         */
        has(name) {
            return name in this.argData;
        }

        /**
         * Returns the parameter value of the given argument name
         * @param {string} name The argument name
         * @param {number} [index] The index of the value if there are multiple parameters
         * @returns {* | undefined} The parameter value or undefined if no value is present
         */
        value(name, index = 0) {
            if (!(name in this.argData)) {
                return undefined;
            }
            return this.argData[name].values[0][index];
        }

        /**
         * Returns the parameter values of the given argument name
         * @param {string} name The argument name
         * @returns {*[] | undefined} The parameter value or undefined if no value is present
         */
        values(name) {
            if (!(name in this.argData)) {
                return undefined;
            }
            return this.argData[name].values[0];
        }

        /**
         * Returns the parameter value for each occurence of the given argument name.
         * @param {string} name The argument name
         * @param {number} [index] The index of the value if there are multiple parameters per argument
         * @returns {*[] | undefined} The parameter value or undefined if no value is present
         */
        multiValue(name, index = 0) {
            if (!(name in this.argData)) {
                return undefined;
            }
            let out = [];

            for (let val of this.argData[name].values) {
                out.push(val[index]);
            }
            return out;
        }

        /**
         * Returns all parameter values for each occurence of the given argument name.
         * @param {string} name The argument name
         * @returns {*[][] | undefined} The parameter value or undefined if no value is present
         */
        multiValues(name) {
            if (!(name in this.argData)) {
                return undefined;
            }
            return this.argData[name].values;
        }
    }

    /**
     * A parser and dispatcher for commands. It combines the definition of all defined commands into
     * one object that can be used to parse an input string against all the definitions and get valuable
     * feedback for the user about the parsed command and any errors that may have occurred.
     */
    class CommandDispatcher {
        constructor(commands) {
            this.commands = [];

            // TODO Check double aliases

            for (let command of commands) {
                this.commands.push(new Command(command));
            }
        }

        /**
         * Parses the given command input against all command definitions and returns a result
         * object that can be used to show the user useful information about the input or to
         * execute the command.
         * 
         * @param {string} text The command text
         * @returns {{
         *  text: string,
         *  errors: string[],
         *  argData?: Object,
         *  result?: CommandResult,
         *  command?: Command,
         *  execute: function(): any,
         *  renderHighlighted?: function(): HTMLSpanElement
         * }} The result
         */
        parse(text) {
            let tokens = tokenizeCommand(text);
            if (tokens.length === 0) {
                let error = ERRORS.unknown_command("");
                return {
                    errors: [error],
                    text: text,
                    execute() {
                        throw new Error("Cannot execute command as parse errors are present");
                    }
                };
            }

            let context = new TokenReader(tokens);
            let preparsed = preParseCommand(context);

            for (let command of this.commands) {
                let match = command.match(preparsed);
                if (match.match) {
                    return {
                        text: text,
                        errors: match.errors,
                        argData: match.argData,
                        result: new CommandResult(match.argData),
                        command: command,
                        execute() {
                            if (this.errors.length > 0) {
                                throw new Error("Cannot execute command as parse errors are present");
                            }
                            return command.trigger(this.result);
                        },
                        renderHighlighted() {
                            return context.renderHighlighted();
                        }
                    };
                }
            }

            let error = ERRORS.unknown_command(preparsed.command);
            return {
                errors: [error],
                text: text,
                execute() {
                    throw new Error("Cannot execute command as parse errors are present");
                }
            };
        }
    }

    // Export CommandDispatcher
    window.CommandDispatcher = CommandDispatcher;


    // Basic argument types

    window.ArgTypes = {
        /**
         * An argument type allowing any value
         */
        string: {
            parser(str) {
                return {match: true, value: str};
            },
            suggestor() {
                return [];
            }
        },

        /**
         * An argument type allowing only integer numbers
         */
        int: {
            parser(str) {
                let i = parseInt(str);
                if (isNaN(i)) {
                    return {match: false, error: ERRORS.nan(str)};
                }
                return {match: true, value: i};
            },
            suggestor() {
                return [];
            }
        },

        /**
         * An argument type allowing only numbers
         */
        float: {
            parser(str) {
                let i = parseFloat(str);
                if (isNaN(i)) {
                    return {match: false, error: ERRORS.nan(str)};
                }
                return {match: true, value: i};
            },
            suggestor() {
                return [];
            }
        },

        /**
         * An argument type allowing only booleans
         */
        boolean: {
            parser: createLiteralParser(["true", "false"]),
            suggestor: createLiteralSuggestor(["true", "false"])
        }
    };
})();