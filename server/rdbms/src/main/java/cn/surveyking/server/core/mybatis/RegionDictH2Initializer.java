package cn.surveyking.server.core.mybatis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.zip.GZIPInputStream;

/**
 * H2 模式行政区字典预置。
 * <p>
 * 背景：H2 profile 的 {@code spring.sql.init.mode=always} 每次启动都会执行
 * {@code classpath:scripts/init-h2.sql}（建表 + 少量种子数据，幂等）。行政区字典
 * （dict_code=region，五级全量 66 万余条）体积大，不能并入 init-h2.sql，否则每次
 * 启动都会重灌。
 * <p>
 * 本类仅在 h2 profile 生效：启动时检查 {@code t_comm_dict_item} 是否已有
 * {@code dict_code='region'}，无则从 {@code classpath:scripts/data-region-dict.sql.gz}
 * 导入一次（脚本幂等：先 DELETE 后 INSERT），有则跳过。
 * <p>
 * 资源以 gzip 压缩存储（镜像/仓库瘦身，且 SQL 约 91MB 超过 GitHub 50MB 限制），
 * 此处先解压再交由 ScriptUtils 执行；显式指定 UTF-8 保证中文地名不乱码。
 * <p>
 * MySQL 模式无需本类：docker-compose 通过挂载 data-region-dict.sql.gz 到
 * {@code /docker-entrypoint-initdb.d/}（MySQL 官方镜像原生支持 .sql.gz 自动解压）完成初始化。
 *
 * @author eng-doc
 */
@Slf4j
@Component
@Profile("h2")
@RequiredArgsConstructor
public class RegionDictH2Initializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		try {
			Integer count = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM t_comm_dict_item WHERE dict_code = 'region'", Integer.class);
			if (count != null && count > 0) {
				log.info("Region dictionary already present in H2, skip import (count={})", count);
				return;
			}

			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource == null) {
				log.warn("RegionDictH2Initializer skipped: no DataSource");
				return;
			}
			ClassPathResource resource = new ClassPathResource("scripts/data-region-dict.sql.gz");
			if (!resource.exists()) {
				log.warn("RegionDictH2Initializer skipped: scripts/data-region-dict.sql.gz not found in classpath");
				return;
			}

			// data-region-dict.sql 为 MySQL 语法（反引号标识符），H2 MODE=MySQL 兼容；
			// 文件为纯 INSERT/DELETE、无 BEGIN/COMMIT 事务包裹，逐条自动提交即可。
			try (Connection connection = dataSource.getConnection();
					InputStream raw = resource.getInputStream();
					InputStream gz = new GZIPInputStream(raw)) {
				connection.setAutoCommit(true);
				EncodedResource encoded = new EncodedResource(new InputStreamResource(gz), StandardCharsets.UTF_8);
				ScriptUtils.executeSqlScript(connection, encoded);
			}
			log.info("Region dictionary imported into H2 (dict_code=region)");
		}
		catch (Exception ex) {
			log.warn("RegionDictH2Initializer skipped: {}", ex.getMessage());
		}
	}

}
