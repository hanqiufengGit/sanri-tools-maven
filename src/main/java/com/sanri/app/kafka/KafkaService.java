package com.sanri.app.kafka;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.app.dtos.kafka.*;
import com.sanri.app.dtos.kafka.MBeanInfo;
import com.sanri.app.servlet.FileManagerServlet;
import com.sanri.app.servlet.ZkServlet;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Constants;
import org.springframework.util.ReflectionUtils;
import sanri.utils.NumberUtil;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sanri.app.servlet.ZkServlet.zkSerializerMap;

public class KafkaService {
    // 依赖于 zookeeper,filemanager
    private ZkServlet zkServlet;
    private FileManagerServlet fileManagerServlet;

    public KafkaService(ZkServlet zkServlet, FileManagerServlet fileManagerServlet) {
        this.zkServlet = zkServlet;
        this.fileManagerServlet = fileManagerServlet;
    }

    private static final Map<String, AdminClient> adminClientMap = new HashMap<>();

    public AdminClient loadAdminClient(String clusterName) throws IOException {
        AdminClient adminClient = adminClientMap.get(clusterName);
        if(adminClient == null){
            Properties properties = kafkaProperties(clusterName);

            adminClient =  KafkaAdminClient.create(properties);
            adminClientMap.put(clusterName,adminClient);
        }

        return adminClient;
    }

    private static final String modul = "kafka";
    public void writeConfig(String zkConn, KafkaConnInfo kafkaConnInfo) throws IOException {
        String zkConnStrings = fileManagerServlet.readConfig(ZkServlet.modul, zkConn);
        kafkaConnInfo.setZkConnectStrings(zkConnStrings);
        kafkaConnInfo.setClusterName(zkConn);
        String chroot = kafkaConnInfo.getChroot();
        kafkaConnInfo.setJaasConfig(StringEscapeUtils.escapeJava(kafkaConnInfo.getJaasConfig()));
        fileManagerServlet.writeConfig(modul,zkConn, JSONObject.toJSONString(kafkaConnInfo));
    }

    public KafkaConnInfo readConfig(String clusterName) throws IOException {
        String kafkaConnInfoJson = fileManagerServlet.readConfig(modul,clusterName);
        return JSONObject.parseObject(kafkaConnInfoJson,KafkaConnInfo.class);
    }

