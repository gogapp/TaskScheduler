package com.meraki.configuration;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.serialization.Mapper;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public ProxyManager<String> proxyManager(RedissonClient redissonClient) {

        CommandAsyncExecutor executor =
                ((Redisson) redissonClient).getCommandExecutor();

        return RedissonBasedProxyManager
                .builderFor(executor)
                .withKeyMapper(Mapper.STRING)
                .withExpirationStrategy(ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(300))) // prefix keys
                .build();
    }

}
