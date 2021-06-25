package nl.utwente.m4.lossprevention.sql;

import nl.utwente.m4.lossprevention.register.PasswordHasher;
import org.json.simple.JSONObject;

import java.awt.event.PaintEvent;
import java.sql.*;

public enum Queries {
    instance;

    private final String host = "bronto.ewi.utwente.nl";
    private final String dbName = "dab_di20212b_225";
    private final String url = "jdbc:postgresql://" + host + "/" + dbName + "?currentSchema=nedap";
    private final String username = "dab_di20212b_225";
    private final String password = "4gPNr326lyRQcR1J";
    private Connection connection;
    private Statement st;
    private PreparedStatement checkEmailSt;
    private PreparedStatement addNewUser;
    private PreparedStatement checkUserAndPass;
    private PreparedStatement getSalt;
    private PreparedStatement getUser;
    private PreparedStatement modifyPass;
    private PreparedStatement modifyFName;
    private PreparedStatement modifyLName;
    private PreparedStatement modifyRole;
    private PreparedStatement deleteAccount;

    private Queries() {
        try { // load the driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Error while loading driver: " + cnfe);
        }

        try { // establish connection
            connection = DriverManager.getConnection(url, username, password);
            checkEmailSt = connection.prepareStatement("SELECT EXISTS (select 1 FROM users u WHERE u.email = ? LIMIT 1)");
            addNewUser = connection.prepareStatement("INSERT INTO users (email,hashed_pass,first_name,last_name,type, salt) " +
                                                          "VALUES (?, ?, ?, ?, ?, ?)");
            checkUserAndPass = connection.prepareStatement("SELECT EXISTS (SELECT 1 FROM users u " +
                                                                    "WHERE u.email = ? AND u.hashed_pass = ? LIMIT 1)");
            getSalt = connection.prepareStatement("SELECT salt FROM users WHERE email = ?");
//            getUser = connection.prepareStatement("(SELECT json_build_object('email',email,'password',hashed_pass,'first_name',first_name,'last_name',last_name,'type',type))::json" +
//                                                            "FROM users WHERE email = ?");
            getUser = connection.prepareStatement("SELECT u.email,u.hashed_pass,u.first_name,u.last_name,u.type FROM users u WHERE u.email = ?");
            modifyPass = connection.prepareStatement("UPDATE users SET hashed_pass = ? WHERE email = ?");
            modifyFName = connection.prepareStatement("UPDATE users SET first_name = ? WHERE email = ?");
            modifyLName = connection.prepareStatement("UPDATE users SET last_name = ? WHERE email = ?");
            modifyRole = connection.prepareStatement("UPDATE users SET type = ? WHERE email = ?");
            deleteAccount = connection.prepareStatement("DELETE FROM users WHERE email = ?");
//            modifyPass = connection.prepareStatement("UPDATE users SET hashed_pass = ?, first_name = ?, last_name = ?, type = ? WHERE email = ?");
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }
    }
// Checking if the email already exists on the database (true = not exists, false = exists)
    public boolean checkEmailValidity(String email) {
        try {
            if (email.contains("@")) {
                checkEmailSt.setString(1, email);
                ResultSet rs = checkEmailSt.executeQuery();
                rs.next();
                boolean result = rs.getBoolean(1);
                rs.close();
                return !result;
            }
        } catch (SQLException e) {
            System.err.println("Email check failed: " + e);
        }
        return false;
    }

//    adding new user to the database
    public boolean addNewUser(String email, byte[] hashedPass, String firstName, String lastName, String type, String salt){
        try{
            addNewUser.setString(1, email);
            addNewUser.setBytes(2, hashedPass);
            addNewUser.setString(3, firstName);
            addNewUser.setString(4, lastName);
            addNewUser.setString(5, type);
            addNewUser.setString(6, salt);
            addNewUser.execute();
            return true;
        } catch (SQLException e){
            System.err.println("User creation failed " + e);
            return false;
        }
    }

//    check the user name and it's password
    public boolean checkUserAndPass(String email, String password){
        try{
            String salt = this.getSalt(email);
            byte[] hashedPass = PasswordHasher.instance.hashPassword(password, salt);
            checkUserAndPass.setString(1,email);
            checkUserAndPass.setBytes(2, hashedPass);
            ResultSet rs = checkUserAndPass.executeQuery();
            rs.next();
            boolean result  = rs.getBoolean(1);
            rs.close();
            return result;
        } catch (SQLException e){
            System.err.println("User checking failed " + e);
            return false;
        }
    }

    public String getSalt(String email) throws SQLException {
        getSalt.setString(1, email);
        ResultSet rs = getSalt.executeQuery();
        rs.next();
        return rs.getString(1);
    }

    public JSONObject getUser(String email) throws SQLException {
            getUser.setString(1,email);
            ResultSet rs = getUser.executeQuery();
            rs.next();
//            JSONObject json = rs.getObject(1, JSONObject.class);
            String userEmail = rs.getString(1);
            String password = rs.getString(2);
            String fname = rs.getString(3);
            String lname = rs.getString(4);
            String type = rs.getString(5);

            JSONObject json = new JSONObject();
            json.put("email", userEmail);
            json.put("password", password);
            json.put("first_name", fname);
            json.put("last_name", lname);
            json.put("type", type);
            return json;
//            return new JSONObject(rs.getString(1));

    }

    public void modifyUser(String email, String column, String value) throws SQLException{
            switch (column) {
                case "password":
                    String salt = this.getSalt(email);
                    byte[] hashed_pass = PasswordHasher.instance.hashPassword(value, salt);
                    modifyPass.setBytes(1, hashed_pass);
                    modifyPass.setString(2, email);
                    modifyPass.execute();
                    break;
                case "first_name":
                    modifyFName.setString(1, value);
                    modifyFName.setString(2, email);
                    modifyFName.execute();
                    break;
                case "last_name":
                    modifyLName.setString(1, value);
                    modifyLName.setString(2, email);
                    modifyLName.execute();
                    break;
                case "type":
                    modifyRole.setString(1, value);
                    modifyRole.setString(2, email);
                    modifyRole.execute();
            }
        }

        public void DeleteAccount(String email) throws SQLException{
            deleteAccount.setString(1, email);
            deleteAccount.execute();
        }
}
