package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.dto.SpuDetailDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.BaseApiService;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Autowired
    private BrandService brandService;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;


    @Resource
    private SkuMapper skuMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private StockMapper stockMapper;


    @Transactional
    @Override
    public Result<JSONObject> del(Integer spuId) {

        //删除spu
        spuMapper.deleteByPrimaryKey(spuId);
        //删除spuDeteil
        spuDetailMapper.deleteByPrimaryKey(spuId);

        //查询
        List<Long> skuIdArr = this.getSkuIdArrBySpuId(spuId);
        //做判断是避免删除全表
        if(skuIdArr.size() > 0){
            //删除skus
            skuMapper.deleteByIdList(skuIdArr);
            //删除stock,与修改时的逻辑一样,先查询出所有将要修改skuId然后批量删除
            stockMapper.deleteByIdList(skuIdArr);
        }

        return this.setResultSuccess();
    }

    //修改
    @Transactional
    @Override
    public Result<JSONObject> editGoots(SpuDTO spuDTO) {

        //修改spu
        Date date = new Date();
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        //修改spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(),SpuDetailEntity.class));

//        //修改sku
//        //先通过spuId删除sku
//        //然后新增数据
//        Example example = new Example(SkuEntity.class);
//        example.createCriteria().andEqualTo("spuId",spuDTO.getId());
//        //通用Mapper，通过criteria.andEqualTo进行查询时，加上空格仍能查询到结果
//        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
//        List<Long> skuIdArr = skuEntities.stream().map(sku -> sku.getId()).collect(Collectors.toList());
        List<Long> skuIdArr = this.getSkuIdArrBySpuId(spuDTO.getId());

        skuMapper.deleteByIdList(skuIdArr);

        //修改stock
        //删除stock
        //但是sku在上面已经被删除掉了
        //所以应该先查询出被删除的skuid
        //新增stock
        stockMapper.deleteByIdList(skuIdArr);
        List<SkuDTO> skus = spuDTO.getSkus();
        //将新数据新增到数据库
        this.saveSkusAndStocks(spuDTO.getSkus(),spuDTO.getId(),date);


        return this.setResultSuccess();
    }


    @Override
    public Result<SpuDetailEntity> getSpuDetailBydSpu(Integer spuId) {

        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);

        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
    public Result<List<SkuDTO>> getSkuBySpuId(Integer spuId) {

        List<SkuDTO> skuDTOS = skuMapper.selectSkuAndStockBySpuId(spuId);

        return this.setResultSuccess(skuDTOS);
    }


    //增加
    @Transactional
    @Override
    public Result<JSONObject> addInfo(SpuDTO spuDTO) {

        Date date = new Date();

        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        //新增spu
        spuMapper.insertSelective(spuEntity);

        //新增完spu的id
        Integer spuId = spuEntity.getId();
        //新增spudetail                             将DTO转成entity            原                ，     值
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuId);
        spuDetailMapper.insertSelective(spuDetailEntity);

        //将新数据新增到数据库
        this.saveSkusAndStocks(spuDTO.getSkus(),spuEntity.getId(),date);
//       spuDTO.getSkus().stream().forEach( skuDTO -> {
//            //新增sku                     将DTO转成entity
//            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
//            skuEntity.setSpuId(spuId);
//            skuEntity.setCreateTime(date);
//            skuEntity.setLastUpdateTime(date);
//            skuMapper.insertSelective(skuEntity);
//
//            //新增stock
//            StockEntity stockEntity = new StockEntity();
//            stockEntity.setSkuId(skuEntity.getId());
//            stockEntity.setStock(skuDTO.getStock());
//            stockMapper.insertSelective(stockEntity);
//
//        });


        return this.setResultSuccess();
    }


    //查询
    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {

        //分页
        if(ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        //构建条件查询     表示实例化的同时传递了一个对象给构造方法, 这个对象是一个Class对象
        Example example = new Example(SpuEntity.class);
        //构建查询条件
        Example.Criteria criteria = example.createCriteria();

        //按标题模糊匹配
        if(StringUtil.isNotEmpty(spuDTO.getTitle()))
            criteria.andLike("title","%" + spuDTO.getTitle() +"%");
        //如果值为2的话不进行拼接查询,默认查询所有
        if (ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)
            criteria.andEqualTo("saleable",spuDTO.getSaleable());

        //排序
        if(ObjectUtil.isNotNull(spuDTO.getSort()))
            example.setOrderByClause(spuDTO.getOrderByClause());

        List<SpuEntity> spuList = spuMapper.selectByExample(example);

        List<SpuDTO> SpuDTOList = spuList.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);

            //设置品牌名称
            BrandDTO brandDTO = new BrandDTO();
            brandDTO.setId(spuEntity.getBrandId());
            Result<PageInfo<BrandEntity>> brandInfo = brandService.getBrandInfo(brandDTO);

            if (ObjectUtil.isNotNull(brandInfo)) {

                PageInfo<BrandEntity> data = brandInfo.getData();
                List<BrandEntity> list = data.getList();

                if (!list.isEmpty() && list.size() == 1) {
                    spuDTO1.setBrandName(list.get(0).getName());
                }
            }

            /*BrandEntity brandEntity = brandMapper.selectByPrimaryKey(spuDTO1.getBrandId());
            if(ObjectUtil.isNotNull(brandEntity)) spuDTO1.setBrandName(brandEntity.getName());*/

            //分类名称
            String caterogyName = categoryMapper.selectByIdList(
                    Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()))
                    .stream().map(category -> category.getName())
                    .collect(Collectors.joining("/"));

            spuDTO1.setCategoryName(caterogyName);

            return  spuDTO1;
        }).collect(Collectors.toList());


        PageInfo<SpuEntity> objectPageInfo = new PageInfo<>(spuList);

        //要返回的是dto的数据,但是pageinfo中没有总条数
        long total = objectPageInfo.getTotal();
        //借用一下message属性
        return this.setResult(HTTPStatus.OK,total + "",SpuDTOList);
    }

    //与新增的部分代码重复,所以将重复代码抽取出来,记得修改新增方法!!!
    private void saveSkusAndStocks(List<SkuDTO> skus,Integer spuId,Date date){
        skus.stream().forEach(skuDto -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDto, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDto.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }


    //删除调用的查询
    private List<Long> getSkuIdArrBySpuId(Integer spuId){

        //修改sku
        //先通过spuId删除sku
        //然后新增数据
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        return skuEntities.stream().map(sku -> sku.getId()).collect(Collectors.toList());
    }
}
