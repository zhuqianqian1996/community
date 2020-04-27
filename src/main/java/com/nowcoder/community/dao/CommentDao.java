package com.nowcoder.community.dao;

import com.nowcoder.community.model.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentDao {

    //根据实体查询所有的评论分页展示
    List<Comment> selectCommentsByEntity(int entityId,int entityType,int offset,int limit);

    //查询评论的数量
    int selectCommentCount(int entityId,int entityType);
}
