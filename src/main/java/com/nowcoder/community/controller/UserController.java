package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.FollowerService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowerService followerService;

    @LoginRequired
    @GetMapping(path = "/setting")
    public String getSetting(){
        return "site/setting";
    }

    //上传图片
    @LoginRequired
    @PostMapping(path = "/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        //判断图片是否为空
        if (headerImage==null){
            model.addAttribute("error","您还没有选择图片！");
            return "site/setting";
        }
        //判断图片的后缀名是否有误
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式错误");
            return "site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID()+suffix;
        //确定文件的存放路径
        File dest  = new File(uploadPath+"/"+fileName);
        //存储文件
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败，服务器发生异常"+e.getMessage());
        }
        //更新当前用户的头像的路径(web访问路径)

        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        userService.uploadHeaderUrl(user.getId(),headerUrl);
        return "redirect:/index";
    }

    //获取头像
    @GetMapping(path = "/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try (FileInputStream fis = new FileInputStream(fileName)){
             OutputStream os = response.getOutputStream();
             byte[] buffer = new byte[10240];
             int b = 0;
             while ((b = fis.read(buffer))!=-1){
                 os.write(buffer,0,b);
             }
        }catch (IOException e){
            logger.error("获取头像失败"+e.getMessage());
        }

    }

    //个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
         User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        //用户信息
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        //被点赞数
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followerCount = followerService.findFollowerCount(ENTITY_TYPE_USER, user.getId());
        model.addAttribute("followerCount",followerCount);
        //对象粉丝的数量
        long followeeCount = followerService.findFolloweeCount(user.getId(), ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //当前用户对这个用户是否已经关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followerService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "site/profile";
    }
}
