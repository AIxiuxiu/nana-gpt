package com.website.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.entity.Interact;
import com.website.enums.Prompt;
import com.website.model.Result;
import com.website.service.InteractService;
import com.website.service.PromptService;
import com.website.service.cache.KbCacheService;
import com.website.service.http.DataCenterHttp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.website.controller.base.BaseController;

import java.util.Map;

@Api(tags="互动问答相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/interact")
@Slf4j
public class InteractController extends BaseController {

    @Autowired
    InteractService interactService;

    @Autowired
    DataCenterHttp dataCenterHttp;

    @Autowired
    private PromptService promptService;

    @Autowired
    private KbCacheService kbCacheService;


    @GetMapping("/list")
    @ApiOperation("互动问答列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "searchValue", value = "互动问答搜索", paramType = "query", dataType = "String")
    })
    public Result<IPage<Interact>> list(Integer page, Integer pageSize, String searchValue) {
        LambdaQueryWrapper<Interact> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(searchValue)) {
            queryWrapper.like(Interact::getCompanyName, searchValue).or().like(Interact::getCompanyShortName, searchValue).or().like(Interact::getCompanyCode, searchValue);
        }
        queryWrapper.eq(Interact::getStatus, 1);
        queryWrapper.orderByDesc(Interact::getCreateTime);

        Page<Interact> inputDataPage = new Page<>();
        inputDataPage.setSize(pageSize);
        inputDataPage.setCurrent(page);
        IPage<Interact> pageList = interactService.page(inputDataPage, queryWrapper);
        return success(pageList);
    }

    @PostMapping("/add")
    @ApiOperation("新增互动问答")
    @ApiOperationSupport(order = 2)
    public Result<Interact> add(@RequestBody Interact interact) {
        try {
            interact.setUserId(Long.valueOf(getUserId()));

            Map<String, Object> orgMap = kbCacheService.getOrgMap(interact.getOrgId());
            if (orgMap == null) {
                return fail("获取公司信息失败！");
            }
            interact.setCompanyName(orgMap.get("NAME").toString());
            interact.setCompanyCode(orgMap.get("CODE").toString());
            interact.setCompanyShortName(orgMap.get("SHORTNAME").toString());

            if (StringUtils.isEmpty(interact.getQPrompt())) {
                interact.setQPrompt(promptService.getByTopic(Prompt.QUESTION.topic));
            }
            interactService.save(interact);
            String result = interactService.getNewsList(interact);
            if (StringUtils.isNotEmpty(result)) {
                interactService.removeById(interact);
                return fail(result);
            }

            return success(interact);
        } catch (Exception e) {
            log.error("调用新增互动问答接口【add】失败：", e);
            return failByCatch("新增互动问答失败", e);
        }
    }

    @PostMapping("/update")
    @ApiOperation("更新互动问答")
    @ApiOperationSupport(order = 2)
    public Result<Interact> update(@RequestBody Interact interact) {
        try {
            if (interact.getId() == null) {
                return fail("ID不能为空！");
            }
            interact.setUserId(Long.valueOf(getUserId()));
            interactService.updateById(interact);
            return success(interact);
        } catch (Exception e) {
            log.error("调用更新互动问答接口【update】失败：", e);
            return failByCatch("更新互动问答失败", e);
        }
    }



    @GetMapping("/remove")
    @ApiOperation("删除互动问答")
    @ApiOperationSupport(order = 5)
    @ApiImplicitParam(name = "id", value = "互动问答id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<Interact> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", Long.valueOf(id));
            updateWrapper.set("userId", Long.valueOf(getUserId()));
            updateWrapper.set("status", 0);
            Boolean result = interactService.update(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用删除互动问答接口【remove】失败：", e);
            return failByCatch("删除互动问答失败", e);
        }
    }

    @GetMapping("/detail")
    @ApiOperation("互动问答详情")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParam(name = "id", value = "互动问答ID", paramType = "query", dataType = "String")
    public Result<Interact> detail(String id) {
        Interact interact = interactService.getById(id);
        return success(interact);
    }
}
