package com.website.service;

import com.website.entity.OperationLog;
import com.website.service.base.BaseService;

import java.util.List;

/**
 * <p>
 * 操作记录 服务类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
public interface OperationLogService extends BaseService<OperationLog> {

    List<OperationLog> getOperationLogListByDate(String startDate, String endDate);

    void saveOperationLog(OperationLog operationLog);

    void deleteOperationLogById(Long id);
}
