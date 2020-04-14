package com.nowcoder.community.util;

import com.nowcoder.community.model.User;
import org.springframework.stereotype.Component;

/**
 *持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {
        //为每一条线程找到当前线程关联的变量类似于Map<ThreadID,User>，只是一个工具
        private  ThreadLocal<User> users = new ThreadLocal<>();

        public User getUser(){
            return users.get();
        }

        public void setUser(User user){
            users.set(user);
        }

        public void clear(){
            users.remove();
        }
}


