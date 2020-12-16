package io.kimmking.rpcfx.client;

import io.kimmking.rpcfx.client.helper.RequestHelper;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author : Luyz
 * @date : 2020/12/16 14:49
 */
public class ByteBuddyInterceptor {
    public static String url = "";

    @RuntimeType
    public static Object intercept(@AllArguments Object[] allArguments, @Origin Method method) throws Exception {
        return RequestHelper.doRequest(allArguments, method.getDeclaringClass().getName(), method.getName(), url);
    }
}
