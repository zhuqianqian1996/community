package com.nowcoder.community.prepare;

import com.nowcoder.community.dao.UserDAO;
import com.nowcoder.community.model.User;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

//@Service
public class AlphaService {

    @Resource
    private AlphaDao alphaDao;

    @Resource
    private UserDAO userDAO;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destory(){
        System.out.println("销毁AlphaService");
    }

    @Autowired
    private TransactionTemplate transactionTemplate;

    public String find(){
        return alphaDao.select();
    }

    /**
     * Spring 对事务的支持
     * Isolation:隔离级别。
     * propagation:事务传播机制（解决多个事务交叉传播的问题）：
      REQUIRED:支持当前事务（外部事务，调用者的事务），如果不存在就创建新事务。
      REQUIRES_NEW：创建一个新的事务，并且暂停当前事务（外部事务）。
      NESTED:如果当前存在事务（外部事务），则嵌套在该事物中执行（独立提交或回滚），否则就会和REQUIRED一样。
     */
    @Transactional(isolation = Isolation.READ_COMMITTED ,propagation = Propagation.REQUIRED)
    public void demo(){
        //在这里实现需要进行事务管理的逻辑
    }

    public Object demo2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //在这里实现需要进行事务管理的逻辑
                return "OK";
            }
        });
    }
}
