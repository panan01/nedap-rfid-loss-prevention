package nl.utwente.m4.lossprevention.JWT;

import io.jsonwebtoken.Jwts;
import nl.utwente.m4.lossprevention.login.Login;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@AdminJWTNeeded
@Priority(Priorities.AUTHENTICATION)
public class AdminJWTNeededFilter implements ContainerRequestFilter {

    //check if the header has the filed "Authorization" and if the token in the Authorization exists
    //return UNAUTHORIZED if the token does not exist/expired
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String token = containerRequestContext.getHeaderString("Authorization");

        try {
            Jwts.parserBuilder().setSigningKey(TokenGarage.getKey()).requireSubject("admin").build().parseClaimsJws(token);
        } catch (Exception e) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}