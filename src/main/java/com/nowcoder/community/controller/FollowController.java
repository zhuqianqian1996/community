package com.nowcoder.community.controller;

import com.nowcoder.community.model.User;
import com.nowcoder.community.service.FollowerService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController {

    @Autowired
    private FollowerService followerService;

    @Autowired
    private HostHolder hostHolder;

    //关注
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
         User user = hostHolder.getUser();
         followerService.follow(user.getId(),entityType,entityId);
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
}
