package nl.utwente.m4.lossprevention;


import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import static nl.utwente.m4.lossprevention.utils.excelUtils.*;
import static nl.utwente.m4.lossprevention.utils.sqlUtils.*;
import static org.junit.jupiter.api.Assertions.*;


public class sqlUtilsTest {

    private static final ArrayList<String> requiredLabelsType1 = new ArrayList<>(Arrays.asList(
            "EPC (UT)", "Timestamp", "Store ID (UT)", "Article ID (UT)"
    ));  // alarm type
    private static final ArrayList<String> requiredLabelsType2 = new ArrayList<>(Arrays.asList(
            "Article ID (UT)", "Category (UT)", "Article (UT)", "Color", "Size", "Price (EUR)"
    ));  // article type
    private static final ArrayList<String> requiredLabelsType3 = new ArrayList<>(Arrays.asList(
            "Store ID (UT)", "Latitude (UT)", "Longitude (UT)"
    ));  // store type

    @BeforeEach
    public void setUp() {
        Connection connection = getConnection();
        assertEquals(true, connection != null);

    }


    @Test
    public void queryBuilderTest() {
        //Most stolen articles by category
        String query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.category|-1|article|-0-1|article.category|-0-1|article.category|-0";
        String[] generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.category, COUNT(nedap.article.category) FROM nedap.article GROUP BY nedap.article.category ORDER BY COUNT(article.category) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Most stolen articles by product
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.product|-1|article|-0-1|article.product|-0-1|article.product|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.product, COUNT(nedap.article.product) FROM nedap.article GROUP BY nedap.article.product ORDER BY COUNT(article.product) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Most stolen articles by color
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.color|-1|article|-0-1|article.color|-0-1|article.color|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.color, COUNT(nedap.article.color) FROM nedap.article GROUP BY nedap.article.color ORDER BY COUNT(article.color) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Most stolen articles by size
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.size|-1|article|-0-1|article.size|-0-1|article.size|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.size, COUNT(nedap.article.size) FROM nedap.article GROUP BY nedap.article.size ORDER BY COUNT(article.size) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Most stolen articles by price
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.price|-1|article|-0-1|article.price|-0-1|article.price|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.price, COUNT(nedap.article.price) FROM nedap.article GROUP BY nedap.article.price ORDER BY COUNT(article.price) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Shop (Specific Query)
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|alarm.store_id|-2|alarm:article|-1|article.id:=:alarm.article_id|-1|alarm.store_id|-0-1|alarm.store_id|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) FROM nedap.alarm, nedap.article WHERE article.id = alarm.article_id GROUP BY nedap.alarm.store_id ORDER BY COUNT(alarm.store_id) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Location (Specific Query)
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-2-1|store|-5|store.longitude:>:0:AND:store.latitude:>:10|-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.store.id AS store_id, nedap.store.longitude, nedap.store.latitude FROM nedap.store WHERE store.longitude > 0 AND store.latitude > 10 ", generateSetStringInputs(generationCodeArray[1]));

        //Location (Specific Query) - Longitude only
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-2-1|store|-1|store.longitude:>:0|-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.store.id AS store_id, nedap.store.longitude, nedap.store.latitude FROM nedap.store WHERE store.longitude > 0 ", generateSetStringInputs(generationCodeArray[1]));

        //Location (Specific Query) - Latitude only
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-2-1|store|-1|store.latitude:>:10|-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.store.id AS store_id, nedap.store.longitude, nedap.store.latitude FROM nedap.store WHERE store.latitude > 10 ", generateSetStringInputs(generationCodeArray[1]));

        //Location (Specific Query) - No options
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-2-1|store|-0-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.store.id AS store_id, nedap.store.longitude, nedap.store.latitude FROM nedap.store ", generateSetStringInputs(generationCodeArray[1]));

