package com.website.service;

import com.website.entity.KbSetting;
import com.website.service.base.BaseService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ahl
 * @since 2023-07-27
 */
public interface KbSettingService extends BaseService<KbSetting> {

    KbSetting getSettingByKbId(String kbId);

    KbSetting getSettingByDocId(String docId);
}
