package cn.surveyking.server.core.mybatis;

import cn.surveyking.server.core.constant.ProjectModeEnum;
import cn.surveyking.server.domain.dto.SurveySchema;
import cn.surveyking.server.domain.model.Template;
import cn.surveyking.server.mapper.TemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 内置样例模板种子。
 * <p>
 * 背景：SurveyKing 原生不携带任何内置模板，所有模板均由用户经 UI 自建。
 * 为让新用户开箱即用，本项目随 v1.0.8 内置一批原创样例模板（问卷 / 考试 / 报名 / 投票），
	 * 以 {@code shared=1} 注入 {@code t_template}，归属系统管理员，所有登录用户均可在
	 * 编辑器的「从模板新建/插入」面板（该面板后端以 {@code questionType=Survey} + {@code shared} 查询）看到并一键复用。
	 * 注意：模板广场（listTemplate）默认查询 {@code questionType==null} 时排除整卷 Survey 模板，
	 * 故内置整卷模板只在该编辑器面板出现，与用户自建后「存为模板」的可见路径一致。
 * <p>
 * 落点机制：复用 {@link RegionDictH2Initializer} 的幂等思路——启动检查是否已存在，
 * 缺失则按 {@code serialNo = 'BUILTIN-###'} 逐条插入一次；已存在则跳过，绝不在每次启动重复灌入。
 * 与 RegionDictH2Initializer（仅 h2 profile）不同，本类在所有 profile 生效，
 * 以保证 H2 体验模式与 MySQL 生产库（含已存在的旧库）都能拿到内置模板。
 * <p>
 * 模板内容来源：{@code classpath:builtin-templates.json}（原创，非抓取第三方平台），
 * 避免版权/合规风险；考试模板正确选项以 {@code attribute.examCorrectAnswer} 非 null 标记，
 * 配合 {@code examScore}/{@code examAnswerMode} 即可被 {@code AnswerScoreEvaluator} 自动判分。
 * <p>
 * 健壮性：任何异常均捕获并仅告警，绝不拖垮应用启动（内置模板缺失不影响核心功能）。
 *
 * @author eng-doc
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BuiltinTemplateInitializer implements ApplicationRunner {

	/**
	 * 系统管理员账号 ID（与 init-mysql.sql / init-h2.sql 中的 admin 一致）。
	 * 内置模板归属该用户，使其在模板列表中以「系统/公共」身份展示，而非某个真实用户私有。
	 */
	private static final String SYSTEM_ADMIN_ID = "1457995481966747649";

	/**
	 * 内置模板 serialNo 统一前缀，用于幂等检查（LIKE 'BUILTIN%'）。
	 */
	private static final String SERIAL_NO_PREFIX = "BUILTIN";

	private final TemplateMapper templateMapper;

	private final ObjectMapper objectMapper;

	@Override
	public void run(ApplicationArguments args) {
		try {
			ClassPathResource resource = new ClassPathResource("builtin-templates.json");
			if (!resource.exists()) {
				log.warn("BuiltinTemplateInitializer skipped: builtin-templates.json not found in classpath");
				return;
			}

			try (InputStream in = resource.getInputStream()) {
				List<BuiltinTemplateDef> defs = objectMapper.readValue(in,
						new TypeReference<List<BuiltinTemplateDef>>() {
						});
				if (defs == null || defs.isEmpty()) {
					log.warn("BuiltinTemplateInitializer skipped: builtin-templates.json is empty");
					return;
				}

				int inserted = 0;
				int skipped = 0;
				for (BuiltinTemplateDef def : defs) {
					if (def == null || def.getSerialNo() == null) {
						continue;
					}
					Long count = templateMapper
							.selectCount(new QueryWrapper<Template>().eq("serial_no", def.getSerialNo()));
					if (count != null && count > 0) {
						skipped++;
						continue;
					}
					Template template = new Template();
					template.setSerialNo(def.getSerialNo());
					template.setName(def.getName());
					template.setQuestionType(SurveySchema.QuestionType.valueOf(def.getQuestionType()));
					template.setMode(ProjectModeEnum.valueOf(def.getMode()));
					template.setCategory(def.getCategory());
					template.setTag(def.getTag());
					template.setShared(def.getShared());
					template.setPriority(def.getPriority());
					template.setTemplate(def.getTemplate());
					// 显式设置归属，strictInsertFill 仅在字段为空时填充，故不会被覆盖为 null
					template.setCreateBy(SYSTEM_ADMIN_ID);
					templateMapper.insert(template);
					inserted++;
				}
				log.info("BuiltinTemplateInitializer done: inserted={}, skipped(already exist)={}", inserted, skipped);
			}
		}
		catch (Throwable ex) {
			// 捕获一切异常/错误，内置模板缺失仅影响「开箱即用」，不影响登录与核心功能
			log.warn("BuiltinTemplateInitializer skipped: {}", ex.toString());
		}
	}

	/**
	 * builtin-templates.json 的结构映射（仅取种子所需字段；template 直接反序列化为 SurveySchema）。
	 */
	@Data
	static class BuiltinTemplateDef {

		private String serialNo;

		private String name;

		private String questionType;

		private String mode;

		private String category;

		private String[] tag;

		private Integer shared;

		private Integer priority;

		private SurveySchema template;

	}

}
