package nl.utwente.m4.lossprevention.sql;

import nl.utwente.m4.lossprevention.register.PasswordHasher;

import java.sql.*;

public enum Queries {
    instance;

    private final String host = "bronto.ewi.utwente.nl";
    private final String dbName = "dab_di20212b_225";
    private final String url = "jdbc:postgresql://" + host + "/" + dbName + "?currentSchema=nedap";
    private final String username = "dab_di20212b_225";
    private final String password = "4gPNr326lyRQcR1J";
//    private final String password = System.getenv("DB_PASS");
    private Connection connection;
    private Statement st;
    private PreparedStatement checkEmailSt;
    private PreparedStatement addNewUser;
    private PreparedStatement checkUserAndPass;
    private PreparedStatement getSalt;

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
}
