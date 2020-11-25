package com.qingyi.week6.homework.service.impl;

import com.qingyi.week6.homework.entity.User;
import com.qingyi.week6.homework.mapper.UserMapper;
import com.qingyi.week6.homework.service.IUserService;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
