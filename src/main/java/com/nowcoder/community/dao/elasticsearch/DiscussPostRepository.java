package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.model.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
    //继承ES接口，将实体类和ES仓库进行关联，指明主键类型
}
