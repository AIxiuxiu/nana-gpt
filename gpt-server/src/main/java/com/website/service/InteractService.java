package com.website.service;

import com.website.entity.Interact;
import com.website.service.base.BaseService;

/**
 * <p>
 * 互动问答 服务类
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
public interface InteractService extends BaseService<Interact> {

    String getNewsList(Interact interact);

    void setLastPrompt(Interact interact);

}
