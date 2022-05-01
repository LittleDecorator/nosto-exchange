package com.nosto.exchange.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

  @Bean
  public Caffeine<Object, Object> caffeineConfig(CacheProperties properties) {
    return Caffeine.newBuilder()
      .expireAfterWrite(properties.getTtl())
      .maximumSize(properties.getMaxSize())
      .initialCapacity(properties.getInitialSize());
  }

  @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
    var caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(caffeine);
    return caffeineCacheManager;
  }

}
