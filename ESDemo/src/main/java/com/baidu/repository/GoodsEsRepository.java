package com.baidu.repository;

import com.baidu.entity.EsEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import javax.swing.text.Document;
import java.util.List;

public interface GoodsEsRepository extends ElasticsearchRepository<EsEntity,Long> {

    List<EsEntity> findAllByTitle(String title);

    List<EsEntity> findByAndPriceBetween(Double start,Double end);

}
