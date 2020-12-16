package io.kimmking.rpcfx.client;


import com.alibaba.fastjson.parser.ParserConfig;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public final class RpcfxByteBuddy {

    static {
        ParserConfig.getGlobalInstance().addAccept("io.kimmking");
    }

    public static <T> T create(final Class<T> serviceClass, final String url) {
        // 0. 替换动态代理 -> AOP{字节码增强}
        try {
            ByteBuddyInterceptor.url = url;
            return new ByteBuddy()
                    .subclass(serviceClass)
                    .method(ElementMatchers.isDeclaredBy(serviceClass))
                    .intercept(MethodDelegation.to(ByteBuddyInterceptor.class))
                    .make()
                    .load(ClassLoader.getSystemClassLoader())
                    .getLoaded()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
