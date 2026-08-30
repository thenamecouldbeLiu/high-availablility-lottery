if redis.call('HGET', KEYS[1], '_status') ~= 'PENDING' then
    return 0
end

local index = 1
while true do
    local stockKey = redis.call('HGET', KEYS[1], 'key:' .. tostring(index))
    if not stockKey then
        break
    end
    local quantity = tonumber(redis.call('HGET', KEYS[1], tostring(index)) or '0')
    if quantity > 0 then
        redis.call('INCRBY', stockKey, quantity)
    end
    redis.call('PEXPIRE', stockKey, tonumber(ARGV[1]) * 2)
    index = index + 1
end

redis.call('HSET', KEYS[1], '_status', 'RELEASED')
redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[1]))
return 1
