package learntest;

import com.sanri.app.jsoup.biquge1.NovelChapter;
import com.sanri.app.jsoup.biquge1.NovelContent;
import com.sanri.app.jsoup.biquge1.NovelSearch;
import com.sanri.app.jsoup.SpiderHelper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import sanri.utils.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Test
    public void testPostFile() throws IOException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
//        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntityBuilder.addBinaryBody("file",new File("d:/test/test.txt"), ContentType.APPLICATION_OCTET_STREAM,"xxx.zip");
        // 少了文件名就不行 坑
//        multipartEntityBuilder.addBinaryBody("file",new File("d:/test/test.txt"));

        multipartEntityBuilder.addTextBody("username","sanri");
        multipartEntityBuilder.addTextBody("password","9420");

        HttpEntity build = multipartEntityBuilder
                .setBoundary(System.currentTimeMillis() +"")
                .setCharset(StandardCharsets.UTF_8)
                .build();

        HttpClient httpClient = HttpUtil.getHttpClient();
        HttpPost httpPost= new HttpPost("http://localhost:8089/test/multipartParam");
        httpPost.setEntity(build);
        HttpResponse execute = httpClient.execute(httpPost);
        StatusLine statusLine = execute.getStatusLine();
        if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            System.out.println("成功");
        }
        HttpClientUtils.closeQuietly(httpClient);
    }
}
