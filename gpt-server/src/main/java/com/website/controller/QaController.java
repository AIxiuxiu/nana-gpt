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
import com.website.entity.DocContents;
import com.website.entity.Document;
import com.website.entity.Qa;
import com.website.model.Result;
import com.website.service.DocumentService;
import com.website.service.QaService;
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

/**
 * <p>
 *  QA 前端控制器
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Api(tags="QA相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/qa")
@Slf4j
public class QaController extends BaseController {
    @Autowired
    QaService qaService;

    @Autowired
    DocumentService documentService;

    @GetMapping("/list")
    @ApiOperation("问答对列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "docId", value = "文档id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "searchValue", value = "搜索字段", paramType = "query", dataType = "String"),
    })
    public Result<IPage<Qa>> list(String docId, Integer page, Integer pageSize, String searchValue) {
        MPJLambdaWrapper<Qa> wrapper = JoinWrappers.lambda(Qa.class)
                .select(Qa::getId,Qa::getContentId,Qa::getQuestion,Qa::getAnswer,Qa::getUserId,Qa::getEmbedStatus,Qa::getTokens,Qa::getCreateTime)
                .leftJoin(DocContents.class, DocContents::getId, Qa::getContentId)
                .leftJoin(Document.class, Document::getId, DocContents::getDocId)
                .eq(Document::getId, docId);

        if (StringUtils.isNotEmpty(searchValue)) {
            wrapper.like(Qa::getQuestion, searchValue);
        }
        wrapper.orderByAsc(Qa::getId);
        wrapper.orderByDesc(Qa::getCreateTime);
        Page<Qa> inputDataPage = new Page<>();
        inputDataPage.setSize(pageSize);
        inputDataPage.setCurrent(page);
        IPage<Qa> pageList = qaService.page(inputDataPage, wrapper);
        return success(pageList);
    }


    @PostMapping("/addOrUpdate")
    @ApiOperation("添加或修改QA问答对")
    @ApiOperationSupport(order = 3)
    public Result<Qa> addOrUpdate(@RequestBody Qa qa) {
        try {
            if (qa.getId() == null) {
                if (qa.getContentId() == null) {
                    return fail("添加问答对，文档ContentId不能为空！");
                }
            }
            if (StringUtils.isEmpty(qa.getQuestion()) || StringUtils.isEmpty(qa.getAnswer())) {
                return fail("问答对内容不能为空！");
            }
            qa.setUserId(Long.valueOf(getUserId()));
            qa.setEmbedStatus(0);
            qa.setTokens(TikTokensUtil.tokens(EncodingType.CL100K_BASE, qa.getQuestion()));
            qaService.saveOrUpdate(qa);
            return success(qa);
        } catch (Exception e) {
            log.error("添加或修改QA问答对【addOrUpdate】失败：", e);
            return failByCatch("添加或修改QA问答对失败", e);
        }
    }

    @GetMapping("/remove")
    @ApiOperation("删除QA问答对")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "id", value = "QA问答对id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<Qa> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            Boolean result = qaService.remove(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用删除QA问答对接口【remove】失败：", e);
            return failByCatch("删除QA问答对失败", e);
        }
    }


    @ApiOperation("导出问答对")
    @GetMapping("/export")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "docId", value = "文档id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "fileName", value = "文件名称", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "fileType", value = "文件类型", required = true, paramType = "query", dataType = "String"),
    })
    @PassToken
    public void export(HttpServletResponse response, String docId, String fileName, String fileType){
        try {
            if (StringUtils.isEmpty(fileName)) {
                Document document = documentService.getById(docId);
                if (document == null) {
                    fileName = "问答对.xlsx";
                } else {
                    fileName = document.getDocName() +  "-问答对";
                }
            }

            MPJLambdaWrapper<Qa> wrapper = JoinWrappers.lambda(Qa.class)
                    .select(Qa::getId, Qa::getQuestion, Qa::getAnswer)
                    .leftJoin(DocContents.class, DocContents::getId, Qa::getContentId)
                    .leftJoin(Document.class, Document::getId, DocContents::getDocId)
                    .eq(Document::getId, docId)
                    .orderByAsc(Qa::getId)
                    .orderByDesc(Qa::getCreateTime);

            List<Qa> qaList = qaService.list(wrapper);
            
            
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
                        .head(Qa.class)
                        .registerWriteHandler(horizontalCellStyleStrategy)
                        .build();

                excelWriter.write(qaList, sheet);
                excelWriter.finish();
                out.flush();
                out.close();
            } else if ("word".equals(fileType)) {
                fileName += ".docx";
                Word07Writer writer = new Word07Writer();
                AtomicInteger index = new AtomicInteger(1);

                for (Qa qa: qaList) {
                    writer.addText(new Font("宋体", Font.BOLD, 12), index.getAndIncrement() + "、" + qa.getQuestion());
                    writer.addText(new Font("宋体", Font.PLAIN, 12), "回答：" + qa.getAnswer());
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
                String text = qaList.stream().map(qa -> index.getAndIncrement() + "、" + qa.getQuestion() + "\r\n" + "回答：" + qa.getAnswer() + "\r\n" + "\r\n").collect(Collectors.joining());

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
