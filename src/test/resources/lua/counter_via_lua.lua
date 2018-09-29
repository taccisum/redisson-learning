local count = redis.call('get', KEYS[1])
if(tonumber(count) < tonumber(ARGV[1])) then
    redis.call('incr', KEYS[1])
    return true
else
    return false
end