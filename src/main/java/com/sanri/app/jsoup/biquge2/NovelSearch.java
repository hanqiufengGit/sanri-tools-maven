package com.sanri.app.jsoup.biquge2;

import com.sanri.app.jsoup.Request;
import com.sanri.app.jsoup.Select;
import io.swagger.models.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * 新笔越阁只有搜索不一样,章节和内容一样的 html
 */
@Request(value = "http://www.xbiquge.la/modules/article/waps.php",httpMethod = HttpMethod.POST)
public class NovelSearch {

    @Select("#content>form>table>tbody")
    private List<CandidateNovel> candidateNovels = new ArrayList<>();

    @Select(">tr")
    public static class CandidateNovel{
        @Select(value = "td:eq(0)>a",attr = "content")
        private String title;
        @Select(value = "td:eq(0)>a",attr = "href")
        private String link;
        @Select(value = "td:eq(2)>a",attr = "content")
        private String author;
        @Select(value = "td:eq(3)>a",attr = "content")
        private String updateTime;
    }
}
