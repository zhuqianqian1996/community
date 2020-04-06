package com.nowcoder.community.dao;

import com.nowcoder.community.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDAO {
    String TABLE_NAME = " user ";
    String INSERT_FIELDS = "username, password, salt, email, type, status, activation_code, header_url, create_time";
    String SELECT_FIELDS = "id,"+INSERT_FIELDS;

    //根据id查询user
    @Select({"select ",SELECT_FIELDS,"from ",TABLE_NAME," where id=#{id}"})
    User selectUserById(int id);

    //根据用户名查询user
    @Select({"select ",SELECT_FIELDS,"from ",TABLE_NAME,"where username=#{username}"})
    User selectUserByName(String username);

    //根据邮箱查询user
    @Select({"select ",SELECT_FIELDS,"from ",TABLE_NAME,"where email=#{email}"})
    User selectUserByEmail(String email);

    //增加一个用户
    @Insert({"insert into",TABLE_NAME,"(",INSERT_FIELDS,") " +
            "values(#{id},#{username},#{password},#{salt},#{email},#{type},#{status},#{activationCode},#{headerUrl},#{createTime})"})
    int addUser(User user);

    //修改的状态
    @Update({"update ",TABLE_NAME,"set status=#{status} where id=#{id}"})
    int UpdateStatus(int id,int status);

    //更新头像
    @Update({"update ",TABLE_NAME,"set header_url=#{headerUrl} where id=#{id}"})
    int UpdateHeaderUrl(int id,String headerUrl);

    //更新密码
    @Update({"update ",TABLE_NAME,"set password=#{password} where id=#{id}"})
    int UpdatePassword(int id,String password);
}
