package com.nowcoder.community.controller;

import com.nowcoder.community.model.DiscussPost;
import com.nowcoder.community.model.Page;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //展示首页信息
    @GetMapping(path = "/index")
    public String getIndexPage(Model model, Page page){
        //方法调用之前，springMVC会自动实例化Model和Page，并将Page注入Model
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> discussPostList = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        //如果查到的帖子不为空，则通过map将帖子的信息集中到帖子列表中最后将帖子列表返回给前端页面
        if(discussPostList != null) {
            for (DiscussPost post : discussPostList) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        //将帖子列表和分页信息传递给前端页面
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("page",page);
        return "index";
    }

    //得到500的页面
    @GetMapping("/error")
    public String getErrorPage(){
        return "error/500";
    }
}
