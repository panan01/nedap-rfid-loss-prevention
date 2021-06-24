package nl.utwente.m4.lossprevention;

import nl.utwente.m4.lossprevention.InputSanitization.InputNotAllowedException;
import nl.utwente.m4.lossprevention.InputSanitization.InputSanitizer;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class InputSantinizationTest{

    @Test
    //test the checkEmail method in the InputSanitizer class
    public void emailInputTest() throws InputNotAllowedException {
        String wrongFormatEmail = "wrongemail.com";
        String wrongFormatEmail2 = "wrongemail2";
        String correctEmail = "test@test.com";
        String correctEmail2 = "nedap@student.utwente.nl";
        Throwable exception = assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkEmail(wrongFormatEmail));
        assertEquals("email", exception.getMessage());
        exception = assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkEmail(wrongFormatEmail2));
        assertEquals("email", exception.getMessage());
        assertTrue(InputSanitizer.checkEmail(correctEmail));
        assertTrue(InputSanitizer.checkEmail(correctEmail2)) ;
    }

    @Test
    public void userInputTest() throws InputNotAllowedException {
        String allowedInput = "Aab123456-";
        String notAllowedInput = "<script>hello<script>";
        String notAllowedInput2 = "+=,./,';[";

        assertTrue(InputSanitizer.checkUserInput(allowedInput));
        assertTrue(InputSanitizer.checkUserInput("correctInput", allowedInput));
        assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkUserInput(notAllowedInput));
        Exception exception = assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkUserInput("username",notAllowedInput));
        assertEquals("username", exception.getMessage());
        assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkUserInput(notAllowedInput2));
        exception = assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkUserInput("inputFiled",notAllowedInput2));
        assertEquals("inputFiled", exception.getMessage());
    }

    @Test
    public void userTypeSanitizerTest() throws InputNotAllowedException {
        String rightUserType1 = "admin";
        String rightUserType2 = "store manager";
        String rightUserType3 = "division manager";
        String rightUserType4 = "loss prevention manager";
        String wrongUserType1 = "administrator";
        String wrongUserType2 = "anything";
        String wrongUserType3 = "bfsdjfibsdjf";
        assertTrue(InputSanitizer.checkUserType(rightUserType1));
        assertTrue(InputSanitizer.checkUserType(rightUserType2));
        assertTrue(InputSanitizer.checkUserType(rightUserType3));
        assertTrue(InputSanitizer.checkUserType(rightUserType4));
        Exception exception = assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkUserType(wrongUserType1));
        assertEquals("type", exception.getMessage());
        exception = assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkUserType(wrongUserType2));
        assertEquals("type", exception.getMessage());
        exception = assertThrows(InputNotAllowedException.class, () -> InputSanitizer.checkUserType(wrongUserType3));
        assertEquals("type", exception.getMessage());
    }

}
