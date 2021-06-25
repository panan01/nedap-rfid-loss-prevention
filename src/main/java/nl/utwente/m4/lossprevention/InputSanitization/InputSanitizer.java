package nl.utwente.m4.lossprevention.InputSanitization;

import nl.utwente.m4.lossprevention.InputSanitization.InputNotAllowedException;

import java.util.regex.Pattern;

public enum InputSanitizer {
    instance;
    private static final Pattern inputPattern = Pattern.compile("[a-zA-Z0-9\\s-]+");
    private static final Pattern emailPattern = Pattern.compile("^\\S+@\\S+.\\S+$");

    public static boolean checkEmail(String email) throws InputNotAllowedException{
        if(!email.equals("") && email.matches(emailPattern.pattern())){
            return true;
        } else {
            throw new InputNotAllowedException("email");
        }
    }
    public static boolean checkUserInput(String inputType, String input) throws InputNotAllowedException{
        if (!input.equals("") && input.matches(inputPattern.pattern())){
            return true;
        } else {
            throw new InputNotAllowedException(inputType);
        }
    }

    public static boolean checkUserInput(String input) throws InputNotAllowedException{
        if (!input.equals("") && input.matches(inputPattern.pattern())){
            return true;
        } else {
            throw new InputNotAllowedException();
        }
    }

    public static boolean checkUserType(String type) throws InputNotAllowedException{
        if (!type.equals("") && (type.equals("admin") || type.equals("store manager") || type.equals("division manager") || type.equals("loss prevention manager"))){
            return true;
        } else {
            throw new InputNotAllowedException("type");
        }
    }

}
