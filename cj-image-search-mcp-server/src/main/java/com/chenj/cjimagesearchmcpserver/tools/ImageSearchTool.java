package com.chenj.cjimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchTool {
    private static final String API_KEY = "AX8XSQ6x9NCxd9waZ9yUYKCTqI4F5QVxt2yBQGiDkWeBuv2R95rYjrXd";


    private static final String API_URL = "https://api.pexels.com/v1/search"; //接口地址

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }


    public List<String> searchMediumImages(String query) {

        Map<String, String> headers = new HashMap<>();  //new 个MAP 来装请求头的一些信息
        headers.put("Authorization", API_KEY); //包括apikey


        Map<String, Object> params = new HashMap<>();  //再new一个MAP 来装{“query”:query}
        params.put("query", query);


        String response = HttpUtil.createGet(API_URL)//用hutool工具类来构建请求（包含请求头+参数，并执行请求）
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();


        return JSONUtil.parseObj(response)//返回的数据
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
