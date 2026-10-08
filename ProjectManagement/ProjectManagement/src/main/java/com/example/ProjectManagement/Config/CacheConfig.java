package com.example.ProjectManagement.Config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_USERS = "users";
    public static final String CACHE_PROJECTS = "projects";
    public static final String CACHE_PROJECT_BY_ID = "projectById";
    public static final String CACHE_PROJECT_TASKS = "projectTasks";
    public static final String CACHE_PROJECT_MEMBERS = "projectMembers";
    public static final String CACHE_DASHBOARD = "dashboardSummary";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_USERS,
                CACHE_PROJECTS,
                CACHE_PROJECT_BY_ID,
                CACHE_PROJECT_TASKS,
                CACHE_PROJECT_MEMBERS,
                CACHE_DASHBOARD
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());

        return cacheManager;
    }
}
