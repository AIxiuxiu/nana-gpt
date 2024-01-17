package com.website.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.website.controller.base.BaseController;
import com.website.entity.base.BaseEntity;
import com.website.service.base.BaseService;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class MPGenerator {

	private static String projectPath = System.getProperty("user.dir");
	/**
	 * 生成文件输出路径请自行修改
	 */
	private static String OUTPUT_DIR = projectPath + "/target";
//	private static String OUTPUT_DIR = projectPath + "/src/main/java";
	/**
	 * 数据源配置
	 */
	private static final DataSourceConfig.Builder DATA_SOURCE_CONFIG = new DataSourceConfig.Builder("jdbc:mysql://172.22.9.37:3306/dev_gpt?serverTimezone=GMT%2B8", "dev", "dev")
			.typeConvert(new MySqlTypeConvert());

	/**
	 * mp代码生成
	 */
	public static void main(String[] args) throws SQLException {
		// 项目路径
		log.info("输出路径" + OUTPUT_DIR);
		FastAutoGenerator.create(DATA_SOURCE_CONFIG)
				.globalConfig((scanner, builder) -> {
					builder.author(scanner.apply("请输入作者名称？")) // 设置作者
							.enableSwagger() // 开启 swagger 模式
							.outputDir(OUTPUT_DIR); // 指定输出目录
				})
				.packageConfig(builder -> {
					builder.parent("com.website") // 设置父包名
							.entity("entity") //生成实体层
							.service("service") //生成服务层
							.mapper("mapper") //生成mapper层
							.controller("controller"); //生成controller层，自行选择
				})
				.strategyConfig((scanner, builder) -> builder.addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
							.addTablePrefix("t_") // 设置过滤表前缀
							.serviceBuilder() //开启service策略配置
							.superServiceClass(BaseService.class)
//							.fileOverride() //开启service的覆盖策略
							.formatServiceFileName("%sService") //取消Service前的I
							.controllerBuilder() //开启controller策略配置
							.enableRestStyle() //配置restful风格
							.enableHyphenStyle() //url中驼峰转连字符
							.entityBuilder() //开启实体类配置
							.superClass(BaseEntity.class)
//							.fileOverride() //开启实体类的覆盖策略（修改数据库后更新）
							.enableLombok() //开启lombok
							.enableChainModel() //开启lombok链式操作
							.controllerBuilder().superClass(BaseController.class)
//							.fileOverride() //开启controller的覆盖策略
							.serviceBuilder().superServiceClass(BaseService.class)
//							.fileOverride() //开启service的覆盖策略
							.mapperBuilder()
//							.fileOverride() //开启mapper的覆盖策略
							.build())

				.execute();

	}

	// 处理 all 情况
	protected static List<String> getTables(String tables) {
		return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
	}

}
