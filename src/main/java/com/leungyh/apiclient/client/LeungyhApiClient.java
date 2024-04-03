package com.leungyh.apiclient.client;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.leungyh.apiclient.exception.ErrorCode;
import com.leungyh.apiclient.exception.LeungyhApiException;
import com.leungyh.apiclient.model.BaseRequest;
import com.leungyh.apiclient.model.entity.User;
import com.leungyh.apiclient.model.enums.PathToMethodEnum;
import com.leungyh.apiclient.model.response.LoveTalkResponse;
import com.leungyh.apiclient.utils.SignUtils;

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
        String json = JSONUtil.toJsonStr(user);
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
                .addHeaders(getHeaderMap(json))
                .body(json)
                .execute();
        String result = httpResponse.body();
        return result;
    }

    public LoveTalkResponse getLoveTalk() {
        HttpResponse httpResponse = HttpRequest.get(GATEWAY_HOST + PathToMethodEnum.loveTalk.getPath()).addHeaders(getHeaderMap("")).execute();
        String loveTalk = httpResponse.body();
        LoveTalkResponse result = new LoveTalkResponse();
        result.setText(loveTalk);
        return result;
    }

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
