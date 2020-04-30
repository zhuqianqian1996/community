package com.nowcoder.community.controller;

import com.nowcoder.community.model.Message;
import com.nowcoder.community.model.Page;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.jws.WebParam;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

   @Autowired
   private MessageService messageService;

   @Autowired
   private HostHolder hostHolder;

   @Autowired
    private  UserService userService;

   //私信列表
   @GetMapping("/letter/list")
    public String getLetterList(Model model , Page page){
       //获取当前用户
        User user = hostHolder.getUser();
        //分页信息
       page.setLimit(5);
       page.setPath("/letter/list");
       page.setRows(messageService.getConversationCount(user.getId()));
       //获取会话列表
       List<Message> conversationList = messageService.getConversations(user.getId(), page.getOffset(), page.getLimit());
       List<Map<String,Object>> conversations = new ArrayList<>();
       if (conversationList != null){
           for (Message message : conversationList) {
               Map<String,Object> map = new HashMap<>();
               map.put("conversation",message);
               map.put("letterCount",messageService.getLetterCount(message.getConversationId()));
               map.put("unreadCount",messageService.getLetterUnreadCount(user.getId(),message.getConversationId()));
               //当前用户是否是消息发送用户，是的就将目标用户设置为消息接收用户，否则目标用户就是发送用户
               int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
               User targetUser = userService.getUserById(targetId);
               map.put("target",targetUser);
               conversations.add(map);
           }
       }
       model.addAttribute("conversations",conversations);
       //获取所有的会话信息
       int letterUnreadCount = messageService.getLetterUnreadCount(user.getId(),null);
       model.addAttribute("letterUnreadCount",letterUnreadCount);
       return "/site/letter";
   }

   //私信详情
   @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(Model model, Page page, @PathVariable("conversationId") String conversationId){
       //分页信息
       page.setLimit(5);
       page.setPath("/letter/detail/"+conversationId);
       page.setRows(messageService.getLetterCount(conversationId));

       //私信列表
       List<Message> letterList = messageService.getLetters(conversationId, page.getOffset(), page.getLimit());
       List<Map<String,Object>> letters = new ArrayList<>();
       if (letterList != null){
           for (Message letter : letterList) {
               Map<String,Object> map = new HashMap<>();
               map.put("letter",letter);
               map.put("fromUser",userService.getUserById(letter.getFromId()));
               letters.add(map);
           }
       }
       model.addAttribute("letters",letters);
       //私信目标
       model.addAttribute("target",getTargetUser(conversationId));
       return "/site/letter-detail";
   }

   private User getTargetUser(String conversationId){
       String[] ids = conversationId.split("_");
       int first_id = Integer.parseInt(ids[0]);
       int last_id = Integer.parseInt(ids[1]);
       //当前用户在前，则发送用户在后
       if (hostHolder.getUser().getId() == first_id){
        return userService.getUserById(last_id);
       }else {
           return userService.getUserById(first_id);
       }
   }
}
