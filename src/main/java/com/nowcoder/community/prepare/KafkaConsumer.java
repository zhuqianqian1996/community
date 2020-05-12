package com.nowcoder.community.prepare;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    /**
     * 指定监听的是哪个topic的事件
     * @param record ：消息被封装成 ConsumerRecord
     */
    @KafkaListener(topics = {"test1"})
    public void handleEvent(ConsumerRecord record) {
        System.out.println(record.value());
    }
}