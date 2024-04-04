package com.leungyh.apiclient.model.response;

import lombok.Data;

/**
 * @author Leungyh
 */
@Data
public class WeiboHotItem {

    /**
     * 热度指数
     */
    private Integer hotNum;

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 标签类型，如：热门 新
     */
    private String label;

    /**
     * 分类
     */
    private String category;

    /**
     * 标题
     */
    private String title;

    /**
     * 链接
     */
    private String url;

}
