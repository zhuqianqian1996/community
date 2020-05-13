package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.controller.LoginController;
import com.nowcoder.community.model.Event;
import com.nowcoder.community.model.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import sun.security.x509.CertificatePolicyMap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT , TOPIC_FOLLOW , TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        //获取消息队列中的记录
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        //获取事件对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());
        //将事件的触发者，事件类型，事件id存到map中
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        //判断事件数据是否为空
        if (!event.getData().isEmpty()){
             //将entry的key和value存到 content中
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                 content.put(entry.getKey(),entry.getValue());
            }
        }
        //最后将content以json字符串的形式存储到message中
        message.setContent(JSONObject.toJSONString(content));
        //将初始化完毕的message存储到数据库中
        messageService.addMessage(message);
    }
}
