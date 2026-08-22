package com.interview.lottory.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.github.f4b6a3.uuid.UuidCreator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public final class IdGeneratorUtil {
    private static final long MAX_NODE_ID = 31L;
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(
            resolveNodeId("SNOWFLAKE_WORKER_ID", defaultWorkerId()),
            resolveNodeId("SNOWFLAKE_DATACENTER_ID", defaultDataCenterId())
    );

    private IdGeneratorUtil() {
    }

    public static long nextSnowflakeId() {
        return SNOWFLAKE.nextId();
    }

    public static String nextSnowflakeIdString() {
        return SNOWFLAKE.nextIdStr();
    }

    public static UUID nextUuid() {
        return UuidCreator.getTimeOrderedEpoch();
    }

    public static String nextUuidString() {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }

    private static long resolveNodeId(String environmentName, long defaultValue) {
        String configured = System.getenv(environmentName);
        if (configured == null || configured.isBlank()) {
            return defaultValue;
        }
        try {
            long value = Long.parseLong(configured);
            if (value < 0 || value > MAX_NODE_ID) {
                throw new IllegalStateException(environmentName + " 必須介於 0 到 31");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(environmentName + " 必須是整數", exception);
        }
    }

    private static long defaultWorkerId() {
        return Math.floorMod(ProcessHandle.current().pid(), MAX_NODE_ID + 1);
    }

    private static long defaultDataCenterId() {
        try {
            return Math.floorMod(InetAddress.getLocalHost().getHostName().hashCode(), MAX_NODE_ID + 1);
        } catch (UnknownHostException exception) {
            return 0L;
        }
    }
}
