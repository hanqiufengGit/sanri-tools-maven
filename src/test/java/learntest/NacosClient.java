package learntest;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import org.junit.Test;

public class NacosClient {
    @Test
    public void test() throws NacosException {
        ConfigService configService = NacosFactory.createConfigService("10.101.72.42:8848");
    }
}
