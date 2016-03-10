package org.zalgosircular.extempfiller2.research.fetching.web.urls;

/**
 * Created by Logan Lembke on 7/12/2015.
 */
public enum SEARCH_ENGINE {
    GOOGLE("http://www.google.com/search?q=%s&start=%s",
            "div#ires ol#rso div.srg div.g div.rc h3.r a",
            "div#foot span#xjs div#navcnt table#nav tbody tr td.b a.pn span"
    ),
    DDG("http://duckduckgo.com/html/?q=%s&s=%s",
            "div#links div.links_main.links_deep a.result__a",
            "div#links div.results_links_more form input.navbutton"
    );

    public final String QUERY_STRING, RESULT_SELECTOR, NEXT_SELECTOR;

    SEARCH_ENGINE(String query_string, String result_selector, String next_selector) {
        RESULT_SELECTOR = result_selector;
        QUERY_STRING = query_string;
        NEXT_SELECTOR = next_selector;
    }
}
