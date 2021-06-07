/**
 * Called when logging in from the log-in screen
 * @param {string} email                  E-mail address entered
 * @param {string} password               Password entered
 * @param {function(object): void} result The result function: call this when the result is ready.
 * @returns A result object
 */
function onLogin(email, password, result) {
    console.log("Replace with own action. Loggin in with email " + email + " and password " + password + ".");

    // Request the server for login and call the 'result' function when the server has responded
    // You can use jQuery ('$.ajax') for the request, see https://api.jquery.com/jquery.ajax/ for documentation

    $.ajax({
        url: "login",   // what should the URL be?
        method: 'POST',
        data: { username: email, password: password},
        success: function(data) {
            console.log(data);
        },
        error: function() {
            // error handling;
        }
    })

    // Result values
    // {error: "message"} to show error message
    //   - {focus: "email"} will focus the email field on error
    //   - {focus: "password"} will focus the password field on error
    // {done: true} when login is complete (note: this will change, but this should do its thing for now)

    // Emulate server delay for placeholder
    setTimeout(function() {
        if (email !== "email@example.com") {
            result({
                error: "Unknown e-mail address.",
                focus: "email"
            });
            return;
        }

        if (password !== "password") {
            result({
                error: "Incorrect password.",
                focus: "password"
            });
            return;
        }

        result({redirect: "#"});
    }, 1000);
}

/**
 * Called when requesting a new password from the reset-password screen
 * @param {string} email                  E-mail address entered
 * @param {function(object): void} result The result function: call this when the result is ready.
 * @returns A result object
 */
function onSendResetPasswordLink(email, result) {
    console.log("Replace with own action. Send password reset to email " + email + ".");

    // Request the server for a reset request and call the 'result' function when the server has responded
    // You can use jQuery ('$.ajax') for the request, see https://api.jquery.com/jquery.ajax/ for documentation

    // Return values
    // {error: "message"} to show error message
    //   - {focus: "email"} will focus the email input field on error
    // {done: true} to open done message

    // Emulate server delay for placeholder
    setTimeout(function() {
        if (email !== "email@example.com") {
            result({
                error: "No account is registered with that e-mail address.",
                focus: "email"
            });
            return;
        }

        result({done: true});
    }, 1000);
}

/**
 * Called when requesting a new account from the sign-up screen
 * @param {string} email                  E-mail address entered
 * @param {function(object): void} result The result function: call this when the result is ready.
 * @returns A result object
 */
function onSendSignUpLink(email, result) {
    console.log("Replace with own action. Signing up for email " + email + ".");

    // Request the server for a sign-up request and call the 'result' function when the server has responded
    // You can use jQuery ('$.ajax') for the request, see https://api.jquery.com/jquery.ajax/ for documentation

    // Return values
    // {error: "message"} to show error message
    //   - {focus: "email"} will focus the email input field on error
    // {done: true} to open done message

    // Emulate server delay for placeholder
    setTimeout(function() {
        if (email === "email@example.com") {
            result({
                error: "An account is already registered with that e-mail address. Try logging in instead.",
                focus: "email"
            });
            return;
        }

        result({done: true});
    }, 1000);
}
