package io.kimmking.rpcfx.client.helper;

import com.alibaba.fastjson.JSON;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.exception.RpcfxException;

import java.io.IOException;

/**
 * @author : Luyz
 * @date : 2020/12/16 15:14
 */
public class RequestHelper {
    //    public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");
    public static Object doRequest(Object[] params, String className, String methodName, String url) throws IOException,
            RpcfxException {
        RpcfxRequest request = new RpcfxRequest();
        request.setServiceClass(className);
        request.setMethod(methodName);
        request.setParams(params);

        RpcfxResponse response = post(request, url);
        // 这里判断response.status，处理异常
        // 考虑封装一个全局的RpcfxException
        if (!response.isStatus()) {
            throw response.getException();
        }
        return JSON.parse(response.getResult().toString());
    }

    private static RpcfxResponse post(RpcfxRequest req, String url) throws IOException {
        String reqJson = JSON.toJSONString(req);
        String respJson = "";

        // 1.可以复用client
        // 2.尝试使用httpclient或者netty client
//        OkHttpClient client = new OkHttpClient();
//        final Request request = new Request.Builder()
//                .url(url)
//                .post(RequestBody.create(JSONTYPE, reqJson))
//                .build();
//        String respJson = client.newCall(request).execute().body().string();

        // netty 方式
        NettyClient client = new NettyClient(url);
        try {
            respJson = client.sendMessage(reqJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.parseObject(respJson, RpcfxResponse.class);
    }
}

