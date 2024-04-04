package com.leungyh.apiclient.client;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.leungyh.apiclient.exception.ErrorCode;
import com.leungyh.apiclient.exception.LeungyhApiException;
import com.leungyh.apiclient.model.BaseRequest;
import com.leungyh.apiclient.model.entity.User;
import com.leungyh.apiclient.model.enums.PathToMethodEnum;
import com.leungyh.apiclient.model.response.LoveTalkResponse;
import com.leungyh.apiclient.utils.SignUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Leungyh
 */
public class LeungyhApiClient {

    public static final String GATEWAY_HOST = "http://localhost:8088";

    private String accessKey;

    private String secretKey;

    public LeungyhApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getNameByGet(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.get(GATEWAY_HOST + "/api/name/", paramMap);
        return result;
    }

    public String getNameByPost(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.post(GATEWAY_HOST + "/api/name/", paramMap);
        return result;
    }

    /**
     * 根据用户输入的信息返回用户名
     *
     * @param user
     * @return
     */
    public String getUsernameByPost(User user) {
        return sendRequest(GATEWAY_HOST + PathToMethodEnum.name.getPath(), "POST", user, String.class);
    }

    /**
     * 土味情话
     *
     * @return
     */
    public LoveTalkResponse getLoveTalk() {
        return sendRequest(GATEWAY_HOST + PathToMethodEnum.loveTalk.getPath(), "GET", null, LoveTalkResponse.class);
    }

    /**
     * 发送调用url对应模拟接口的请求
     *
     * @param url
     * @param method
     * @param params
     * @param responseType
     * @param <T>
     * @return
     */
    public <T> T sendRequest(String url, String method, Object params, Class<T> responseType) {
        HttpRequest request;
        Gson gson = new Gson();
        String jsonBody = gson.toJson(params);
        switch (method) {
            case "GET":
                request = HttpRequest.get(url);
                request.form(convertObjToMap(params));
                break;
            case "POST":
                request = HttpRequest.post(url);
                request.header("Content-Type", "application/json; charset=UTF-8")
                        .body(jsonBody);
                break;
            default:
                throw new LeungyhApiException(ErrorCode.PARAMS_ERROR, "请求方式有误");
        }
        // 添加统一请求头
        request.addHeaders(getHeaderMap(jsonBody));
        // 发送请求 获得响应
        HttpResponse response = request.execute();
        String responseBody = response.body();
        if (response.getStatus() != 200) {
            throw new LeungyhApiException(ErrorCode.SYSTEM_ERROR, "请求接口失败");
        }
        return gson.fromJson(responseBody, responseType);
    }

    /**
     * 将参数对象转换为 Map
     *
     * @param params
     * @return
     */
    private Map<String, Object> convertObjToMap(Object params) {
        if (params == null) {
            return null;
        }
        HashMap<String, Object> map = new HashMap<>();
        Field[] fields = params.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(params));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    /**
     * 解析用户调用模拟接口的请求 并调用对应的模拟接口
     *
     * @param baseRequest
     * @return
     */
    public Object resolveAndCall(BaseRequest baseRequest) {
        String path = baseRequest.getPath();
        String method = baseRequest.getMethod();
        Map<String, Object> requestParams = baseRequest.getRequestParams();
        HttpServletRequest userRequest = baseRequest.getUserRequest();
        try {
            if (path.equals(PathToMethodEnum.name.getPath())) {
                return invokeMethod(PathToMethodEnum.name.getMethod(), requestParams, User.class);
            } else if (path.equals(PathToMethodEnum.loveTalk.getPath())) {
                return invokeMethod(PathToMethodEnum.loveTalk.getMethod());
            }
        } catch (LeungyhApiException e) {
            throw new LeungyhApiException(e.getCode(), e.getMessage());
        }
        return null;
    }

    /**
     * 根据方法名反射调用（无参）
     *
     * @param methodName
     * @return
     */
    private Object invokeMethod(String methodName) {
        return this.invokeMethod(methodName, null, null);
    }

    /**
     * 根据方法名反射调用（带参数）
     *
     * @param methodName
     * @param params
     * @param paramsType
     * @return
     * @throws LeungyhApiException
     */
    private Object invokeMethod(String methodName, Map<String, Object> params, Class<?> paramsType) throws LeungyhApiException {
        try {
            Class<?> clazz = LeungyhApiClient.class;
            if (params == null) {
                Method method = clazz.getMethod(methodName);
                return method.invoke(this);
            } else {
                Method method = clazz.getMethod(methodName, paramsType);
                Object paramsObject = BeanUtil.mapToBean(params, paramsType, true, CopyOptions.create());
                return method.invoke(this, paramsObject);
            }
        } catch (NoSuchMethodException e) {
            throw new LeungyhApiException(ErrorCode.PARAMS_ERROR, "未能找到与URL匹配的方法");
        } catch (InvocationTargetException e) {
            throw new LeungyhApiException(ErrorCode.PARAMS_ERROR, "对应方法调用失败");
        } catch (IllegalAccessException e) {
            throw new LeungyhApiException(ErrorCode.PARAMS_ERROR, "对应方法访问错误");
        }
    }

    /**
     * 添加统一的请求头
     *
     * @param body
     * @return
     */
    private Map<String, String> getHeaderMap(String body) {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("accessKey", accessKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(4));
        hashMap.put("body", body);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        hashMap.put("sign", SignUtils.genSign(body, secretKey));
        return hashMap;
    }


}
