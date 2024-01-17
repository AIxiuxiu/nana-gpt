package com.website.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.controller.base.BaseController;
import com.website.entity.KnowledgeBase;
import com.website.model.Result;
import com.website.service.KnowledgeBaseService;
import com.website.util.MilvusClientUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 知识库 前端控制器
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Api(tags="知识库相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/knowledgeBase")
@Slf4j
public class KnowledgeBaseController extends BaseController {

    @Autowired
    KnowledgeBaseService knowledgeBaseService;

    @GetMapping("/list")
    @ApiOperation("知识库列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParam(name = "searchValue", value = "知识库搜索", paramType = "query", dataType = "String")
    public Result<List<KnowledgeBase>> list(String searchValue) {
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(searchValue)) {
            queryWrapper.like(KnowledgeBase::getKbName, searchValue).or().like(KnowledgeBase::getCompanyName, searchValue).or().like(KnowledgeBase::getCompanyCode, searchValue);
        }
        queryWrapper.eq(KnowledgeBase::getStatus, 1);
        queryWrapper.orderByDesc(KnowledgeBase::getCreateTime);
        List<KnowledgeBase> list = knowledgeBaseService.list(queryWrapper);
        return success(list);
    }

    @PostMapping("/addOrUpdate")
    @ApiOperation("新增或更新知识库")
    @ApiOperationSupport(order = 2)
    public Result<KnowledgeBase> addOrUpdate(@RequestBody KnowledgeBase knowledgeBase) {
        try {
            knowledgeBase.setUserId(Long.valueOf(getUserId()));
            knowledgeBaseService.saveOrUpdate(knowledgeBase);
            String collectionName =  "kb_"+ knowledgeBase.getId();
            //创建向量库
            MilvusClientUtil.createCollection(collectionName);
            //创建索引
            MilvusClientUtil.createIndex(collectionName);
            return success(knowledgeBase);
        } catch (Exception e) {
            log.error("调用新增或更新知识库接口【addOrUpdate】失败：", e);
            return failByCatch("新增或更新知识库失败", e);
        }
    }

    @GetMapping("/remove")
    @ApiOperation("删除知识库")
    @ApiOperationSupport(order = 5)
    @ApiImplicitParam(name = "id", value = "知识库id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<KnowledgeBase> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            updateWrapper.set("userId", getUserId());
            updateWrapper.set("status", 0);
            Boolean result = knowledgeBaseService.update(updateWrapper);
            if (result) {
                MilvusClientUtil.dropIndex("kb_"+ id);
                MilvusClientUtil.dropCollection("kb_"+ id);
            }
            return success(result);
        } catch (Exception e) {
            log.error("调用删除知识库接口【remove】失败：", e);
            return failByCatch("删除知识库失败", e);
        }
    }

    @GetMapping("/detail")
    @ApiOperation("知识库详情")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParam(name = "id", value = "知识库ID", paramType = "query", dataType = "String")
    public Result<KnowledgeBase> detail(String id) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(id);
        return success(knowledgeBase);
    }

}
