package com.nowcoder.community.service;

import com.nowcoder.community.model.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowerService implements CommunityConstant {

     @Autowired
    private RedisTemplate redisTemplate;

     @Autowired
     private UserService userService;

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

    //统计一个实体粉丝的数量
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

    // 查询某用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.getUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.getUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
