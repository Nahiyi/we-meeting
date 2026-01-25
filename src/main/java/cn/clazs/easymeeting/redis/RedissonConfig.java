package cn.clazs.easymeeting.redis;

import cn.clazs.easymeeting.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 * 当 messaging.handle.channel=redis （即消息队列策略实现为redis）时启用
 */
@Configuration
@ConditionalOnProperty(
        name = Constants.MESSAGE_HANDLE_CHANNEL, // 使用的策略键名
        havingValue = Constants.MESSAGE_CHANNEL_REDIS // 键值为redis
)
@Slf4j
public class RedissonConfig {

    // 主动注入 YML 中的配置
    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private String port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    /**
     * 创建 RedissonClient Bean
     * 用于消息发布/订阅、分布式锁等功能
     */
    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 必须手动拼接地址并 setAddress，Redisson 要求地址格式为 redis://ip:port
        String redisAddress = String.format("redis://%s:%s", host, port);

        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(redisAddress) // 显式设置
                .setDatabase(database)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        // 如果有密码，才设置密码
        if (password != null && !password.isBlank()) {
            singleServerConfig.setPassword(password);
        }

        // 可选：设置序列化方式为 JSON，否则存进去的数据是乱码
        // config.setCodec(new org.redisson.codec.JsonJacksonCodec());

        log.info("Redisson 客户端配置完成 | host: {}, port:{}", host, port);
        return Redisson.create(config);
    }
}
