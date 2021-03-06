package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketDAO;
import com.nowcoder.community.dao.UserDAO;
import com.nowcoder.community.model.LoginTicket;
import com.nowcoder.community.model.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Resource
    private UserDAO userDAO;

    @Resource
    private LoginTicketDAO loginTicketDAO;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("server.servlet.context-path")
    private String contextPath;

    @Value("community.path.domain")
    private String domain;

    @Autowired
    private RedisTemplate redisTemplate;

    //根据id在缓存中查找User
    public User getUserById(int id){
         User user = getCache(id);
         if (user == null){
             user = initCache(id);
         }
         return user;
    }

    //注册
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值判断
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //username 空值判断
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空!");
            return map;
        }
        //password 空值判断
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
       //email 空值判断
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
       //验证账号是否已经被注册
        User u = userDAO.selectUserByName(user.getUsername());
        if (u != null){
           map.put("usernameMsg","该账号已存在!");
           return map;
        }
      //验证邮箱是否已经被注册
        u = userDAO.selectUserByEmail(user.getEmail());
       if (u != null){
           map.put("emailMsg","该邮箱已注册!");
       }
       else {

           //注册用户（对密码加密）
           user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
           user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
           user.setType(0);//普通用户
           user.setStatus(0);
           user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
           user.setActivationCode(CommunityUtil.generateUUID());
           user.setCreateTime(new Date());
           userDAO.addUser(user);

           //给用户发送激活邮件
           Context context = new Context();
           context.setVariable("email", user.getEmail());
           //激活路径:http://localhost:8080/community/activation/101/code
           String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
           context.setVariable("url", url);
           String content = templateEngine.process("/mail/activation", context);
           mailClient.sendMessage(user.getEmail(), "激活邮件", content);
          }
           return map;
    }

    //激活
    public int activation(int userId,String code){
        User user = userDAO.selectUserById(userId);
        if (user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userDAO.UpdateStatus(user.getId(),1);
            //修改状态码以后原有的User状态发生变化，需要清除缓存中的User
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    //登录
   public Map<String, Object> login(String username,String password,int expiredSeconds){
       Map<String, Object> map = new HashMap<>();
       //空值处理
       if (StringUtils.isBlank(username)){
           map.put("usernameMsg","账号不能为空!!");
           return map;
       }
       if (StringUtils.isBlank(password)){
           map.put("passwordMsg","密码不能为空!!");
           return map;
       }
       //验证账号
       User user = userDAO.selectUserByName(username);
       if (user==null){
           map.put("usernameMsg","该账号不存在!!");
           return map;
       }
       //验证状态
       if (user.getStatus()==1){
           map.put("usernameMsg","该账号未激活!!");
           return map;
       }
       //验证密码
       password = CommunityUtil.md5(password + user.getSalt());
       if (!user.getPassword().equals(password)){
           map.put("passwordMsg","密码不正确!!");
           return map;
       }
       //生成登录凭证（return ticket）:初始化LoginTicket
       LoginTicket loginTicket = new LoginTicket();
       loginTicket.setUserId(user.getId());
       //将登录凭证置为有效状态
       loginTicket.setStatus(0);
       loginTicket.setTicket(CommunityUtil.generateUUID());
       loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds+1000));

        //将loginTicket存入Redis中,loginTicket在logout方法中取出
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
   }

   //退出
   public void logout(String ticket){
       //从Redis中取出元素，修改状态再存进Redis
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        //退出登录以后将登录凭证置为无效状态
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
   }

   //查询凭证
    public LoginTicket findLoginTicket(String ticket){
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    //上传头像:上传头像以后就改变了User的状态，所以需要将缓存中的User删除
    public int uploadHeaderUrl(int userId,String headerUrl){
         int rows = userDAO.UpdateHeaderUrl(userId, headerUrl);
         clearCache(userId);
         return rows;
    }

   //根据用户名查询用户
    public User getUserByName(String name){
        return userDAO.selectUserByName(name);
    }

    //1.优先从缓存中取值
    public User getCache(int userId){
         String userKey = RedisKeyUtil.getUserKey(userId);
         return (User)redisTemplate.opsForValue().get(userKey);
    }

    //2.取不到时初始化缓存数据
    public User initCache(int userId){
         User user = userDAO.selectUserById(userId);
         String userKey = RedisKeyUtil.getUserKey(userId);
         redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.MILLISECONDS);
         return user;
    }

    //3.数据变更时，清除缓存数据
    public void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

}


