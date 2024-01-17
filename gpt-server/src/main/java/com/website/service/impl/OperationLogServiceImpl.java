package com.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.website.entity.OperationLog;
import com.website.mapper.OperationLogMapper;
import com.website.service.OperationLogService;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 操作记录 服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public List<OperationLog> getOperationLogListByDate(String startDate, String endDate) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("createTime", startDate);
        queryWrapper.le("createTime", endDate);
        queryWrapper.orderByDesc("createTime");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void saveOperationLog(OperationLog operationLog) {
        if (baseMapper.insert(operationLog) != 1) {
            throw new PersistenceException("日志添加失败");
        }
    }

    @Override
    public void deleteOperationLogById(Long id) {
        if (baseMapper.deleteById(id) != 1) {
            throw new PersistenceException("删除日志失败");
        }
    }
}
