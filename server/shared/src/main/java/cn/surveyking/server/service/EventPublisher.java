package cn.surveyking.server.service;

/**
 * 事件发布与开放集成（PRD-09）
 *
 * @author eng-koudouma
 */
public interface EventPublisher {

	/**
	 * 发布事件：异步推送到订阅该事件的启用 Webhook（带 HMAC-SHA256 签名），失败重试落投递记录
	 * 
	 * @param event   事件类型（ANSWER_SUBMITTED/EXAM_FINISHED/PROJECT_PUBLISHED/PROJECT_REVOKED/USER_CREATED）
	 * @param payload 载荷（Map/对象，序列化为 JSON）
	 */
	void publish(String event, Object payload);

}
