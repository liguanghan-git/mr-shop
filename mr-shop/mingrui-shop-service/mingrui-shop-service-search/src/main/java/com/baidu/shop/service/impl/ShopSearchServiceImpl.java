package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.BaseApiService;
import com.baidu.shop.service.ShopSearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/16
 * @Version V1.0
 **/

@RestController
@Slf4j
public class ShopSearchServiceImpl extends BaseApiService implements ShopSearchService {


    @Resource
    private GoodsFeign goodsFeign;

    @Resource
    private SpecificationFeign specificationFeign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private BrandFeign brandFeign;

    @Resource
    private CategoryFeign categoryFeign;


    //新增和修改
    @Override
    public Result<JSONObject> saveData(Integer spuId) {

        //通过spuId查询数据
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);

        List<GoodsDoc> goodsDocs = this.esGoodsInfo(spuDTO);

        elasticsearchRestTemplate.save(goodsDocs.get(0));

        return this.setResultSuccess();
    }

    //删除
    @Override
    public Result<JSONObject> delData(Integer spuId) {
        return null;
    }

    //search方法理论上直接查询es库就可以
    //现在的代码是在查询es库之后，又去查询了两次musql表
    /**
     * 搜索
     * **/
    @Override
    public GoodsResponse search(String search,Integer page,String filter) {

        //判断搜索内容不为空,判断参数               自定义异常
        if (StringUtil.isEmpty(search)) throw new RuntimeException("搜索内容不能为空");

        //查询
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(this.getSearchQueryBuilder(search, page,filter).build(), GoodsDoc.class);
        //整合高亮
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());
        //要返回的数据,返回的商品集合,获取页面上需要显示的数据
        List<GoodsDoc> GoodsList = highLightHit.stream().map(resp -> resp.getContent()).collect(Collectors.toList());

        //总条数&总页数
        long total = searchHits.getTotalHits();
        //double ceil = Math.ceil(Long.valueOf(total).doubleValue() / 10);
        //将double转换成整型 byte(字节型) short(短整型) integer(整型) long(长整型) char-->Character setCharacterEncoding
        //基本数据类型几乎.不出来任何东西
        //基本数据类型和包装数据类型如何拆装箱?????
        //.valueOf()装箱，.longValue()转long类型
        //Math.ceil()向上取整
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();


        //传到前台是一个json字符串-->JSON.parse(message)  obj.total,obj.totalPage

        //aggregations聚合
        //得到聚合的数据
        Aggregations aggregations = searchHits.getAggregations();

        //List<CategoryEntity> cidList = this.getCidList(aggregations);
        Map<Integer, List<CategoryEntity>> map = this.getCidList(aggregations);//获取分类集合，获取信息

        //key cid
        //value list
        //这里不能使用lambda表达式 map.forEach((key,value) -> {})
        //使用entrySet()遍历   map.entrySet()得到set集合    mapEntry是entry对象
        //map.keySet() 获得集合中所有的key
        //map集合不会有重复的key
        //修改map集合的值   put(key,value),有当前key就修改，没有就是map增加  put是函数
        List<CategoryEntity> cidList = null; //value
        Integer hotCid = 0; //key

        for (Map.Entry<Integer,List<CategoryEntity>> mapEntry : map.entrySet()){

            hotCid = mapEntry.getKey();
            cidList = mapEntry.getValue();
        }

        //通过cid查询规格参数
        Map<String, List<String>> specParamValueMap = this.getSpecParams(hotCid, search);


        List<BrandEntity> beandList = this.getBeandList(aggregations);//获取品牌集合，获取信息

        return new GoodsResponse(total, totalPage, beandList, cidList, GoodsList,specParamValueMap);
    }


    private  Map<String, List<String>> getSpecParams(Integer hotCid,String search){

        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);//只搜索有查询属性的规格参数
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);

        if (specParamResult.getCode() == 200) {
            List<SpecParamEntity> specParamList = specParamResult.getData();
            //聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search, "brandName", "categoryName", "title"));
            //分页
            queryBuilder.withPageable(PageRequest.of(0, 1));

            specParamList.stream().forEach(specParam -> {
                //.terms(specParam.getName())分组   .field("specs." + specParam.getName() + ".keyword")聚合  specParam.getName()-->聚合字段
                queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs." + specParam.getName() + ".keyword"));
            });

            //查询
            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

            Map<String, List<String>> map = new HashMap<>();
            //取桶里的数据
            Aggregations aggregations = searchHits.getAggregations();

            //不知道使用那个优先使用forEach()  有返回信息使用map
            specParamList.stream().forEach(specParam -> {

                //Aggregation --> 用它的子类Terms
                //specParam.getName()通过这个名字得到桶里里的内容
                Terms terms = aggregations.get(specParam.getName());
                //桶里的内容
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                //遍历桶
                // .getKeyAsString()--> 定为String类型 bucket-->因为桶了都是String类型
                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

                map.put(specParam.getName(), valueList);
            });

            return map;
        }

        return null;
    }

    /**
     * 构建查询条件
     * **/
    private NativeSearchQueryBuilder  getSearchQueryBuilder(String search,Integer page,String filter){

        // 多条件查询方法 --> 多条件查询方法
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

        //点击分类,品牌或参数查询
        if (StringUtil.isNotEmpty(filter) && filter.length() > 2){
            // bool把各种其它查询通过 must（与）、must_not（非）、should（或）的方式进行组合 ,组合查询
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //将String 转成 Map  前台把key,value转成String传过来的
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);

            filterMap.forEach((key,value) -> {
                //matchQuery("key", Obj) 单个匹配, field不支持通配符, 前缀具高级特性
                MatchQueryBuilder matchQueryBuilder = null;

                //分类 品牌和 规格参数的查询方式不一样
                if(key.equals("cid3") || key.equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(key, value);
                }else {
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + key + ".keyword",value);
                }
                // .must --> 布尔组合查询
                boolQueryBuilder.must(matchQueryBuilder);
            });
            //使用withFilter，对结果过滤
            searchQueryBuilder.withFilter(boolQueryBuilder);
        }


        //Match 通过查询一个字段 ，multiMatch通过值查询多个字段
        //QueryBuilders.multiMatchQuery()分词查询
        searchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));

        //通过品牌id -->brandId -->聚合
        //通过分类 -->cid3  -->聚合
        //terms()-- >相当于定义别名    field("cid3") --> 通过字段聚合
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3"));
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("branId_agg").field("brandId"));

        //高亮
        searchQueryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));
        //分页
        searchQueryBuilder.withPageable(PageRequest.of(page-1,10));

        return searchQueryBuilder;
    }

    /**
     * 获取品牌集合
     * **/
    private List<BrandEntity> getBeandList(Aggregations aggregations){

        Terms branId_agg = aggregations.get("branId_agg");

        //List<? extends Terms.Bucket> brandBuckets = branId_agg.getBuckets();
        List<String> bandIdList = branId_agg.getBuckets()
                .stream().map(brandBucket -> brandBucket.getKeyAsString())
                .collect(Collectors.toList());

        //通过brandid获取brand详细数据
        //String.join(分隔符,List<String>),将list集合转为,分隔的字符串
        Result<List<BrandEntity>> brandRelust = brandFeign.getBrandByIds(String.join(",", bandIdList));

        return brandRelust.getData();
    }

    /**
     * 获取分类集合
     * **/
    private  Map<Integer, List<CategoryEntity>> getCidList(Aggregations aggregations){

        Terms cid_agg = aggregations.get("cid_agg");
        //聚合的桶 -->getBuckets()
        List<? extends Terms.Bucket> cidBuckets = cid_agg.getBuckets();

        //热度最高cid
        List<Integer> hotCidArr = Arrays.asList(0);//把数组转换成集合时，不能使用其修改集合相关的方法
        List<Long> maxCount = Arrays.asList(0L);
        //ArrayList --> array -->定长的 --> list可以无限的add
        //所以ArrayList --> 默认数组长度是10 -->add时，数组长度大于等于10，会发生扩容1.5倍

        //用map接值，因为有两个返回值，用list要强转类型，list已经规定好一个类型了，传两个只好用map集合
        Map<Integer, List<CategoryEntity>> map = new HashMap<>();

        List<String> cidList = cidBuckets.stream().map(cidBucket -> {
            //是将桶key转为number类型   key为字符串类型的数据时，是无法转为numebr类型的。
            Number keyAsNumber = cidBucket.getKeyAsNumber();

            if (cidBucket.getDocCount() > maxCount.get(0)) {
                //set赋值，给下标0赋值
                maxCount.set(0, cidBucket.getDocCount());
                hotCidArr.set(0, keyAsNumber.intValue());
            }

            //intValue() -->输出int数据
            //valueOf(Object obj) 返回 Object 参数的字符串表示形式。 表示的是将( )中的 值， 转换  成  字符串类型
            return keyAsNumber.intValue() + "";
        }).collect(Collectors.toList());

        //通过分类id获取分类详细数据
        Result<List<CategoryEntity>> cidRelust = categoryFeign.getCateByIds(String.join(",", cidList));

        map.put(hotCidArr.get(0),cidRelust.getData());
        return map;
    }

    /**
     * 初始化es数据
     * **/
    @Override
    public Result<JSONObject> initGoodsEsData() {

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (!indexOperations.exists()){
            indexOperations.create();
            log.info("索引创建成功");
            indexOperations.createMapping();
            log.info("映射创建成功");
        }
        List<GoodsDoc> goodsDocs = this.esGoodsInfo(new SpuDTO());
        
        if (!goodsDocs.isEmpty()){

            elasticsearchRestTemplate.save(goodsDocs);
        }
        return this.setResultSuccess();
    }

    /**
     * 清空es数据
     * **/
    @Override
    public Result<JSONObject> clearGoodsEsData() {

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(indexOperations.exists()){
            indexOperations.delete();
            log.info("索引删除成功");
        }

        return this.setResultSuccess();
    }


    /**
     * 获取mysql数据
     * **/
    private List<GoodsDoc> esGoodsInfo(SpuDTO spuDTO) {

        //查询出来的数据是多个spu
        List<GoodsDoc> goodsDocs = new ArrayList<>();

        //查询spu信息
//        SpuDTO spuDTO = new SpuDTO();

        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);


        if (spuInfo.getCode() ==  HTTPStatus.OK){

            //SPU数据
            List<SpuDTO> spuList = spuInfo.getData();

            List<GoodsDoc> collect = spuList.stream().map(spu -> {

                GoodsDoc goodsDoc = new GoodsDoc();

                //.longValue()将 integter装成Long
                //spu信息填充
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setSubTitle(spu.getSubTitle());
                //可查询数据
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());


                //通过spuId查询skuList,sku数据填充
                Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuId(spu.getId());
                Map<List<Long>, List<Map<String, Object>>> skusAndPriceList = this.getSkusAndPriceList(spu.getId());

                ///任意类型都可以做为map集合的key
                if (skuResult.getCode() == HTTPStatus.OK) {
                    skusAndPriceList.forEach((key, value) -> {
                        goodsDoc.setPrice(key);
                        goodsDoc.setSkus(JSONUtil.toJsonString(value));
                    });

                    List<SkuDTO> skuList = skuResult.getData();

                    //通过cid3查询规格参数
                    Map<String, Object> specMap = this.getSpecMap(spu);

                    goodsDoc.setSpecs(specMap);
                    goodsDocs.add(goodsDoc);  //
                }
                return goodsDoc;
            }).collect(Collectors.toList());
            System.out.println(collect);

        }
        return goodsDocs;
    }


    private Map<List<Long>,List<Map<String,Object>>>  getSkusAndPriceList(Integer spuId){

        Map<List<Long>,List<Map<String,Object>>> hashMap = new HashMap<>();

        Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuId(spuId);
        List<Long> priceList = new ArrayList<>();
        List<Map<String,Object>> skuMap = null;

        if (skuResult.getCode() == HTTPStatus.OK){

            List<SkuDTO> skuList = skuResult.getData();

            skuMap = skuList.stream().map(sku -> {

                Map<String, Object> map = new HashMap<>();

                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("images", sku.getImages());
                map.put("price", sku.getPrice());

                priceList.add(sku.getPrice().longValue());
                return map;
            }).collect(Collectors.toList());
        }
        hashMap.put(priceList,skuMap);

        return hashMap;
    }


    private Map<String,Object> getSpecMap(SpuDTO spuDTO){

        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuDTO.getCid3());
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);

        Map<String, Object> specMap = new HashMap<>();

        if(specParamResult.getCode() == HTTPStatus.OK){

            //只有规格参数的id和规格参数的名字
            List<SpecParamEntity> paramList = specParamResult.getData();

            //通过spuid查询spuDetail,detail里面有特殊和通用规格参数的值
            Result<SpuDetailEntity> spuDetailBydSpu = goodsFeign.getSpuDetailBydSpu(spuDTO.getId());

            if(spuDetailBydSpu.getCode() == HTTPStatus.OK){

                SpuDetailEntity spuDataInfo = spuDetailBydSpu.getData();

                //通用规格参数的值
                String genericSpecStr = spuDataInfo.getGenericSpec();
                Map<String, String> genericSpecMap  = JSONUtil.toMapValueString(genericSpecStr);

                //特有规格参数的值
                String specialSpec = spuDataInfo.getSpecialSpec();
                Map<String, List<String>> specialSpecMap = JSONUtil.toMapValueStrList(specialSpec);

                paramList.stream().forEach(param ->{

                    if (param.getGeneric()){

                        if (param.getNumeric() && param.getSearching()){

                            specMap.put(param.getName(),this.chooseSegment(genericSpecMap .get(param.getId() +""),param.getSegments(),param.getUnit()));
                        }else {
                            specMap.put(param.getName(),genericSpecMap .get(param.getId() + ""));
                        }
                    }else {
                        specMap.put(param.getName(),specialSpecMap.get(param.getId() +""));
                    }
                });
            }
        }

        return specMap;
    }


    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }


}
