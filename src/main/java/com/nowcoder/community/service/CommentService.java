package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentDAO;
import com.nowcoder.community.model.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Resource
    private CommentDAO commentDao;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityId,int entityType,int offset,int limit){
        return commentDao.selectCommentsByEntity(entityId,entityType,offset,limit);
    }

    public int findCommentCount(int entityId,int entityType){
        return commentDao.getCommentCount(entityId,entityType);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
      if (comment == null){
          throw new IllegalArgumentException("评论不能为空！");
      }
      comment.setContent(sensitiveFilter.filter(comment.getContent()));
      comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
      int rows = commentDao.insertComment(comment);

        //更新帖子评论的数量
        if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            int count = commentDao.getCommentCount(comment.getEntityId(),comment.getEntityType());
            discussPostService.updateCommentCount(comment.getId(), count);
        }
        return rows;
    }
    
    //根据id查询评论
    public Comment findCommentById(int id){
        return commentDao.selectCommentById(id);
    }
}
