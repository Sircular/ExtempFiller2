package org.zalgosircular.extempfiller2.research.formatting;

import org.junit.Test;
import org.zalgosircular.extempfiller2.research.Article;

import java.util.Date;

import static org.junit.Assert.assertTrue;

public class HTML_ENMLFormatterTest {

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
                "<invalid_tag_self_closing />\r\n" +
                "<invalid_div_like>\r\n" +
                "<p>test</p>" +
                "</invalid_div_like>\r\n" +
                "</body>\r\n" +
                "</html>";
        String enmlHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
                "<en-note>";
        //info div in between
        String enmlBody = "<div><div><p>test</p></div></div>";
        String enmlClose = "</en-note>";
        Article testArticle = new Article(url, title, author, date, html);

        //ignore formatting
        String out = new ENMLFormatter().format(testArticle).replaceAll("[^\\S]", "");
        enmlHead = enmlHead.replaceAll("[^\\S]", "");
        enmlBody = enmlBody.replaceAll("[^\\S]", "");
        enmlClose = enmlClose.replaceAll("[^\\S]", "");

        assertTrue("ENML Header broken", out.contains(enmlHead));
        assertTrue("HTML improperly sanitized", out.contains(enmlBody));
        assertTrue("ENML improperly closed", out.endsWith(enmlClose));
    }
}