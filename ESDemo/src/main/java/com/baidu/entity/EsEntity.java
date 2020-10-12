package com.baidu.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/14
 * @Version V1.0
 **/
//声明当前类是一个文档(indexName="索引名称", shards="索引的分片数",replicas="索引的副本数-->拷贝")
@Document(indexName = "ll",shards = 1,replicas = 0)
public class EsEntity {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title; //标题

    /**
     *
     * - text：可分词，不可参与聚合
     * - keyword：不可分词，数据会作为完整字段进行匹配，可以参与聚合
     *
     *
     * terms：划分桶的方式，这里是根据词条划分
     * - field：划分桶的字段
     *
     */

    @Field(type = FieldType.Keyword)
    private String category;// 分类

    @Field(type = FieldType.Keyword)
    private String brand; // 品牌

    @Field(type = FieldType.Double)
    private Double price; // 价格

    //index = false 不参与索引搜索
    /*
     * 设置index为to 的好处false是，当您为文档建立索引时，Elasticsearch将不必为该字段构建反向索引。结果，索引文档将稍快一些。同样，由于该字段在磁盘上将没有持久化的反向索引，因此您将使用更少的磁盘空间。*/
    @Field(index = false,type = FieldType.Keyword)
    private String images; // 图片地址


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "EsEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", images='" + images + '\'' +
                '}';
    }
}
