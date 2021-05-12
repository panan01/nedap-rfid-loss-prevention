package nl.utwente.m4.lossprevention.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class createDB {

    public static void driverLoader() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Error loading driver: " + cnfe);
        }

    }

    public static void getConnection() {
        try {
            String host = "bronto.ewi.utwente.nl";
            String dbName = "dab_di20212b";
            String url = "jdbc:postgresql://"
                    + host + ":5432/" + dbName + "?currentSchema=Nedap";

            String username = "dab_di20212b_225";
            String password = "4gPNr326lyRQcR1J";
            Connection connection =
                    DriverManager.getConnection(url, username, password);

            String query = "SELECT DISTINCT dir.name " +
                    " FROM movies.movie mv, movies.acts ac, movies.person p, movies.writes d, movies.person dir " +
                    " WHERE (mv.mid = ac.mid AND p.pid = ac.pid) AND (p.name LIKE ?) AND (mv.mid = d.mid AND d.pid=dir.pid)";

            PreparedStatement st =
                    connection.prepareStatement(query);


            st.executeQuery();


            connection.close();


        } catch (SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }
    }

    public static void main(String[] args) {
        driverLoader();
        getConnection();
    }
}
