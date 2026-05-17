# spring-cache-pattern

A reusable Spring Boot library that centralizes and standardizes caching strategies across applications using Redis, Caffeine, and distributed cache solutions.

## Overview

This library abstracts cache providers and allows applications to use local and distributed caching with standardized configurations.

---

## Features

- Redis support
- Caffeine support
- Local cache
- Distributed cache
- TTL configuration
- Cache invalidation
- Multi-layer cache
- Annotation-based caching
- Metrics integration

---

## Supported Providers

- Redis
- Caffeine
- Hazelcast
- Ehcache

---

## Example

```java
@Cacheable(
    value="users",
    key="#id"
)
public User findById(Long id){
}
```

---

## Architecture

Application
↓
Cache Layer
↓
Redis / Caffeine
↓
Data Source

---

## Project Structure

src/
├── redis/
├── caffeine/
├── config/
├── annotations/
├── strategy/
└── metrics/

---

## Future Features

- Cache warmup
- Distributed invalidation
- Cache versioning
- Hybrid cache strategy
