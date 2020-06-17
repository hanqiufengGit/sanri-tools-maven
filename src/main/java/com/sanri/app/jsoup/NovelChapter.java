package com.sanri.app.jsoup;

/**
 * 小说章节
 */
@Request(value = "${link}")
public class NovelChapter {
    /**
     * 章节标题
     */
    @Select(value = "")
    private String title;

    /**
     * 章节链接
     */
    @Select(value = "",attr = "href")
    private String link;

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

}
