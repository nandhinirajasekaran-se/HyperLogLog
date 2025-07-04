package com.syde.HyperLogLog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class HLLConsumer {

    private final Jedis jedis = new Jedis("redis", 6379);

    @KafkaListener(topics = "events", groupId = "hll-spring")
    public void consume(String message) {
        System.out.println("✅ Kafka message received: " + message);  // <--- This is important

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(message);
            String userId = node.get("user_id").asText();
            String siteId = node.get("site_id").asText();
            jedis.pfadd("hll:" + siteId, userId);
            System.out.println("🔁 Updated Redis HLL for site: " + siteId + ", user: " + userId);
        } catch (Exception e) {
            System.err.println("❌ Error parsing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

