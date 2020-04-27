package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentDao;
import com.nowcoder.community.model.Comment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentService {

    @Resource
    private CommentDao commentDao;

    public List<Comment> findCommentsByEntity(int entityId,int entityType,int offset,int limit){
        return commentDao.selectCommentsByEntity(entityId,entityType,offset,limit);
    }

    public int findCommentCount(int entityId,int entityType){
        return commentDao.selectCommentCount(entityId,entityType);
    }
}
