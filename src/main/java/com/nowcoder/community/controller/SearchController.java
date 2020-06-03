package com.nowcoder.community.controller;

import com.nowcoder.community.model.DiscussPost;
import com.nowcoder.community.model.Page;
import com.nowcoder.community.service.ElasticsearchService;
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
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public String search(String keyword, Page page, Model model){
        //从ES服务器中查询关键字的帖子
        org.springframework.data.domain.Page<DiscussPost>
                discussPostResoult = elasticsearchService.SearchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        //设置一个VO列表
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (discussPostResoult != null) {
            for (DiscussPost post : discussPostResoult) {
                Map<String,Object> map = new HashMap<>();
                //将帖子、帖子作者、赞的数量封装到map中
                map.put("post",post);
                map.put("user",userService.getUserById(post.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);
        //分页
        page.setPath("/search?keyword=" + keyword);
        page.setRows(discussPostResoult == null ? 0 : discussPostResoult.getNumberOfElements());
        //页面跳转
        return "site/search";
    }

}
















