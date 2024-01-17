package com.website.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.website.config.TaskExecutorConfig;
import com.website.entity.Interact;
import com.website.mapper.InteractMapper;
import com.website.service.InteractContentsService;
import com.website.service.InteractService;
import com.website.service.http.BaseApiResponse;
import com.website.service.http.DataCenterHttp;
import com.website.service.http.ResultCodeEnum;
import com.website.util.PlaceholderResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动问答 服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
@Service
@Slf4j
public class InteractServiceImpl extends ServiceImpl<InteractMapper, Interact> implements InteractService {

    @Autowired
    DataCenterHttp dataCenterHttp;

    @Autowired
    InteractContentsService interactContentsService;

    @Override
    public String getNewsList(Interact interact) {
        int num = interact.getMaxNum() * 2;
        if (num< 10) {
            num = 10;
        }
        Map<String, String> params = new HashMap<>();
        params.put("orgvalue", interact.getOrgId());
        params.put("page", "1");
        params.put("num", String.valueOf(num));
        params.put("sort", "publishdate:decreasing");
        params.put("db", "vnews");
        params.put("is_core", interact.getIsCore().toString());
        params.put("s_date", interact.getStartDate());
        params.put("e_date", interact.getEndDate());
        BaseApiResponse baseApiResponse = dataCenterHttp.get("/idol/getALLList", params);
        if (!baseApiResponse.getCode().equals(ResultCodeEnum.SUCCESS_CODE.getType())) {
            log.error("获取新闻失败" + baseApiResponse.getMessage());
            return "获取数据失败！";
        } else {
            List<Object> list = baseApiResponse.getData();
            if (list == null || list.size() == 0) {
                return "未获取到数据！";
            }
            Map<String, Object> jo = (Map<String, Object>) list.get(0);
            List<Map<String, Object>> datas = (List<Map<String, Object>>) jo.get("__KEY_DATAS");
            if (datas.size() > 0) {
                AtomicInteger atomicTotal = new AtomicInteger(0);
                List<String> referencesLis = datas.stream().map(v -> v.get("reference").toString()).collect(Collectors.toList());
                List<List<String>> splitReferences = ListUtil.splitAvg(referencesLis, TaskExecutorConfig.cpuNum);
                splitReferences.forEach(references -> {
                    if (references.size() > 0) {
                        interactContentsService.getContents(references, interact, atomicTotal);
                    }
                });
            }
        }
        return "";
    }

    @Override
    public void setLastPrompt(Interact interact) {

        Map<String, Object> params = new HashMap<>();
        params.put("companyName", interact.getCompanyName());
        params.put("companyCode", interact.getCompanyCode());
        params.put("companyShortName", interact.getCompanyShortName());
        params.put("startDate", interact.getStartDate());
        params.put("endDate", interact.getEndDate());
        String prompt = PlaceholderResolver.getDefaultResolver().resolveByMap(interact.getQPrompt(), params);

        interact.setQPrompt(prompt);
    }
}
