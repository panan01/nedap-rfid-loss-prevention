package nl.utwente.m4.lossprevention.register;

import nl.utwente.m4.lossprevention.JWT.AdminJWTNeeded;
import nl.utwente.m4.lossprevention.JWT.JWTNeeded;
import nl.utwente.m4.lossprevention.sql.Queries;
import org.json.simple.JSONObject;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;


@Path("/register")
public class Register {
    private final String salt = "$1$N8qsKOcF$dWj1idimpoRJbyVJhU4uk1";
    private final String gen = "!\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private SecureRandom rnd = new SecureRandom();
    /*
        JSON FILE SEND FROM FRONT END FOR REGISTERING USER
        {
          "email":
          "password":
          "first_name":
          "last_name":
          "type":
        }
    */
    // Returns status: 400 + "no such user type" if the user type does not exist
    // Returns status: 200 + "success" if the user is successfully created
    // Returns status: 200 + "fail" if the user is unsuccessfully created
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

        // If the user type filed is not empty, then check the type if it's actually exists.
        if (!type.equals("") && !(type.equals("admin") || type.equals("store manager") || type.equals("division manager") || type.equals("loss prevention manager"))){
            return Response.status(400).entity("no such user type").build();
        }

        if (Queries.instance.checkEmailValidity(email) && hashed_pass != null){
            boolean queryResult = Queries.instance.addNewUser(email, hashed_pass, first_name, last_name, type, salt);
            return queryResult ? Response.status(200).entity("success").build() : Response.status(200).entity("fail").build();
        }
        return Response.status(200).entity("fail").build();
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
