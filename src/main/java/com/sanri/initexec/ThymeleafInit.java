package com.sanri.initexec;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.annotation.PostConstruct;

/**
 * 模板引擎初始化
 */
public class ThymeleafInit {
    public final TemplateEngine templateEngine = new TemplateEngine();

    @PostConstruct
    public void init(){
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("com/sanri/config/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("utf-8");
        templateResolver.setCacheable(false);

        templateEngine.setTemplateResolver(templateResolver);
    }

}
