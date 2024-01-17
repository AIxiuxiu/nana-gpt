package com.website.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.annotation.OperationLogger;
import com.website.annotation.PassToken;
import com.website.controller.base.BaseController;
import com.website.entity.User;
import com.website.model.OperatingType;
import com.website.model.Result;
import com.website.service.UserService;
import com.website.util.JwtUtils;
import com.website.vo.LoginVo;
import com.website.vo.UserInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 公共api
 */
@Slf4j
@Api(tags="用户相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    UserService userService;
    /**
     * 登录
     */
    @ApiOperation("登录")
    @ApiOperationSupport(order = 1)
    @PostMapping(value = "/login")
    @ApiImplicitParam(name = "username", value = "用户名",  paramType = "query", dataType = "String")
    @OperationLogger(type = OperatingType.LOGIN, value="#{login.username}")
    @PassToken
    public Result<UserInfoVo> getDictionary(@RequestBody LoginVo login)  {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", login.getUsername());

        User user = userService.getOne(queryWrapper);
        if (user == null || user.getId() == null) {
            return fail("用户不存在！");
        } if (!user.getPassword().equals(login.getPassword())) {
            return fail("用户密码错误！");
        } else {
            UserInfoVo userInfo = new UserInfoVo()
                    .setId(String.valueOf(user.getId()))
                    .setUsername(user.getUsername());
            String jwtToken = JwtUtils.doGenerateToken(userInfo);
            userInfo.setToken(jwtToken);
            return success(userInfo);
        }
    }


    @GetMapping("/setting")
    @ApiOperation("用户设置")
    @ApiOperationSupport(order = 2)
    public Result<User> detail() {
        User user = userService.getById(getUserId());
        user.setPassword("");
        return success(user);
    }


    @PostMapping("/update")
    @ApiOperation("更新用户设置")
    @ApiOperationSupport(order = 3)
    public Result<User> update(@RequestBody User user) {
        try {
            user.setId(Long.valueOf(getUserId()));
            user.setUsername(null);
            user.setPassword(null);
            userService.updateById(user);
            return success(user);
        } catch (Exception e) {
            log.error("调用更新用户设置接口【update】失败：", e);
            return failByCatch("更新用户设置失败", e);
        }
    }

}
