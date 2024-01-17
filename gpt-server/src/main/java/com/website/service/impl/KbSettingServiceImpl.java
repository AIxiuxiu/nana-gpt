package com.website.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.website.entity.KbSetting;
import com.website.mapper.KbSettingMapper;
import com.website.service.KbSettingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-07-27
 */
@Service
public class KbSettingServiceImpl extends ServiceImpl<KbSettingMapper, KbSetting> implements KbSettingService {

    @Resource
    private KbSettingMapper kbSettingMapper;

    @Override
    public KbSetting getSettingByKbId(String kbId) {
        KbSetting kbSetting = kbSettingMapper.getSettingByKbId(kbId);
        if (kbSetting == null) {
            kbSetting = new KbSetting().setKbId(Long.valueOf(kbId));
            baseMapper.insert(kbSetting);
            kbSetting = kbSettingMapper.getSettingByKbId(kbId);
        }
        return kbSetting;
    }

    @Override
    public KbSetting getSettingByDocId(String docId) {
        return kbSettingMapper.getSettingByDocId(docId);
    }

}
