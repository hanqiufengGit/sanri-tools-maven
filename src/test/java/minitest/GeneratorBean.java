package minitest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import sanri.utils.VelocityUtil;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorBean {
    public static void main(String[] args) throws IOException, SQLException, InterruptedException, InvalidConfigurationException, XMLParserException {
        URL resource = GeneratorBean.class.getResource("/tkmapper.xml");
        String s = IOUtils.toString(resource.openStream(), "utf-8");
        Map<String,Object> map = new HashMap<>();
        map.put("driverClass","com.mysql.jdbc.Driver");
        map.put("connectionURL","jdbc:mysql://localhost:3306/card");
        map.put("userId","root");
        map.put("password","h123");
        Map<String,String> package_ = new HashMap<>();
        package_.put("entity","com.sanri.entity");
        map.put("package",package_);
        map.put("baseMapper","tk.mybatis.mapper.common.Mapper");

        map.put("tableNames","enterprise_card_base");
        map.put("idColumn","id");
        map.put("sqlStatement","JDBC");
        String formatString = VelocityUtil.formatString(s, map);

        System.out.println(formatString);


        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config  = cp.parseConfiguration(new StringReader(formatString));
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        for (String warning : warnings) {
            System.out.println(warning);
        }
    }
}
