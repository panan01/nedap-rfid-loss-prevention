package nl.utwente.m4.lossprevention;

import java.sql.*;

public class Main {
    public static void driverLoader() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException cnfE) {
            System.err.println("Error loading driver: " + cnfE);
        }
    }

    public static void getConnection() {
        String host = "bronto.ewi.utwente.nl";
        String dbName = "dab_di20212b_225";
        String url = String.format("jdbc:postgresql://%s:5432/%s?currentSchema=movies", host, dbName);

        String username = "dab_di20212b_225";
        String password = "4gPNr326lyRQcR1J";

        String query =
            "SELECT DISTINCT dir.name " +
                "FROM movies.movie mv, movies.acts ac, movies.person p, movies.writes d, movies.person dir " +
                "WHERE (mv.mid = ac.mid AND p.pid = ac.pid) AND (p.name LIKE ?) AND (mv.mid = d.mid AND d.pid=dir.pid)";

        try(Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, "Bruce Willis");

            try(ResultSet set = st.executeQuery()) {
                while(set.next()) {
                    System.out.println(set.getString(1));
                }
            }
        } catch(SQLException sqle) {
            System.err.println("Error connecting: " + sqle);
        }
    }

    public static void main(String[] args) {
        driverLoader();
        getConnection();
    }
}
