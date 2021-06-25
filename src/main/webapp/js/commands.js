// ADD COMMANDS HERE
/*
 - A command has certain arguments, which are specified as -argname
 - Arguments can have 0 or more input parameters: -argname param param
 - A special argument $main is for the parameters right after the command name

 /commandname param param -argname param -anotherarg -yetanother param param
 Gives
 $main:      [param, param]
 argname:    [param]
 anotherarg: []
 yetanother: [param, param]
 */

var dispatcher = new CommandDispatcher([
    {
        aliases: ["add"],
        args: [{ name: "$main", params: [ArgTypes.float, ArgTypes.float] }],
        trigger: function(result) {
            alert(result.value("$main", 0) + result.value("$main", 1));
        }
    }, {
        aliases: ["sub"],
        args: [{ name: "$main", params: [ArgTypes.float, ArgTypes.float] }],
        trigger: function(result) {
            alert(result.value("$main", 0) - result.value("$main", 1));
        }
    }, {
        aliases: ["hello"],
        trigger: function(result) {
            alert("Hello world!");
        }
    }, {
        aliases: ["say"],
        args: [{ name: "$main", params: [ArgTypes.string] }],
        trigger: function(result) {
            alert(result.value("$main"));
        }
    }, {
        aliases: ["arg"],
        args: [
            { name: "a", params: [ArgTypes.string], required: true },
            { name: "b", params: [ArgTypes.int] }
        ],
        trigger: function(result) {
            if (result.has("b")) {
                alert(result.value("a") + result.value("b"));
            } else {
                alert(result.value("a"));
            }
        }
    }
]);

// Call this function in a html page to make the search bar a command bar
function makeCommandBar(opts) {
    var element = opts.element;
    var $element = $(element);

    $element.on("input", (event) => {
        if ($element.val()[0] === '/') {
            $element.addClass("command-mode");

            let parseResult = dispatcher.parse($element.val().substring(1));
            if (parseResult.errors.length > 0) {
                $element.addClass("command-error");
            } else {
                $element.removeClass("command-error");
            }
        } else {
            $element.removeClass("command-mode");
            $element.removeClass("command-error");
        }
    });

    $element.keypress((event) => {
        var key = event.which || event.keyCode;

        if (key === 13) { // enter
            event.stopPropagation();
            event.preventDefault();

            if ($element.val()[0] === '/') {
                $element.addClass("command-mode");

                let parseResult = dispatcher.parse($element.val().substring(1));
                if (parseResult.errors.length > 0) {
                    $element.addClass("command-error");
                    console.log(parseResult.errors);
                } else {
                    $element.val("");
                    $element.removeClass("command-mode");
                    $element.removeClass("command-error");
                    parseResult.execute();
                }
            } else {
                $element.removeClass("command-mode");
                $element.removeClass("command-error");
            }
        }
    });
}
