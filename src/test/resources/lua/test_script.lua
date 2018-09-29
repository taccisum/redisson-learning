redis.log(redis.LOG_WARNING, 'test log')
return redis.call('get', KEYS[1])