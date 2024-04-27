package com.lys.xydc.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author 陆玉升
 * date: 2023/04/2505/19
 * Description:
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

  @Bean
  public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<Object,Object> redisTemplate = new RedisTemplate<>();
    // 默认的Key序列化器为:JdkSerializationRedisSerializer
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setConnectionFactory(connectionFactory);
    return redisTemplate;
  }

}
