package com.mds.cache.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Configuration properties that control Redis-related logging behaviour.
 *
 * <p>Bound to the {@code management.health.custom.redis} prefix.
 * When {@code notShowLog} is {@code true}, all cache log output produced
 * by {@link com.mds.cache.factory.LoggerFactory} is suppressed.
 * Supports Spring Cloud {@link RefreshScope} for runtime toggling.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Data
@Component("cacheLoggerConfigProperties")
@RefreshScope
@ConfigurationProperties(prefix = "management.health.custom.redis")
public class LoggerConfigProperties {

  private boolean notShowLog;

}
