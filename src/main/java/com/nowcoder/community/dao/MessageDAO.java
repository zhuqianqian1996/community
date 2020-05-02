package com.nowcoder.community.dao;

import com.nowcoder.community.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageDAO {

   //查询当前用户的会话列表，针对每个会话只返回一条最新的私信（分页）
    List<Message> selectConversations( int userId,int offset,int limit);

   //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表（分页）
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个对话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读的私信的数量
    int selectLetterUnreadCount( int userId, String conversationId);

    //新增私信
    int insertMessage(Message message);

    //修改私信的状态
    int updateMessageStatus(List<Integer> ids,int status);

}