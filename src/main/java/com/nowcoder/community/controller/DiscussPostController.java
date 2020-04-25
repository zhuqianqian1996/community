package com.nowcoder.community.controller;

import com.nowcoder.community.model.DiscussPost;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //新增帖子
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
         User user = hostHolder.getUser();
         if (user == null){
             return CommunityUtil.getJOSNString(403,"你还没有登录！");
         }
         DiscussPost post = new DiscussPost();
         post.setTitle(title);
         post.setContent(content);
         post.setCreateTime(new Date());
         post.setUserId(user.getId());
         discussPostService.addDiscussPost(post);

         //报错情况，由统一异常处理逻辑处理
         return CommunityUtil.getJOSNString(0,"发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model){
         DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
         User user = userService.getUserById(post.getUserId());
         model.addAttribute("post",post);
         model.addAttribute("user",user);
         return "site/discuss-detail";
    }

}


