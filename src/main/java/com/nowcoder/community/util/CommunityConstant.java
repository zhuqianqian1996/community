package com.nowcoder.community.util;

public interface CommunityConstant {

    //激活成功
    int ACTIVATION_SUCCESS = 0;

    //重复激活
    int ACTIVATION_REPEAT = 1;

    //激活失败
    int ACTIVATION_FAILURE = 2;

    //默认状态下的激活时间
    int DEFAULT_EXPIRE_SECONDS = 360000 * 120;

    //记住状态下的激活时间
    int REMEMBER_EXPIRE_SECONDS = 36000 * 12 * 1000;

    //实体类型：帖子
    int ENTITY_TYPE_POST = 1;

    //实体类型：评论
    int ENTITY_TYPE_COMMENT = 2;

    //实体类型：用户
    int ENTITY_TYPE_USER = 3;

    //主题类型：评论
    String TOPIC_COMMENT = "comment";

    //主题类型：点赞
    String TOPIC_LIKE = "like";

    //主题类型：关注
    String TOPIC_FOLLOW = "follow";

    //主题类型：发布帖子
    String TOPIC_PUBLISH = "PUBLISH";

    //系统用户Id
    int SYSTEM_USER_ID = 1;
}
