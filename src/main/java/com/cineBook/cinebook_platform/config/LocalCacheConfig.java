package com.cineBook.cinebook_platform.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Configuration
@Profile("local")
public class LocalCacheConfig {

    // ConcurrentMapCacheManager is Spring's built-in in-memory cache.
    // Just list the cache names your application uses — same names
    // you'll use in @Cacheable annotations.
    /*@Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "browse-theatres",       // BrowseService.browseTheatresForMovie
                "show-offers",           // BrowseService.getOffersForCity
                "seat-layout",           // ShowManagementService.getSeatLayout
                "available-seat-count"   // per-show available seat count
        );
    }*/

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Each cache gets its own Caffeine spec — same TTLs as Redis prod config
        // so local behaviour closely mirrors production behaviour
        manager.registerCustomCache("browse-theatres",
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.SECONDS)
                        .maximumSize(500)
                        .build());

        manager.registerCustomCache("show-offers",
                Caffeine.newBuilder()
                        .expireAfterWrite(300, TimeUnit.SECONDS)
                        .maximumSize(100)
                        .build());

        manager.registerCustomCache("seat-layout",
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.SECONDS)
                        .maximumSize(1000)
                        .build());

        manager.registerCustomCache("available-seat-count",
                Caffeine.newBuilder()
                        .expireAfterWrite(15, TimeUnit.SECONDS)
                        .maximumSize(1000)
                        .build());

        return manager;
    }
}
