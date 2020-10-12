package com.baidu.shop.feign;

import com.baidu.shop.document.GoodsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<GoodsDoc,Long> {
}
