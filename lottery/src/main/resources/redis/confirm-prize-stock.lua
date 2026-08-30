if redis.call('HGET', KEYS[1], '_status') ~= 'PENDING' then
    return 0
end
redis.call('HSET', KEYS[1], '_status', 'CONFIRMED')
redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[1]))
return 1
