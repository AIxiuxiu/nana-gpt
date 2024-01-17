package com.website.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.annotation.PassToken;
import com.website.controller.base.BaseController;
import com.website.entity.Dictionary;
import com.website.model.Result;
import com.website.search.*;
import com.website.service.DictionaryService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 公共api
 */
@Slf4j
@Api(tags="公共api")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/commons")
public class CommonsController extends BaseController {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    SearchService searchService;

    /**
     * 字典查询
     */
    @PassToken
    @ApiOperation("字典查询")
    @ApiOperationSupport(order = 1)
    @GetMapping(value = "/dictionary")
    @ApiImplicitParam(name = "type", value = "1：机构类型 2：公司类型 3视频类型 4 培训类型",  paramType = "query", dataType = "String")
    public Result<List<Map<String, Object>>> getDictionary(@ApiParam String type)  {
        QueryWrapper<Dictionary> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name");
        queryWrapper.eq("type", type);
        queryWrapper.orderByAsc("id");
        List<Map<String, Object>> dictionaries = dictionaryService.listMaps(queryWrapper);
        return success(dictionaries);
    }

    @PassToken
    @ApiOperation("搜索查询")
    @ApiOperationSupport(order = 1)
    @GetMapping(value = "/search")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "query", value = "搜索参数",  paramType = "query", dataType = "String"),
        @ApiImplicitParam(name = "type", value = "搜索类型", defaultValue = "", paramType = "query", dataType = "String")
    })
    public Result<List<SearchResult>> searchList(@ApiParam String query, @ApiParam String type)  {
        return success(searchService.searchListByType(type, query));

    }

    @PassToken
    @ApiOperation("搜索查询-包含正文")
    @ApiOperationSupport(order = 1)
    @GetMapping(value = "/searchContent")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "搜索参数",  paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "type", value = "搜索类型", defaultValue = "", paramType = "query", dataType = "String")
    })
    public Result<List<SearchResult>> searchContent(@ApiParam String query, @ApiParam String type)  {
        return success(searchService.searchContentByType(type, query));
    }

}
