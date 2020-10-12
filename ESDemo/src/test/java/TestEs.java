import com.baidu.RunTestEsApplication;
import com.baidu.entity.EsEntity;
import com.baidu.repository.GoodsEsRepository;
import com.baidu.utils.ESHighLightUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/14
 * @Version V1.0
 **/
//让测试在Spring容器环境下执行
@RunWith(SpringRunner.class)
//声明启动类,当测试方法运行的时候会帮我们自动启动容器
@SpringBootTest(classes = {RunTestEsApplication.class})
public class TestEs {


    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private GoodsEsRepository goodsEsRepository;

    /***
     创建索引
     * */
    @Test
    public void createGoodsIndex(){

        IndexOperations baidu = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("baidu"));

        baidu.create();//创建索引

        System.out.println(baidu.exists()?"索引创建成功":"索引创建失败");

    }

    /***
     删除索引
     * */
    @Test
    public void  deleteGoodsIndex(){

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(EsEntity.class);

        indexOperations.delete();

        System.out.println("删除索引成功");
    }


    /**
     创建映射
     * **/
    @Test
    public void createBaiDuGoodsMapper(){

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(EsEntity.class);
        System.out.println(indexOperations.exists());
//        indexOperations.create();
        System.out.println("映射创建成功");
    }


    /*
    新增文档,修改
     */
    @Test
    public void saveData(){

        EsEntity entity = new EsEntity();
        entity.setId(1L);
        entity.setBrand("小米");
        entity.setCategory("手机");
        entity.setImages("xiaomi.jpg");
        entity.setPrice(1000D);
        entity.setTitle("小米");

        //增加 修改
        goodsEsRepository.save(entity);

        System.out.println("新增成功");
    }


    /*
    批量新增文档
     */
    @Test
    public void saveAllData(){

        EsEntity entity = new EsEntity();
        entity.setId(2L);
        entity.setBrand("苹果");
        entity.setCategory("手机");
        entity.setImages("pingguo.jpg");
        entity.setPrice(5000D);
        entity.setTitle("iphone11手机");

        EsEntity entity2 = new EsEntity();
        entity2.setId(3L);
        entity2.setBrand("三星");
        entity2.setCategory("手机");
        entity2.setImages("sanxing.jpg");
        entity2.setPrice(3000D);
        entity2.setTitle("w2019手机");

        EsEntity entity3 = new EsEntity();
        entity3.setId(4L);
        entity3.setBrand("华为");
        entity3.setCategory("手机");
        entity3.setImages("huawei.jpg");
        entity3.setPrice(4000D);
        entity3.setTitle("华为mate30手机");

        EsEntity entity4 = new EsEntity();
        entity4.setId(5L);
        entity4.setBrand("小米");
        entity4.setCategory("手机");
        entity4.setImages("huawei.jpg");
        entity4.setPrice(9990D);
        entity4.setTitle("小米900");

        goodsEsRepository.saveAll(Arrays.asList(entity,entity2,entity3,entity4));

        System.out.println("批量新增成功");
    }


    /***
     删除文档
     * */
    @Test
    public void deleteData(){

        EsEntity esEntity = new EsEntity();
        esEntity.setId(1L);

        goodsEsRepository.delete(esEntity);

        System.out.println("删除成功");

    }


    /**
     查询所有
     * **/
    @Test
    public void searchAll(){
        //查询总条数
        long count = goodsEsRepository.count();
        System.out.println(count);
        //查询所有数据
        Iterable<EsEntity> all = goodsEsRepository.findAll();
        all.forEach(goods -> {
            System.out.println(goods);
        });
    }

    /**
     条件查询
     * **/
    @Test
    public  void queryCondition(){

        List<EsEntity> AllByTitle = goodsEsRepository.findAllByTitle("手机");

        AllByTitle.stream().forEach(e -> {
            System.out.println(e);
        });

        //System.out.println(AllByTitle);

        System.out.println("==================");

        List<EsEntity> byAndPriceBetween = goodsEsRepository.findByAndPriceBetween(999D, 10000D);
        System.out.println(byAndPriceBetween);
    }

    /**
     自定义查询
     * **/

    @Test
    public void  customizeSearch(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //QueryBuilders是QueryBuilder的工具类
        //queryBuilder.withQuery(QueryBuilders.matchQuery("title","三星"));

        //gte(1000).lte(4000) 大于等于1000 小于等于4000
        //条件查询
        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","华为手机"))
                        .must(QueryBuilders.rangeQuery("price").gte(999).lte(10000))
        );


        //排序 DESC降序 ASC升序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));

        //分页
        queryBuilder.withPageable(PageRequest.of(0,2));



        SearchHits<EsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), EsEntity.class);

        List<SearchHit<EsEntity>> searchHits = search.getSearchHits();
        searchHits.stream().forEach(e -> {System.out.println(e);});

        //Collections是Collection工具类
        //Collections.copy(); 拷贝list
        //Collections.disjoint();数据交互,查询一个集合里是否包含另一个集合里的参数
        //Collections.sort();用来做list集合内容排序
        //Collections.reverse();反转集合内容
        //Collections.replaceAll();替换集合里的某一项数据
        //Collections.lastIndexOfSubList(0,2);通过下标接续list里的信息

        //queryBuilder.withPageable()   分页
        //queryBuilder.withSort()  排序

    }

    /**
     高亮
     * **/
    @Test
    public  void brightnessSearch(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

//        HighlightBuilder highlightBuilder = new HighlightBuilder();
//        HighlightBuilder.Field title = new HighlightBuilder.Field("title");
//        title.preTags("<font style='color:red'>");//开始标签
//        title.postTags("</font>");//结束标签
//        highlightBuilder.field(title);
//        queryBuilder.withHighlightBuilder(highlightBuilder);


        //设置高亮
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));


        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","华为手机"))
        );

        SearchHits<EsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), EsEntity.class);

        List<SearchHit<EsEntity>> searchHits = search.getSearchHits();

        //重新设置title
