package com.leungyh.apiclient.model.response;

import lombok.Data;

import java.util.List;

/**
 * @author Leungyh
 */
@Data
public class WeiboHotSearchResponse {

    private List<WeiboHotItem> weiboHotItemList;

}
