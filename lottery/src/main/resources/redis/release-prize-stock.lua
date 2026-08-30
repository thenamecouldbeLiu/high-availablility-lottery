-- 釋放庫存預留：DB transaction rollback 或 cron 判定 draw 未成功時呼叫。
-- KEYS[1]：reservation hash key。
-- KEYS[2]：該 campaign 的 pending reservation sorted-set key。
-- ARGV[1]：release 紀錄的保留時間（毫秒）。

-- 只有 PENDING reservation 可以歸還庫存，確保重複 release 不會重複加庫存。
if redis.call('HGET', KEYS[1], '_status') ~= 'PENDING' then
    -- 清除可能殘留的 pending index，避免 cron 重複處理。
    redis.call('ZREM', KEYS[2], KEYS[1])
    return 0
end

-- reservation 使用以下成對欄位保存每個獎項：
--   key:1 = Redis stock key，1 = 預留數量
--   key:2 = Redis stock key，2 = 預留數量
local index = 1
while true do
    local stockKey = redis.call('HGET', KEYS[1], 'key:' .. tostring(index))
    -- index 必須連續；讀不到下一組代表已處理全部獎項。
    if not stockKey then
        break
    end
    local quantity = tonumber(redis.call('HGET', KEYS[1], tostring(index)) or '0')
    if quantity > 0 then
        -- 將先前 DECRBY 的數量原子加回庫存。
        redis.call('INCRBY', stockKey, quantity)
    end
    -- 延長 stock cache 壽命，讓歸還後的正確數值可繼續使用；最終仍會從 DB 重建。
    redis.call('PEXPIRE', stockKey, tonumber(ARGV[1]) * 2)
    index = index + 1
end

-- 保留 RELEASED 狀態一段時間，讓重複訊息可以辨識這筆 reservation 已處理。
redis.call('HSET', KEYS[1], '_status', 'RELEASED')
redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[1]))
-- 已補償完成，不再需要 cron 追蹤。
redis.call('ZREM', KEYS[2], KEYS[1])
-- 1 代表這次成功釋放 reservation 並歸還庫存。
return 1
