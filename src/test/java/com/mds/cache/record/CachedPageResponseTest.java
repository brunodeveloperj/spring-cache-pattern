package com.mds.cache.record;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CachedPageResponseTest {

    @Test
    public void should_instantiate_record() {
        CachedPageResponse<String> response = new CachedPageResponse<>(List.of("item1", "item2"), 0, 2, 2L);
        assertEquals(2, response.content().size());
        assertEquals(0, response.page());
        assertEquals(2, response.size() );
        assertEquals(2L, response.totalElements());
    }
}
