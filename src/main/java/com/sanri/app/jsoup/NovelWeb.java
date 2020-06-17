package com.sanri.app.jsoup;

import java.util.ArrayList;
import java.util.List;

/**
 * 小说新笔趣阁抓取
 */
@Request(value = "https://www.xsbiquge.com/search.php?keyword=${keyword}")
public class NovelWeb {
    @Select(".result-list")
    private List<CandidateNovel> candidateNovels = new ArrayList<>();

    /**
     * 候选小说
     */
    @Select(">.result-item.result-game-item")
    public static class CandidateNovel{
        @Select(value = ">.result-game-item-detail>h3>a",attr = "title" )
        private String title;
        @Select(value = ">.result-game-item-detail>.result-game-item-info>p:eq(2)>span:eq(1)",attr = "content" )
        private String updateTime;
        @Select(value = ">.result-game-item-detail>.result-game-item-info>p:eq(1)>span:eq(1)",attr = "content" )
        private String category;
        @Select(value = ">.result-game-item-detail>.result-game-item-info>p:eq(0)>span:eq(1)",attr = "content" )
        private String author;
        @Select(value = ">.result-game-item-pic>a>img",attr = "src")
        private String logo;
        @Select(value = ">.result-game-item-detail>h3>a",attr = "href" )
        private String link;
        @Select(value = ">.result-game-item-detail>p.result-game-item-desc",attr = "content" )
        private String describe;

        public String getTitle() {
            return title;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public String getDescribe() {
            return describe;
        }

        public String getCategory() {
            return category;
        }

        public String getAuthor() {
            return author;
        }

        public String getLogo() {
            return logo;
        }

        public String getLink() {
            return link;
        }

    }

    public List<CandidateNovel> getCandidateNovels() {
        return candidateNovels;
    }
}