        //Timestamp Detailed Time Interval (Specific Query) - Without optionals
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|alarm.store_id|-2|alarm:article|-3|article.id:=:alarm.article_id:0:0|-1|alarm.store_id|-0-1|alarm.store_id|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) FROM nedap.alarm, nedap.article WHERE article.id = alarm.article_id GROUP BY nedap.alarm.store_id ORDER BY COUNT(alarm.store_id) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Timestamp Detailed Time Interval (Specific Query) - With both optionals
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|alarm.store_id|-2|alarm:article|-3|article.id:=:alarm.article_id:'07#01#2021 10_25_33':'07#01#2021 10_28_33'|-1|alarm.store_id|-0-1|alarm.store_id|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) FROM nedap.alarm, nedap.article WHERE article.id = alarm.article_id AND alarm.timestamp >= to_timestamp('07-01-2021 10:25:33', 'dd-mm-yyyy hh24:mi:ss') AND alarm.timestamp <= to_timestamp('07-01-2021 10:28:33', 'dd-mm-yyyy hh24:mi:ss') GROUP BY nedap.alarm.store_id ORDER BY COUNT(alarm.store_id) DESC ", generateSetStringInputs(generationCodeArray[1]));

        //Timestamp Date (Specific Query)
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|alarm.store_id|-2|alarm:article|-4|'2021#01#07'|-1|alarm.store_id|-0-1|alarm.store_id|-1|5|";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) FROM nedap.alarm, nedap.article WHERE article.id = alarm.article_id AND date(timestamp) = '2021-01-07' GROUP BY nedap.alarm.store_id ORDER BY COUNT(alarm.store_id) DESC LIMIT 5 ", generateSetStringInputs(generationCodeArray[1]));

        //Timestamp Interval Date (Specific Query) - Without optionals:
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|alarm.store_id|-2|alarm:article|-2|article.id:=:alarm.article_id:0:0|-1|alarm.store_id|-0-1|alarm.store_id|-1|5|";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) FROM nedap.alarm, nedap.article WHERE article.id = alarm.article_id GROUP BY nedap.alarm.store_id ORDER BY COUNT(alarm.store_id) DESC LIMIT 5 ", generateSetStringInputs(generationCodeArray[1]));

        //Timestamp Interval Date (Specific Query) - With both optionals:
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|alarm.store_id|-2|alarm:article|-2|article.id:=:alarm.article_id:'2021#01#07':'2021#01#08'|-1|alarm.store_id|-0-1|alarm.store_id|-1|5|";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) FROM nedap.alarm, nedap.article WHERE article.id = alarm.article_id AND date(timestamp) >='2021-01-07' AND date(timestamp) <='2021-01-08' GROUP BY nedap.alarm.store_id ORDER BY COUNT(alarm.store_id) DESC LIMIT 5 ", generateSetStringInputs(generationCodeArray[1]));

        //Sum Timestamp Interval (Specific Query)
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-3|stolen_items:\"Stolen Items Within Interval\":0|-4|1:'2021#01#07':'2021#01#08'|-0-0-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT SUM(stolen_items) AS \"Stolen Items Within Interval\" FROM (SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id AND date(timestamp) >= '2021-01-07' AND date(timestamp) <= '2021-01-08' GROUP BY alarm.store_id) AS SumInterval", generateSetStringInputs(generationCodeArray[1]));

        //Sum Timestamp Interval (Specific Query) - No optionals:
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-3|stolen_items:\"Stolen Items Within Interval\":0|-4|0:0:0|-0-0-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT SUM(stolen_items) AS \"Stolen Items Within Interval\" FROM (SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id GROUP BY alarm.store_id) AS SumInterval", generateSetStringInputs(generationCodeArray[1]));

        //Additional condition for timestamp queries (Specific Query)
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-4|alarm.store_id:stolen_items|-2|alarm:article|-2|article.id:=:alarm.article_id:'2021#01#07':'2021#01#08'|-1|alarm.store_id|-1|alarm.store_id:>:20|-1|alarm.store_id|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.alarm.store_id, COUNT(alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE article.id = alarm.article_id AND date(timestamp) >='2021-01-07' AND date(timestamp) <='2021-01-08' GROUP BY nedap.alarm.store_id HAVING COUNT(alarm.store_id) > 20 ORDER BY COUNT(alarm.store_id) DESC ", generateSetStringInputs(generationCodeArray[1]));


        //Most stolen items by time interval (Specific Query)
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-1|timeinterval|-5|'hour'|-0-1|timeinterval|-0-1|timeinterval|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT timeinterval, COUNT(timeinterval) FROM (SELECT DATE_PART('hour', timestamp) as timeinterval, store_id FROM nedap.alarm GROUP BY alarm.timestamp, store_id) AS timeinterval_table GROUP BY timeinterval ORDER BY timeinterval ASC ", generateSetStringInputs(generationCodeArray[1]));

        //Items stolen per week day (Specific Query)
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-5-6-6|1023553|-1|day|-0-2-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT day AS weekday, COUNT(day FROM (SELECT trim(to_char(timestamp, 'day')) AS day, store_id FROM nedap.alarm) AS day_table WHERE store_id = 1023553GROUP BY day ORDER BY CASE WHEN day = 'monday' THEN 1 WHEN day = 'tuesday' THEN 2 WHEN day = 'wednesday' THEN 3 WHEN day = 'thursday' THEN 4 WHEN day = 'friday' THEN 5 WHEN day = 'saturday' THEN 6 WHEN day = 'sunday' THEN 7 END ASC ", generateSetStringInputs(generationCodeArray[1]));

        //JSON query for dashboard map
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-1|*|-1|store|-0-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT * FROM nedap.store ", generateSetStringInputs(generationCodeArray[1]));

        //SQL voor store_access 2
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-6|store.id|-2|users:store|-7|test@test.com|-0-0-4|store.id|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT nedap.store.id FROM nedap.users, nedap.store WHERE users.email = 'test@test.com' AND ((SELECT store_access.allowed_user FROM nedap.store_access WHERE nedap.store.id = nedap.store_access.store_id) = 'test@test.com' OR users.type = 'admin') ORDER BY store.id ", generateSetStringInputs(generationCodeArray[1]));

        //SQL voor users
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-6|users.first_name:users.last_name:users.email:users.type|-0-0-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT nedap.users.first_name, nedap.users.last_name, nedap.users.email, nedap.users.type ", generateSetStringInputs(generationCodeArray[1]));

        //for Trends
        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;2-7-0-0-0-0-0-0";
        generationCodeArray = query.split(";");
        assertEquals("WITH jan AS (SELECT store.id, (SELECT COALESCE(SUM(1), 0) FROM nedap.alarm WHERE store.id = alarm.store_id AND alarm.timestamp >= to_timestamp('01-01-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-02-2021', 'dd-mm-yyyy')) AS amount\n" +
                "    FROM nedap.store GROUP BY store.id ORDER BY store.id ), feb AS (SELECT store.id, (SELECT COALESCE(SUM(1), 0) FROM nedap.alarm WHERE store.id = alarm.store_id AND alarm.timestamp >= to_timestamp('01-02-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-03-2021', 'dd-mm-yyyy')) AS amount\n" +
                "    FROM nedap.store GROUP BY store.id ORDER BY store.id ), mar AS (SELECT store.id, (SELECT COALESCE(SUM(1), 0) FROM nedap.alarm WHERE store.id = alarm.store_id AND alarm.timestamp >= to_timestamp('01-03-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-04-2021', 'dd-mm-yyyy')) AS amount\n" +
                "    FROM nedap.store GROUP BY store.id  ORDER BY store.id  ) SELECT jan.id, jan.amount AS jan_amount, feb.amount AS feb_amount, mar.amount AS mar_amount FROM jan, feb, mar WHERE jan.id = feb.id AND feb.id = mar.id", generateSetStringInputs(generationCodeArray[1]));


    }

    @Test
    public void checkLabelsTest() {
        ArrayList<String> labels = new ArrayList<>();
        labels.add("EPCC");
        labels.add("Longitude");
        labels.add("Latitude");
        assertNotEquals(true, checkLabels(labels, getRequiredLabels(2)));

        labels = new ArrayList<>();
        labels.add("Store ID (UT)");
        labels.add("Longitude");
        labels.add("Latitude");
        assertNotEquals(true, checkLabels(labels, getRequiredLabels(2)));

        labels = new ArrayList<>();
        labels.add("Store ID (UT)");
        labels.add("Longitude (UT)");
        labels.add("Latitude (UT)");
        assertEquals(true, checkLabels(labels, getRequiredLabels(2)));

        labels = new ArrayList<>(Arrays.asList(
                "Article ID (UT)", "Category (UT)", "Article (UT)", "Color", "Size", "Price (EUR)"
        ));
        assertEquals(true, checkLabels(labels, getRequiredLabels(1)));
    }

    @Test
    public void getVariablesFromQuerycode() {
        assertEquals("[test, test2, 02323]", getVariables("|test:test2:02323|").toString());
        assertEquals("[123123, -23123 02323, 02323, 4$23##]", getVariables("|123123:-23123 02323:02323:4$23##|").toString());
        assertEquals("[_dfdsf]", getVariables("|_dfdsf|").toString());
    }

    @Test
    public void checkColumnsTest() {
        assertEquals(true, checkVariableIsColumn("alarm.store_id"));
        assertNotEquals(true, checkVariableIsColumn("users.password"));
        assertNotEquals(true, checkVariableIsColumn("test"));
    }

    @Test
    public void checkTableTest() {
        assertEquals(true, checkVariableIsTable("article"));
        assertEquals(true, checkVariableIsTable("alarm"));
        assertEquals(true, checkVariableIsTable("store"));
        assertEquals(true, checkVariableIsTable("store_access"));
        assertEquals(true, checkVariableIsTable("users"));
        assertNotEquals(true, checkVariableIsTable("sometable"));
        assertNotEquals(true, checkVariableIsTable("test"));
    }

    @Test
    public void checkVariableSecurity() {
        ArrayList<String> variables = new ArrayList<>();
        variables.add("OR");
        variables.add("=");
        variables.add("1");
        assertNotEquals(true, variablesValid(variables, 3, 1));

        variables = new ArrayList<>();
        variables.add("'year'");
        assertNotEquals(true, variablesValid(variables, 2, 5));
    }
}
