package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageDAO;
import com.nowcoder.community.model.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageDAO messageDAO;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> getConversations(int userId,int offset,int limit){
        return messageDAO.selectConversations(userId,offset,limit);
    }

    public int getConversationCount(int userId){
        return messageDAO.selectConversationCount(userId);
    }

    public List<Message> getLetters(String conversationId,int offset,int limit){
        return messageDAO.selectLetters(conversationId,offset,limit);
    }

    public int getLetterCount(String conversationId){
        return messageDAO.selectLetterCount(conversationId);
    }

    public int getLetterUnreadCount(int userId , String conversationId){
        return messageDAO.selectLetterUnreadCount(userId,conversationId);
    }

    public int addMessage(Message message){
        message.setContent(sensitiveFilter.filter(message.getContent()));
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        return messageDAO.insertMessage(message);
    }

    public int readMessage(List<Integer> ids , int status){
        return messageDAO.updateMessageStatus(ids,status);
    }
}
