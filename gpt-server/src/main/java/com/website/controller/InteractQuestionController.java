package com.website.controller;

import cn.hutool.poi.word.Word07Writer;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.knuddels.jtokkit.api.EncodingType;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.annotation.PassToken;
import com.website.controller.base.BaseController;
import com.website.entity.Interact;
import com.website.entity.InteractContents;
import com.website.entity.InteractQuestion;
import com.website.model.Result;
import com.website.service.InteractQuestionService;
import com.website.service.InteractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Api(tags="互动问答问题相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/interactQuestion")
@Slf4j
public class InteractQuestionController extends BaseController {

    @Autowired
    InteractQuestionService questionService;

    @Autowired
    InteractService interactService;


    @GetMapping("/list")
    @ApiOperation("问题列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "interactId", value = "互动问答id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "searchValue", value = "搜索字段", paramType = "query", dataType = "String"),
    })
    public Result<IPage<InteractQuestion>> list(String interactId, Integer page, Integer pageSize, String searchValue) {
        MPJLambdaWrapper<InteractQuestion> wrapper = JoinWrappers.lambda(InteractQuestion.class)
                .select(InteractQuestion::getId,InteractQuestion::getContentId,InteractQuestion::getQuestion,InteractQuestion::getUserId,InteractQuestion::getTokens,InteractQuestion::getCreateTime)
                .leftJoin(InteractContents.class, InteractContents::getId, InteractQuestion::getContentId)
                .leftJoin(Interact.class, Interact::getId, InteractContents::getInteractId)
                .eq(Interact::getId, interactId);

        if (StringUtils.isNotEmpty(searchValue)) {
            wrapper.like(InteractQuestion::getQuestion, searchValue);
        }
        wrapper.orderByAsc(InteractContents::getId);
        wrapper.orderByDesc(InteractQuestion::getCreateTime);
        Page<InteractQuestion> inputDataPage = new Page<>();
        inputDataPage.setSize(pageSize);
        inputDataPage.setCurrent(page);
        IPage<InteractQuestion> pageList = questionService.page(inputDataPage, wrapper);
        return success(pageList);
    }


    @PostMapping("/addOrUpdate")
    @ApiOperation("添加或修改问题")
    @ApiOperationSupport(order = 3)
    public Result<InteractQuestion> addOrUpdate(@RequestBody InteractQuestion question) {
        try {
            if (question.getId() == null) {
                if (question.getContentId() == null) {
                    return fail("添加问答对，文档ContentId不能为空！");
                }
            }
            if (StringUtils.isEmpty(question.getQuestion())) {
                return fail("问题不能为空！");
            }
            question.setUserId(Long.valueOf(getUserId()));
            question.setTokens(TikTokensUtil.tokens(EncodingType.CL100K_BASE, question.getQuestion()));
            questionService.saveOrUpdate(question);
            return success(question);
        } catch (Exception e) {
            log.error("添加或修改问题【addOrUpdate】失败：", e);
            return failByCatch("添加或修改问题失败", e);
        }
    }

    @GetMapping("/remove")
    @ApiOperation("删除问题")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "id", value = "问题id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<InteractQuestion> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            Boolean result = questionService.remove(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用删除问题接口【remove】失败：", e);
            return failByCatch("删除问题失败", e);
        }
    }


    @ApiOperation("导出问答对")
    @GetMapping("/export")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "interactId", value = "互动问答id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "fileName", value = "文件名称", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "fileType", value = "文件类型", required = true, paramType = "query", dataType = "String"),
    })
    @PassToken
    public void export(HttpServletResponse response, String interactId, String fileName, String fileType) {
        try {
            if (StringUtils.isEmpty(fileName)) {
                Interact interact = interactService.getById(interactId);
                if (interact == null) {
                    fileName = "问题";
                } else {
                    fileName = interact.getCompanyShortName() + "（" + interact.getCompanyCode() + "）";
//                    fileName = interact.getCompanyShortName() + interact.getStartDate()  + "至" + interact.getEndDate()+ "问题";
                }
            }

            MPJLambdaWrapper<InteractQuestion> wrapper = JoinWrappers.lambda(InteractQuestion.class)
                    .select(InteractQuestion::getId, InteractQuestion::getQuestion)
                    .leftJoin(InteractContents.class, InteractContents::getId, InteractQuestion::getContentId)
                    .leftJoin(Interact.class, Interact::getId, InteractContents::getInteractId)
                    .eq(Interact::getId, interactId);
            wrapper.orderByAsc(InteractContents::getId);
            wrapper.orderByDesc(InteractQuestion::getCreateTime);

            List<InteractQuestion> questions = questionService.list(wrapper);

            if ("excel".equals(fileType)) {
                fileName += ".xlsx";
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("GBK"), "ISO8859_1"));

                ServletOutputStream out = response.getOutputStream();
                ExcelWriter excelWriter = EasyExcel
                        .write(out)
//                        .registerWriteHandler(new CustomCellWeightWeightConfig(140))
                        .build();

                // 内容样式
                WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
                contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                // 设置自动换行，前提内容中需要加「\n」才有效
                contentWriteCellStyle.setWrapped(true);
                // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
                HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                        new HorizontalCellStyleStrategy(null, contentWriteCellStyle);
                WriteSheet sheet = EasyExcel
                        .writerSheet(0, "问答对")
                        .head(InteractQuestion.class)
                        .registerWriteHandler(horizontalCellStyleStrategy)
                        .build();

                excelWriter.write(questions, sheet);
                excelWriter.finish();
                out.flush();
                out.close();
            } else if ("word".equals(fileType)) {
                fileName += ".docx";
                Word07Writer writer = new Word07Writer();
                AtomicInteger index = new AtomicInteger(1);

                for (InteractQuestion question: questions) {
                    writer.addText(new Font("宋体", Font.PLAIN, 12), index.getAndIncrement() + "、" + question.getQuestion());
                    writer.addText(new Font("宋体", Font.PLAIN, 12), "");
                }

                response.setContentType("application/msword");
                response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("GBK"), "ISO8859_1"));
                ServletOutputStream out = response.getOutputStream();
                writer.flush(out, true);
                out.close();
                writer.close();
            } else {
                fileName += ".txt";
                AtomicInteger index = new AtomicInteger(1);
                String text = questions.stream().map(qa -> index.getAndIncrement() + "、" + qa.getQuestion() + "\r\n" + "\r\n").collect(Collectors.joining());

                response.setContentType("text/plain");
                response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("GBK"), "ISO8859_1"));
                ServletOutputStream outStr = response.getOutputStream();
                BufferedOutputStream buff = new BufferedOutputStream(outStr);
                buff.write(text.getBytes(StandardCharsets.UTF_8));
                buff.flush();
                buff.close();
            }

        } catch (Exception e) {
            log.error("调用接口【export】失败：", e);
        }
    }
}
