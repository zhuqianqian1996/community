package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserDAO;
import com.nowcoder.community.model.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService {
    @Resource
    UserDAO userDAO;

    public User getUserById(int id){
        return userDAO.selectUserById(id);
    }
}


