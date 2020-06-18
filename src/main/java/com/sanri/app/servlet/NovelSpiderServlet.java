package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.dtos.NovelBook;
import com.sanri.app.jsoup.SpiderHelper;
import com.sanri.app.jsoup.biquge1.NovelChapter;
import com.sanri.app.jsoup.biquge1.NovelContent;
import com.sanri.app.jsoup.biquge1.NovelSearch;
import com.sanri.frame.RequestMapping;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RequestMapping("/novel")
public class NovelSpiderServlet extends BaseServlet {
    //保存最近查看的 10 本书
    private Queue<NovelBook> latestUse = new ArrayDeque<NovelBook>(10);

    SpiderHelper spiderHelper = new SpiderHelper();

    public Queue<NovelBook> latestUse(){
        return latestUse;
    }
    /**
     * 小说的实现类列表
     * @return
     */
    public List<String> implClasses(){
        return Arrays.asList(NovelSearch.class.getName(), com.sanri.app.jsoup.biquge2.NovelSearch.class.getName());
    }

    /**
     * 书搜索
     * @param keyword
     * @param pageNo
     * @return
     */
    public List<NovelBook> search(String implClass,String keyword,String pageNo) throws ClassNotFoundException, IOException {
        Class<?> clazz = Class.forName(implClass);
        Map<String, String> params = new HashMap<>();
        params.put("keyword",keyword);
        params.put("pageNo",pageNo);
        Object spider = spiderHelper.spider(clazz, params);

        try {
            List candidateNovels = (List) FieldUtils.readDeclaredField(spider, "candidateNovels", true);
            if(candidateNovels != null){
                Iterator iterator = candidateNovels.iterator();
                List<NovelBook> novelBooks = new ArrayList<>(candidateNovels.size());
                while (iterator.hasNext()){
                    Object next = iterator.next();
                    NovelBook novelBook = new NovelBook();novelBooks.add(novelBook);
                    try {
                        BeanUtils.copyProperties(novelBook,next);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                return novelBooks;
            }
        } catch (IllegalAccessException e) {
            logger.error("解析类型出错[{}]",e.getMessage(),e);
        }
        return null;
    }

    /**
     * 搜索书的章节信息
     * @param link
     * @return
     */
    public List<NovelChapter.Chapter> chapters(String link) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("link",link);
        NovelChapter spider = (NovelChapter) spiderHelper.spider(NovelChapter.class, params);
        List<NovelChapter.Chapter> chapters = spider.getChapters();
        for (NovelChapter.Chapter chapter : chapters) {
            String shortLink = chapter.getLink();
            try {
                URI resolve = new URI(link).resolve(new URI(shortLink));
                chapter.setLink(resolve.toString());
            }catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return chapters;
    }

    /**
     * 小说内容抓取
     * @param link
     * @return
     * @throws IOException
     */
    public NovelContent content(String link) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("link",link);
        NovelContent spider = (NovelContent) spiderHelper.spider(NovelContent.class, params);
        try {
            URI root = new URI(link).resolve("/");
            spider.setNext(root.resolve(new URI(spider.getNext())).toString());
            spider.setPrev(root.resolve(new URI(spider.getPrev())).toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return spider;
    }

}

