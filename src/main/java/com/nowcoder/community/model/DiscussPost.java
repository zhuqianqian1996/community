package com.nowcoder.community.model;

import lombok.Data;

import java.util.Date;

@Data
public class DiscussPost {
   private int id;
   private int userId;
   private String title;
   private String content;
   private int type; //0-普通; 1-置顶;
   private int status; //0-正常; 1-精华; 2-拉黑;
   private Date createTime;
   private int commentCount;
   private double score;
}
