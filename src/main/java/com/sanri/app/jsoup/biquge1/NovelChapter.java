package com.sanri.app.jsoup.biquge1;

import com.sanri.app.jsoup.Request;
import com.sanri.app.jsoup.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * 小说章节
 */
@Request(value = "${link}")
public class NovelChapter {

    @Select("#list")
    private List<Chapter> chapters = new ArrayList<>();


    @Select(">dl>dd")
    public static class Chapter{
        /**
         * 章节标题
         */
        @Select(value = ">a",attr = "content")
        private String title;

        /**
         * 章节链接
         */
        @Select(value = ">a",attr = "href")
        private String link;

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    public List<Chapter> getChapters() {
        return chapters;
    }
}
