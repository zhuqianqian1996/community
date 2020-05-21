package com.nowcoder.community.controller;

import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.Event;
import com.nowcoder.community.model.Page;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.FollowerService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowerService followerService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;
    //关注
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
         User user = hostHolder.getUser();
         followerService.follow(user.getId(),entityType,entityId);
         //触发关注事件
        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(user.getId());

        eventProducer.fireEvent(event);
         return CommunityUtil.getJOSNString(0,"已关注");
    }

    //取消关注
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followerService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJOSNString(0,"已取消关注");
    }

    //关注对象列表
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        //获取当前对象
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user", user);
        //分页
        page.setLimit(5);
        page.setPath("/followees" + userId);
        page.setRows((int)followerService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        //查询当前用户关注对象的列表
        List<Map<String, Object>> followees = followerService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followees != null){
            for (Map<String, Object> map : followees) {
                 User u = (User)map.get("user");
                 //判断当前用户是否关注了map中的用户
                 map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",followees);
        return "site/followee";
    }

    //粉丝列表
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user", user);
        //分页
        page.setLimit(5);
        page.setPath("/followers" + userId);
        page.setRows((int)followerService.findFollowerCount(ENTITY_TYPE_USER,userId));
        //查询当前用户关注对象的列表
         List<Map<String, Object>> followers = followerService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followers != null){
            for (Map<String, Object> map : followers) {
                User u = (User)map.get("user");
                //判断当前用户是否关注了map中的用户
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",followers);
        return "site/follower";
    }


    //判断当前用户是否关注某一个用户
    private  boolean hasFollowed(int userId){
        User user = hostHolder.getUser();
        if (user == null){
            return false;
        }
        return followerService.hasFollowed(user.getId(), ENTITY_TYPE_USER, userId);
    }
}
