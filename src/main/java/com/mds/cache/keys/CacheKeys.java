package com.mds.cache.keys;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for TTL duration parsing in {@link com.mds.cache.config.CacheCustomConfig}.
 *
 * <p>Holds time-unit suffixes ({@link #HOURS}, {@link #MINUTES},
 * {@link #SECONDS}) and default values used when converting
 * configuration strings like {@code "5m"} or {@code "2h"} into
 * {@link java.time.Duration} instances.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKeys {

  public static final String EMPTY = "";
  public static final String HOURS = "h";
  public static final String MINUTES = "m";
  public static final String SECONDS = "s";
  public static final int ONE_INDEX = 1;
  public static final long ONE_MINUTES = 1L;

}
