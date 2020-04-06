package com.nowcoder.community.dao;

import com.nowcoder.community.model.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostDAO {

    //分页查询帖子的数量
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    //查询帖子的行数(方便分页)
    int selectDiscussPostRows(@Param("userId") int userId);


}
