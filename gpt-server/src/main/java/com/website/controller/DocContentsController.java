package com.website.controller;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.knuddels.jtokkit.api.EncodingType;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.controller.base.BaseController;
import com.website.entity.*;
import com.website.enums.Prompt;
import com.website.model.Result;
import com.website.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 文档分段内容 前端控制器
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Api(tags="文档内容相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/docContents")
@Slf4j
public class DocContentsController extends BaseController {

    @Autowired
    private DocContentsService docContentsService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private KbSettingService kbSettingService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private PromptService promptService;

    @GetMapping("/list")
    @ApiOperation("文档内容列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "docId", value = "文档id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "searchValue", value = "搜索字段", paramType = "query", dataType = "String"),
    })
    public Result<IPage<DocContents>> list(String docId, Integer page, Integer pageSize, String searchValue) {
        LambdaQueryWrapper<DocContents> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocContents::getId,DocContents::getDocId,DocContents::getUserId,DocContents::getContent,DocContents::getSummary,DocContents::getEmbedStatus,DocContents::getTokens,DocContents::getCreateTime);
        if (StringUtils.isNotEmpty(docId)) {
            queryWrapper.eq(DocContents::getDocId, docId);
        }
        if (StringUtils.isNotEmpty(searchValue)) {
            queryWrapper.like(DocContents::getContent, searchValue);
        }
        queryWrapper.orderByAsc(DocContents::getDocId);

        Page<DocContents> inputDataPage = new Page<>();
        inputDataPage.setSize(pageSize);
        inputDataPage.setCurrent(page);
        IPage<DocContents> pageList = docContentsService.page(inputDataPage, queryWrapper);
        return success(pageList);
    }


    @PostMapping("/addOrUpdate")
    @ApiOperation("添加或修改文档内容")
    @ApiOperationSupport(order = 3)
    public Result<DocContents> addOrUpdate(@RequestBody DocContents docContent) {
        try {
            if (docContent.getId() == null) {
                if (docContent.getDocId() == null) {
                    return fail("添加文档内容，文档docId不能为空！");
                }
            }
            if (StringUtils.isEmpty(docContent.getContent())) {
                return fail("文档内容不能为空！");
            }
            docContent.setUserId(Long.valueOf(getUserId()));
            docContent.setEmbedStatus(0);
            docContent.setSummaryStatus(0);
            docContent.setHasQA(0);
            docContent.setTokens(TikTokensUtil.tokens(EncodingType.CL100K_BASE, docContent.getContent()));
            docContentsService.saveOrUpdate(docContent);
            return success(docContent);
        } catch (Exception e) {
            log.error("添加或修改文档内容【addOrUpdate】失败：", e);
            return failByCatch("添加或修改文档内容失败", e);
        }
    }

    @GetMapping("/remove")
    @ApiOperation("删除文档内容")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "id", value = "文档内容id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<DocContents> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            Boolean result = docContentsService.remove(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用删除文档内容接口【remove】失败：", e);
            return failByCatch("删除文档内容失败", e);
        }
    }

    @GetMapping("/embedding")
    @ApiOperation("向量化文档内容")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "docId", value = "文档id", paramType = "query", dataType = "String")
    public Result<Boolean> embedding(String docId) {
        try {
            if (StringUtils.isEmpty(docId)) {
               return fail("docId不能为空！");
            }

            Document document = documentService.getById(docId);
            if (document.getEmbedStatus() == 1) {
                return fail("文档内容向量化中，请勿重复执行！");
            }

            LambdaQueryWrapper<DocContents> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocContents::getDocId, docId);
            long total = docContentsService.count(queryWrapper);
            queryWrapper.select(DocContents::getId,DocContents::getContent);
            queryWrapper.ne(DocContents::getEmbedStatus, 1);
            List<DocContents> docContentsList = docContentsService.list(queryWrapper);
            if (docContentsList.size() == 0) {
                return fail("无向量化内容！");
            }
            // 开始向量化
            UpdateWrapper<Document> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", docId);
            updateWrapper.set("userId", getUserId());
            updateWrapper.set("embedStatus", 1);
            documentService.update(updateWrapper);

            KbSetting kbSetting = kbSettingService.getSettingByDocId(docId);

            AtomicInteger atomicTotal = new AtomicInteger(docContentsList.size());
            // 需要根据chatGPT速率限制设置 https://platform.openai.com/docs/guides/rate-limits/overview
            List<List<DocContents>> splitContents = ListUtil.splitAvg(docContentsList, kbSetting.getPoolSize());
            splitContents.forEach(contents -> {
                if (contents.size() > 0) {
                    docContentsService.embeddingContents(contents, atomicTotal, docId, getUserId(), (int) total);
                }
            });
            return success(true);
        } catch (Exception e) {
            log.error("向量化文档内容接口【embedding】失败：", e);
            return failByCatch("向量化文档内容失败", e);
        }
    }


    @GetMapping("/embeddingStatus")
    @ApiOperation("向量化文档内容状态")
    @ApiOperationSupport(order = 5)
    @ApiImplicitParam(name = "docId", value = "文档id", paramType = "query", dataType = "String")
    public Result<EmbeddingStatus> embeddingStatus(String docId) {

        Document document = documentService.getById(docId);

        EmbeddingStatus embeddingStatus = new EmbeddingStatus().setDocId(docId);
        embeddingStatus.setEmbedStatus(document.getEmbedStatus());
        embeddingStatus.setStatus("");
        if (document.getEmbedStatus() == 0) {
            //未开始
            embeddingStatus.setPercentage(0);
            return success(embeddingStatus);
        } else if (document.getEmbedStatus() == 2) {
            // 已完成
            embeddingStatus.setStatus("success");
            embeddingStatus.setPercentage(100);
            return success(embeddingStatus);
        } else {
            if (document.getEmbedStatus() == 3) {
                // 错误
                embeddingStatus.setStatus("exception");
            }
            Map<String, Object> embedCount = docContentsService.getEmbedCount(docId);
            int total =  Integer.parseInt(embedCount.get("total").toString());
            int completed =  Integer.parseInt(embedCount.get("completed").toString());
            embeddingStatus.setPercentage(completed*100/total);
            return success(embeddingStatus);
        }
    }

    @GetMapping("/createQA")
    @ApiOperation("文档内容生成问答对")
    @ApiOperationSupport(order = 14)
    @ApiImplicitParam(name = "docId", value = "文档id", paramType = "query", dataType = "String")
    public Result<Boolean> createQA(String docId) {
        try {
            if (StringUtils.isEmpty(docId)) {
                return fail("docId不能为空！");
            }
            Document document = documentService.getById(docId);
            if (document.getQaStatus() == 1) {
                return fail("文档内容生成问答对中，请勿重复执行！");
            }

            LambdaQueryWrapper<DocContents> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocContents::getDocId, docId);
            long total = docContentsService.count(queryWrapper);
            queryWrapper.select(DocContents::getId,DocContents::getContent,  DocContents::getSummary, DocContents::getHasQA);
            queryWrapper.ne(DocContents::getHasQA, 1);
            List<DocContents> docContentsList = docContentsService.list(queryWrapper);
            if (docContentsList.size() == 0) {
                return fail("无生成问答对的内容！");
            }

            KbSetting kbSetting = kbSettingService.getSettingByDocId(docId);
            if (StringUtils.isEmpty(kbSetting.getQaPrompt() )) {
                kbSetting.setQaPrompt(promptService.getByTopic(Prompt.QA.topic));
            }
            if (StringUtils.isEmpty(kbSetting.getOptimizePrompt() )) {
                kbSetting.setOptimizePrompt(promptService.getByTopic(Prompt.OPTIMIZE.topic));
            }
            kbSetting.setQaPrompt(knowledgeBaseService.setLastPrompt(kbSetting.getQaPrompt(), kbSetting.getKbId()));
            kbSetting.setOptimizePrompt(knowledgeBaseService.setLastPrompt(kbSetting.getOptimizePrompt(), kbSetting.getKbId()));

            // 开始生成问答对
            UpdateWrapper<Document> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", docId);
            updateWrapper.set("userId", getUserId());
            updateWrapper.set("qaStatus", 1);
            documentService.update(updateWrapper);

            AtomicInteger atomicTotal = new AtomicInteger(docContentsList.size());
            // 需要根据chatGPT速率限制设置 https://platform.openai.com/docs/guides/rate-limits/overview
            List<List<DocContents>> splitContents = ListUtil.splitAvg(docContentsList, kbSetting.getPoolSize());
            String summary = "";
            for (List<DocContents> contents : splitContents) {
                if (contents.size() > 0) {
                    docContentsService.qaContents(contents, atomicTotal, docId, getUserId(), (int) total, kbSetting, summary);
                    if (kbSetting.getQaAddSummary() == 1) {
                        summary = StringUtils.isNotEmpty(contents.get(0).getSummary()) ? contents.get(0).getSummary() : "";
                    }
                }
            }
            return success(true);
        } catch (Exception e) {
            log.error("文档内容生成问答对接口【createQA】失败：", e);
            return failByCatch("文档内容生成问答对失败", e);
        }
    }


    @GetMapping("/qaStatus")
    @ApiOperation("问答对状态")
    @ApiOperationSupport(order = 5)
    @ApiImplicitParam(name = "docId", value = "文档id", paramType = "query", dataType = "String")
    public Result<QaStatus> qaStatus(String docId) {
        Document document = documentService.getById(docId);
        QaStatus qaStatus = new QaStatus().setDocId(docId);
        qaStatus.setQaStatus(document.getQaStatus());
        qaStatus.setStatus("");
        if (document.getQaStatus() == 0) {
            //未开始
            qaStatus.setPercentage(0);
            return success(qaStatus);
        } else if (document.getQaStatus() == 2) {
            // 已完成
            qaStatus.setStatus("success");
            qaStatus.setPercentage(100);
            return success(qaStatus);
        } else {
            if (document.getQaStatus() == 3) {
                // 错误
                qaStatus.setStatus("exception");
            }
            Map<String, Object> qaCount = docContentsService.getQaCount(docId);
            int total =  Integer.parseInt(qaCount.get("total").toString());
            int completed =  Integer.parseInt(qaCount.get("completed").toString());
            qaStatus.setPercentage(completed*100/total);
            return success(qaStatus);
        }
    }



    @GetMapping("/summary")
    @ApiOperation("总结文档内容")
    @ApiOperationSupport(order = 14)
    @ApiImplicitParam(name = "docId", value = "文档id", paramType = "query", dataType = "String")
    public Result<Boolean> summary(String docId) {
        try {
            if (StringUtils.isEmpty(docId)) {
                return fail("docId不能为空！");
            }

            Document document = documentService.getById(docId);
            if (document.getSummaryStatus() == 1) {
                return fail("文档内容总结中，请勿重复执行！");
            }

            LambdaQueryWrapper<DocContents> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocContents::getDocId, docId);
            long total = docContentsService.count(queryWrapper);
            queryWrapper.select(DocContents::getId, DocContents::getContent);
            queryWrapper.ne(DocContents::getSummaryStatus, 1);
            List<DocContents> docContentsList = docContentsService.list(queryWrapper);
            if (docContentsList.size() == 0) {
                return fail("无需总结内容！");
            }
            // 开始向量化
            UpdateWrapper<Document> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", docId);
            updateWrapper.set("userId", getUserId());
            updateWrapper.set("summaryStatus", 1);
            documentService.update(updateWrapper);

            KbSetting kbSetting = kbSettingService.getSettingByDocId(docId);
            if (StringUtils.isEmpty(kbSetting.getSummaryPrompt())) {
                kbSetting.setSummaryPrompt(promptService.getByTopic(Prompt.SUMMARY.topic));
            }
            kbSetting.setSummaryPrompt(knowledgeBaseService.setLastPrompt(kbSetting.getSummaryPrompt(), kbSetting.getKbId()));

            AtomicInteger atomicTotal = new AtomicInteger(docContentsList.size());
            // 需要根据chatGPT速率限制设置 https://platform.openai.com/docs/guides/rate-limits/overview
            List<List<DocContents>> splitContents = ListUtil.splitAvg(docContentsList, kbSetting.getPoolSize());
            splitContents.forEach(contents -> {
                if (contents.size() > 0) {
                    docContentsService.summaryContents(contents, atomicTotal, docId, getUserId(), (int) total, kbSetting);
                }
            });
            return success(true);
        } catch (Exception e) {
            log.error("总结文档内容接口【summary】失败：", e);
            return failByCatch("总结文档内容失败", e);
        }
    }


    @GetMapping("/summaryStatus")
    @ApiOperation("总结状态")
    @ApiOperationSupport(order = 15)
    @ApiImplicitParam(name = "docId", value = "文档id", paramType = "query", dataType = "String")
    public Result<SummaryStatus> summaryStatus(String docId) {
        Document document = documentService.getById(docId);
        SummaryStatus summaryStatus = new SummaryStatus().setDocId(docId);
        summaryStatus.setSummaryStatus(document.getSummaryStatus());
        summaryStatus.setStatus("");
        if (document.getSummaryStatus() == 0) {
            //未开始
            summaryStatus.setPercentage(0);
            return success(summaryStatus);
        } else if (document.getSummaryStatus() == 2) {
            // 已完成
            summaryStatus.setStatus("success");
            summaryStatus.setPercentage(100);
            return success(summaryStatus);
        } else {
            if (document.getSummaryStatus() == 3) {
                // 错误
                summaryStatus.setStatus("exception");
            }
            Map<String, Object> summaryCount = docContentsService.getSummaryCount(docId);
            int total =  Integer.parseInt(summaryCount.get("total").toString());
            int completed =  Integer.parseInt(summaryCount.get("completed").toString());
            summaryStatus.setPercentage(completed*100/total);
            return success(summaryStatus);
        }
    }

}
