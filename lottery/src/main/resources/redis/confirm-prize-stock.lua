-- 確認庫存預留：DB transaction 成功後呼叫。
-- KEYS[1]：reservation hash key。
-- KEYS[2]：該 campaign 的 pending reservation sorted-set key。
-- ARGV[1]：確認紀錄的保留時間（毫秒），供除錯與短期冪等判斷使用。

-- 只有 PENDING 可以轉成 CONFIRMED。重複 confirm、已 release 或紀錄已過期時不再改變庫存。
if redis.call('HGET', KEYS[1], '_status') ~= 'PENDING' then
    -- 即使 reservation 已不在，也要清除 pending index，避免 cron 不斷重複掃描。
    redis.call('ZREM', KEYS[2], KEYS[1])
    return 0
end

-- DB 已成功扣除並保存 draw result，因此只確認 reservation，不歸還 Redis 庫存。
redis.call('HSET', KEYS[1], '_status', 'CONFIRMED')
-- 暫時保留確認紀錄，TTL 到期後讓 Redis 自動回收。
redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[1]))
-- 已完成，不再需要 cron 追蹤。
redis.call('ZREM', KEYS[2], KEYS[1])
-- 1 代表這次成功完成 PENDING -> CONFIRMED。
return 1
