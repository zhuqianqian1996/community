package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostDAO;
import com.nowcoder.community.model.DiscussPost;
import jdk.internal.dynalink.linker.LinkerServices;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DiscussPostService {

   @Resource
   private DiscussPostDAO discussPostDAO;

   public List<DiscussPost> getDiscussPosts(int userId,int offset,int limit){
       return discussPostDAO.selectDiscussPosts(userId,offset,limit);
   }

   public int findDiscussPostRows(int userId){
       return discussPostDAO.selectDiscussPostRows(userId);
   }
}
