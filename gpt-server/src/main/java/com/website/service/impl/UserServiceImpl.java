package com.website.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.website.entity.User;
import com.website.mapper.UserMapper;
import com.website.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author ahl
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
