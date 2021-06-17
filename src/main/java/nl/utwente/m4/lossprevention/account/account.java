package nl.utwente.m4.lossprevention.account;

import nl.utwente.m4.lossprevention.JWT.*;
import nl.utwente.m4.lossprevention.sql.Queries;
import org.glassfish.jersey.server.ContainerRequest;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/account/{email}")
public class account {

    /*
    returns JSON as the following format
        {
            "last_name":
            "type":
            "first_name":
            "email":
        }
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // Requires a filed in the header called "Authorization" that returns the token
    @JWTNeeded
    public Response getAccount(@PathParam("email") String email, @Context HttpServletRequest request) {
        if (!Queries.instance.checkEmailValidity(email)) {
            try {
                JSONObject userJson = Queries.instance.getUser(email);
                return Response.status(200).entity(userJson).build();
            } catch (SQLException e) {
                System.err.println("Get User Error: " + e);
                return Response.status(200).entity("error").build();
            }
        } else {
            return Response.status(200).entity("Email does not exist").build();
        }
    }


    /*
    Expecting from the front end:
        {
            "password": if this filed is not modified pls return ""
            "last_name": if this filed is not modified pls return ""
            "type": if this filed is not modified pls return ""
            "first_name": if this filed is not modified pls return ""
        }
     */
    // returns: status 400 + "no such user type" if the user type does not exist
    // returns: status 401 + "unauthorized password changing" if it's not admin changing the pass
    // returns: status 400 for any other invalid token
    // returns: status 200 + "success" if password/user info changed successfully
    // returns: status 200 + "fail" if SQL error occur
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    // Requires a filed in the header called "Authorization" that returns the token
    @JWTNeeded
    public Response modifyAccount(JSONObject body, @PathParam("email") String email, @Context HttpServletRequest request) {
        String password = (String) body.get("password");
        String first_name = (String) body.get("first_name");
        String last_name = (String) body.get("last_name");
        String type = (String) body.get("type");
        // If the user type filed is not empty, then check the type if it's actually exists.
        if (!type.equals("") && !(type.equals("admin") || type.equals("store manager") || type.equals("division manager") || type.equals("loss prevention manager"))){
            return Response.status(400).entity("no such user type").build();
        }
        // get the JWT token of the modifier
        String token = request.getHeader("Authorization");
        // get the email of the modifier
        String requestedFrom = TokenGarage.getTokenUser(token);
        try {
            // get the user type/role of the modifier
            String modifierRole = Queries.instance.getUserRole(requestedFrom);
            // check if the modifier is the user itself or an "admin" and the password is empty
            if ((requestedFrom.equals(email) || modifierRole.equals("admin")) && password.equals("")) {
                //modify the user according to the input from the front end
                this.modifyUser(email,password,first_name,last_name,type);
                System.out.println("Modified user " + email + " successfully");
                return Response.status(200).entity("success").build();
            // check if the user is admin and password is not empty (because only the admin can change password)
            } else if (modifierRole.equals("admin") && !password.equals("")){
                this.modifyUser(email,password,first_name,last_name,type);
                System.out.println("User " + email + " password changed");
                return Response.status(200).entity("success").build();
            // if the password id not empty and is not admin modifying
            } else if (!password.equals("")){
                return Response.status(Response.Status.UNAUTHORIZED).entity("unauthorized password changing").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

        } catch (SQLException e) {
            System.err.println("Something wrong happened while modifying user:  " + e);
            return Response.status(200).entity("fail").build();
        }
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    // Requires a filed in the header called "Authorization" that returns the token
    @AdminJWTNeeded
    public Response deleteAccount(@PathParam("email") String email){
        try {
            Queries.instance.DeleteAccount(email);
            System.out.println("Deleted account \"" + email + " \"");
            return Response.status(200).entity("success").build();
        } catch (SQLException e){
            System.err.println("Something wrong happened while deleting user " + email);
            return Response.status(200).entity("fail").build();
        }
    }

    public void modifyUser(String email, String password, String fname, String lname, String type) throws SQLException {
        if (!password.equals("")) {
            Queries.instance.modifyUser(email, "password", password);
            System.out.println("User: " + email + " Password changed");
        }
        if (!fname.equals("")) {
            Queries.instance.modifyUser(email, "first_name", fname);
            System.out.println("User: " + email + " first name changed");
        }
        if (!lname.equals("")) {
            Queries.instance.modifyUser(email, "last_name", lname);
            System.out.println("User: " + email + " last name changed");
        }
        if (!type.equals("")) {
            Queries.instance.modifyUser(email, "type", type);
            System.out.println("User: " + email + " role changed");
        }
    }

}
