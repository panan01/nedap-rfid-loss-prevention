package nl.utwente.m4.lossprevention;

import nl.utwente.m4.lossprevention.sql.DbConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnectionTestMain {
    public static void driverLoader() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException cnfE) {
            System.err.println("Error loading driver: " + cnfE);
        }
    }

    public static void getConnection() {
        String query =
            "SELECT DISTINCT dir.name " +
                "FROM movies.movie mv, movies.acts ac, movies.person p, movies.writes d, movies.person dir " +
                "WHERE (mv.mid = ac.mid AND p.pid = ac.pid) AND (p.name LIKE ?) AND (mv.mid = d.mid AND d.pid=dir.pid)";

        try(Connection connection = DbConnector.connect("databases/movies.properties")) {
            PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, "Bruce Willis");

            try(ResultSet set = st.executeQuery()) {
                while(set.next()) {
                    System.out.println(set.getString(1));
                }
            }
        } catch(SQLException | IOException sqle) {
            System.err.println("Error connecting: " + sqle);
        }
    }

    public static void main(String[] args) {
        driverLoader();
        getConnection();
    }
}
