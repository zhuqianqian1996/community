package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_ENTITY_USER = "like:user";
    private static final String PREFIX_ENTITY_FOLLOWEE = "followee:userId";
    private static final String PREFIX_ENTITY_FOLLOWER = "follower:entityType";


    // 某个实体的赞
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    public static String getUserLikeKey(int userId){
        return PREFIX_ENTITY_USER + SPLIT + userId;
    }

    //某个实体拥有的粉丝
    public static String getFolloweeKey(int userId ,int entityType){
        return PREFIX_ENTITY_FOLLOWER + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体被关注
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_ENTITY_FOLLOWEE + SPLIT + entityType + SPLIT + entityId;
    }
}
