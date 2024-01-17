package com.website.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.unfbx.chatgpt.entity.billing.BillingUsage;
import com.unfbx.chatgpt.entity.billing.Subscription;
import com.website.annotation.PassToken;
import com.website.controller.base.BaseController;
import com.website.entity.ChatRecord;
import com.website.model.Result;
import com.website.service.ChatRecordService;
import com.website.service.ChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


/**
 * @Author: ahl
 * @Date: 2023/3/9 15:44
 */
@Slf4j
@Api(tags="chat api")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/chat")
public class ChatController extends BaseController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRecordService chatRecordService;

    /**
     * 对话历史记录
     *
     * @param kbId
     * @return
     */
    @ApiOperation("对话历史记录")
    @ApiOperationSupport(order = 1)
    @GetMapping(value = "/history")
    @ApiImplicitParam(name = "kbId", value = "知识库ID",  paramType = "query", dataType = "String")
    public Result<List<ChatRecord>> history(String kbId) {
        LambdaQueryWrapper<ChatRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecord::getKbId, kbId);
        queryWrapper.eq(ChatRecord::getUserId, getUserId());
        queryWrapper.eq(ChatRecord::getStatus, 1);
        queryWrapper.ge(ChatRecord::getCreateTime, DateUtil.today());
        queryWrapper.orderByDesc(ChatRecord::getCreateTime);
        queryWrapper.last("limit 100");
        List<ChatRecord> recordList = chatRecordService.list(queryWrapper);
        Collections.reverse(recordList);
        return success(recordList);
    }


    @PostMapping("/saveChat")
    @ApiOperation("保存聊天信息")
    @ApiOperationSupport(order = 3)
    public Result<ChatRecord> saveChat(@RequestBody ChatRecord chatRecord) {
        try {
            chatRecord.setUserId(getUserId());
            chatRecordService.save(chatRecord);
            return success(chatRecord);
        } catch (Exception e) {
            log.error("调用保存聊天信息接口【saveChat】失败：", e);
            return failByCatch("保存聊天信息失败", e);
        }
    }

    @GetMapping("/cleanHistory")
    @ApiOperation("清空历史记录")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "kbId", value = "知识库ID",  paramType = "query", dataType = "String")
    public Result<Boolean> cleanHistory(String kbId) {
        try {
            UpdateWrapper<ChatRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("kbId", kbId);
            updateWrapper.set("userId", getUserId());
            updateWrapper.set("status", 0);
            Boolean result = chatRecordService.update(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用保存聊天信息接口【saveChat】失败：", e);
            return failByCatch("保存聊天信息失败", e);
        }
    }


    /**
     * 调用openai的ChatGPT接口实现单论对话
     *
     * @param message
     * @return
     */
    @ApiOperation("单论对话")
    @ApiOperationSupport(order = 1)
    @GetMapping(value = "/oneShot")
    @ApiImplicitParam(name = "message", value = "问题",  paramType = "query", dataType = "String")
    @PassToken
    public Result<ChatRecord> oneShot(String message) {
        if (StringUtils.isEmpty(message)) {
            return fail("问题不能为空");
        }
        ChatRecord chatRecord = chatService.oneShotChat(message);
        return success(chatRecord);
    }

    /**
     * 调用openai的ChatGPT接口实现单论对话
     *
     * @param message
     * @return
     */
    @ApiOperation("单论对话-流式输出")
    @ApiOperationSupport(order = 1)
    @GetMapping(value = "/sseShot")
    @ApiImplicitParam(name = "type", value = "问题",  paramType = "query", dataType = "String")
    public SseEmitter sseShot(String message) {
        return chatService.sseShotChat(message);
    }

    /**
     * 查询openai apikey的余额
     * @return
     */
    @ApiOperation("openai余额")
    @ApiOperationSupport(order = 13)
    @GetMapping(value = "/subscription")
    public Result<Subscription> getSubscription() {
        Subscription subscription = chatService.subscription();
        return success(subscription);
    }

    /**
     * 查询openai apikey的余额
     * @return
     */
    @ApiOperation("openai账单")
    @ApiOperationSupport(order = 14)
    @GetMapping(value = "/billingUsage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "开始时间", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", paramType = "query", dataType = "String"),
    })
    @PassToken
    public Result<BillingUsage> getBillingUsage(String startDate, String endDate) {
        LocalDate startLocalDate = LocalDate.now().minusDays(30);
        if (StringUtils.isNotEmpty(startDate)) {
            startLocalDate = LocalDateTimeUtil.parseDate(startDate);
        }
        LocalDate endLocalDate = LocalDate.now().plusDays(1);
        if (StringUtils.isNotEmpty(startDate)) {
            endLocalDate = LocalDateTimeUtil.parseDate(endDate);
        }
        BillingUsage billingUsage = chatService.billingUsage(startLocalDate, endLocalDate);
        return success(billingUsage);
    }
}
