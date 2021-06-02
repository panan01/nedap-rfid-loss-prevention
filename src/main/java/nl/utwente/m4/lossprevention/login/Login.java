package nl.utwente.m4.lossprevention.login;

import nl.utwente.m4.lossprevention.register.PasswordHasher;
import nl.utwente.m4.lossprevention.sql.Queries;
import org.json.simple.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/login")
public class Login {
/*
Expected JSON
{
"email":
"password":
 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(JSONObject body){
        String email = (String) body.get("email");
        String passw = (String) body.get("password");

        if (passw != null){
            boolean queryResult = Queries.instance.checkUserAndPass(email,passw);
            return queryResult ? Response.status(200).entity("success").build() : Response.status(200).entity("fail").build();
        }
        return Response.status(200).entity("fail").build();
    }
}
