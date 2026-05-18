package com.mds.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring auto-configuration entry point for the MDS Cache Pattern library.
 *
 * <p>Activates when <strong>all</strong> conditions are met:
 * <ol>
 *   <li>At least one cache backend is configured:
 *       {@code redis.configuration} is set <em>or</em>
 *       {@code cache.provider} is set (e.g. {@code caffeine}, {@code composite}).</li>
 *   <li>{@code cache.enabled} / {@code redis.enabled} is {@code true}
 *       (defaults to {@code true} if absent).</li>
 * </ol>
 *
 * <p>If neither property is present, or the kill-switch is {@code false},
 * no cache beans are created and no errors are thrown.
 *
 * <h3>Provider selection</h3>
 * <table>
 *   <tr><th>{@code cache.provider}</th><th>Backend</th></tr>
 *   <tr><td>{@code redis} (default)</td><td>Redis (Standalone or Sentinel)</td></tr>
 *   <tr><td>{@code caffeine}</td><td>Caffeine (local in-memory)</td></tr>
 *   <tr><td>{@code composite}</td><td>Caffeine (L1) + Redis (L2)</td></tr>
 * </table>
 *
 * @author MDS
 * @since 0.0.2
 */
@EnableCaching
@EnableAsync
@Configuration
@ComponentScan
@ConditionalOnExpression(
    "${cache.enabled:${redis.enabled:true}} "
    + "and ('${cache.provider:}' != '' or '${redis.configuration:}' != '')"
)
public class CacheAutoConfiguration {
}
