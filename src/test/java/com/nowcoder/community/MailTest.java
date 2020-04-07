package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommunityApplication.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testMail(){
        mailClient.sendMessage("2305608098@qq.com","你好请回信","千千你好帅啊");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","千千");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMessage("javazhu1995@sina.com","欢迎你",content);
        System.out.println(content);
    }

}
