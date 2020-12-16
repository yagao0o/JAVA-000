package io.kimmking.rpcfx.demo.provider;

import io.kimmking.rpcfx.demo.api.User;
import io.kimmking.rpcfx.demo.api.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public User findById(Integer id) {
        System.out.println("req here");
        return new User(id, "KK" + System.currentTimeMillis());
    }
}
