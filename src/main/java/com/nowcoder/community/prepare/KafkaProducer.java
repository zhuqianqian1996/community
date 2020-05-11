package com.nowcoder.community.prepare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 使用kafka的生产者发送消息
     * @param topic 消息的主题
     * @param content 消息的内容
     */
    public void sendMessage(String topic, String content){
           kafkaTemplate.send(topic,content);
     }
}
