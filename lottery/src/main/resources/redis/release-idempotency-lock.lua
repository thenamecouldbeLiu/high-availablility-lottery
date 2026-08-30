-- 安全釋放 request idempotency lock。
-- KEYS[1]：冪等鎖 key。
-- ARGV[1]：取得鎖時產生的唯一 token。

-- 必須確認 token 仍屬於目前呼叫者，避免舊請求刪除已被其他請求重新取得的鎖。
if redis.call('GET', KEYS[1]) == ARGV[1] then
    -- 比對與刪除在同一支 Lua 中原子完成，不會出現 check/delete race condition。
    return redis.call('DEL', KEYS[1])
end

-- key 不存在或 token 不符時不進行任何操作。
return 0
