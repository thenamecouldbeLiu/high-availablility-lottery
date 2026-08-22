package com.interview.lottory.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorUtilTest {
    @Test
    void shouldGenerateUuidVersion7() {
        var uuid = IdGeneratorUtil.nextUuid();

        assertEquals(7, uuid.version());
        assertEquals(uuid, java.util.UUID.fromString(uuid.toString()));
    }

    @Test
    void shouldGenerateDistinctSnowflakeIds() {
        assertNotEquals(IdGeneratorUtil.nextSnowflakeId(), IdGeneratorUtil.nextSnowflakeId());
    }
}
