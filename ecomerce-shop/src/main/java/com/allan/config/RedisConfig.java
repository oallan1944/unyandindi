package com.allan.config;

import com.allan.model.Promotion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * Redis bean configuration.
 *
 * <p><strong>Why this exists:</strong> Spring Boot's auto-configuration only
 * provides a raw {@code RedisTemplate<Object, Object>} bean.
 * {@code PromotionCacheService} depends on a specifically-typed
 * {@code RedisTemplate<String, List<Promotion>>}, which Spring's
 * generics-aware injection treats as a distinct bean requirement — hence
 * the explicit {@code @Bean} here rather than relying on auto-configuration.
 *
 * <p><strong>JSON serialization, not JDK serialization:</strong> the default
 * {@code RedisTemplate} value serializer is {@code JdkSerializationRedisSerializer},
 * which requires every cached type to implement {@code Serializable} and
 * produces opaque binary values you can't inspect in Redis directly. Using
 * {@link GenericJackson2JsonRedisSerializer} instead gives readable JSON in
 * Redis and works with plain (non-{@code Serializable}) classes.
 *
 * <p><strong>{@code JavaTimeModule} is required</strong> — {@code Promotion}
 * carries {@code LocalDateTime} fields ({@code startsAt}/{@code endsAt}).
 * Jackson's default {@code ObjectMapper} cannot serialize {@code java.time}
 * types without this module registered; omitting it fails at cache-write
 * time with an {@code InvalidDefinitionException}.
 *
 * <p><strong>{@code activateDefaultTyping} is required</strong> for a
 * generic collection type like {@code List<Promotion>} — without embedded
 * type metadata in the JSON, deserialization on a cache read produces a
 * {@code List<LinkedHashMap>} instead of {@code List<Promotion>}, failing
 * with a {@code ClassCastException} at the call site.
 *
 * <p><strong>Still open:</strong> if {@code Promotion} has lazy
 * ({@code FetchType.LAZY}) associations (rules, rewards), serializing it
 * outside its originating transaction will throw
 * {@code LazyInitializationException} regardless of this config — see the
 * caveat already documented on {@code PromotionCacheService}. This class
 * fixes the missing-bean startup failure; it does not resolve that separate
 * lazy-loading risk.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, List<Promotion>> promotionRedisTemplate(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisTemplate<String, List<Promotion>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}