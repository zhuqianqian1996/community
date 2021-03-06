package com.nowcoder.community.controller;

import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.*;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer producer;


    //新增帖子
    @PostMapping(path = "/add")
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

         //发布帖子触发事件
        Event event = new Event().setUserId(user.getId())
                                 .setTopic(TOPIC_PUBLISH)
                                 .setEntityId(post.getId())
                                 .setEntityType(ENTITY_TYPE_POST);
        //发送事件
        producer.fireEvent(event);
         //报错情况，由统一异常处理逻辑处理
         return CommunityUtil.getJOSNString(0,"发布成功!");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
         DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
         User user = userService.getUserById(post.getUserId());
         model.addAttribute("post",post);
         model.addAttribute("user",user);
         //点赞的数量
         long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
         model.addAttribute("likeCount",likeCount);
        //点赞的状态
         int likeStatus = hostHolder == null ? 0 :likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_POST, post.getId());
         model.addAttribute("likeStatus",likeStatus);
        //分页信息
         page.setLimit(5);
         page.setPath("/discuss/detail/"+discussPostId);
         page.setRows(post.getCommentCount());

         //评论：帖子的评论  回复：评论的回复
         //评论列表
         //评论
        List<Comment> comments = commentService.findCommentsByEntity(post.getId(), ENTITY_TYPE_POST, page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (comments != null){
            for (Comment comment : comments) {
                // 评论VO
                Map<String,Object> commentVo = new HashMap<>();
                commentVo.put("user",userService.getUserById(post.getUserId()));
                commentVo.put("comment",comment);
                //点赞的数量
                 likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                 commentVo.put("likeCount",likeCount);
                //点赞的状态
                 likeStatus = hostHolder == null ? 0 :likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                 commentVo.put("likeStatus",likeStatus);
                //回复
               List<Comment> replys = commentService.findCommentsByEntity(comment.getId(), ENTITY_TYPE_COMMENT, 0, Integer.MAX_VALUE);
               //回复VO列表
               List<Map<String,Object>> replyList = new ArrayList<>();
               if (replys != null){
                   for (Comment reply : replys) {
                       Map<String,Object> replyVo = new HashMap<>();
                       //回复
                       replyVo.put("reply",reply);
                       //用户
                       replyVo.put("user",userService.getUserById(reply.getUserId()));
                       //回复目标用户
                       User targetUser = reply.getTargetId() == 0 ? null : userService.getUserById(reply.getTargetId());
                       replyVo.put("target",targetUser);
                       //点赞的数量
                       likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                       replyVo.put("likeCount",likeCount);
                       //点赞的状态
                       likeStatus = hostHolder == null ? 0 :likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, reply.getId());
                       replyVo.put("likeStatus",likeStatus);
                       replyList.add(replyVo);
                   }
               }
               commentVo.put("replys",replyList);

               //回复的数量
                int replyCount = commentService.findCommentCount(comment.getId(),ENTITY_TYPE_COMMENT);
                commentVo.put("replyCount",replyCount);

               commentVoList.add(commentVo);
            }
           model.addAttribute("comments",commentVoList);
        }
        return "site/discuss-detail";
    }
}


