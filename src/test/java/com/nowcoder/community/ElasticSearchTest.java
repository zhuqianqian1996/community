package com.nowcoder.community;


import com.nowcoder.community.dao.DiscussPostDAO;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.model.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.metrics.geobounds.InternalGeoBounds;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {

    @Resource
    private DiscussPostDAO discussPostDAO;

    @Resource
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    //插入一条数据
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostDAO.selectDiscussPostById(241));
        discussPostRepository.save(discussPostDAO.selectDiscussPostById(242));
        discussPostRepository.save(discussPostDAO.selectDiscussPostById(243));
    }

    //插入多条数据
    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostDAO.selectDiscussPosts(134, 0, 100));
    }

    //修改数据
    @Test
    public void testUpdate(){
        DiscussPost post = discussPostDAO.selectDiscussPostById(231);
        post.setContent("我是新人，我要灌水");
        //插入到ES中
        discussPostRepository.save(post);
    }

    //删除
    @Test
    public void testDelete(){
        //删除一条数据
       //discussPostRepository.deleteById(231);
       //删除全部数据
        discussPostRepository.deleteAll();
    }

    //搜索
    @Test
    public void testSearch(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                //查询匹配规则：查询的文本，查询的域
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                //排序的规则：按照某个字段排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //分页展示
                .withPageable(PageRequest.of(0,10))
                //高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                        ).build();
       //底层原理 elasticsearchTemplate.queryForPage(searchQuery, class ,SearchResultMapper) 底层获取到了高亮显示的值但是没有返回
        //执行查询
        Page<DiscussPost> page = discussPostRepository.search(searchQuery);
        //获取所有的数据
        System.out.println(page.getTotalElements());
        //获取页数
        System.out.println(page.getTotalPages());
        //当前页数
        System.out.println(page.getNumber());
        //每一页显示数据的数量
        System.out.println(page.getSize());
        //遍历数据
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    //使用 ElasticsearchTemplate进行查询操作
    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                //查询匹配规则：查询的文本，查询的域
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                //排序的规则：按照某个字段排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //分页展示
                .withPageable(PageRequest.of(0,10))
                //高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        //查询数据:返回的就是数据
        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                //获取多个命令的数据
                SearchHits hits = response.getHits();
                //如果没有命令，则说明没有查询到数据
                if (hits.getTotalHits() <= 0){
                    return null;
                }
                //大于0要对数据进行处理
                List<DiscussPost> list = new ArrayList<>();
                //遍历命令
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();
                    //将数据存储到模型中
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String type = hit.getSourceAsMap().get("type").toString();
                    post.setType(Integer.valueOf(type));

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    String score = hit.getSourceAsMap().get("score").toString();
                    post.setScore(Double.valueOf(score));

                    //处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        //如果标题不为空，则将第0号元素设置到标题中
                        post.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        //如果内容不为空，则将第0号元素设置到标题中
                        post.setContent(contentField.getFragments()[0].toString());
                    }
                    list.add(post);
                }
                //返回所需要的结果
                return new AggregatedPageImpl(list,pageable,hits.getTotalHits(),
                        response.getAggregations(),response.getScrollId(),hits.getMaxScore());
            }
        });
        //获取所有的数据
        System.out.println(page.getTotalElements());
        //获取页数
        System.out.println(page.getTotalPages());
        //当前页数
        System.out.println(page.getNumber());
        //每一页显示数据的数量
        System.out.println(page.getSize());
        //遍历数据
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }
}