//        List<SearchHit<EsEntity>> result = searchHits.stream().map(e -> {
//
//            Map<String, List<String>> highlightFields = e.getHighlightFields();
//            highlightFields.get("title");
//            return  e;
//
//        }).collect(Collectors.toList());


        //重新设置title
        List<SearchHit<EsEntity>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits);

        highLightHit.stream().forEach(e -> {
            System.out.println(e);
        });


        //searchHits.stream().forEach(e -> {System.out.println(e);});

    }


    /**
     聚合为桶桶,分组
     * **/
    @Test
    public void searchAgg(){

        NativeSearchQueryBuilder nSQB = new NativeSearchQueryBuilder();

        nSQB.addAggregation(
                AggregationBuilders.terms("brand_agg").field("brand")
        );

        SearchHits<EsEntity> search = elasticsearchRestTemplate.search(nSQB.build(), EsEntity.class);

        Aggregations aggregations = search.getAggregations();

        //terms 是Aggregation的子类
        //Aggregation brand_agg = aggregations.get("brand_agg");/
        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());
        });
        System.out.println(search);

    }


    /*
   聚合函数
    */
    @Test
    public void searchAggMethod(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg")
                        .field("brand")
                        //聚合函数
                        .subAggregation(AggregationBuilders.max("max_price").field("price"))
        );

        SearchHits<EsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), EsEntity.class);

        Aggregations aggregations = search.getAggregations();

        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());

            //获取聚合
            Aggregations aggregations1 = bucket.getAggregations();
            //得到map
            Map<String, Aggregation> map = aggregations1.asMap();
            //需要强转,Aggregations是一个类 Terms是他的子类,Aggregation是一个接口Max是他的子接口,而且Max是好几个接口的子接口
            Max max_price = (Max) map.get("max_price");
            System.out.println(max_price.getValue());
        });
    }


}
