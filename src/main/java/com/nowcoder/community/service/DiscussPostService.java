package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentDAO;
import com.nowcoder.community.dao.DiscussPostDAO;
import com.nowcoder.community.model.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import jdk.internal.dynalink.linker.LinkerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.swing.text.html.HTML;
import java.util.List;

@Service
public class DiscussPostService {

   @Resource
   private DiscussPostDAO discussPostDAO;

   @Autowired
   private SensitiveFilter sensitiveFilter;

   public List<DiscussPost> getDiscussPosts(int userId,int offset,int limit){
       return discussPostDAO.selectDiscussPosts(userId,offset,limit);
   }

   public int findDiscussPostRows(int userId){
       return discussPostDAO.selectDiscussPostRows(userId);
   }

   public int addDiscussPost(DiscussPost post){
       if (post == null){
           throw new IllegalArgumentException("参数不能为空");
       }
       //过滤HTML标签
       post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
       post.setContent(HtmlUtils.htmlEscape(post.getContent()));

       //过滤敏感词
       post.setTitle(sensitiveFilter.filter(post.getTitle()));
       post.setContent(sensitiveFilter.filter(post.getContent()));

       return discussPostDAO.addDiscussPost(post);
   }


   public DiscussPost findDiscussPostById(int id){
       return discussPostDAO.selectDiscussPostById(id);
   }

   public int updateCommentCount(int id,int count){
       return discussPostDAO.updateCommentCount(id,count);
   }
}
