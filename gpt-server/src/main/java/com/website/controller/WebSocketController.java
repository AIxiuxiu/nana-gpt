package com.website.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.controller.base.BaseController;
import com.website.model.Result;
import com.website.websocket.endpoint.WSMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webscolet")
@CrossOrigin
@Api(tags = "webscolet")
public class WebSocketController extends BaseController {

    @Autowired
    private WSMessageService wsMessageService;

    @GetMapping("/onlineCount")
    @ApiOperation("查询在线数量")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParam(name = "type", value = "0:连接数，1用户数", paramType = "query", dataType = "Integer")
    public Result<Integer> onlineCount(Integer type) {
        try {
            if (type == 0) {
                return success(WSMessageService.ONLINE_COUNT.get());
            } else {
                return success(WSMessageService.WEB_SOCKET_MAP.size());
            }
        } catch (Exception e) {
            log.error("调用接口【online】失败：", e);
            return success(0);
        }
    }

    @GetMapping("/sendMessage")
    @ApiOperation("发送信息")
    @ApiOperationSupport(order = 2)
    @ApiImplicitParam(name = "userId", value = "用户ID", paramType = "query", dataType = "String")
    public Result<String> sendMessage(String userId, String message) {
        try {
            wsMessageService.pushMessage(message, userId);
            return success("发送信息信息成功");
        } catch (Exception e) {
            log.error("调用接口【sendMessage】失败：", e);
            return failByCatch("发送信息信息失败", e);
        }
    }

}
