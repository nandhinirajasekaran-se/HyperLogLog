package com.syde.HyperLogLog;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import redis.clients.jedis.Jedis;

import java.util.Properties;

@SpringBootApplication
public class HyperLogLogApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(HyperLogLogApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "hll-unique-user-estimator");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv().getOrDefault("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092"));

		StreamsBuilder builder = new StreamsBuilder();
		KStream<String, String> inputStream = builder.stream("events");

		ObjectMapper mapper = new ObjectMapper();
		Jedis redis = new Jedis("localhost", 6379);

		inputStream.foreach((key, value) -> {
			try {
				Event e = mapper.readValue(value, Event.class);
				String redisKey = "hll:" + e.site_id;
				redis.pfadd(redisKey, e.user_id);
			} catch (Exception ex) {
				System.err.println("Failed to parse message: " + ex.getMessage());
			}
		});

		KafkaStreams streams = new KafkaStreams(builder.build(), props);
		streams.start();

		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

	static class Event {
		public String user_id;
		public String site_id;
		public String timestamp;
	}

}
