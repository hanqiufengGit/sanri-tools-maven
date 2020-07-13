package sanri.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sanri.utils.regex.OrdinaryNode;

public class RegexRandomUtil {
    private static Logger log = LoggerFactory.getLogger(RegexRandomUtil.class);

    /**
     * 从正则表达式生成随机数据
     * @param expression
     * @return
     */
    public static String regexRandom(String expression)  {
        try {
            OrdinaryNode ordinaryNode = new OrdinaryNode(expression);
            return ordinaryNode.random();
        } catch (Exception e) {
            log.error("使用正则表达式生成数据失败,{}:{}",e.getClass().getSimpleName(),e.getMessage());
        }
        return "";
    }
}