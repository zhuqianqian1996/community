package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageDAO;
import com.nowcoder.community.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageDAO messageDAO;

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
}
