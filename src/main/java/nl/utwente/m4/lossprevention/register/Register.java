package nl.utwente.m4.lossprevention.register;

import nl.utwente.m4.lossprevention.sql.Queries;
import org.json.simple.JSONObject;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/register")
public class Register {
    private final String salt = "$1$N8qsKOcF$dWj1idimpoRJbyVJhU4uk1";

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
        byte[] hashed_pass = PasswordHasher.instance.hashPassword(password);
        if (Queries.instance.checkEmailValidity(email) && hashed_pass != null){
            boolean queryResult = Queries.instance.addNewUser(email, hashed_pass, first_name, last_name, type);
            return queryResult ? Response.status(200).entity("success").build() : Response.status(200).entity("fail").build();
        }
        return Response.status(200).entity("fail").build();
    }

}
