package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostDAO;
import com.nowcoder.community.dao.LoginTicketDAO;
import com.nowcoder.community.dao.UserDAO;
import com.nowcoder.community.model.DiscussPost;
import com.nowcoder.community.model.LoginTicket;
import com.nowcoder.community.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Resource
    UserDAO userDAO;

    @Resource
    DiscussPostDAO discussPostDAO;

    @Resource
    LoginTicketDAO loginTicketDAO;

    @Test
    public void selectUserById(){
        //测试根据id查数据
        User user = userDAO.selectUserById(1);
        System.out.println(user);
        //测试根据Name查数据
        user = userDAO.selectUserByName("liubei");
        System.out.println(user);
        //测试根据email查数据
        user = userDAO.selectUserByEmail("nowcoder11@sina.com");
        System.out.println(user);
    }

    @Test
    public void updateTest(){
        int update = userDAO.UpdateStatus(150, 3);
        System.out.println(update);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> discussPosts = discussPostDAO.selectDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
        int rows = discussPostDAO.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60));
        loginTicketDAO.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectByTicket(){
        LoginTicket loginTicket = loginTicketDAO.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketDAO.updateStatus("abc", 1);
        loginTicket = loginTicketDAO.selectByTicket("abc");
        System.out.println(loginTicket);

    }

}
