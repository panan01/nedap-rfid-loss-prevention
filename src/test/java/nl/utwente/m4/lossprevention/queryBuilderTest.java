package nl.utwente.m4.lossprevention;


import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import static nl.utwente.m4.lossprevention.utils.excelUtils.*;
import static nl.utwente.m4.lossprevention.utils.sqlUtils.*;
import static org.junit.jupiter.api.Assertions.*;


public class queryBuilderTest {
    @BeforeEach
    public void setUp() {


    }



    @Test
    public void mostStolenByQueriesTest() {

        String query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.category|-1|article|-0-1|article.category|-0-1|article.category|-0";
        String[] generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.category, COUNT(nedap.article.category) FROM nedap.article GROUP BY nedap.article.category ORDER BY COUNT(article.category) DESC ",generateSetStringInputs(generationCodeArray[1]));

        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.product|-1|article|-0-1|article.product|-0-1|article.product|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.product, COUNT(nedap.article.product) FROM nedap.article GROUP BY nedap.article.product ORDER BY COUNT(article.product) DESC ",generateSetStringInputs(generationCodeArray[1]));

        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.color|-1|article|-0-1|article.color|-0-1|article.color|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.color, COUNT(nedap.article.color) FROM nedap.article GROUP BY nedap.article.color ORDER BY COUNT(article.color) DESC ",generateSetStringInputs(generationCodeArray[1]));

        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.size|-1|article|-0-1|article.size|-0-1|article.size|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.size, COUNT(nedap.article.size) FROM nedap.article GROUP BY nedap.article.size ORDER BY COUNT(article.size) DESC ",generateSetStringInputs(generationCodeArray[1]));

        query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|article.price|-1|article|-0-1|article.price|-0-1|article.price|-0";
        generationCodeArray = query.split(";");
        assertEquals("SELECT DISTINCT nedap.article.price, COUNT(nedap.article.price) FROM nedap.article GROUP BY nedap.article.price ORDER BY COUNT(article.price) DESC ",generateSetStringInputs(generationCodeArray[1]));



    }

}
