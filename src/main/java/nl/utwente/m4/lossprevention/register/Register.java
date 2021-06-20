package nl.utwente.m4.lossprevention.register;

import nl.utwente.m4.lossprevention.InputSanitization.InputNotAllowedException;
import nl.utwente.m4.lossprevention.JWT.AdminJWTNeeded;
import nl.utwente.m4.lossprevention.sql.Queries;
import org.json.simple.JSONObject;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.regex.Pattern;


@Path("/register")
public class Register {
    private static final String gen = "!\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private final SecureRandom rnd = new SecureRandom();
    /*
    Required JSON
        {
            "email": NOT NULL
            "password": NOT NULL
            "first_name": NOT NULL
            "last_name": NOT NULL
            "type": "store manager" OR "admin" OR "division manager" OR "loss prevention manager"
        }

    What does it return
        1.Status "400" and "invalid type" (For not existing user type)
        2.Status "200" and "user already exist" (For creating user with existed email in the database)
        3.Status "400" and "invalid first name" (For the first name that does not pass the sanitization)
        4.Status "400" and "invalid last name" (For the last name that does not pass the sanitization)
        5.Status "200" and "success" (For successfully registered user)
        5.Status "200" and "fail" (For failing registering user)
        6.Status "400" and "invalid email" (For the email that does not pass the sanitization)

     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    // Requires a filed in the header called "Authorization" that returns the token
    @AdminJWTNeeded
    public Response register(JSONObject body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String first_name = (String) body.get("first_name");
        String last_name = (String) body.get("last_name");
        String type = (String) body.get("type");
        String salt = this.saltGenerator();
        byte[] hashed_pass = PasswordHasher.instance.hashPassword(password, salt);

        try {
            if (!Queries.instance.checkIfEmailExists(email) && hashed_pass != null) {
                boolean queryResult = Queries.instance.addNewUser(email, hashed_pass, first_name, last_name, type, salt);
                return queryResult ? Response.status(200).entity("success").build() : Response.status(200).entity("fail").build();
            } else {
                return Response.status(200).entity("user already exists").build();
            }
        } catch (InputNotAllowedException e) {
            return Response.status(400).entity("invalid " + e.getMessage()).build();
        }
    }

    //Generate a size of 16 salt
    public String saltGenerator(){
        String result = "";
        for (int i = 0; i < 16; i++) {
            result += gen.charAt(rnd.nextInt(gen.length()));
        }
        return result;
    }

}
