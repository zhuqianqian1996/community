package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowerService {

     @Autowired
    private RedisTemplate redisTemplate;

     //关注
     public void follow(int userId,int entityType,int entityId){
         redisTemplate.execute(new SessionCallback() {
             @Override
             public Object execute(RedisOperations operations) throws DataAccessException {
                 //获取某一个用户关注实体的key
                String FollowerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                 //获取某一个对象粉丝的key
                 String FolloweeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                 //开启事务
                 operations.multi();
                 redisTemplate.opsForZSet().add(FollowerKey,userId,System.currentTimeMillis());
                 redisTemplate.opsForZSet().add(FolloweeKey,entityId,System.currentTimeMillis());
                 return operations.exec();
             }
         });
     }

    //取消关注
    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //获取某一个用户关注实体的key
                String FollowerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                //获取某一个对象粉丝的key
                String FolloweeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                //开启事务
                operations.multi();
                redisTemplate.opsForZSet().remove(FollowerKey,userId);
                redisTemplate.opsForZSet().remove(FolloweeKey,entityId);
                return operations.exec();
            }
        });
    }

    //查询某一个实体被关注的数量
    public long findFolloweeCount(int userId ,int entityType){
        String FolloweeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(FolloweeKey);
    }

    //统计一个实体关注的数量
    public long findFollowerCount(int entityType,int entityId){
         String FollowerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
         return redisTemplate.opsForZSet().zCard(FollowerKey);
    }

    //查询当前对象是否被用户关注
    public boolean hasFollowed(int userId ,int entityType,int entityId){
         //查询这个实体的分数，如果查不到说明该实体不存在
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }
}
