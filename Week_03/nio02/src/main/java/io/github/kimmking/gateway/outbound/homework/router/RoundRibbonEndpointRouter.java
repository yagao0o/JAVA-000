package io.github.kimmking.gateway.outbound.homework.router;

import io.github.kimmking.gateway.router.HttpEndpointRouter;

import java.util.List;

/**
 * @author : Luyz
 * @date : 2020/11/4 21:44
 */
public class RoundRibbonEndpointRouter implements HttpEndpointRouter {
    private static Integer i = 0;
    @Override
    public String route(List<String> endpoints) {
        return endpoints.get(getNext(endpoints.size()));
    }

    private synchronized int getNext(int size) {
        i += 1;
        if (i >= size) {
            i = 0;
        }
        return i;
    }
}
