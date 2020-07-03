package com.sanri.app.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.app.BaseServlet;
import com.sanri.app.dtos.kafka.*;
import com.sanri.app.dtos.kafka.MBeanInfo;
import com.sanri.app.kafka.ClientMessage;
import com.sanri.app.kafka.KafkaService;
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
    public KafkaService kafkaService;

    //构造注入,目前仅支持注入 servlet
    public KafkaServlet(ZkServlet zkServlet,FileManagerServlet fileManagerServlet){
        kafkaService = new KafkaService(zkServlet,fileManagerServlet);
    }

    /**
     * 保存配置,新连接
     * @param zkConn
     * @param kafkaConnInfo
     * @throws IOException
     */
    public int writeConfig(String zkConn, KafkaConnInfo kafkaConnInfo) throws IOException {
        kafkaService.writeConfig(zkConn,kafkaConnInfo);
        return 0;
    }

    /**
     * 读取 kafka 连接配置
     * @param clusterName
     * @return
     * @throws IOException
     */
    public KafkaConnInfo readConfig(String clusterName) throws IOException {
       return kafkaService.readConfig(clusterName);
    }

    /**
     * 创建主题
     * @param clusterName
     * @param topic
     * @param partitions
     * @param replication
     * @return
     */
    public int createTopic(String clusterName,String topic,int partitions,int replication) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
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
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
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
       return kafkaService.brokers(clusterName);
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
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
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
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
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

        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);

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
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
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
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
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
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);

        //创建 KafkaConsumer 来获取 offset ,lag,logsize
        Properties properties = kafkaService.kafkaProperties(clusterName);
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
        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
        Properties properties = kafkaService.kafkaProperties(clusterName);
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

        Properties properties = kafkaService.kafkaProperties(clusterName);
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
        Properties properties = kafkaService.kafkaProperties(clusterName);
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
        Properties properties = kafkaService.kafkaProperties(clusterName);
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
        Properties properties = kafkaService.kafkaProperties(clusterName);
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
        Properties properties = kafkaService.kafkaProperties(clusterName);
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
    public Collection<MBeanInfo> brokerMonitor(String clusterName) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return kafkaService.monitor(clusterName,BrokerTopicMetrics.BrokerMetrics.class,null);
    }

    /**
     * topic 监控
     * @param clusterName
     * @param topic
     * @return
     */
    public Collection<MBeanInfo> topicMonitor(String clusterName, String topic) throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException{
       return  kafkaService.monitor(clusterName,BrokerTopicMetrics.TopicMetrics.class,topic);
    }

//    private Collection<DescribeLogDirsResponse.LogDirInfo> logDirsInfosxxx(String clusterName) throws IOException, InterruptedException, ExecutionException {
//        AdminClient adminClient = kafkaService.loadAdminClient(clusterName);
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

}