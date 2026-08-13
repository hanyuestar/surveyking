package cn.surveyking.server.core.mybatis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * 存量库迁移：启动时检测 t_account.token_version 列，缺失则补列（MySQL/H2 兼容，幂等）。
 * 新增 install 库已包含该列（见 init-*.sql），此处仅用于老库升级场景。
 *
 * @author eng-koudouma
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenVersionMigrator implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		try {
			if (!columnExists("t_account", "token_version")) {
				jdbcTemplate.execute("ALTER TABLE t_account ADD COLUMN token_version int DEFAULT 0");
				log.info("Migrated: added column token_version to t_account");
			}
		}
		catch (Exception ex) {
			log.warn("TokenVersionMigrator skipped: {}", ex.getMessage());
		}
	}

	private boolean columnExists(String tableName, String columnName) {
		DataSource dataSource = jdbcTemplate.getDataSource();
		if (dataSource == null) {
			return true;
		}
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			// MySQL 与 H2 对未加引号标识符的大小写归一化行为不同，兼容探测
			for (String candidate : new String[] { tableName, tableName.toUpperCase(), tableName.toLowerCase() }) {
				try (ResultSet rs = metaData.getColumns(null, null, candidate, columnName)) {
					if (rs.next()) {
						return true;
					}
				}
			}
			return false;
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to inspect table metadata", ex);
		}
	}

}
