package io.github.kimmking.gateway.outbound.homework.router;

import io.github.kimmking.gateway.router.HttpEndpointRouter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author : Luyz
 * @date : 2020/11/4 21:45
 */
public class WeightEndpointRouter implements HttpEndpointRouter {
    static List<Integer> weightList;
    public WeightEndpointRouter() {
        initWeightList();
    }
    /***
     * @param endpoints
     * @return
     */
    @Override
    public String route(List<String> endpoints) {
        int i = getNext();
        return endpoints.get(i);
    }

    private int getNext() {
        Integer total = weightList.stream().reduce(Integer::sum).get();
        Random random = new Random();
        int next = random.nextInt(total);
        for (int i = 0; i < weightList.size(); i++) {
            next = next - weightList.get(i);
            if (next < 0) {
                return i;
            }
        }
        return 0;
    }

    /***
     * 暂且默认设置为10，20，40，80的权重
     */
    private synchronized void initWeightList() {
        if (weightList == null || weightList.size() == 0) {
            String proxyServerWeights = System.getProperty("proxyServerWeights", "10,20,40,80");
            List<String> weightListString = Arrays.asList(proxyServerWeights.split(","));
            weightList = weightListString.stream().map(Integer::parseInt).collect(Collectors.toList());
        }
    }
}
