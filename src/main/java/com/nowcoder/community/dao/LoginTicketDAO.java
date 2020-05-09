package com.nowcoder.community.dao;

import com.nowcoder.community.model.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
@Deprecated
public interface LoginTicketDAO {

    String TABLE_NAME = " login_ticket ";
    String INSERT_FIELDS = "user_id ,ticket ,status ,expired";
    String SELECT_FIELDS = "id,"+INSERT_FIELDS;

    //增加一个LoginTicket
    @Insert({"insert into",TABLE_NAME,"(",INSERT_FIELDS,") values(#{userId},#{ticket},#{status},#{expired})"})
    int insertLoginTicket(LoginTicket loginTicket);

    //查询LoginTicket
    @Select({"select  ",SELECT_FIELDS,"from ",TABLE_NAME," where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticket);

    //凭证失效（更改状态）
    @Update({"update ",TABLE_NAME,"set status=#{status} where ticket=#{ticket}"})
    int updateStatus(String ticket,int status);

}
