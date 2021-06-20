package nl.utwente.m4.lossprevention.account;

import nl.utwente.m4.lossprevention.InputSanitization.InputNotAllowedException;
import nl.utwente.m4.lossprevention.InputSanitization.InputSanitizer;
import nl.utwente.m4.lossprevention.JWT.*;
import nl.utwente.m4.lossprevention.sql.Queries;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/account/{email}")
public class account {


    /*
    What does it return
        1.Status "200" and a user JSON (For successfully getting the user info)
            {
                "last_name":
                "type":
                "first_name":
                "email":
            }
        2.Status "404" and "user not found" (For the getting a user that does not exist);
        3.Status "200" and "fail" (For SQLException)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // Requires a filed in the header called "Authorization" that returns the token
    @JWTNeeded
    public Response getAccount(@PathParam("email") String email, @Context HttpServletRequest request) {
        try {
            if (Queries.instance.checkIfEmailExists(email)) {
                JSONObject userJson = Queries.instance.getUser(email);
                return Response.status(200).entity(userJson).build();
            } else {
                return Response.status(404).entity("user not found").build();
            }
        } catch (InputNotAllowedException e) {
            return Response.status(404).entity("user not found").build();
        } catch (SQLException f) {
            System.err.println("Get User Error: " + f);
            return Response.status(200).entity("fail").build();
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

    What does it return
        1.Status "200" and "success" (For successfully modified user)
        2:Status "401" and "unauthorized password changing" (For not admin changing the password)
        3:Status "404" (For any other unauthorized modification)
        4:Status "200" and "fail" (For unsuccessful modification of user)
        5:Status "400" and "invalid email/type/first name/last name" (For the input that did not pass the sanitization)
     */
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

        // get the JWT token of the modifier
        String token = request.getHeader("Authorization");
        // get the email of the modifier
        String requestedFrom = TokenGarage.getTokenUser(token);
        try {
            // get the user type/role of the modifier
            String modifierRole = Queries.instance.getUserRole(requestedFrom);
            // check if the modifier is the user itself or an "admin" and the password is empty
            if ((requestedFrom.equals(email) || modifierRole.equals("admin")) && password.equals("")) {
                //modify the user according to the input from the user
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
            //If user input does not pass the sanitization
        } catch (InputNotAllowedException f){
            System.err.println("User Input not allowed");
            return Response.status(400).entity("invalid " + f.getMessage()).build();
        }
    }

    /*
    What does it return
        1.Status "200" and "success" (For successfully deleting the account)
        2.Status "200" and "fail" (For unsuccessful deleting account)
     */
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

    public void modifyUser(String email, String password, String fname, String lname, String type) throws SQLException, InputNotAllowedException{
        if (!password.equals("")) {
            Queries.instance.modifyUser(email, "password", password);
            System.out.println("User: " + email + " Password changed");
        }
        if (!fname.equals("")) {
            InputSanitizer.checkUserInput("first name",fname);
            Queries.instance.modifyUser(email, "first_name", fname);
            System.out.println("User: " + email + " first name changed");
        }
        if (!lname.equals("")) {
            InputSanitizer.checkUserInput("last name", lname);
            Queries.instance.modifyUser(email, "last_name", lname);
            System.out.println("User: " + email + " last name changed");
        }
        if (!type.equals("")) {
            InputSanitizer.checkUserType(type);
            Queries.instance.modifyUser(email, "type", type);
            System.out.println("User: " + email + " role changed");
        }
    }

}
