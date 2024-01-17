package com.website.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.controller.base.BaseController;
import com.website.entity.KbSetting;
import com.website.model.Result;
import com.website.service.KbSettingService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  知识库设置
 * </p>
 *
 * @author ahl
 * @since 2023-07-27
 */
@Slf4j
@RestController
@RequestMapping("/kbSetting")
public class KbSettingController extends BaseController {

    @Autowired
    KbSettingService kbSettingService;

    @GetMapping("/detail")
    @ApiOperation("知识库设置")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParam(name = "kbId", value = "知识库ID", paramType = "query", dataType = "String")
    public Result<KbSetting> detail(String kbId) {
        KbSetting kbSetting = kbSettingService.getSettingByKbId(kbId);
        return success(kbSetting);
    }


    @PostMapping("/update")
    @ApiOperation("更新知识库设置")
    @ApiOperationSupport(order = 2)
    public Result<KbSetting> update(@RequestBody KbSetting kbSetting) {
        try {
            if (kbSetting.getId() == null) {
                return fail("ID不能为空！");
            }
            kbSettingService.updateById(kbSetting);
            return success(kbSetting);
        } catch (Exception e) {
            log.error("调用更新知识库设置接口【update】失败：", e);
            return failByCatch("更新知识库设置失败", e);
        }
    }



}
