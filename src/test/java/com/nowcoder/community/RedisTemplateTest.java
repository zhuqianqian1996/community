package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTemplateTest{

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
         String redisKey  = "test:count";

         redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
         String redisKey = "test:user";

         redisTemplate.opsForHash().put(redisKey,"id",1);
         redisTemplate.opsForHash().put(redisKey,"username","zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testLists(){
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        //输出list的长度
        System.out.println(redisTemplate.opsForList().size(redisKey));
        //获取第index位置的数据
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        //获取范围内的数据
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSet(){
        String redisKey = "test:hero";
        redisTemplate.opsForSet().add(redisKey,"张飞","赵云","马超");

        //查询某一个集合的长度
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        //弹出一个元素
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        //查看集合中的元素
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSet(){
        String redisKey = "test:student";

        //增加元素
        redisTemplate.opsForZSet().add(redisKey,"唐僧",8);
        redisTemplate.opsForZSet().add(redisKey,"悟空",9);
        redisTemplate.opsForZSet().add(redisKey,"八戒",7);
        redisTemplate.opsForZSet().add(redisKey,"龙王",5);
        redisTemplate.opsForZSet().add(redisKey,"沙僧",6);

        //统计数量
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        //获取某一个value的分数
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"八戒"));
        //获取某一个对象的排名
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"八戒"));
    }

    @Test
    public void testKey(){
        //删除某一个key
        redisTemplate.delete("test:student");
        //判断是否存在这个key
        System.out.println(redisTemplate.hasKey("test:student"));
        //设置过期时间
        redisTemplate.expire("test:hero",10, TimeUnit.SECONDS);
    }

    //多次访问同一个key
    @Test
    public void testBoundOperations(){
        String redisKey = "test:hero";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    //编程式事务
    @Test
    public void testTransactional(){

        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                //开启事务
                operations.multi();

                operations.opsForSet().add(redisKey,"张三");
                operations.opsForSet().add(redisKey,"李四");
                operations.opsForSet().add(redisKey,"王五");
                //未提交，无效查询
                System.out.println(operations.opsForSet().members(redisKey));
                //提交事务
                return operations.exec();
            }
        });

    }
}

