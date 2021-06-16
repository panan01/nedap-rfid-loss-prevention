"use strict";

/**
 * Called when logging in from the log-in screen
 * @param {string} email    E-mail address entered
 * @param {string} password Password entered
 * @returns A result object
 */
function onLogin(email, password) {
  console.log("Replace with own action. Loggin in with email " + email + " and password " + password + "."); // Return values
  // {error: "message"} to show error message
  //   - {focus: "email"} will focus the email field on error
  //   - {focus: "password"} will focus the password field on error
  // {redirect: "href"} to open the specified page

  if (email !== "email@example.com") {
    return {
      error: "Unknown e-mail address.",
      focus: "email"
    };
  }

  if (password !== "password") {
    return {
      error: "Incorrect password.",
      focus: "password"
    };
  }

  return {
    redirect: "#"
  };
}
/**
 * Called when requesting a new password from the reset-password screen
 * @param {string} email E-mail address entered
 * @returns A result object
 */


function onSendResetPasswordLink(email) {
  console.log("Replace with own action. Send password reset to email " + email + "."); // Return values
  // {error: "message"} to show error message
  //   - {focus: "email"} will focus the email input field on error
  // {done: true} to open done message

  if (email !== "email@example.com") {
    return {
      error: "No account is registered with that e-mail address.",
      focus: "email"
    };
  }

  return {
    done: true
  };
}
/**
 * Called when requesting a new account from the sign-up screen
 * @param {string} email E-mail address entered
 * @returns A result object
 */


function onSendSignUpLink(email) {
  console.log("Replace with own action. Signing up for email " + email + "."); // Return values
  // {error: "message"} to show error message
  //   - {focus: "email"} will focus the email input field on error
  // {done: true} to open done message

  if (email === "email@example.com") {
    return {
      error: "An account is already registered with that e-mail address. Try logging in instead.",
      focus: "email"
    };
  }

  return {
    done: true
  };
}