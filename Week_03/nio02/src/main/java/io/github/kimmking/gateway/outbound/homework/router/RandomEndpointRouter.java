package io.github.kimmking.gateway.outbound.homework.router;

import io.github.kimmking.gateway.router.HttpEndpointRouter;

import java.util.List;
import java.util.Random;

/**
 * @author : Luyz
 * @date : 2020/11/4 21:42
 */
public class RandomEndpointRouter implements HttpEndpointRouter {
    @Override
    public String route(List<String> endpoints) {
        Random random = new Random();
        return endpoints.get(random.nextInt(endpoints.size()));
    }
}
