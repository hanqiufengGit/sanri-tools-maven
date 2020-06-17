package learntest;

import com.sanri.app.jsoup.biquge1.NovelChapter;
import com.sanri.app.jsoup.biquge1.NovelContent;
import com.sanri.app.jsoup.biquge1.NovelSearch;
import com.sanri.app.jsoup.SpiderHelper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestNovelGet {

    @Test
    public void test(){
        NovelSearch novelWeb = null;
        List<NovelSearch.CandidateNovel> candidateNovels = novelWeb.getCandidateNovels();
        for (NovelSearch.CandidateNovel candidateNovel : candidateNovels) {
            String link = candidateNovel.getLink();
            NovelChapter novelChapter = null;

        }
    }

    @Test
    public void testPart() throws IOException {
        SpiderHelper spiderHelper = new SpiderHelper();
        Map<String, String> params = new HashMap<>();
        params.put("keyword","校花");
        Object spider = spiderHelper.spider(NovelSearch.class, params);
        System.out.println(ToStringBuilder.reflectionToString(spider, ToStringStyle.SHORT_PREFIX_STYLE));
    }

    @Test
    public void testChapter() throws IOException {
        SpiderHelper spiderHelper = new SpiderHelper();
        Map<String, String> params = new HashMap<>();
        params.put("link","https://www.xsbiquge.com/0_780/");
        Object spider = spiderHelper.spider(NovelChapter.class, params);
        System.out.println(spider);
    }

    @Test
    public void testContent() throws IOException {
        SpiderHelper spiderHelper = new SpiderHelper();
        Map<String, String> params = new HashMap<>();
        params.put("link","https://www.xsbiquge.com/0_780/5032773.html");
        Object spider = spiderHelper.spider(NovelContent.class, params);
        System.out.println(spider);
    }

    @Test
    public void testPost() throws IOException {
        Document post = Jsoup.connect("http://www.xbiquge.la/modules/article/waps.php")
                .timeout(10000)
                .data("searchkey", "校花")
                .post();
        System.out.println(post);
    }
}
