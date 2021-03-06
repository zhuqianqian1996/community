package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("server.servlet.context-path")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    //找到首页
    @GetMapping(path = "/register")
    public String getRegisterPage(){
        return "site/register";
    }

    //注册用户
    @PostMapping(path = "/register")
    public String register(Model model , User user){
        Map<String, Object> map = userService.register(user);
        //注册成功
        if (map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/register";
        }
    }

    //登录页面
    @GetMapping(path = "/login")
    public String getLoginPage(){
        return "site/login";
    }

    //激活邮件 激活路径:http://localhost:8080/community/activation/101/code
    @GetMapping(path = "/activation/{userId}/{code}")
    public String activation(@PathVariable("userId") int userId,
                             @PathVariable("code") String code,
                             Model model){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号可以正常使用！");
            model.addAttribute("target","/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","激活失败，您的账号已经被激活！");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，无效的激活码！");
            model.addAttribute("target","/index");
        }
        return "site/operate-result";
    }
    
    //生成验证码的方法(/kaptcha)
    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //使用Redis优化，获取一个随机字符串作为验证码的拥有者
        String kaptchaOwner = CommunityUtil.generateUUID();
        //将该验证码的服务用户设置到Cookie中
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
          //获取验证码的key
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
          //将验证码存到Redis中
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);

        //响应给浏览器
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("生成验证码错误"+e.getMessage());
        }
    }

    //登录
    @PostMapping(path = "/login")
    public String login(String username, String password, String code, boolean rememberme,
                        HttpServletResponse response, Model model, @CookieValue("kaptchaOwner") String kaptchaOwner){
        //初始化验证码为空
        String kaptcha = null;
         //判断Cookie中是否有验证码的拥有者
        if (StringUtils.isNotBlank(kaptchaOwner)){
            //根据验证码的拥有者获取验证码在redis中的key
             String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
             kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }
        if (StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "site/login";
        }
        //检查账号密码
       int expiredSeconds =  rememberme?REMEMBER_EXPIRE_SECONDS:DEFAULT_EXPIRE_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",  map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    //退出
    @GetMapping(path = "/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
