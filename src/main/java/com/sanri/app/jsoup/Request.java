package com.sanri.app.jsoup;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Request {
    /**
     * 请求地址,查询参数拼接在后面
     * @return
     */
    String value();
}
