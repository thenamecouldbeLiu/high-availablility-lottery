local reservationKey = KEYS[1]
local ttl = tonumber(ARGV[1])

if redis.call('EXISTS', reservationKey) == 1 then
    local existing = {}
    for index = 2, #KEYS do
        table.insert(existing, tonumber(redis.call('HGET', reservationKey, tostring(index - 1)) or '0'))
    end
    return existing
end

local reserved = {}
for index = 2, #KEYS do
    local available = tonumber(redis.call('GET', KEYS[index]) or '0')
    local requested = tonumber(ARGV[index])
    local quantity = math.min(available, requested)
    if quantity > 0 then
        redis.call('DECRBY', KEYS[index], quantity)
    end
    redis.call('PEXPIRE', KEYS[index], ttl * 2)
    redis.call('HSET', reservationKey, tostring(index - 1), quantity)
    redis.call('HSET', reservationKey, 'key:' .. tostring(index - 1), KEYS[index])
    table.insert(reserved, quantity)
end

redis.call('HSET', reservationKey, '_status', 'PENDING')
redis.call('PEXPIRE', reservationKey, ttl)
return reserved
