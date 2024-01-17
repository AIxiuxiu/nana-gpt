package com.website.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 定时更新KB信息的定时策略
 * @author XAYQ-ZhangJun1
 *
 */

@Service
@Slf4j
public class CacheSchedule {
	
	private final KbCacheService kbCacheService;

	public CacheSchedule(KbCacheService kbCacheService) {
		this.kbCacheService = kbCacheService;
	}

	@Scheduled(cron = "0 15 0-23 * * ?")
	public void  getKbInfoSchedule() {
		log.info("开始缓存所有实体信息!" + LocalDateTime.now());
		kbCacheService.loadKbInfoMap();
		log.info(">>>缓存所有实体信息完成!" + LocalDateTime.now());
	}

	/**
	 * 每24个小时缓存上市公司oid和code
	 */
	@Scheduled(initialDelay=1000, fixedRate=24 * 1000 * 60 * 60)
	public void loadCodeAndOids() {
		log.info("开始缓存所有公司代码" + LocalDateTime.now());
		kbCacheService.loadCodeAndOid();
		log.info(">>>缓存所有公司代码完成" + LocalDateTime.now());
	}

}
