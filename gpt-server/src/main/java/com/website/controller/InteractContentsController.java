package com.website.controller;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.config.TaskExecutorConfig;
import com.website.entity.*;
import com.website.enums.Prompt;
import com.website.model.Result;
import com.website.service.InteractContentsService;
import com.website.service.InteractService;
import com.website.service.PromptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.website.controller.base.BaseController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Api(tags="互动问答相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/interactContents")
@Slf4j
public class InteractContentsController extends BaseController {

    @Autowired
    InteractContentsService interactContentsService;

    @Autowired
    InteractService interactService;

    @Autowired
    private PromptService promptService;

    @GetMapping("/list")
    @ApiOperation("新闻内容列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "interactId", value = "互动问答id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "searchValue", value = "搜索字段", paramType = "query", dataType = "String"),
    })
    public Result<IPage<InteractContents>> list(String interactId, Integer page, Integer pageSize, String searchValue) {
        LambdaQueryWrapper<InteractContents> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(InteractContents::getId,InteractContents::getInteractId,InteractContents::getUserId,InteractContents::getTitle,InteractContents::getContent,InteractContents::getPublishDate,InteractContents::getSummary,InteractContents::getTokens,InteractContents::getCreateTime);
        if (StringUtils.isNotEmpty(interactId)) {
            queryWrapper.eq(InteractContents::getInteractId, interactId);
        }
        if (StringUtils.isNotEmpty(searchValue)) {
            queryWrapper.like(InteractContents::getTitle, searchValue);
        }
        queryWrapper.orderByDesc(InteractContents::getPublishDate);
        Page<InteractContents> inputDataPage = new Page<>();
        inputDataPage.setSize(pageSize);
        inputDataPage.setCurrent(page);
        IPage<InteractContents> pageList = interactContentsService.page(inputDataPage, queryWrapper);
        return success(pageList);
    }


    @PostMapping("/addOrUpdate")
    @ApiOperation("添加或修改文档内容")
    @ApiOperationSupport(order = 3)
    public Result<InteractContents> addOrUpdate(@RequestBody InteractContents interactContents) {
        try {
            if (interactContents.getId() == null) {
                if (interactContents.getInteractId() == null) {
                    return fail("添加文档内容，互动问答interactId不能为空！");
                }
            }
            if (StringUtils.isEmpty(interactContents.getContent())) {
                return fail("内容不能为空！");
            }
            interactContents.setUserId(Long.valueOf(getUserId()));
            interactContents.setHasQ(0);
            interactContents.setTokens(TikTokensUtil.tokens(ChatCompletion.Model.GPT_3_5_TURBO_16K.getName(), interactContents.getContent()));
            interactContentsService.saveOrUpdate(interactContents);
            return success(interactContents);
        } catch (Exception e) {
            log.error("添加或修改新闻内容【addOrUpdate】失败：", e);
            return failByCatch("添加或修改新闻内容失败", e);
        }
    }

    @GetMapping("/remove")
    @ApiOperation("删除新闻内容")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "id", value = "新闻内容id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<InteractContents> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            Boolean result = interactContentsService.remove(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用删除新闻内容接口【remove】失败：", e);
            return failByCatch("删除新闻内容失败", e);
        }
    }

    @GetMapping("/detail")
    @ApiOperation("详情")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParam(name = "id", value = "ID", paramType = "query", dataType = "String")
    public Result<InteractContents> detail(String kbId) {
        InteractContents interactContents = interactContentsService.getById(kbId);
        return success(interactContents);
    }

    @GetMapping("/createQuestion")
    @ApiOperation("新闻内容生成问答对")
    @ApiOperationSupport(order = 14)
    @ApiImplicitParam(name = "interactId", value = "互动问答id", paramType = "query", dataType = "String")
    public Result<Boolean> createQuestion(String interactId) {
        try {
            if (StringUtils.isEmpty(interactId)) {
                return fail("interactId不能为空！");
            }
            Interact interact = interactService.getById(interactId);
            if (interact.getQuestionStatus() == 1) {
                return fail("互动问答生成问题中，请勿重复执行！");
            }

            LambdaQueryWrapper<InteractContents> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(InteractContents::getInteractId, interactId);
            long total = interactContentsService.count(queryWrapper);
            queryWrapper.select(InteractContents::getId,InteractContents::getContent,  InteractContents::getSummary, InteractContents::getHasQ);
            queryWrapper.ne(InteractContents::getHasQ, 1);
            List<InteractContents> interactContents = interactContentsService.list(queryWrapper);
            if (interactContents.size() == 0) {
                return fail("无生成题对的内容！");
            }

            if (StringUtils.isEmpty(interact.getQPrompt() )) {
                interact.setQPrompt(promptService.getByTopic(Prompt.QUESTION.topic));
            }
            interactService.setLastPrompt(interact);

            // 开始生成问答对
            UpdateWrapper<Interact> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", interactId);
            updateWrapper.set("userId", getUserId());
            updateWrapper.set("questionStatus", 1);
            interactService.update(updateWrapper);

            AtomicInteger atomicTotal = new AtomicInteger(interactContents.size());
            // 需要根据chatGPT速率限制设置 https://platform.openai.com/docs/guides/rate-limits/overview
            List<List<InteractContents>> splitContents = ListUtil.splitAvg(interactContents, TaskExecutorConfig.cpuNum);
            for (List<InteractContents> contents : splitContents) {
                if (contents.size() > 0) {
                    interactContentsService.questionContents(contents, atomicTotal, interact, (int) total, getUserId());
                }
            }
            return success(true);
        } catch (Exception e) {
            log.error("新闻内容生成问题接口【createQA】失败：", e);
            return failByCatch("新闻内容生成问题失败", e);
        }
    }


    @GetMapping("/questionStatus")
    @ApiOperation("问题生成状态")
    @ApiOperationSupport(order = 5)
    @ApiImplicitParam(name = "interactId", value = "互动问答id", paramType = "query", dataType = "String")
    public Result<QuestionStatus> questionStatus(String interactId) {
        Interact interact = interactService.getById(interactId);
        QuestionStatus questionStatus = new QuestionStatus().setInteractId(interactId);
        questionStatus.setQuestionStatus(interact.getQuestionStatus());
        questionStatus.setStatus("");
        if (interact.getQuestionStatus() == 0) {
            //未开始
            questionStatus.setPercentage(0);
            return success(questionStatus);
        } else if (interact.getQuestionStatus() == 2) {
            // 已完成
            questionStatus.setStatus("success");
            questionStatus.setPercentage(100);
            return success(questionStatus);
        } else {
            if (interact.getQuestionStatus() == 3) {
                // 错误
                questionStatus.setStatus("exception");
            }
            Map<String, Object> qCount = interactContentsService.getQCount(interactId);
            int total =  Integer.parseInt(qCount.get("total").toString());
            int completed =  Integer.parseInt(qCount.get("completed").toString());
            questionStatus.setPercentage(completed*100/total);
            return success(questionStatus);
        }
    }


}
