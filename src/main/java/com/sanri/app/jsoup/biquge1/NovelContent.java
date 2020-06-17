package com.sanri.app.jsoup.biquge1;

import com.sanri.app.jsoup.Request;
import com.sanri.app.jsoup.Select;

@Request("${link}")
public class NovelContent {
    @Select(value = "#content",attr = "html")
    private String content;
    @Select(value = ".bottem1>a:eq(0)",attr = "href")
    private String prev;
    @Select(value = ".bottem1>a:eq(2)",attr = "href")
    private String next;

    public String getContent() {
        return content;
    }

    public String getPrev() {
        return prev;
    }

    public String getNext() {
        return next;
    }
}
