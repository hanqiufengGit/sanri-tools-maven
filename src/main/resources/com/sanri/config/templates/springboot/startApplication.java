package ${config.packageConfig.base};

import com.sanri.web.configs.EnableWebUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableWebUI
@MapperScan(basePackages = "${config.packageConfig.mapper}")
public class $StringUtils.capitalize(${config.projectName})Application {

    public static void main(String[] args) {
        SpringApplication.run($StringUtils.capitalize(${config.projectName})Application .class, args);
    }

}