
-- ----------------------------
-- Table structure for t_account
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_account (
    id varchar(64) NOT NULL COMMENT 'ID',
    user_type varchar(100) NOT NULL DEFAULT 'SysUser' COMMENT '用户类型',
    user_id varchar(64) NOT NULL COMMENT '用户ID',
    auth_type varchar(20) NOT NULL DEFAULT 'PWD' COMMENT '认证方式',
    auth_account varchar(100) NOT NULL COMMENT '用户名',
    ext_uid varchar(128) DEFAULT NULL COMMENT 'IdP侧唯一标识(SSO,PRD-02)',
    auth_secret varchar(64) DEFAULT NULL COMMENT '密码',
    secret_salt varchar(32) DEFAULT NULL COMMENT '加密盐',
    status int(11) NOT NULL DEFAULT '1' COMMENT '用户状态',
    token_version int NOT NULL DEFAULT '0' COMMENT 'token版本号，重置密码后自增使旧token失效',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_account
-- ----------------------------
BEGIN;
INSERT INTO t_account VALUES ('1457995482310680578', 'SysUser', '1457995481966747649', 'PWD', 'admin', '$2a$10$z77KkXgV2pbmbafS3DzG0e9/o9lpskz3bBh1k9pE1LUa7oUNaT13i', NULL, 1, 0, 0, '2021-11-09 16:56:26', NULL, '2022-02-01 23:57:27', '1457995481966747649');
COMMIT;

-- ----------------------------
-- Table structure for t_answer
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_answer (
    id varchar(64)  NOT NULL,
    project_id varchar(64)  DEFAULT NULL,
    answer text  COMMENT '问卷答案',
    attachment varchar(1024)  DEFAULT NULL COMMENT '问卷元数据',
    meta_info text  COMMENT '问卷元数据',
    temp_save int(11) DEFAULT NULL COMMENT '0暂存 1已完成',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_answer
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_comm_dict
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_comm_dict (
    id varchar(64)  NOT NULL,
    code varchar(256)  DEFAULT NULL COMMENT '字典编码',
    name varchar(256)  DEFAULT NULL COMMENT '字典中文名称',
    remark varchar(256)  DEFAULT NULL COMMENT '备注信息',
    dict_type int(11) DEFAULT '1' COMMENT '字典类型 1:问卷字典 2:系统字典',
    create_at datetime DEFAULT NULL COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at datetime DEFAULT NULL COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_comm_dict
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_comm_dict_item
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_comm_dict_item (
    id varchar(64)  NOT NULL,
    dict_code varchar(256)  DEFAULT NULL COMMENT '字典编码',
    item_name varchar(256)  DEFAULT NULL COMMENT '字典项中文名称',
    item_value varchar(256)  NOT NULL COMMENT '字典项值',
    item_order int(11) DEFAULT NULL COMMENT '字典顺序',
    item_level int(11) DEFAULT NULL COMMENT '层级',
    parent_item_value varchar(64)  DEFAULT NULL COMMENT '父字典项值',
    create_at datetime DEFAULT NULL COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at datetime DEFAULT NULL COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id, item_value)
    )  ;

-- ----------------------------
-- Records of t_comm_dict_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_dashboard
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_dashboard (
    id varchar(64) NOT NULL COMMENT 'ID',
    key varchar(256) NOT NULL COMMENT '仪表盘组件key',
    type int(2) DEFAULT NULL COMMENT '仪表盘分类',
    project_id varchar(64) DEFAULT NULL COMMENT '项目ID',
    setting varchar(1024)   DEFAULT NULL COMMENT '仪表盘设置',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_dashboard
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_dept
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_dept (
    id varchar(64) NOT NULL COMMENT 'ID',
    parent_id varchar(64) NOT NULL,
    name varchar(64) DEFAULT NULL COMMENT '名称',
    short_name varchar(64) NOT NULL COMMENT '简称',
    code varchar(64) DEFAULT NULL COMMENT '数据权限类型',
    manager_id varchar(64) DEFAULT NULL COMMENT '扩展字段',
    sort_code int(11) DEFAULT NULL,
    property_json varchar(256) DEFAULT NULL COMMENT '扩展字段',
    status varchar(20) DEFAULT NULL COMMENT '扩展字段',
    remark varchar(256) DEFAULT NULL COMMENT '扩展字段',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_dept
-- ----------------------------
BEGIN;
INSERT INTO t_dept VALUES ('1', '0', '卷王', 'juanwang', 'juanwang', '1457995481966747649', NULL, NULL, NULL, NULL, 0, '2021-11-21 14:12:08', NULL, '2021-11-21 14:22:58', '1457995481966747649');
COMMIT;

-- ----------------------------
-- Table structure for t_file
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_file (
    id varchar(64)  NOT NULL,
    original_name varchar(256)  DEFAULT NULL,
    file_name varchar(256)  DEFAULT NULL,
    file_path varchar(512)  DEFAULT NULL,
    thumb_file_path varchar(512)  DEFAULT NULL,
    storage_type int(11) DEFAULT NULL,
    shared int(11) DEFAULT '0',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_file
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_flow_entry
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_flow_entry (
    id varchar(64)  NOT NULL COMMENT '主键',
    project_id varchar(64)  DEFAULT NULL COMMENT '流程定义key',
    process_definition_id varchar(64)  DEFAULT NULL COMMENT '流程定义 id',
    deploy_id varchar(64)  DEFAULT NULL COMMENT '部署id',
    bpmn_xml longtext  COMMENT '流程XML',
    nodes longtext  COMMENT '流程节点',
    icon varchar(256)  DEFAULT NULL COMMENT '流程图标',
    status int(11) DEFAULT '0' COMMENT '0未发布 1已发布',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_flow_entry
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_flow_entry_node
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_flow_entry_node (
    id varchar(64)  NOT NULL COMMENT '节点id',
    name varchar(256)  DEFAULT NULL COMMENT '节点名称',
    project_id varchar(64)  DEFAULT NULL COMMENT '项目id',
    task_type int(11) DEFAULT NULL COMMENT '流程节点类型',
    field_permission text  COMMENT '字段权限',
    setting text  COMMENT '流程设置',
    identity text  COMMENT '授权用户',
    expression text  COMMENT '表达式',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_flow_entry_node
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_flow_entry_publish
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_flow_entry_publish (
    id varchar(64)  NOT NULL COMMENT '流程部署Id',
    entry_id bigint(20) NOT NULL COMMENT '流程Id',
    process_definition_id varchar(64)  NOT NULL COMMENT '流程引擎的定义Id',
    publish_version int(11) NOT NULL COMMENT '发布版本',
    active_status bit(1) NOT NULL COMMENT '激活状态',
    main_version bit(1) NOT NULL COMMENT '是否为主版本',
    publish_time datetime NOT NULL COMMENT '发布时间',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    ) ;

-- ----------------------------
-- Records of t_flow_entry_publish
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_flow_instance
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_flow_instance (
    id varchar(64)  NOT NULL COMMENT '流程实例ID',
    project_id varchar(64)  DEFAULT NULL COMMENT '项目id',
    answer_id varchar(64)  DEFAULT NULL COMMENT '答案id',
    status varchar(64)  DEFAULT NULL COMMENT '当前状态',
    approval_stage varchar(256)  DEFAULT NULL COMMENT '审批阶段',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_flow_instance
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_flow_operation
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_flow_operation (
    id varchar(64)  NOT NULL COMMENT '主键id',
    instance_id varchar(64)  DEFAULT NULL COMMENT '流程实例id',
    project_id varchar(64)  DEFAULT NULL COMMENT '项目id',
    answer_id varchar(64)  DEFAULT NULL COMMENT '答案id',
    activity_id varchar(64)  DEFAULT NULL COMMENT '任务对应的xml节点id',
    task_id varchar(64)  DEFAULT NULL COMMENT '任务id',
    task_name varchar(64)  DEFAULT NULL COMMENT '任务名称',
    task_type int(11) DEFAULT NULL COMMENT '任务类型',
    approval_type varchar(64)  DEFAULT NULL COMMENT '审批类型',
    comment varchar(64)  DEFAULT NULL COMMENT '注释内容',
    delegate_assignee varchar(64)  DEFAULT NULL COMMENT '委托指定人',
    answer varchar(1024)  DEFAULT NULL COMMENT '当前节点答案',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    new_activity_id varchar(64)  DEFAULT NULL COMMENT '新的任务节点id',
    latest bit(1) DEFAULT NULL COMMENT '当前流程最新操作',
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_flow_operation
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_flow_operation_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_flow_operation_user (
    id varchar(64)  NOT NULL COMMENT '节点id',
    operation_id varchar(64)  DEFAULT NULL COMMENT '操作id',
    user_id varchar(64)  DEFAULT NULL COMMENT '用户id',
    group_id varchar(256)  DEFAULT NULL COMMENT '组id',
    link_type varchar(64)  DEFAULT NULL COMMENT '用户类型',
    latest TINYINT NOT NULL DEFAULT 1 COMMENT '是否最新记录',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    ) ;

-- ----------------------------
-- Records of t_flow_operation_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_position
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_position (
    id varchar(64) NOT NULL COMMENT 'ID',
    name varchar(50) NOT NULL,
    code varchar(20) DEFAULT NULL,
    is_virtual tinyint(1) NOT NULL COMMENT '是否虚拟岗',
    data_permission_type varchar(256) DEFAULT NULL COMMENT '数据权限类型',
    property_json varchar(20) DEFAULT NULL COMMENT '扩展字段',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_position
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_project
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_project (
    id varchar(64)  NOT NULL,
    short_id varchar(32)  DEFAULT NULL,
    name varchar(64)  DEFAULT NULL COMMENT '项目名称',
    survey longtext  COMMENT '问卷',
    setting text  DEFAULT NULL COMMENT '问卷设置',
    status int(11) DEFAULT '0' COMMENT '0未发布 1已发布',
    belong_group varchar(256)  DEFAULT NULL,
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY short_id (short_id)
    )  ;

-- ----------------------------
-- Records of t_project
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_project_partner
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_project_partner (
    id varchar(64)  NOT NULL,
    project_id varchar(64)  DEFAULT NULL COMMENT '项目id',
    type int(2) DEFAULT NULL COMMENT '参与者类型',
    user_id varchar(64)  DEFAULT NULL COMMENT '参与者id',
    group_id varchar(64)  DEFAULT NULL COMMENT '参与组id',
    data_permission text  COMMENT '数据权限',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    ) ;

-- ----------------------------
-- Records of t_project_partner
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_repo
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_repo (
    id varchar(64)  NOT NULL,
    name varchar(64)  DEFAULT NULL COMMENT '标题',
    description varchar(512)  DEFAULT NULL COMMENT '备注',
    category varchar(64)  DEFAULT NULL COMMENT '题库分类',
    mode varchar(32)  DEFAULT NULL COMMENT 'survey问卷 exam考试',
    shared tinyint(1) DEFAULT '0' COMMENT '1共享 0私有',
    tag varchar(512)  DEFAULT NULL COMMENT '标签',
    priority int(11) DEFAULT NULL COMMENT '排序优先级',
    setting text  COMMENT '设置',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    is_practice tinyint(1) DEFAULT NULL COMMENT '添加到练习题库 1是 0否',
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_repo
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_repo_template
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_repo_template (
    id varchar(64)  NOT NULL,
    template_id varchar(64)  DEFAULT NULL COMMENT '模板id',
    repo_id varchar(64)  DEFAULT NULL COMMENT '模板库id',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_repo_template
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_role (
    id varchar(64) NOT NULL COMMENT 'ID',
    name varchar(50) NOT NULL COMMENT '名称',
    code varchar(50) NOT NULL COMMENT '编码',
    remark varchar(100) DEFAULT NULL COMMENT '备注',
    authority varchar(3000) DEFAULT NULL COMMENT '权限列表',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_role
-- ----------------------------
BEGIN;
INSERT INTO t_role VALUES ('1457995481928998914', 'Admin', 'admin', '系统初始化角色', 'answer,answer:list,answer:detail,answer:create,answer:update,answer:delete,answer:export,file,file:detail,file:list,file:import,file:delete,project,project:list,project:detail,project:create,project:update,project:delete,project:report,system,system:role,system:role:list,system:user,system:user:list,system:role:create,system:role:update,system:role:delete,system:user:create,system:user:update,system:user:updatePosition,system:user:delete,position,position:list,position:create,system:position,system:position:update,system:position:delete,system:org,system:org:list,system:org:create,system:org:update,system:org:delete,template,template:list,template:create,template:update,template:delete,system:position:list,system:position:create,system:dept,system:dept:list,system:dept:create,system:dept:update,system:dept:delete,system:audit:list,system:audit:export,user:view-plain,system:setting:list,system:setting:edit', 0, '2021-11-09 16:56:26', NULL, '2022-02-01 23:53:28', '1457995481966747649');
INSERT INTO t_role VALUES ('1462366121347792897', '普通用户', 'role', NULL, 'answer,answer:export,answer:list,answer:detail,answer:create,answer:update,answer:delete,file,file:detail,file:import,file:list,file:delete,project,project:list,project:detail,project:create,project:update,project:delete,project:report,system,system:user,system:user:list,system:role,system:role:list,system:role:create,system:role:update,system:role:delete,system:user:create,system:user:update,system:user:updatePosition,system:user:delete,system:position,system:position:list,system:position:create,system:position:update,system:position:delete,system:org,system:org:list,system:org:create,system:org:update,system:org:delete,template,template:list,template:create,template:update,template:delete', 1, '2021-11-21 18:23:47', '1457995481966747649', '2022-01-27 14:08:14', '1457995481966747649');
COMMIT;

-- ----------------------------
-- Table structure for t_sys_info
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_sys_info (
    id varchar(64)  NOT NULL COMMENT '主键',
    name varchar(64)  DEFAULT NULL COMMENT '系统名称',
    description varchar(128)  DEFAULT NULL COMMENT '系统描述信息',
    avatar varchar(64)  DEFAULT NULL COMMENT '图标',
    locale varchar(64)  DEFAULT NULL COMMENT '默认语言',
    version varchar(64)  DEFAULT NULL COMMENT '版本号',
    setting varchar(1024) DEFAULT NULL COMMENT '其他系统设置',
    ai_setting varchar(1024) DEFAULT NULL COMMENT 'AI设置',
    register_info varchar(1024) DEFAULT NULL COMMENT '注册信息',
    is_default tinyint(1) DEFAULT NULL COMMENT '是否默认设置',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- 兼容已存在的旧库（t_sys_info 缺失 setting/ai_setting/register_info 三列会导致 /api/system 500）：
-- 因 sql.init.mode=always 每次启动都会执行本脚本，此处幂等补齐列，旧库无需清空数据即可升级。
ALTER TABLE t_sys_info ADD COLUMN IF NOT EXISTS setting varchar(1024) DEFAULT NULL COMMENT '其他系统设置';
ALTER TABLE t_sys_info ADD COLUMN IF NOT EXISTS ai_setting varchar(1024) DEFAULT NULL COMMENT 'AI设置';
ALTER TABLE t_sys_info ADD COLUMN IF NOT EXISTS register_info varchar(1024) DEFAULT NULL COMMENT '注册信息';

-- ----------------------------
-- Records of t_sys_info
-- ----------------------------
BEGIN;
INSERT INTO t_sys_info (id, name, description, avatar, locale, version, setting, ai_setting, register_info, is_default, create_at, create_by, update_at, update_by) VALUES ('1', '卷王', '做更好的调查问卷系统', NULL, 'zh-CN', '1.0.0', NULL, NULL, NULL, 1, '2022-02-11 10:13:19', NULL, '2022-02-11 14:29:03', NULL);
COMMIT;

-- ----------------------------
-- Table structure for t_tag
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_tag (
    id varchar(64)  NOT NULL,
    entity_id varchar(64)  DEFAULT NULL COMMENT '实体ID',
    name varchar(128)  DEFAULT NULL COMMENT '名称',
    category varchar(256)  DEFAULT NULL COMMENT '分类',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_tag
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_template
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_template (
    id varchar(64)  NOT NULL,
    name varchar(64)  DEFAULT NULL COMMENT '模板标题',
    question_type varchar(64)  DEFAULT NULL COMMENT '问题类型',
    template longtext  COMMENT '模板',
    category varchar(256)  DEFAULT NULL COMMENT '模板分类',
    tag varchar(512)  DEFAULT NULL COMMENT '标签',
    priority int(11) DEFAULT NULL COMMENT '排序优先级',
    preview_url varchar(512)  DEFAULT NULL COMMENT '预览地址',
    shared int(11) DEFAULT '0',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    PRIMARY KEY (id)
    ) ;

-- ----------------------------
-- Records of t_template
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_user (
    id varchar(64) NOT NULL COMMENT 'ID',
    name varchar(50) NOT NULL COMMENT '真实姓名',
    dept_id varchar(20) DEFAULT NULL,
    gender varchar(10) NOT NULL COMMENT '性别',
    birthday date DEFAULT NULL COMMENT '出生日期',
    phone varchar(20) DEFAULT NULL COMMENT '手机号',
    email varchar(50) DEFAULT NULL COMMENT 'Email',
    avatar varchar(200) DEFAULT NULL COMMENT '头像地址',
    status int(11) NOT NULL DEFAULT '1' COMMENT '用户状态',
    is_deleted tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    profile varchar(255) DEFAULT NULL COMMENT '个人简介',
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_user
-- ----------------------------
BEGIN;
INSERT INTO t_user VALUES ('1457995481966747649', 'Admin', '1', 'F', NULL, '13800138000', 'sqlzero@sqlzero.com', '1492007810605539329', 1, 0, '2021-11-09 16:56:26', NULL, '2022-02-11 13:29:17', '1457995481966747649', 'hello world');
COMMIT;

-- ----------------------------
-- Table structure for t_user_book
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_user_book (
    id varchar(64)  NOT NULL,
    name varchar(2048)  DEFAULT NULL COMMENT '问题名称',
    template_id varchar(64)  DEFAULT NULL COMMENT '模板ID',
    wrong_times int(11) DEFAULT NULL COMMENT '错误次数',
    correct_times int(11) DEFAULT NULL COMMENT '正确次数',
    note text  COMMENT '笔记',
    status int(11) DEFAULT NULL COMMENT '1标记为简单',
    type int(11) DEFAULT NULL COMMENT '1错题 2收藏',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256)  DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256)  DEFAULT NULL,
    repo_id varchar(256)  DEFAULT NULL,
    is_marked tinyint(1) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_user_book
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_user_position
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_user_position (
    id varchar(64) NOT NULL COMMENT 'ID',
    user_id varchar(64) NOT NULL,
    dept_id varchar(64) DEFAULT NULL,
    position_id varchar(64) DEFAULT NULL COMMENT '数据权限类型',
    is_primary_position tinyint(1) DEFAULT NULL COMMENT '是否主岗',
    propertyJson varchar(256) DEFAULT NULL COMMENT '扩展字段',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_user_position
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_user_role
-- ----------------------------

CREATE TABLE IF NOT EXISTS t_user_role (
    id varchar(64) NOT NULL COMMENT 'ID',
    user_type varchar(100) NOT NULL DEFAULT 'SysUser' COMMENT '用户类型',
    user_id bigint(20) NOT NULL COMMENT '用户ID',
    role_id bigint(20) NOT NULL COMMENT '角色ID',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;

-- ----------------------------
-- Records of t_user_role
-- ----------------------------
BEGIN;
INSERT INTO t_user_role VALUES ('1488542015867121666', 'SysUser', 1457995481966747649, 1457995481928998914,  '2022-02-01 23:57:27', '1457995481966747649', NULL, NULL);
COMMIT;

-- ============================================================================
-- 幂等补齐（fork 缺陷根除）：H2 初始化脚本多张表缺列，与实体 / MySQL 初始化
-- 不一致，导致 SELECT 报 Column XXX not found（典型如 t_user.correct_times）。
-- 以下用 ALTER TABLE ADD COLUMN IF NOT EXISTS 在每次启动时补齐，
-- 旧 H2 库（含已上线用户库）无需清空即可升级。
-- 类型已剥离 CHARACTER SET / COLLATE（H2 不支持）；NOT NULL 放宽为 DEFAULT NULL
-- 以兼容「旧表已有数据」的场景（如 t_dashboard.key），避免 ALTER 因非空约束失败。
-- 覆盖表：t_user / t_answer / t_project / t_project_partner / t_template / t_role / t_dashboard
-- 与 v1.0.3 的 t_sys_info 补齐同源，属同一类缺陷的彻底修复。
-- ============================================================================

-- t_user: 错题答对清除次数
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS correct_times int DEFAULT NULL COMMENT '错题答对清除次数';

-- t_account: SSO 外部标识（PRD-02）
ALTER TABLE t_account ADD COLUMN IF NOT EXISTS ext_uid varchar(128) DEFAULT NULL COMMENT 'IdP侧唯一标识(SSO,PRD-02)';

-- t_answer: 考试 / 暂存相关字段
ALTER TABLE t_answer ADD COLUMN IF NOT EXISTS exam_exercise_type varchar(4) DEFAULT NULL COMMENT '考试练习类型';
ALTER TABLE t_answer ADD COLUMN IF NOT EXISTS exam_info longtext DEFAULT NULL COMMENT '考试信息';
ALTER TABLE t_answer ADD COLUMN IF NOT EXISTS exam_score float DEFAULT NULL COMMENT '考试分数';
ALTER TABLE t_answer ADD COLUMN IF NOT EXISTS repo_id varchar(256) DEFAULT NULL COMMENT '所属题库';
ALTER TABLE t_answer ADD COLUMN IF NOT EXISTS survey longtext DEFAULT NULL COMMENT '问卷';
ALTER TABLE t_answer ADD COLUMN IF NOT EXISTS temp_answer longtext DEFAULT NULL COMMENT '暂存答案';

-- t_project
ALTER TABLE t_project ADD COLUMN IF NOT EXISTS mode varchar(32) DEFAULT NULL COMMENT '问卷模式';
ALTER TABLE t_project ADD COLUMN IF NOT EXISTS parent_id varchar(64) DEFAULT '0' COMMENT '父ID';
ALTER TABLE t_project ADD COLUMN IF NOT EXISTS priority int DEFAULT 1000 COMMENT '优先级';
ALTER TABLE t_project ADD COLUMN IF NOT EXISTS dept_id varchar(64) DEFAULT NULL COMMENT '所属部门(PRD-03 数据权限)';

-- t_project_partner
ALTER TABLE t_project_partner ADD COLUMN IF NOT EXISTS initial_value longtext DEFAULT NULL COMMENT '初始值';
ALTER TABLE t_project_partner ADD COLUMN IF NOT EXISTS status int DEFAULT 0 COMMENT '0未访问 1已访问 2已答题';
ALTER TABLE t_project_partner ADD COLUMN IF NOT EXISTS uid varchar(64) DEFAULT NULL COMMENT '项目内唯一ID';
ALTER TABLE t_project_partner ADD COLUMN IF NOT EXISTS user_name varchar(256) DEFAULT NULL COMMENT '参与者姓名';

-- t_template
ALTER TABLE t_template ADD COLUMN IF NOT EXISTS mode varchar(32) DEFAULT NULL COMMENT '模板模式 survey/exam';
ALTER TABLE t_template ADD COLUMN IF NOT EXISTS repo_id varchar(64) DEFAULT NULL COMMENT '所属题库';
ALTER TABLE t_template ADD COLUMN IF NOT EXISTS serial_no varchar(256) DEFAULT NULL COMMENT '序号';

-- t_role
ALTER TABLE t_role ADD COLUMN IF NOT EXISTS status tinyint(1) DEFAULT 1 COMMENT '1激活 0失活';

-- t_dashboard（key 在 MySQL 为 NOT NULL，旧表已有数据时放宽可空避免 ALTER 失败）
ALTER TABLE t_dashboard ADD COLUMN IF NOT EXISTS `key` varchar(256) DEFAULT NULL COMMENT '仪表盘组件key';

-- ----------------------------
-- Table structure for t_audit_log (PRD-01 审计日志中心)
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_audit_log (
    id varchar(64) NOT NULL,
    user_id varchar(64) DEFAULT NULL COMMENT '操作人ID',
    username varchar(100) NOT NULL COMMENT '操作人',
    ip varchar(64) NOT NULL COMMENT '来源IP',
    module varchar(32) NOT NULL COMMENT '模块 survey/exam/user/role/dept/template/system',
    action varchar(32) NOT NULL COMMENT '动作 create/update/delete/publish/revoke/export/reset',
    object_type varchar(32) DEFAULT NULL COMMENT '对象类型',
    object_id varchar(64) DEFAULT NULL COMMENT '对象ID',
    detail varchar(512) DEFAULT NULL COMMENT '人类可读摘要，不含敏感值',
    result tinyint(1) NOT NULL DEFAULT '1' COMMENT '1成功 0失败',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
    )  ;
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON t_audit_log (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created ON t_audit_log (create_at);
CREATE INDEX IF NOT EXISTS idx_audit_log_module_action ON t_audit_log (module, action);

-- ----------------------------
-- Table structure for t_login_log (PRD-01 登录日志)
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_login_log (
    id varchar(64) NOT NULL,
    user_id varchar(64) DEFAULT NULL COMMENT '用户ID',
    username varchar(100) NOT NULL COMMENT '登录账号',
    ip varchar(64) NOT NULL COMMENT '来源IP',
    user_agent varchar(256) DEFAULT NULL COMMENT '浏览器UA',
    success tinyint(1) NOT NULL DEFAULT '0' COMMENT '1成功 0失败',
    fail_reason varchar(64) DEFAULT NULL COMMENT '失败原因 bad_password/locked/captcha',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
    )  ;
CREATE INDEX IF NOT EXISTS idx_login_log_username ON t_login_log (username);
CREATE INDEX IF NOT EXISTS idx_login_log_created ON t_login_log (create_at);

-- ----------------------------
-- Table structure for t_account_lock (PRD-01 登录失败锁定)
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_account_lock (
    username varchar(100) NOT NULL COMMENT '登录账号(主键)',
    fail_count int NOT NULL DEFAULT '0' COMMENT '连续失败次数',
    locked_until datetime DEFAULT NULL COMMENT '锁定截止时间',
    update_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (username)
    )  ;

-- ----------------------------
-- Table structure for t_auth_provider (PRD-02 SSO/目录集成)
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_auth_provider (
    id varchar(64) NOT NULL,
    type varchar(32) NOT NULL COMMENT 'LDAP/OIDC/WECHAT_WORK/DINGTALK/FEISHU',
    enabled tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
    auto_create tinyint(1) NOT NULL DEFAULT '1' COMMENT 'SSO首次登录自动建号',
    config text DEFAULT NULL COMMENT 'JSON: url/baseDn/filter/ClientId/Secret/CorpId/AgentId...',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;
CREATE UNIQUE INDEX IF NOT EXISTS uk_auth_provider_type ON t_auth_provider (type);

-- ----------------------------
-- Table structure for t_project_notify_rule (PRD-05 分发与提醒规则)
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_project_notify_rule (
    id varchar(64) NOT NULL,
    project_id varchar(64) NOT NULL,
    target_group varchar(255) NOT NULL COMMENT 'D:deptId / R:roleCode / U:id,id',
    channels varchar(128) NOT NULL COMMENT 'EMAIL,WECHAT_WORK_BOT',
    remind_before_end int DEFAULT NULL COMMENT '截止前N天催办',
    overdue_notify tinyint(1) NOT NULL DEFAULT '1' COMMENT '逾期是否提醒',
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    update_at timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;
CREATE INDEX IF NOT EXISTS idx_notify_rule_project ON t_project_notify_rule (project_id);

-- ----------------------------
-- Table structure for t_notification_record (PRD-05 通知发送记录)
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_notification_record (
    id varchar(64) NOT NULL,
    project_id varchar(64) DEFAULT NULL,
    channel varchar(32) NOT NULL,
    receiver varchar(128) NOT NULL COMMENT '手机号/openid/email',
    title varchar(255) DEFAULT NULL,
    status tinyint(1) NOT NULL DEFAULT '0' COMMENT '0待发 1成功 2失败',
    err_msg varchar(512) DEFAULT NULL,
    sent_at datetime DEFAULT NULL,
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by varchar(256) DEFAULT NULL,
    PRIMARY KEY (id)
    )  ;
CREATE INDEX IF NOT EXISTS idx_notification_project ON t_notification_record (project_id);
CREATE INDEX IF NOT EXISTS idx_notification_status ON t_notification_record (status);

SET FOREIGN_KEY_CHECKS = 1;
