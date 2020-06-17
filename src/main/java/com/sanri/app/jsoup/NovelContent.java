package com.sanri.app.jsoup;

@Request("${link}")
public class NovelContent {
    @Select("")
    private String content;

    public String getContent() {
        return content;
    }

}
