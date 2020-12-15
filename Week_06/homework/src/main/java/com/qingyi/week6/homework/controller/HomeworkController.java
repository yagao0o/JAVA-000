package com.qingyi.week6.homework.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyi.week6.homework.entity.Good;
import com.qingyi.week6.homework.entity.Order;
import com.qingyi.week6.homework.entity.User;
import com.qingyi.week6.homework.service.IGoodService;
import com.qingyi.week6.homework.service.IOrderService;
import com.qingyi.week6.homework.service.IUserService;
import com.sun.tools.corba.se.idl.constExpr.Or;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author : Luyz
 * @date : 2020/11/25 23:24
 */
@SuppressWarnings({"AlibabaUndefineMagicConstant", "AlibabaThreadPoolCreation"})
@RestController
@RequestMapping("/homework")
@Slf4j
public class HomeworkController {
    private IUserService userService;

    @Autowired
    private void setUserService(IUserService userService) {
        this.userService = userService;
    }

    private IGoodService goodService;

    @Autowired
    private void setGoodService(IGoodService goodService) {
        this.goodService = goodService;
    }

    private IOrderService orderService;

    @Autowired
    public void setOrderService(IOrderService orderService) {
        this.orderService = orderService;
    }


    @RequestMapping(value = "/addTestGoods", method = RequestMethod.POST)
    public void addGoods() {
        int goodSize = 500;
        List<Good> goods = new ArrayList<>();
        for (int i = 0; i < goodSize; i++) {
            goods.add(getTestGood());
        }
        goodService.saveBatch(goods);
    }

    @RequestMapping(value = "/testUserOperation", method = RequestMethod.POST)
    public void addUser() {
        User user;
        // 添加
        for (int i = 36; i <= 100; i++) {
            user = getTestUser();
            userService.save(user);
        }

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 查询
        List<User> fetchUsers = userService.list();
        log.info("total user =" + fetchUsers.size());
        // 修改操作,取最后一个改
        user = fetchUsers.get(fetchUsers.size() - 1);
        userService.update(new UpdateWrapper<User>().eq("id", user.getId()).set("address", "addr:" + System.currentTimeMillis()));

        fetchUsers = userService.list();
        log.info("total user = " + fetchUsers.size());

        //删除操作
        if (fetchUsers.size() > 0) {
            userService.removeById(fetchUsers.get(0).getId());
        }

        fetchUsers = userService.list();
        log.info("Now, total user = " + fetchUsers.size());
    }

    /***
     * 以下仅为模拟订单，不包括订单详情表
     */
    @RequestMapping(value = "/testAddOrder", method = RequestMethod.POST)
    public void test1mAdd() {
        // setup
        List<User> users = userService.list();
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPool = Executors.newFixedThreadPool(cores);
        CountDownLatch lock = new CountDownLatch(1000);
        // 记录时间戳
        long startTime = System.nanoTime();
        log.info("start: " + startTime);
        // 生成订单数据1000*1000
        for (int i = 0; i < 1000; i++) {
            List<Order> orders = new ArrayList<>();
            for (int j = 0; j < 1000; j++) {
                orders.add(getTestOrder(users));
            }
            // 每1000个订单提交一次
            threadPool.submit(() -> {
                orderService.saveBatch(orders);
                lock.countDown();
            });
        }
        // 记录时间戳
        try {
            lock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        log.info("end: " + endTime);
        // 计算消耗时间
        log.info("总消耗时间：" + (endTime - startTime));
    }

    @RequestMapping(value = "/testSearchOrder", method = RequestMethod.POST)
    public void test1mSearch() {
        // 查询数据总量
        int total = orderService.count();
        log.info("total: " + total);
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPool = Executors.newFixedThreadPool(cores);
        CountDownLatch lock = new CountDownLatch(10);
        // 记录开始时间戳
        long startTime = System.nanoTime();
        log.info("start: " + startTime);
        // 10个线程并发操作
        for (int i = 0; i < 10; i++) {
            // 每1000个订单提交一次
            threadPool.submit(() -> {
                // 分页查询，每页100条记录，每个线程随机查询100次
                for (int j = 0; j < 100; j++) {
                    IPage<Order> orderPage = orderService.page(
                            new Page<>(new Random().nextInt(total / 100) * 100 + 1,
                                    100));
                }
                lock.countDown();
            });
        }
        // 记录结束时间戳
        try {
            lock.await(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        log.info("end: " + endTime);
        // 计算消耗时间
        log.info("总消耗时间：" + (endTime - startTime));
    }

    @RequestMapping(value = "/testUpdateOrder", method = RequestMethod.POST)
    public void test1mUpdate() {
        // 查询数据总量
        int total = orderService.count();
        log.info("total: " + total);
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPool = Executors.newFixedThreadPool(cores);
        CountDownLatch lock = new CountDownLatch(10);
        // 记录开始时间戳
        long startTime = System.nanoTime();
        log.info("start: " + startTime);
        // 10个线程并发操作
        for (int i = 0; i < 10; i++) {
            // 每个线程循环1k次：随机生成一个数字<总量，更新address字段为随机值
            threadPool.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    IPage<Order> orderPage = orderService.page(
                            new Page<>(new Random().nextInt(total) + 1,
                                    1));
                    Order order = orderPage.getRecords().get(0);
                    orderService.update(new UpdateWrapper<Order>()
                            .eq("id", order.getId())
                            .set("state", order.getState() + 1)
                    );
                }
                lock.countDown();
            });
        }
        // 记录结束时间戳
        try {
            lock.await(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        log.info("end: " + endTime);
        // 计算消耗时间
        log.info("总消耗时间：" + (endTime - startTime));
    }

    public void test1mDelete() {
        // 查询数据总量
        // 记录开始时间戳

        // 循环1m次：随机生成一个数字<总量，删除对应行，总量 - 1
        // 记录结束时间戳
        // 计算消耗时间
    }


    private Order getTestOrder(List<User> users) {
        int i = new Random().nextInt(users.size());
        return new Order().setState(0).setUserId(users.get(i).getId());
    }

    private User getTestUser() {
        String account = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        return new User(null, account, account, account, null, null, null);
    }

    private Good getTestGood() {
        String goodName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        return new Good(null, goodName, new Random().nextInt(500000), 0, null, null);
    }
}
