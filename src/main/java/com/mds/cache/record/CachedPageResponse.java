package com.mds.cache.record;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

/**
 * Immutable record representing a paginated response suitable for Redis
 * serialization.
 *
 * <p>Annotated with {@link JsonTypeInfo} so Jackson embeds the concrete
 * class type ({@code @class}) in the JSON payload, enabling polymorphic
 * deserialization when read back from cache.
 *
 * @param <T>           the element type contained in the page.
 * @param content       the list of elements on this page.
 * @param page          the zero-based page index.
 * @param size          the requested page size.
 * @param totalElements the total number of elements across all pages.
 *
 * @author MDS
 * @since 0.0.1-SNAPSHOT
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
public record CachedPageResponse<T>(List<T> content, int page, int size, long totalElements) {
}
