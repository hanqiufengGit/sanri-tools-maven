package com.sanri.app.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.app.BaseServlet;
import com.sanri.app.dtos.kafka.*;
import com.sanri.app.dtos.kafka.MBeanInfo;
import com.sanri.frame.RequestMapping;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.*;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.springframework.core.Constants;
import org.springframework.util.ReflectionUtils;
import sanri.utils.NumberUtil;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sanri.app.servlet.ZkServlet.zkSerializerMap;

@RequestMapping("/kafka")
public class KafkaServlet extends BaseServlet{

    // 依赖于 zookeeper,filemanager
    private ZkServlet zkServlet;
    private FileManagerServlet fileManagerServlet;
    private static final String modul = "kafka";
    private static final String relativeBrokerPath = "/brokers/ids";

    private static final Map<String, AdminClient> adminClientMap = new HashMap<>();

    //构造注入,目前仅支持注入 servlet
    public KafkaServlet(ZkServlet zkServlet,FileManagerServlet fileManagerServlet){
        this.zkServlet = zkServlet;
        this.fileManagerServlet = fileManagerServlet;
    }

    /**
     * 保存配置,新连接
     * @param zkConn
     * @param kafkaConnInfo
     * @throws IOException
     */
    public int writeConfig(String zkConn, KafkaConnInfo kafkaConnInfo) throws IOException {
        String zkConnStrings = fileManagerServlet.readConfig(ZkServlet.modul, zkConn);
        kafkaConnInfo.setZkConnectStrings(zkConnStrings);
        kafkaConnInfo.setClusterName(zkConn);
        String chroot = kafkaConnInfo.getChroot();
        kafkaConnInfo.setJaasConfig(StringEscapeUtils.escapeJava(kafkaConnInfo.getJaasConfig()));
        fileManagerServlet.writeConfig(modul,zkConn, JSONObject.toJSONString(kafkaConnInfo));
        return 0;
    }

    /**
     * 读取 kafka 连接配置
     * @param clusterName
     * @return
     * @throws IOException
     */
    public KafkaConnInfo readConfig(String clusterName) throws IOException {
        String kafkaConnInfoJson = fileManagerServlet.readConfig(modul,clusterName);
        return JSONObject.parseObject(kafkaConnInfoJson,KafkaConnInfo.class);
    }

    /**
     * 创建主键
     * @param clusterName
     * @param topic
     * @param partitions
     * @param replication
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public int createTopic(String clusterName,String topic,int partitions,int replication) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        NewTopic newTopic = new NewTopic(topic,partitions,(short)replication);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singletonList(newTopic));
        KafkaFuture<Void> voidKafkaFuture = createTopicsResult.values().get(topic);
        voidKafkaFuture.get();
        return 0;
    }

    /**
     * 删除主题
     * @param clusterName
     * @param topic
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public int deleteTopic(String clusterName,String topic) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singletonList(topic));
        deleteTopicsResult.all().get();
        return 0;
    }

    /**
     * 获取所有的节点连接信息
     * @param clusterName
     * @return
     * @throws IOException
     */
    public List<String> brokers(String clusterName) throws IOException {
        List<BrokerInfo> brokerInfos = brokers(readConfig(clusterName));
        List<String> servers = new ArrayList<>();
        for (BrokerInfo brokerInfo : brokerInfos) {
            String server = brokerInfo.getHost() + ":" + brokerInfo.getPort();
            servers.add(server);
        }
        return servers;
    }

