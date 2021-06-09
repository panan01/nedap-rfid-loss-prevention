package nl.utwente.m4.lossprevention.login;

import nl.utwente.m4.lossprevention.register.PasswordHasher;
import nl.utwente.m4.lossprevention.sql.Queries;
import org.json.simple.JSONObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

@Path("/login")
public class Login {

    private static final String SECRET = "69KA420HAH666";

/*
Expected JSON
{
"email":
"password":
 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(@FormParam("email") String email,
                                     @FormParam("password") String password) {
        try {
            // Authenticate the user using the credentials provided
            login(email, password);

            // Issue a token for the user
            String token = issueToken(email);

            // Return the token on the response
            return Response.ok(token).build();

        } catch (Exception e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    // Login user, should throw exception if user doesn't exist/wrong credentials
    public void login(String email, String passw) throws Exception {
        if (passw != null) {
            Queries.instance.checkUserAndPass(email,passw);
        }
    }

    //Issue token using JWT
    public String issueToken(String email) {
        Date issuedTime = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(issuedTime)
                .signWith(SignatureAlgorithm.HS512, SECRET).compact();
    }
}


