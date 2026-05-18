package com.mds.cache.factory;


import com.mds.cache.config.properties.LoggerConfigProperties;
import com.mds.cache.enumerator.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centralized logging facade for the cache layer.
 *
 * <p>Delegates to SLF4J at the requested {@link LogLevel} while
 * respecting the {@link LoggerConfigProperties#isNotShowLog()} flag,
 * which allows runtime suppression of all cache-related log output
 * via Spring Cloud {@code @RefreshScope}.
 *
 * <p>Provides overloaded {@code log()} methods for plain messages,
 * messages with throwables, and parameterised templates.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@EnableConfigurationProperties(LoggerConfigProperties.class)
public class LoggerFactory {

  private final LoggerConfigProperties properties;

  public LoggerFactory(@Qualifier("cacheLoggerConfigProperties") LoggerConfigProperties properties) {
    this.properties = properties;
  }

  public void log(LogLevel level, String message) {
    if (properties.isNotShowLog()) return;

    switch (level) {
      case INFO -> log.info(message);
      case DEBUG -> log.debug(message);
      case WARN -> log.warn(message);
      case ERROR -> log.error(message);
      case TRACE -> log.trace(message);
    }
  }

  public void log(LogLevel level, String message, Throwable throwable) {
    if (properties.isNotShowLog()) return;

    switch (level) {
      case ERROR -> log.error(message, throwable);
      case WARN -> log.warn(message, throwable);
      case DEBUG -> log.debug(message, throwable);
      case INFO -> log.info(message, throwable);
      case TRACE -> log.trace(message, throwable);
    }
  }

  public void log(LogLevel level, String message, Object... args) {
    if (properties.isNotShowLog()) return;

    switch (level) {
      case INFO -> log.info(message, args);
      case DEBUG -> log.debug(message, args);
      case WARN -> log.warn(message, args);
      case ERROR -> log.error(message, args);
      case TRACE -> log.trace(message, args);
    }
  }

  public boolean isNotShowLog() {
    return properties.isNotShowLog();
  }

}

