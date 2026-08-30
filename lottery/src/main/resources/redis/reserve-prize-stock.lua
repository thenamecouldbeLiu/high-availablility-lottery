-- 一次原子預留同一 campaign 的多個獎項庫存。
-- KEYS[1]：reservation hash key，包含 campaignId 與 eventId。
-- KEYS[2]：該 campaign 的 pending reservation sorted-set key。
-- KEYS[3...]：依 prizeId 排序後的 stock keys。
-- ARGV[1]：reservation 判定逾期的 TTL（毫秒）。
-- ARGV[2...]：與 KEYS[3...] 一一對應的要求數量。
-- 回傳：與 stock keys 相同順序的實際預留數量陣列。

local reservationKey = KEYS[1]
local pendingKey = KEYS[2]
local ttl = tonumber(ARGV[1])

-- eventId 相同的重試直接回傳第一次的預留結果，不會再次扣除庫存。
if redis.call('EXISTS', reservationKey) == 1 then
    local existing = {}
    for index = 3, #KEYS do
        table.insert(existing, tonumber(redis.call('HGET', reservationKey, tostring(index - 2)) or '0'))
    end
    return existing
end

local reserved = {}
for index = 3, #KEYS do
    -- stock key 不存在時視為 0；Java 端會先用 DB remainingStock 執行 SETNX 初始化。
    local available = tonumber(redis.call('GET', KEYS[index]) or '0')
    local requested = tonumber(ARGV[index - 1])
    -- 庫存不足時只預留剩餘數量，其餘抽獎結果會降級為 NO_PRIZE。
    local quantity = math.min(available, requested)
    if quantity > 0 then
        -- Lua 在 Redis 中原子執行，多個 consumer 不會對同一份庫存超賣。
        redis.call('DECRBY', KEYS[index], quantity)
    end
    -- Stock 是 DB 的 cache；保留較長 TTL，過期後可重新從 PostgreSQL 初始化以修復漂移。
    redis.call('PEXPIRE', KEYS[index], ttl * 4)
    -- 同時保存實際數量和 stock key，rollback/release 時才能逐項歸還。
    redis.call('HSET', reservationKey, tostring(index - 2), quantity)
    redis.call('HSET', reservationKey, 'key:' .. tostring(index - 2), KEYS[index])
    table.insert(reserved, quantity)
end

-- 新 reservation 從 PENDING 開始，等待 DB transaction confirm 或 release。
redis.call('HSET', reservationKey, '_status', 'PENDING')
-- 使用 Redis server time，避免不同 application instance 的系統時間偏差。
local currentTime = redis.call('TIME')
local deadline = currentTime[1] * 1000 + math.floor(currentTime[2] / 1000) + ttl
-- score 是逾期時間；cron 使用 ZRANGEBYSCORE 找出需要確認或補償的 reservation。
redis.call('ZADD', pendingKey, deadline, reservationKey)
-- Reservation 本體必須比逾期時間活得更久，讓 cron 有時間讀取預留明細並歸還庫存。
redis.call('PEXPIRE', reservationKey, ttl * 3)
return reserved
