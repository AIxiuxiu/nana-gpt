package com.website.mapper;

import com.website.entity.KbSetting;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ahl
 * @since 2023-07-27
 */
public interface KbSettingMapper extends BaseMapper<KbSetting> {

    KbSetting getSettingByKbId(String kbId);

    KbSetting getSettingByDocId(String docId);

}