    /**
     * 查询所有分组
     * @param clusterName
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<String> groups(String clusterName) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        List<String> groupNames = new ArrayList<>();

        ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
        Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
        for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
            String groupId = consumerGroupListing.groupId();
            groupNames.add(groupId);
        }
        return groupNames;
    }

    /**
     * 删除消费组
     * @param clusterName
     * @param group
     * @return
     */
    public int deleteGroup(String clusterName,String group) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        DeleteConsumerGroupsResult deleteConsumerGroupsResult = adminClient.deleteConsumerGroups(Collections.singletonList(group));
        deleteConsumerGroupsResult.all().get();
        return 0;
    }

    /**
     * 所有主题查询
     *
     * @return
     */
    public Map<String, Integer> topics(String clusterName) throws IOException, ExecutionException, InterruptedException {
        Map<String, Integer> result = new HashMap<>();

        AdminClient adminClient = loadAdminClient(clusterName);

        ListTopicsResult listTopicsResult = adminClient.listTopics();
        Set<String> topics = listTopicsResult.names().get();
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics);
        Map<String, KafkaFuture<TopicDescription>> values = describeTopicsResult.values();
        Iterator<Map.Entry<String, KafkaFuture<TopicDescription>>> iterator = values.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, KafkaFuture<TopicDescription>> topicDescriptionEntry = iterator.next();
            String topic = topicDescriptionEntry.getKey();
            TopicDescription topicDescription = topicDescriptionEntry.getValue().get();
            List<TopicPartitionInfo> partitions = topicDescription.partitions();
            result.put(topic,partitions.size());
        }
        return result;
    }

    public int topicPartitions(String clusterName,String topic) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topic));
        TopicDescription topicDescription = describeTopicsResult.values().get(topic).get();
        return topicDescription.partitions().size();
    }

    /**
     * 查询分组订阅的主题列表
     * @param clusterName
     * @param group
     * @return
     * @throws IOException
     */
    public Set<String> groupSubscribeTopics(String clusterName, String group) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        Set<String> subscribeTopics = new HashSet<>();

        DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singletonList(group));
        Map<String, KafkaFuture<ConsumerGroupDescription>> stringKafkaFutureMap = describeConsumerGroupsResult.describedGroups();
        ConsumerGroupDescription consumerGroupDescription = stringKafkaFutureMap.get(group).get();
        Collection<MemberDescription> members = consumerGroupDescription.members();
        for (MemberDescription member : members) {
            MemberAssignment assignment = member.assignment();
            Set<TopicPartition> topicPartitions = assignment.topicPartitions();
            Iterator<TopicPartition> iterator = topicPartitions.iterator();
            while (iterator.hasNext()){
                TopicPartition topicPartition = iterator.next();
                subscribeTopics.add(topicPartition.topic());
            }
        }
        return subscribeTopics;
    }

    /**
     * 消费组订阅主题消费情况查询
     * @param clusterName
     * @param group
     * @return
     * @throws IOException
     */
    public List<TopicOffset> groupSubscribeTopicsMonitor(String clusterName, String group) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);

        //创建 KafkaConsumer 来获取 offset ,lag,logsize
        Properties properties = kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<byte[], byte[]>(properties);

        List<TopicPartition> topicPartitionsQuery = new ArrayList<>();
        DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singletonList(group));
        Map<String, KafkaFuture<ConsumerGroupDescription>> stringKafkaFutureMap = describeConsumerGroupsResult.describedGroups();
        ConsumerGroupDescription consumerGroupDescription = stringKafkaFutureMap.get(group).get();
        Collection<MemberDescription> members = consumerGroupDescription.members();
        List<TopicPartition> allTopicPartition = new ArrayList<>();
        for (MemberDescription member : members) {
            MemberAssignment assignment = member.assignment();
            Set<TopicPartition> topicPartitions = assignment.topicPartitions();
            allTopicPartition.addAll(topicPartitions);
        }

        //查询  offset 信息
        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = adminClient.listConsumerGroupOffsets(group).partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(allTopicPartition);
        Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
        Map<String,TopicOffset> topicOffsets = new HashMap<>();
        while (iterator.hasNext()){
            Map.Entry<TopicPartition, Long> entry = iterator.next();
            TopicPartition topicPartition = entry.getKey();
            String topic = topicPartition.topic();
            int partition = topicPartition.partition();
            Long logSize = entry.getValue();
            long offset = offsetAndMetadataMap.get(topicPartition).offset();
            long lag = logSize - offset;
//            long offset = consumer.position(topicPartition);
//            long lag = logSize - offset;

            TopicOffset topicOffset = topicOffsets.get(topic);
            if(topicOffset == null){
                topicOffset = new TopicOffset(group,topic);
                topicOffsets.put(topic,topicOffset);
            }
            topicOffset.addPartitionOffset(new OffsetShow(topic,partition,offset,logSize));
        }
        return new ArrayList<>(topicOffsets.values());
    }

    /**
     * 消费组主题信息监控
     * @param clusterName
     * @param group
     * @param topic
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<OffsetShow> groupTopicMonitor(String clusterName, String group, String topic) throws IOException, ExecutionException, InterruptedException {
        List<OffsetShow> offsetShows = new ArrayList<>();
        AdminClient adminClient = loadAdminClient(clusterName);
        Properties properties = kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(properties);

        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singletonList(topic));
        TopicDescription topicDescription = describeTopicsResult.values().get(topic).get();
        List<TopicPartitionInfo> partitions = topicDescription.partitions();
        List<TopicPartition> topicPartitions = new ArrayList<>();
        for (TopicPartitionInfo partition : partitions) {
            TopicPartition topicPartition = new TopicPartition(topic, partition.partition());
            topicPartitions.add(topicPartition);
        }

        //查询  offset 信息
        ListConsumerGroupOffsetsOptions listConsumerGroupOffsetsOptions = new ListConsumerGroupOffsetsOptions();
        listConsumerGroupOffsetsOptions.topicPartitions(topicPartitions);

        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = adminClient.listConsumerGroupOffsets(group,listConsumerGroupOffsetsOptions).partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
        Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<TopicPartition, Long> entry = iterator.next();
            TopicPartition topicPartition = entry.getKey();
            Long logSize = entry.getValue();
//            long offset = consumer.position(topicPartition);
            OffsetAndMetadata offsetAndMetadata = offsetAndMetadataMap.get(topicPartition);
            long offset = offsetAndMetadata.offset();
            int partition = topicPartition.partition();
            long lag = logSize - offset;
            OffsetShow offsetShow = new OffsetShow(topic, partition, offset, logSize);
            offsetShows.add(offsetShow);
        }

        Collections.sort(offsetShows);
        return offsetShows;
    }

    public Map<String, Long> logSizes(String clusterName, String topic) throws IOException, ExecutionException, InterruptedException {
        Map<String, Long> results = new HashMap<>();
//        int partitions = topicPartitions(clusterName, topic);

        Properties properties = kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(properties);

        try {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);

            List<TopicPartition> topicPartitions = new ArrayList<>();
            for (int i = 0; i < partitionInfos.size(); i++) {
                topicPartitions.add(new TopicPartition(topic, i));
            }

            Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
            Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TopicPartition, Long> entry = iterator.next();
                TopicPartition topicPartition = entry.getKey();
                Long logSize = entry.getValue();

                results.put(topicPartition.partition() + "", logSize);
            }
        }finally {
            if(consumer != null)
                consumer.close();
        }

        return results;
    }

    /**
     * 消费某一分区最后的数据,最后 100 条
     * @param name
     * @param topic
     * @param partition
     * @param serialize
     * @return
     */
    public List<KafkaData> lastDatas(String clusterName,String topic,int partition,String serialize) throws IOException {
        Properties properties = kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<byte[], byte[]>(properties);
        List<KafkaData> datas = new ArrayList<>();
        try {
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            consumer.assign(Collections.singletonList(topicPartition));

            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Collections.singletonList(topicPartition));
            Long endOffset = endOffsets.get(topicPartition);
            long seekOffset =  endOffset - 100;
            if(seekOffset < 0) {seekOffset = 0L;}

            consumer.seek(topicPartition,seekOffset);

            ZkSerializer zkSerializer = zkSerializerMap.get(serialize);
            while (true) {
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(10));
                List<ConsumerRecord<byte[], byte[]>> records = consumerRecords.records(topicPartition);
                long currOffset = seekOffset;
                if (CollectionUtils.isEmpty(records)) {
                    logger.info("[" + clusterName + "][" + topic + "][" + partition + "][" + seekOffset + "]读取到数据量为 0 ");
                    break;
                }
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    long offset = record.offset();
                    currOffset = offset;
                    long timestamp = record.timestamp();
                    byte[] value = record.value();
                    Object deserialize = zkSerializer.deserialize(value);
                    datas.add(new KafkaData(offset,deserialize,timestamp));
                }
                if (currOffset >= endOffset) {
                    break;
                }
            }
        }finally {
            if(consumer != null)
                consumer.close();
        }
        Collections.sort(datas);
        return datas;
    }

    /**
     * 消费某一分区附近数据; 前 100 条,后 100 条
     * @param name
     * @param group
     * @param topic
     * @param partition
     * @param serialize
     * @return offset => data
     */
    public List<KafkaData> nearbyDatas(String clusterName, String topic, int partition, long offset, String serialize) throws IOException {
        Properties properties = kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<byte[], byte[]>(properties);
        List<KafkaData> datas = new ArrayList<>();
        try {
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            consumer.assign(Collections.singletonList(topicPartition));
            // 查询前 100 条,后 100 条
            long seekOffset = offset - 100;
            if (seekOffset < 0) {
                seekOffset = 0;
            }

            consumer.seek(topicPartition, seekOffset);

            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Collections.singletonList(topicPartition));
            Long endOffset = endOffsets.get(topicPartition);
            long seekEndOffset = offset + 100;
            if (seekEndOffset > endOffset) {
                seekEndOffset = endOffset;
            }
            ZkSerializer zkSerializer = zkSerializerMap.get(serialize);
            while (true) {
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(10));       // 100ms 内抓取的数据，不是抓取的数据量
                List<ConsumerRecord<byte[], byte[]>> records = consumerRecords.records(topicPartition);
                long currOffset = seekOffset;
                if (CollectionUtils.isEmpty(records)) {
                    logger.info("[" + clusterName + "][" + topic + "][" + partition + "][" + seekOffset + "]读取到数据量为 0 ");
                    break;
                }
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    long timestamp = record.timestamp();
                    currOffset = record.offset();
                    byte[] value = record.value();
                    Object deserialize = zkSerializer.deserialize(value);
//                    datas.put(currOffset + "", deserialize);
                    datas.add(new KafkaData(currOffset,deserialize,timestamp));
                }
                if (currOffset >= seekEndOffset) {
                    break;
                }
            }
        }finally {
            if(consumer != null)
                consumer.close();
        }
        Collections.sort(datas);
        return datas;
    }

    // 查询所有分区数据,根据时间排序
    public List<PartitionKafkaData> allPartitionDatas(String clusterName, String topic, long perPartitionMessages, String serialize) throws IOException {
        Properties properties = kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<byte[], byte[]>(properties);
        List<PartitionKafkaData> datas = new ArrayList<>();
        try {
            // 获取分区列表
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            List<TopicPartition> topicPartitions = new ArrayList<>();
            for (PartitionInfo partitionInfo : partitionInfos) {
                int partition = partitionInfo.partition();
                TopicPartition topicPartition = new TopicPartition(topic, partition);
//                consumer.assign(Collections.singletonList(topicPartition));
                topicPartitions.add(topicPartition);
            }
            consumer.assign(topicPartitions);

            // 定位到每一个分区的最后 100 条数据
            Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
            Iterator<Map.Entry<TopicPartition, Long>> iterator = topicPartitionLongMap.entrySet().iterator();
            int seekCount = 0;
            while (iterator.hasNext()){
                Map.Entry<TopicPartition, Long> topicPartitionLongEntry = iterator.next();
                TopicPartition key = topicPartitionLongEntry.getKey();
                Long offset = topicPartitionLongEntry.getValue();
                long seekOffset = offset - perPartitionMessages;
                if(seekOffset < 0){seekOffset = 0;}
                seekCount += (offset - seekOffset);     //计算总共需要抓取多少数据

                consumer.seek(key,seekOffset);
            }
            if(seekCount == 0){
                logger.warn("无数据可抓取");
                return datas;
            }

            ZkSerializer zkSerializer = zkSerializerMap.get(serialize);

            int currentFetchCount = 0;
            while (true) {
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(10));
                Iterator<ConsumerRecord<byte[], byte[]>> consumerRecordIterator = consumerRecords.iterator();
                while (consumerRecordIterator.hasNext()) {
                    ConsumerRecord<byte[], byte[]> consumerRecord = consumerRecordIterator.next();
                    byte[] value = consumerRecord.value();
                    Object deserialize = zkSerializer.deserialize(value);
                    PartitionKafkaData partitionKafkaData = new PartitionKafkaData(consumerRecord.offset(), deserialize, consumerRecord.timestamp(), consumerRecord.partition());
                    datas.add(partitionKafkaData);
                }
                currentFetchCount+= consumerRecords.count();
                if(currentFetchCount >= seekCount){
                    break;
                }
            }
        }finally {
            if(consumer != null)
                consumer.close();
        }
        Collections.sort(datas);
        return datas;
    }

    /**
     * 发送数据到 kafka , 这里只支持 json 数据
     * @param key
     * @param data
     * @return
     */
    public int sendJsonData(String clusterName, String topic, String key, String data) throws IOException, ExecutionException, InterruptedException {
        Properties properties = kafkaProperties(clusterName);
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer kafkaProducer = new KafkaProducer(properties);
        ProducerRecord producerRecord = new ProducerRecord<>(topic, key, data);
        Future send = kafkaProducer.send(producerRecord);
        send.get();     //阻塞，直到发送成功
        kafkaProducer.close();
        return 0;
    }

    /**
     * 数据监控
     * @param clusterName
     * @param topic
     */
    private static final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
    public Collection<MBeanInfo> brokerMonitor(String clusterName) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return monitor(clusterName,BrokerTopicMetrics.BrokerMetrics.class,null);
    }

    /**
     * topic 监控
     * @param clusterName
     * @param topic
     * @return
     */
    public Collection<MBeanInfo> topicMonitor(String clusterName, String topic) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException{
       return  monitor(clusterName,BrokerTopicMetrics.TopicMetrics.class,topic);
    }

    private Collection<MBeanInfo> monitor(String clusterName, Class clazz, String topic) throws IOException, MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException {
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

//    private Collection<DescribeLogDirsResponse.LogDirInfo> logDirsInfosxxx(String clusterName) throws IOException, InterruptedException, ExecutionException {
//        AdminClient adminClient = loadAdminClient(clusterName);
//
//        Map<String, String> brokers = brokers(readConfig(clusterName));
//        List<String> brokerIds = new ArrayList<>(brokers.keySet());
//        Integer firstBrokerId = NumberUtil.toInt(brokerIds.get(0));
//
//        DescribeLogDirsResult describeLogDirsResult = adminClient.describeLogDirs(Collections.singletonList(firstBrokerId));
//        Map<Integer, KafkaFuture<Map<String, DescribeLogDirsResponse.LogDirInfo>>> values = describeLogDirsResult.values();
//        Map<String, DescribeLogDirsResponse.LogDirInfo> stringLogDirInfoMap = values.get(firstBrokerId).get();
//        return stringLogDirInfoMap.values();
//    }

    static Pattern ipPort = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)");
    private List<BrokerInfo> brokers(KafkaConnInfo kafkaConnInfo) throws IOException {
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

    AdminClient loadAdminClient(String clusterName) throws IOException {
        AdminClient adminClient = adminClientMap.get(clusterName);
        if(adminClient == null){
            Properties properties = kafkaProperties(clusterName);

            adminClient =  KafkaAdminClient.create(properties);
            adminClientMap.put(clusterName,adminClient);
        }

        return adminClient;
    }

    private Properties kafkaProperties(String clusterName) throws IOException {
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
}