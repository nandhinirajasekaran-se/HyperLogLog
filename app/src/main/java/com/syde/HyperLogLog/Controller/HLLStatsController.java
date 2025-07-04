package com.syde.HyperLogLog.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

@RestController
public class HLLStatsController {

    private final Jedis jedis = new Jedis("redis", 6379);

    @GetMapping("/stats/{siteId}")
    public long getStats(@PathVariable String siteId) {
        return jedis.pfcount("hll:" + siteId);
    }
}

