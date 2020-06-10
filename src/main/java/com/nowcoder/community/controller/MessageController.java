package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.model.Message;
import com.nowcoder.community.model.Page;
import com.nowcoder.community.model.User;
import com.nowcoder.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

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
       page.setRows(messageService.findConversationCount(user.getId()));
       //获取会话列表
       List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
       List<Map<String,Object>> conversations = new ArrayList<>();
       if (conversationList != null){
           for (Message message : conversationList) {
               Map<String,Object> map = new HashMap<>();
               map.put("conversation",message);
               map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
               map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
               //当前用户是否是消息发送用户，是的就将目标用户设置为消息接收用户，否则目标用户就是发送用户
               int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
               User targetUser = userService.getUserById(targetId);
               map.put("target",targetUser);
               conversations.add(map);
           }
       }
       model.addAttribute("conversations",conversations);
       //获取所有的会话信息
       int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
       model.addAttribute("letterUnreadCount",letterUnreadCount);
       //获取通知的数量
       int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
       model.addAttribute("noticeUnreadCount",noticeUnreadCount);
       return "/site/letter";
   }

   //私信详情
   @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(Model model, Page page, @PathVariable("conversationId") String conversationId){
       //分页信息
       page.setLimit(5);
       page.setPath("/letter/detail/"+conversationId);
       page.setRows(messageService.findLetterCount(conversationId));

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

       //设置已读
       List<Integer> ids = getLettersIds(letterList);
       if (!ids.isEmpty()){
           messageService.readMessage(ids,1);
       }
       return "/site/letter-detail";
   }

   //获取私信的id
    private List<Integer> getLettersIds(List<Message> list){
        List<Integer> ids = new ArrayList<>();
        if (list != null)
            for (Message message : list) {
                //如果当前用户是接收用户并且该条私信未读
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        return ids;
    }

    //获取私信的作者
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

   //添加私信
   @PostMapping("/letter/send")
   @ResponseBody
    public String sendMessage(String toName,String content){
        User targetUser = userService.getUserByName(toName);
        if (targetUser == null){
            return CommunityUtil.getJOSNString(1,"目标用户不存在！");
        }

        //私信的信息
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(userService.getUserByName(toName).getId());
        if (message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" +message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJOSNString(0);
   }

   //显示系统通知
    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //查询评论类的通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String,Object> messageVO = new HashMap<>();
        if (message != null){
            messageVO.put("message",message);
            //对content中的内容解析成字符串
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将数据库中序列化的content转成对象
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            //将content中的字段存储到messageVO中
            messageVO.put("user",userService.getUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
            //显示通知的数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count",count);
            //显示未读的通知数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread",unread);

        }
        model.addAttribute("commentNotice",messageVO);

        //查询关注类型的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if (message != null){
            messageVO.put("message",message);
            //对content中的内容进行转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将数据库中序列化的content转成对象
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            //将content中的字段存储到messageVO中
            messageVO.put("user",userService.getUserById((int)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            //显示通知的数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count",count);
            //显示未读的通知数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread",unread);
        }
        model.addAttribute("followNotice",messageVO);

        //查询点赞类型的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if (message != null){
            messageVO.put("message",message);
            //对content中的内容进行转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将数据库中序列化的content转成对象
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            //将content中的字段存储到data对象中
            messageVO.put("user",userService.getUserById((int)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
            //显示通知的数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count",count);
            //显示未读的通知数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread",unread);
        }
        model.addAttribute("likeNotice",messageVO);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //查询所有通知的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "site/notice";
    }

   //查询某一个主题的通知详情
   @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic ,Page page,Model model){
       User user = hostHolder.getUser();
       //分页查询
       page.setLimit(5);
       page.setRows(messageService.findNoticeCount(user.getId(),topic));
       page.setPath("/notice/detail/"+topic);
       //查询当前用户的通知信息
       List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
       List<Map<String,Object>> noticeVOList = new ArrayList<>();
       Map<String,Object> noticeVO =  new HashMap<>();
       if (notices != null){
           for (Message notice : notices) {
               noticeVO.put("notice",notice);
               //获取内容
               String content = HtmlUtils.htmlUnescape(notice.getContent());
               //反序列化获取对象
               Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
               //将对象中的重要信息存储到VO中
               noticeVO.put("user",userService.getUserById((Integer) data.get("userId")));
               noticeVO.put("entityId",data.get("entityId"));
               noticeVO.put("entityType",data.get("entityType"));
               noticeVO.put("postId",data.get("postId"));
               //通知的作者
               noticeVO.put("fromUser",userService.getUserById(notice.getFromId()));
               noticeVOList.add(noticeVO);
           }
       }
       model.addAttribute("notices",noticeVOList);
       //设置已读
       List<Integer> noticeIds = getLettersIds(notices);
       if (!noticeIds.isEmpty()){
           messageService.readMessage(noticeIds,1);
       }
       return "site/notice-detail";
   }

}
