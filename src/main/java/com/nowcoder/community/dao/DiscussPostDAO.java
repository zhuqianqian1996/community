package com.nowcoder.community.dao;

import com.nowcoder.community.model.DiscussPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussPostDAO {

    String TABLE_NAME = " discuss_post ";
    String INSERT_FIELDS = "user_id ,title ,content ,type ,status ,create_time ,comment_count ,score";
    String SELECT_FIELDS = "id,"+INSERT_FIELDS;


    //分页查询帖子的数量
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    //查询帖子的行数(方便分页)
    int selectDiscussPostRows(@Param("userId") int userId);

    //增加帖子
    @Insert({"insert into",TABLE_NAME,"(",INSERT_FIELDS,")" +
            " values(#{userId},#{title},#{content},#{type},#{status},#{createTime},#{commentCount},#{score})"})
    int addDiscussPost(DiscussPost discussPost);

    //查询帖子的详情
    @Select({"select ",SELECT_FIELDS,"from ",TABLE_NAME," where id=#{id}"})
    DiscussPost selectDiscussPostById(int id);

    //更新帖子的数量
    @Update({"update",TABLE_NAME,"set comment_count = #{commentCount} where id = #{id}"})
    int updateCommentCount(@Param("id") int id,@Param("commentCount") int count);

}
