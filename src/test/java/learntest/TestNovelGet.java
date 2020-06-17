package learntest;

import com.sanri.app.jsoup.NovelChapter;
import com.sanri.app.jsoup.NovelWeb;
import com.sanri.app.jsoup.SpiderHelper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestNovelGet {

    @Test
    public void test(){
        NovelWeb novelWeb = null;
        List<NovelWeb.CandidateNovel> candidateNovels = novelWeb.getCandidateNovels();
        for (NovelWeb.CandidateNovel candidateNovel : candidateNovels) {
            String link = candidateNovel.getLink();
            NovelChapter novelChapter = null;
            String contentLink = novelChapter.getLink();

        }
    }

    @Test
    public void testPart() throws IOException {
        SpiderHelper spiderHelper = new SpiderHelper();
        Map<String, String> params = new HashMap<>();
        params.put("keyword","校花");
        Object spider = spiderHelper.spider(NovelWeb.class, params);
        System.out.println(ToStringBuilder.reflectionToString(spider, ToStringStyle.SHORT_PREFIX_STYLE));
    }
}
