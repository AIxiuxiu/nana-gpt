package com.website.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.controller.base.BaseController;
import com.website.entity.PromptEntity;
import com.website.model.Result;
import com.website.service.PromptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共api
 */
@Slf4j
@Api(tags="prompt相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/prompt")
public class PromptController extends BaseController {

    @Autowired
    PromptService promptService;

    @GetMapping("/list")
    @ApiOperation("prompt列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParam(name = "target", value = "目标", paramType = "query", dataType = "Integer", defaultValue = "0")
    public Result<List<PromptEntity>> list(int target) {
        LambdaQueryWrapper<PromptEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromptEntity::getTarget, target);
        queryWrapper.orderByDesc(PromptEntity::getCreateTime);
        List<PromptEntity> promptEntities = promptService.list(queryWrapper);
        return success(promptEntities);
    }

    @PostMapping("/update")
    @ApiOperation("修改prompt")
    @ApiOperationSupport(order = 2)
    public Result<PromptEntity> update(@RequestBody PromptEntity promptEntity) {
        try {
            if (promptEntity.getId() == null) {
                return fail("id不能为空！");
            }
            if (StringUtils.isEmpty(promptEntity.getContent())) {
                return fail("prompt内容不能为空！");
            }
            promptService.updateById(promptEntity);
            if (promptEntity.getTarget() == 0) {
                promptService.load();
            }
            return success(promptEntity);
        } catch (Exception e) {
            log.error("修改prompt【update】失败：", e);
            return failByCatch("修改prompt失败", e);
        }
    }


    @PostMapping("/add")
    @ApiOperation("添加提示词")
    @ApiOperationSupport(order = 3)
    public Result<PromptEntity> add(@RequestBody PromptEntity prompt) {
        try {
            prompt.setTarget(1);
            promptService.save(prompt);
            return success(prompt);
        } catch (Exception e) {
            log.error("添加提示词【add】失败：", e);
            return failByCatch("添加提示词失败", e);
        }
    }

    @GetMapping("/remove")
    @ApiOperation("删除提示词")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "id", value = "提示词id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<PromptEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            updateWrapper.set("isDeleted", 1);
            Boolean result = promptService.update(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用删除提示词接口【remove】失败：", e);
            return failByCatch("删除提示词失败", e);
        }
    }

}
