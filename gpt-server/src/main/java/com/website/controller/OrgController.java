package com.website.controller;

import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.website.annotation.PassToken;
import com.website.controller.base.BaseController;
import com.website.model.Result;
import com.website.service.cache.KbCacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/org")
@CrossOrigin
@Api(tags = "上市公司实体")
@ApiSort(100)
public class OrgController extends BaseController {

    private final KbCacheService kbCacheService;

    public OrgController(KbCacheService kbCacheService) {
        this.kbCacheService = kbCacheService;
    }

    @ApiOperation("查询上市公司")
    @GetMapping("/searchListed")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchValue", value = "搜索关键字", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "num", value = "返回数量", paramType = "query", dataType = "Integer", defaultValue = "10"),
    })
    @PassToken
    public Result<List<Map<String, String>>> searchListedCompany(String searchValue,
                                                                 @RequestParam(value = "num", defaultValue = "10") Integer num) {
        try {
            List<Map<String, String>> result = new ArrayList<>();

            Set<String> codes = kbCacheService.getOrgCodeMap().keySet();

            if (!codes.isEmpty()) {
                for (String code : codes) {
                    Map<String, Object> orgMap = kbCacheService.getOrgCodeMap().get(code);
                    if (orgMap == null) {
                        continue;
                    }
                    String name = orgMap.get("NAME").toString();
                    String oid = orgMap.get("OID").toString();
                    String pinyin = orgMap.get("pinyin").toString();
                    String pinyinLetter = orgMap.get("pinyinLetter").toString();
                    if (StringUtils.isEmpty(searchValue) || name.contains(searchValue) || code.contains(searchValue) || pinyin.contains(searchValue) || pinyinLetter.contains(searchValue)) {
                        Map<String, String> companyMap = new HashMap<>(3);
                        companyMap.put("code", code);
                        companyMap.put("name", name);
                        companyMap.put("oid", oid);
                        result.add(companyMap);
                    }
                    if (result.size() >= num) {
                        break;
                    }
                }
            }
            return success(result);
        } catch (Exception e) {
            log.error("调用接口【searchListedCompany】失败：", e);
            return failByCatch("查询上市公司失败!", e);
        }
    }
}
