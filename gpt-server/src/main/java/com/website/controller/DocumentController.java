package com.website.controller;

import cn.hutool.core.io.file.FileNameUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.website.controller.base.BaseController;
import com.website.entity.Document;
import com.website.model.Result;
import com.website.service.DocContentsService;
import com.website.service.DocumentService;
import com.website.vo.UploadDocVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  文档 前端控制器
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Api(tags="文档相关接口")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/document")
@Slf4j
public class DocumentController extends BaseController {

    @Autowired
    DocumentService documentService;

    @Autowired
    DocContentsService docContentsService;

    @GetMapping("/list")
    @ApiOperation("文档列表")
    @ApiOperationSupport(order = 1)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "kbId", value = "知识库id", required = true, paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "searchValue", value = "搜索字段", paramType = "query", dataType = "String"),
    })
    public Result<IPage<Document>> list(String kbId, Integer page, Integer pageSize, String searchValue) {
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Document::getKbId, kbId);
        if (StringUtils.isNotEmpty(searchValue)) {
            queryWrapper.like(Document::getDocName, searchValue);
        }
        queryWrapper.eq(Document::getStatus, 1);
        queryWrapper.orderByDesc(Document::getCreateTime);
        Page<Document> inputDataPage = new Page<>();
        inputDataPage.setSize(pageSize);
        inputDataPage.setCurrent(page);
        IPage<Document> pageList = documentService.page(inputDataPage, queryWrapper);
        return success(pageList);
    }

    /**
     *  上传文档分段保存
     */
    @ApiOperation("上传文档分段保存，当前只支持PDF")
    @ApiOperationSupport(order = 2)
    @PostMapping(value = "/uploadDoc",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Document> uploadDoc(UploadDocVo uploadDocVo) throws Exception {
        if (0 == uploadDocVo.getFiles().length){
            return fail("上传文件数量为0");
        }
        if (StringUtils.isEmpty(uploadDocVo.getKbId())) {
            return fail("知识库kbId不能为空");
        }

        try {
            MultipartFile multipartFile = uploadDocVo.getFiles()[0];
            String originalFilename = uploadDocVo.getFiles()[0].getOriginalFilename();
//            File file = File.createTempFile(VerificationCodeGenerator.generateCode(6), "." + FileNameUtil.getSuffix(originalFilename));
//            multipartFile.transferTo(file);

            List<String> contents = documentService.getFileContent(multipartFile, uploadDocVo.getKbId());

            Document document = new Document().setUserId(Long.valueOf(getUserId())).setKbId(Long.valueOf(uploadDocVo.getKbId()));
            if (StringUtils.isEmpty(uploadDocVo.getDocName())) {
                document.setDocName(FileNameUtil.getPrefix(originalFilename));
            } else {
                document.setDocName(uploadDocVo.getDocName());
            }

            boolean result = documentService.save(document);
            if (!result) {
                return fail("上传文件失败！");
            }
            result = docContentsService.saveContents(contents, document);
            if (!result) {
                return fail("保存文件内容失败！");
            }
            return success(document);
        } catch (Exception e) {
            return failByCatch("保存文件内容失败！", e);
        }

    }


    @PostMapping("/update")
    @ApiOperation("更新文档")
    @ApiOperationSupport(order = 3)
    public Result<Document> addOrUpdate(@RequestBody Document document) {
        try {
            if (document.getId() == null) {
                return fail("更新失败，id不能为空");
            }
            document.setUserId(Long.valueOf(getUserId()));
            documentService.updateById(document);
            return success(document);
        } catch (Exception e) {
            log.error("调用更新文档接口【update】失败：", e);
            return failByCatch("更新文档失败", e);
        }
    }

    @GetMapping("/remove")
    @ApiOperation("删除文档")
    @ApiOperationSupport(order = 4)
    @ApiImplicitParam(name = "id", value = "文档id", paramType = "query", dataType = "String")
    public Result<Boolean> remove(String id) {
        try {
            UpdateWrapper<Document> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            updateWrapper.set("userId", getUserId());
            updateWrapper.set("status", 0);
            Boolean result = documentService.update(updateWrapper);
            return success(result);
        } catch (Exception e) {
            log.error("调用删除文档接口【remove】失败：", e);
            return failByCatch("删除文档失败", e);
        }
    }

}
