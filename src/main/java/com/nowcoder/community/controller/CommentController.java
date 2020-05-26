package com.nowcoder.community.controller;

import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.Comment;
import com.nowcoder.community.model.DiscussPost;
import com.nowcoder.community.model.Event;
import com.nowcoder.community.model.Message;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.swing.plaf.DesktopIconUI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
       comment.setStatus(0);
       comment.setCreateTime(new Date());
       comment.setUserId(hostHolder.getUser().getId());
       commentService.addComment(comment);

       //触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getId())
                .setData("postId",discussPostId);

        //根据评论的类型获取事件作用实体的作者
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        //发送事件
        eventProducer.fireEvent(event);

        //触发发帖事件
        if (comment.getEntityType() == ENTITY_TYPE_POST){
             event = new Event().setUserId(comment.getUserId())
                                .setTopic(TOPIC_PUBLISH)
                                .setEntityId(discussPostId)
                                .setEntityType(ENTITY_TYPE_POST);
            eventProducer.fireEvent(event);
        }
        return "redirect:/discuss/detail/"+discussPostId;
    }
}
