package com.qingyi.week6.homework.service.impl;

import com.qingyi.week6.homework.entity.Order;
import com.qingyi.week6.homework.mapper.OrderMapper;
import com.qingyi.week6.homework.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2020-11-25
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}
