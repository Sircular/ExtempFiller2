package org.zalgosircular.extempfiller2.research.formatting;

import org.junit.Test;
import org.zalgosircular.extempfiller2.research.Article;

import java.util.Date;

import static org.junit.Assert.assertTrue;

public class TextFormatterTest {

    public static final String endl = System.getProperty("line.separator");

    @Test
    public void testFormat() throws Exception {
        String url = "http://some_url.com";
        String title = "some title";
        String author = "some author";
        Date date = new Date(1437945280); // 7/26/2015
        String html = "<html>\r\n" +
                "<head>\r\n" +
                "</head>\r\n" +
                "<body invalid=\"to be removed\">\r\n" +
                "<p>line 0</p> <br />\r\n" +
                "<invalid_tag_self_closing />\r\n" +
                "<invalid_div_like>\r\n" +
                "<p>test</p> <br />\r\n" +
                "<p>line 2</p>\r\n" +
                "</invalid_div_like>\r\n" +
                "</body>\r\n" +
                "</html>";

        String plainText = "line 0" + endl + "test" + endl + "line 2";
        Article testArticle = new Article(url, title, author, date, html);

        String out = new TextFormatter().format(testArticle);

        assertTrue("Html incorrectly converted to plain text", out.contains(plainText));
    }
}