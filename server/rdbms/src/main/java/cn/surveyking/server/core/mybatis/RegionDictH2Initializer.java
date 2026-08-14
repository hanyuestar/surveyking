package cn.surveyking.server.core.mybatis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
 * 资源以 gzip 压缩存储（镜像/仓库瘦身；SQL 约 91MB 未压缩，gzip 后约 13.7MB 可入库），
 * 此处先解压再逐行流式执行；显式指定 UTF-8 保证中文地名不乱码。
 * <p>
 * ⚠️ 内存关键点（曾导致容器 OOM 崩溃）：Spring 的 {@code ScriptUtils.executeSqlScript}
 * 会把整个脚本一次性读入单个 String —— 91MB 脚本在堆里会变成约 182MB 的 char 数组，
 * StringBuilder 扩容期峰值可达 ~360MB，512MB 堆必 OOM。因此这里改为「逐行读取 +
 * 按 ';' 切分语句增量执行」，单条语句内存占用被限制在几百 KB（每个 INSERT 约 2000 行），
 * 与堆大小无关，彻底消除 OOM。
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
			// 逐行流式读取 + 按 ';' 切分增量执行，避免整文件入内存导致 OOM。
			try (InputStream raw = resource.getInputStream();
					InputStream gz = new GZIPInputStream(raw);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(gz, StandardCharsets.UTF_8))) {
				StringBuilder stmt = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					String trimmed = line.trim();
					// 跳过空行与 SQL 行注释（-- 开头），避免把注释拼进语句
					if (trimmed.isEmpty() || trimmed.startsWith("--")) {
						continue;
					}
					stmt.append(line).append('\n');
					// 脚本中 ';' 仅作语句结束符，不会出现在数据值内，故可安全按 ';' 切分
					if (trimmed.endsWith(";")) {
						String sql = stmt.toString().trim();
						if (sql.endsWith(";")) {
							sql = sql.substring(0, sql.length() - 1);
						}
						if (!sql.isEmpty()) {
							jdbcTemplate.execute(sql);
						}
						stmt.setLength(0);
					}
				}
			}
			log.info("Region dictionary imported into H2 (dict_code=region)");
		}
		catch (Throwable ex) {
			// 捕获一切异常/错误（含 OutOfMemoryError 等 Throwable），绝不让字典导入失败拖垮整个应用启动。
			// 行政区字典缺失仅影响「地区选择」类题型，登录与主页等核心功能仍可正常使用。
			log.warn("RegionDictH2Initializer skipped: {}", ex.toString());
		}
	}

}
