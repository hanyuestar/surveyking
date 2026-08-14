package cn.surveyking.server.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 使用 caffeine 自定义 cache，允许针对每个 cacheName 设置单独的 expireTime
 *
 * @author javahuang
 * @date 2021/9/8
 */
@Configuration
@EnableCaching
@ConfigurationProperties("custom-cache")
public class CacheConfig extends CachingConfigurerSupport {

	private Map<String, Duration> entries;

	@Bean
	@Override
	public CacheManager cacheManager() {
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {

			@Override
			protected Cache createConcurrentMapCache(final String name) {
				return new ConcurrentMapCache(name, Caffeine.newBuilder().expireAfter(new Expiry<Object, Object>() {
			@Override
			public long expireAfterCreate(Object key, Object value, long currentTime) {
				// 防御：custom-cache.entries 缺失或本缓存名未配置时，回退 1h 默认 TTL，
				// 避免 entries 为 null / 缺 key 触发空指针导致应用启动即退出。
				Duration ttl = entries != null ? entries.getOrDefault(name, Duration.ofHours(1))
						: Duration.ofHours(1);
				return ttl.toNanos();
			}

					@Override
					public long expireAfterUpdate(Object key, Object value, long currentTime,
							@NonNegative long currentDuration) {
						return currentDuration;
					}

					@Override
					public long expireAfterRead(Object key, Object value, long currentTime,
							@NonNegative long currentDuration) {
						return currentDuration;
					}
				}).maximumSize(100).build().asMap(), false);
			}
		};

		// entries 缺失时不预置缓存名，交由按需懒加载（默认 1h TTL），避免 NPE。
		if (entries != null) {
			cacheManager.setCacheNames(
					entries.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
		}
		return cacheManager;
	}

	public Map<String, Duration> getEntries() {
		return entries;
	}

	public void setEntries(Map<String, Duration> entries) {
		this.entries = entries;
	}

}
