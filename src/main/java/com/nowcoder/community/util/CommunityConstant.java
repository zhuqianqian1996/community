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
}
