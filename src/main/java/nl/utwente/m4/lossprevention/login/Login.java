package nl.utwente.m4.lossprevention.login;

import nl.utwente.m4.lossprevention.sql.Queries;

import javax.crypto.KeyGenerator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.simple.JSONObject;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Date;

@Path("/login")
public class Login {

    private static final String SECRET = "69KA420HAH666R5I3C0K88A0S4H6LEY2XVOALEC321";
    private static Key key;
    private static KeyGenerator gen;

    public Login() {
        try {
            gen = KeyGenerator.getInstance("HmacSHA512");
            gen.init(512, new SecureRandom());
            key = gen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
/*
Expected JSON
{
"email":
"password":
 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(JSONObject body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        try {
            // Authenticate the user using the credentials provided
            login(email, password);

            // Issue a token for the user
            String token = issueToken(email);

            // Return the token on the response
            return Response.status(200).entity(token).build();

        } catch (SQLException e) {
            return Response.status(200).entity("fail").build();
        }
    }

    // Login user, should throw SQLException if user doesn't exist/wrong credentials
    public void login(String email, String passw) throws SQLException {
        if (passw != null) {
            Queries.instance.checkUserAndPass(email,passw);
        }
    }

    //Issue token using JWT
    public String issueToken(String email) {
        Date issuedTime = new Date(System.currentTimeMillis());
        Date expDate = new Date(System.currentTimeMillis() + 1800000);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(issuedTime)
                .setExpiration(expDate)
                .signWith(key, SignatureAlgorithm.HS512).compact();
    }

    public static Key getKey() {
        return key;
    }
}


