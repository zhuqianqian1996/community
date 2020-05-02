package com.nowcoder.community.controller;

import com.nowcoder.community.model.DiscussPost;
import com.nowcoder.community.model.Page;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @GetMapping(path = "/index")
    public String getIndexPage(Model model, Page page){
        //方法调用之前，springMVC会自动实例化Model和Page，并将Page注入Model
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        for (DiscussPost post : list) {
            Map<String,Object> map = new HashMap<>();
            map.put("post",post);
            User user = userService.getUserById(post.getUserId());
            map.put("user",user);
            discussPosts.add(map);
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("page",page);
        return "index";
    }
}
