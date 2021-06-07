package nl.utwente.m4.lossprevention.register;

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
        JSON FILE SEND FROM FRONT END FOR REGISTER USER
        {
          "email":
          "password":
          "first_name":
          "last_name":
          "type":
        }
    */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response register(JSONObject body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String first_name = (String) body.get("first_name");
        String last_name = (String) body.get("last_name");
        String type = (String) body.get("type");
        String salt = this.saltGenerator();
        byte[] hashed_pass = PasswordHasher.instance.hashPassword(password, salt);
        if (Queries.instance.checkEmailValidity(email) && hashed_pass != null){
            boolean queryResult = Queries.instance.addNewUser(email, hashed_pass, first_name, last_name, type, salt);
            return queryResult ? Response.status(200).entity("success").build() : Response.status(200).entity("fail").build();
        }
        return Response.status(200).entity("fail").build();
    }

    public String saltGenerator(){
        String result = "";
        for (int i = 0; i < 16; i++) {
            result += gen.charAt(rnd.nextInt(gen.length()));
        }
        return result;
    }

}