    private static final String relativeBrokerPath = "/brokers/ids";
    static Pattern ipPort = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)");
    public List<BrokerInfo> brokers(KafkaConnInfo kafkaConnInfo) throws IOException {
        List<BrokerInfo> brokerInfos = new ArrayList<>();
        String clusterName = kafkaConnInfo.getClusterName();
        String chroot = kafkaConnInfo.getChroot();

        List<String> childrens = zkServlet.childrens(clusterName, chroot + relativeBrokerPath);
        for (String children : childrens) {
            String brokerInfo = Objects.toString(zkServlet.readData(clusterName, chroot + relativeBrokerPath + "/" + children, "string"),"");
            JSONObject brokerJson = JSONObject.parseObject(brokerInfo);
            String host = brokerJson.getString("host");
            int port = brokerJson.getIntValue("port");
            int jmxPort = brokerJson.getIntValue("jmx_port");

            if(StringUtils.isBlank(host)){
                //如果没有提供 host 和 port 信息，则从 endpoints 中拿取信息
                JSONArray endpoints = brokerJson.getJSONArray("endpoints");
                String endpoint = endpoints.getString(0);
                Matcher matcher = ipPort.matcher(endpoint);
                if(matcher.find()) {
                    host = matcher.group(1);
                    port = NumberUtil.toInt(matcher.group(2));
                }
            }

            brokerInfos.add(new BrokerInfo(NumberUtils.toInt(children),host,port,jmxPort));
        }
        return brokerInfos;
    }

    public List<String> brokers(String clusterName) throws IOException {
        List<BrokerInfo> brokerInfos = brokers(readConfig(clusterName));
        List<String> servers = new ArrayList<>();
        for (BrokerInfo brokerInfo : brokerInfos) {
            String server = brokerInfo.getHost() + ":" + brokerInfo.getPort();
            servers.add(server);
        }
        return servers;
    }

    public Properties kafkaProperties(String clusterName) throws IOException {
        KafkaConnInfo kafkaConnInfo = readConfig(clusterName);

        Properties properties = createDefaultConfig(clusterName);

        //从 zk 中拿到 bootstrapServers
        List<String> servers = brokers(clusterName);

        String bootstrapServers = StringUtils.join(servers,',');
        properties.put("bootstrap.servers", bootstrapServers);

        if(StringUtils.isNotBlank(kafkaConnInfo.getSaslMechanism())) {
            properties.put(SaslConfigs.SASL_MECHANISM, kafkaConnInfo.getSaslMechanism());
        }
        if(StringUtils.isNotBlank(kafkaConnInfo.getSecurityProtocol())){
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaConnInfo.getSecurityProtocol());
        }

        String securityChoseValue = kafkaConnInfo.getSecurityProtocol();
        SecurityProtocol securityProtocol = SecurityProtocol.valueOf(securityChoseValue);
        switch (securityProtocol){
            case PLAINTEXT:
                break;
            case SASL_PLAINTEXT:
                properties.put("sasl.jaas.config",kafkaConnInfo.getJaasConfig());
                break;
            case SASL_SSL:
                properties.put("sasl.jaas.config",kafkaConnInfo.getJaasConfig());
            case SSL:
                throw new IllegalArgumentException("不支持 ssl 操作");
        }

        return properties;
    }


    private Properties createDefaultConfig(String clusterName) {
        Properties properties = new Properties();
        final String consumerGroup = "console-"+clusterName;
        //每个消费者分配独立的组号
        properties.put("group.id", consumerGroup);
        //如果value合法，则自动提交偏移量
        properties.put("enable.auto.commit", "true");
        //设置多久一次更新被消费消息的偏移量
        properties.put("auto.commit.interval.ms", "1000");
        //设置会话响应的时间，超过这个时间kafka可以选择放弃消费或者消费下一条消息
        properties.put("session.timeout.ms", "30000");
        //该参数表示从头开始消费该主题
        properties.put("auto.offset.reset", "earliest");
        //注意反序列化方式为ByteArrayDeserializer
        properties.put("key.deserializer","org.apache.kafka.common.serialization.ByteArrayDeserializer");
        properties.put("value.deserializer","org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return properties;
    }

    private static final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
    public Collection<MBeanInfo> monitor(String clusterName, Class clazz, String topic) throws IOException, MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException {
        KafkaConnInfo kafkaConnInfo = readConfig(clusterName);
        List<BrokerInfo> brokers = brokers(kafkaConnInfo);

        List<MBeanInfo> mBeanInfos = new ArrayList<>();
        for (BrokerInfo broker : brokers) {
            String host = broker.getHost();
            int jxmPort = broker.getJxmPort();
            String uri = host+":"+jxmPort;
            if(jxmPort == -1){
                return null;
            }

            JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, uri));
            JMXConnector connector = JMXConnectorFactory.connect(jmxSeriverUrl);
            MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();

            // 遍历所有的 mBean
            Constants constants = new Constants(clazz);
            List<String> mBeans = constansValues(constants);
            for (String mBean : mBeans) {
                if (clazz == BrokerTopicMetrics.TopicMetrics.class){
                    mBean = String.format(mBean,topic);
                }
                Object fifteenMinuteRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.FIFTEEN_MINUTE_RATE);
                Object fiveMinuteRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.FIVE_MINUTE_RATE);
                Object meanRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.MEAN_RATE);
                Object oneMinuteRate = mbeanConnection.getAttribute(new ObjectName(mBean), BrokerTopicMetrics.MBean.ONE_MINUTE_RATE);
                MBeanInfo mBeanInfo = new MBeanInfo(mBean,objectDoubleValue(fifteenMinuteRate), objectDoubleValue(fiveMinuteRate), objectDoubleValue(meanRate), objectDoubleValue(oneMinuteRate));
                mBeanInfos.add(mBeanInfo);
            }
        }

        // 数据合并
        Map<String,MBeanInfo> mergeMap = new HashMap<>();
        for (MBeanInfo mBeanInfo : mBeanInfos) {
            String mBean = mBeanInfo.getmBean();
            MBeanInfo mergeMBeanInfo = mergeMap.get(mBean);
            if(mergeMBeanInfo == null){
                mergeMBeanInfo = mBeanInfo;
                mergeMap.put(mBean,mergeMBeanInfo);
                continue;
            }
            mergeMBeanInfo.addData(mBeanInfo);
        }

        return mergeMap.values();
    }

    private double objectDoubleValue(Object value){
        return  NumberUtils.toDouble(value.toString());
    }

    private List<String> constansValues(Constants constants) {
        List<String> mMbeans = new ArrayList<>();
        try {
            Method getFieldCache = Constants.class.getDeclaredMethod("getFieldCache");
            getFieldCache.setAccessible(true);
            Map<String, Object> invokeMethod = (Map<String, Object>) ReflectionUtils.invokeMethod(getFieldCache, constants);
            Collection<Object> values = invokeMethod.values();

            for (Object value : values) {
                mMbeans.add(Objects.toString(value));
            }
        } catch (NoSuchMethodException e) {}
        return mMbeans;
    }
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 5, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            // 设置线程内的类加载器
            thread.setContextClassLoader(KafkaService.class.getClassLoader());
            return thread;
        }
    });
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private static Map<String,ConsumerTask> consumerTaskMap = new HashMap<>();
    public void startListener(NotifyConsumerRecordClient notifyConsumerRecordClient) {
        // 检测是否有已经在消费的主题任务,有则加入,否则启动新的主题任务
        String topic = notifyConsumerRecordClient.getTopic();
        ConsumerTask consumerTask = consumerTaskMap.get(topic);
        if(consumerTask == null){
            consumerTask = new ConsumerTask(notifyConsumerRecordClient.getClientMessage());
            threadPoolExecutor.submit(consumerTask);
            consumerTaskMap.put(topic,consumerTask);
        }
        consumerTask.addListener(notifyConsumerRecordClient);
    }

    /**
     * 移除所有监听的客户端
     * @param session
     */
    public void removeClient(Session session) {
        Iterator<ConsumerTask> iterator = consumerTaskMap.values().iterator();
        while (iterator.hasNext()){
            iterator.next().removeListener(session);
        }
    }

    class ConsumerTask implements Runnable{
        private Logger log = LoggerFactory.getLogger(getClass());
        private ClientMessage initClientMessage;
        private long lastNoClientTime;

        public ConsumerTask(ClientMessage clientMessage) {
            this.initClientMessage = clientMessage;
        }

        private List<NotifyConsumerRecordClient> notifyConsumerRecordClients = new ArrayList<>();

        @Override
        public void run() {
            String clusterName = initClientMessage.getClusterName();
            String topic = initClientMessage.getTopic();
            try {
                AdminClient adminClient = loadAdminClient(clusterName);
                DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topic));
                TopicDescription topicDescription = describeTopicsResult.values().get(topic).get();
                List<TopicPartitionInfo> partitions = topicDescription.partitions();

                Properties properties = kafkaProperties(clusterName);
                KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<byte[], byte[]>(properties);
                try {
                    List<TopicPartition> topicPartitions = new ArrayList<>();
                    for (int i=0;i<partitions.size();i++) {
                        TopicPartition topicPartition = new TopicPartition(topic, i);
                        topicPartitions.add(topicPartition);
                    }

                    consumer.assign(topicPartitions);

                    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
                    Iterator<Map.Entry<TopicPartition, Long>> iterator = endOffsets.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry<TopicPartition, Long> next = iterator.next();
                        TopicPartition key = next.getKey();
                        Long value = next.getValue();
                        consumer.seek(key,value);
                    }
                    boolean openConsumer = lastNoClientTime == 0 || (System.currentTimeMillis() - lastNoClientTime < 300000);
                    if(!openConsumer){
                        log.info("关闭消费[{}],没有客户端已经超过 5 分钟 ",topic);

                        // 通知客户端,已经没有在监听数据了

                    }
                    while (openConsumer) {      // 当没有客户端并超过 5 分钟时关闭消费
                        ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(10));       // 100ms 内抓取的数据，不是抓取的数据量
                        Iterable<ConsumerRecord<byte[], byte[]>> records = consumerRecords.records(topic);
                        Iterator<ConsumerRecord<byte[], byte[]>> consumerRecordIterator = records.iterator();
                        while (consumerRecordIterator.hasNext()){
                            ConsumerRecord<byte[], byte[]> consumerRecord = consumerRecordIterator.next();
                            for (NotifyConsumerRecordClient notifyConsumerRecordClient : notifyConsumerRecordClients) {
                                notifyConsumerRecordClient.sendMessage(consumerRecord);
                            }
                        }
                    }
                }finally {
                    if(consumer != null)
                        consumer.close();
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                log.error("未能加载 kafka 客户端 [{}]",e.getMessage(),e);
            }

        }

        /**
         * 检查是否需要关闭消费端
         */
        public void checkCloseConsumer(){
            if(notifyConsumerRecordClients.isEmpty()){
                if (lastNoClientTime == 0) {
                    lastNoClientTime = System.currentTimeMillis();
                }
            }
        }

        /**
         * 添加消费监听客户端
         * @param notifyConsumerRecordClient
         */
        public void addListener(NotifyConsumerRecordClient notifyConsumerRecordClient) {
            notifyConsumerRecordClients.add(notifyConsumerRecordClient);
            this.lastNoClientTime = 0 ;
        }

        /**
         * 清除监听者
         * @param session
         */
        public void removeListener(Session session){
            Iterator<NotifyConsumerRecordClient> iterator = notifyConsumerRecordClients.iterator();
            while (iterator.hasNext()){
                NotifyConsumerRecordClient next = iterator.next();
                Session clientSession = next.getSession();
                if(clientSession.getId().equals(session.getId())){
                    iterator.remove();break;
                }
            }
            // 检查是否需要停止消费
            checkCloseConsumer();
        }
    }
}
