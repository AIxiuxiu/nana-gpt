package com.website.service.cache;

import cn.hutool.core.map.BiMap;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.website.service.http.BaseApiResponse;
import com.website.service.http.DataCenterHttp;
import com.website.service.http.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KbCacheService {

    private final DataCenterHttp dataCenterHttp;

    public KbCacheService(DataCenterHttp dataCenterHttp) {
        this.dataCenterHttp = dataCenterHttp;
    }

    /**
     * 所有公司实体信息
     */
    private volatile Map<String, Map<String, Object>> kbInfoMap = new HashMap<>();

    /**
     * 上市公司缓存
     */
    private final Map<String, Map<String, Object>> orgCodeMap = new HashMap<>();

    /**
     * 公司代码和oid缓存,可双向查询
     */
    private final BiMap<String, String> codeOidMap = new BiMap<>(new HashMap<>());

    @Async
    public void  loadKbInfoMap() {
        try {
            BaseApiResponse baseApiResponse = dataCenterHttp.get("/kb/searchOrg");
            if (!baseApiResponse.getCode().equals(ResultCodeEnum.SUCCESS_CODE.getType())) {
                log.error("缓存所有实体信息失败" + baseApiResponse.getMessage());
            } else {
                kbInfoMap = (Map<String, Map<String, Object>>) baseApiResponse.getData().get(0);
            }
        } catch (Exception e) {
            log.error("缓存所有实体信息失败");
        }
    }

    @Async
    public void loadCodeAndOid() {
        try {
            BaseApiResponse baseApiResponse = dataCenterHttp.get("/kb/queryOrgStock");
            if (!baseApiResponse.getCode().equals(ResultCodeEnum.SUCCESS_CODE.getType())) {
                log.error("缓存公司代码失败" + baseApiResponse.getMessage());
                return;
            }
            List<Map<String, Object>> codeAndOids = (List<Map<String, Object>>) baseApiResponse.getData().get(0);
            for (Map<String, Object> codeAndOid : codeAndOids) {
                if (codeAndOid.containsKey("CODE") && codeAndOid.containsKey("OID")) {
                    setPinyin(codeAndOid);
                    orgCodeMap.put(codeAndOid.get("CODE").toString(), codeAndOid);
                    codeOidMap.put(codeAndOid.get("CODE").toString(), codeAndOid.get("OID").toString());
                }
            }

        } catch (Exception e) {
            log.error("缓存所有公司代码失败");
        }
    }

    private void setPinyin(Map<String, Object> codeAndOid) {
        String name =  codeAndOid.get("NAME").toString();
        String pinyin = PinyinHelper.toPinyin(name, PinyinStyleEnum.INPUT);
        String pinyinLetter = StringUtils.join(Arrays.stream(pinyin.split(" ")).map(v -> v.charAt(0)).collect(Collectors.toList()), "");
        pinyin = pinyin.replaceAll(" ", "");
        codeAndOid.put("pinyin", pinyin);
        codeAndOid.put("pinyinLetter", pinyinLetter);
    }

    public void cleanKbInfo() {
        kbInfoMap.clear();
        orgCodeMap.clear();
        codeOidMap.clear(); // 公司代码
    }

    /**
     * 获取KB实体缓存
     * @return InfoMap
     */
    public Map<String, Map<String, Object>> getKbInfoMap() {
        if (kbInfoMap.isEmpty()) {
            synchronized(this) {
                if (kbInfoMap.isEmpty()) {
                    loadKbInfoMap();
                }
            }
        }
        return kbInfoMap;
    }

    /**
     * 获取实体
     * @param id orgId
     * @return orgMap
     */
    public Map<String, Object> getOrgMap(String id) {
        return getKbInfoMap().get(id);
    }

    /**
     * 获取KB CODE 缓存
     * @return orgCodeMap
     */
    public Map<String, Map<String, Object>> getOrgCodeMap() {
        if (orgCodeMap.isEmpty()) {
            synchronized(this) {
                if (orgCodeMap.isEmpty()) {
                    loadCodeAndOid();
                }
            }
        }
        return orgCodeMap;
    }

    /**
     * 获取code查找oid,或者oid查code
     * @param id 公司code或oid
     * @return 对应的oid或code
     */
    public String getOidOrCode(String id) {
        if (codeOidMap.isEmpty()) {
            synchronized(this) {
                if (codeOidMap.isEmpty()) {
                    loadCodeAndOid();
                }
            }
        }
        return codeOidMap.get(id);
    }

}
