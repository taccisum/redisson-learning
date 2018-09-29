local key1 = redis.call('get', KEYS[1])
if (ARGV[1] == 'true') then
    key1 = key1 + 1
end
return key1