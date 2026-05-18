# spring-cache-pattern

![V1](https://img.shields.io/badge/version-V1_(0.0.1--SNAPSHOT)-blue) ![Java 21](https://img.shields.io/badge/Java-21_(LTS)-orange) ![Spring Boot 4.0.6](https://img.shields.io/badge/Spring_Boot-4.0.6-brightgreen) ![Jackson 3](https://img.shields.io/badge/Jackson-3.x-yellow)

Biblioteca Spring Boot reutilizável e multicache que centraliza e padroniza operações de cache com suporte a **Redis**, **Caffeine** e **Composite** (L1 + L2), retry automático, health check customizado e escopo de visibilidade (session/application/global).

> **Opt-in:** A lib é 100% não-intrusiva. Se o projeto consumidor **não** definir `cache.provider` ou `redis.configuration` no `application.yml`, ou definir `cache.enabled: false`, nenhum bean de cache é criado e **a aplicação sobe normalmente sem nenhum erro**.

## Tech Stack

| Dependência | Versão |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 4.0.6 |
| Spring Cloud | 2025.1.1 (Oakwood) |
| Jackson | 3.x (`tools.jackson.*`) |
| Lombok | gerenciado pelo Spring Boot |

## Dependência

```xml
<dependency>
  <groupId>com.mds</groupId>
  <artifactId>spring-cache-pattern</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

## Funcionalidades

- **Multicache** — Redis, Caffeine ou Composite (L1 Caffeine + L2 Redis) via `cache.provider`
- **Opt-in / Non-intrusive** — nenhum bean é criado sem configuração explícita
- **Kill-switch** — `cache.enabled: false` desliga tudo temporariamente sem apagar configs
- **Redis Standalone e Sentinel** — configuração condicional via `redis.configuration`
- **Caffeine local** — cache in-memory de alta performance (nanosegundos)
- **Composite L1+L2** — Caffeine como cache local rápido, Redis como cache distribuído compartilhado
- **Letuce connection pooling** — pool configurável (max idle, min idle, max wait, max active)
- **RedisCacheManager customizado** — TTL padrão e TTL por cache individual
- **Serialização Jackson 3** — valores armazenados como JSON via `tools.jackson.*` (Java Time built-in)
- **Spring Retry integrado** — retry configurável em operações de cache
- **Fallback gracioso** — `RetryFallbackNullException` e `RetryFallbackFalseException` com resolvers
- **Escopo de cache** — `ScopeEnum` (SESSION, APPLICATION, GLOBAL) com TTL independente por scope
- **Health check customizado** — integrado ao Spring Actuator (só ativa se Actuator estiver no classpath)
- **Logging configurável** — supressão de logs via `management.health.custom.redis.notShowLog`
- **REST diagnóstico** — endpoint `/cache-manager/findBy/{key}` (só ativa se Spring Web estiver no classpath)
- **Async** — operações assíncronas fire-and-forget via `@Async` no repository
- **Secret extraction** — leitura de senha Redis a partir de arquivo ou texto plano

---

## Providers

| `cache.provider` | Backend | Requisito | Latência típica |
|---|---|---|---|
| `redis` (default) | Redis (Standalone ou Sentinel) | `redis.configuration` + Redis no classpath | ~1-5ms |
| `caffeine` | Caffeine (local in-memory) | `caffeine` no classpath | ~10-100ns |
| `composite` | Caffeine (L1) + Redis (L2) | Ambos no classpath | L1 hit: ~ns / L2 hit: ~ms |

### Composite — como funciona

```
Leitura:  App → L1 (Caffeine) → hit? retorna : L2 (Redis) → hit? backfill L1 + retorna : miss
Escrita:  App → L1 (Caffeine) + L2 (Redis) — write-through simultâneo
Deleção:  App → L1 (Caffeine) + L2 (Redis) — ambos invalidados
```

---

## Configuração

### Redis (default)

```yaml
cache:
  enabled: true           # kill-switch (default: true)
  provider: redis          # redis | caffeine | composite

redis:
  configuration: standalone  # ou sentinel
  host: localhost
  port: 6379
  password: /run/secrets/redis-pass
  timeout: 900000
  pool:
    max:
      idle: 10
      active: 20
      wait: 5000
    min:
      idle: 5

cache:
  expire:
    session:
      in:
        minutes: 10
    application:
      in:
        minutes: 60
    global:
      in:
        minutes: -1         # -1 = sem expiração
  retry:
    enable: false
    max:
      attempts: 3
    delay: 1000

redis:
  manager:
    available: true
    default:
      timeToLive: 1m
    initial:
      cache: "{cacheName1: '5m', cacheName2: '2h'}"

management:
  health:
    custom:
      redis:
        enabled: true
        notShowLog: false
```

### Caffeine (local only)

```yaml
cache:
  provider: caffeine
  caffeine:
    ttl-minutes: 10          # TTL padrão para entradas
    max-size: 10000          # máximo de entradas por cache key
```

### Composite (L1 + L2)

```yaml
cache:
  provider: composite
  caffeine:
    ttl-minutes: 5           # L1 TTL curto (in-memory)
    max-size: 10000

redis:
  configuration: standalone
  host: localhost
  port: 6379
  # ... demais configs Redis
```

### Desabilitar temporariamente (kill-switch)

```yaml
cache:
  enabled: false             # toda a lib é desligada — nenhum bean criado
  provider: redis            # configs permanecem para quando reativar
```

> **Retrocompatibilidade:** as properties `redis.enabled`, `redis.retry.*` e `redis.expire.*` continuam funcionando como fallback. As novas properties `cache.*` têm precedência.

---

## Exemplo de uso

```java
@Autowired
private CacheRepository cacheRepository;

// A API é idêntica independente do provider (Redis, Caffeine ou Composite)

// Adicionar ao cache global
cacheRepository.addGlobal("user:123", userObject);

// Buscar do cache global
User cached = cacheRepository.findGlobal("user:123", User.class);

// Adicionar com scope específico
cacheRepository.add("users", ScopeEnum.SESSION, "user:123", userObject);

// Buscar por scope
User sessionUser = cacheRepository.find("users", ScopeEnum.SESSION, "user:123", User.class);

// Listar hash keys de uma chave
List<String> keys = cacheRepository.find("users");

// Evicção
cacheRepository.deleteGlobal("user:123");

// Evicção assíncrona (fire-and-forget)
cacheRepository.deleteGlobalAsync("user:123");
```

---

## Arquitetura

```
Application
    ↓
CacheRepository (interface + @Async)
    ↓
CacheService (retry + fallback + scope TTL + JSON serialization)
    ↓
CacheProvider (interface)
    ↓
┌─────────────────────┬──────────────────────┬──────────────────────┐
│ RedisCacheProvider   │ CaffeineCacheProvider │ CompositeCacheProvider│
│ (RedisTemplate)      │ (Caffeine Cache)      │ (L1 + L2)            │
└─────────────────────┴──────────────────────┴──────────────────────┘
```

---

## Estrutura do projeto

```
src/main/java/com/mds/cache/
├── CacheAutoConfiguration.java             # Auto-config (@EnableCaching + @EnableAsync)
├── config/
│   ├── CacheAbstractConfig.java            # Base: pool, client options, RedisTemplate
│   ├── CacheCustomConfig.java              # RedisCacheManager + ObjectMapper unificado
│   ├── CacheProviderAutoConfiguration.java # Seleção condicional do CacheProvider
│   ├── CacheSentinelConfig.java            # Redis Sentinel
│   ├── CacheStandaloneConfig.java          # Redis Standalone
│   └── properties/
│       └── LoggerConfigProperties.java     # Flag de supressão de logs
├── enumerator/
│   ├── CacheProviderType.java              # REDIS, CAFFEINE, COMPOSITE
│   ├── LogLevel.java                       # INFO, DEBUG, WARN, ERROR, TRACE
│   └── ScopeEnum.java                      # SESSION, APPLICATION, GLOBAL
├── exception/
│   ├── RetryFallbackFalseException.java
│   ├── RetryFallbackNullException.java
│   └── resolver/
│       ├── RetryFallbackFalseExceptionResolver.java
│       └── RetryFallbackNullExceptionResolver.java
├── factory/
│   └── LoggerFactory.java                  # Log facade com toggle
├── healthcheck/
│   └── RedisHealthCheck.java               # Actuator HealthIndicator
├── keys/
│   ├── CacheKeys.java                      # Constantes de TTL
│   └── CacheMessageKeys.java               # Códigos de erro ARCCHE_*
├── provider/
│   ├── CacheProvider.java                  # Interface backend-agnostic
│   └── impl/
│       ├── CaffeineCacheProvider.java      # Caffeine (local in-memory)
│       ├── CompositeCacheProvider.java     # L1 Caffeine + L2 Redis
│       └── RedisCacheProvider.java         # Redis (Standalone/Sentinel)
├── record/
│   └── CachedPageResponse.java             # Record paginado para cache
├── repository/
│   ├── CacheRepository.java               # Contrato com suporte a Scope
│   └── impl/
│       └── CacheRepositoryImpl.java        # Implementação + @Async
├── resource/
│   └── CacheResource.java                 # REST diagnóstico
└── service/
    ├── CacheEvictService.java              # Contrato de evicção
    ├── CacheService.java                   # Contrato principal (backend-agnostic)
    └── impl/
        ├── CacheEvictServiceImpl.java      # Evicção via CacheManager
        └── CacheServiceImpl.java           # Delegação ao CacheProvider + retry
```

---

## Requisitos

- **Java** 21+ (LTS)
- **Spring Boot** 4.0.6+
- **spring-error-pattern** (dependência para resolvers de exceção)

### Dependências opcionais

| Dependência | Funcionalidade | Sem ela |
|---|---|---|
| `spring-boot-starter-data-redis` + `lettuce-core` | Provider Redis | Use `caffeine` como provider |
| `com.github.ben-manes.caffeine:caffeine` | Provider Caffeine / Composite L1 | Use `redis` como provider |
| `spring-boot-starter-web` | Endpoint REST `/cache-manager/**` | Endpoint não é registrado |
| `spring-boot-actuator` | Health check no `/actuator/health` | Health check não é registrado |
| `spring-cloud-starter` | `@RefreshScope` no `LoggerConfigProperties` | Toggle de log não recarrega em runtime |

---

## Changelog

### v0.0.2

- **Multicache** — suporte a Redis, Caffeine e Composite via `cache.provider`
- **CacheProvider interface** — abstração backend-agnostic para operações de cache
- **CaffeineCacheProvider** — cache local in-memory de alta performance
- **CompositeCacheProvider** — L1 (Caffeine) + L2 (Redis) com backfill automático
- **CacheServiceImpl refatorado** — delega ao CacheProvider em vez de RedisTemplate direto
- **Retrocompatível** — properties `redis.*` continuam funcionando como fallback
- **Java 21** — atualizado de Java 17 para 21 (LTS)
- **Spring Boot 4.0.6** — atualizado de 3.4.0 para 4.0.6
- **Spring Cloud 2025.1.1** — atualizado de 2023.0.1 para Oakwood
- **Jackson 3** — migração completa de `com.fasterxml.jackson.databind` para `tools.jackson.databind`
- **JsonMapper imutável** — `Jackson2ObjectMapperBuilder` substituído por `JsonMapper.builder()` (thread-safe)
- **GenericJacksonJsonRedisSerializer** — substitui `GenericJackson2JsonRedisSerializer` (deprecated)
- **Módulos built-in** — `JavaTimeModule` e `ParameterNamesModule` removidos (auto-detectados no Jackson 3)

### v0.0.1

- **Opt-in / Non-intrusive** — toda a lib é condicional; sem config = sem beans = sem erros
- **Kill-switch** — `cache.enabled=false` desliga tudo temporariamente
- **TTL por scope** — SESSION, APPLICATION e GLOBAL com expiração independente
- **ObjectMapper unificado** — 1 bean `redisCacheObjectMapper` (sem `@Primary`)
- **@Async corrigido** — métodos assíncronos retornam `void` (fire-and-forget)
- **Segurança** — `BasicPolymorphicTypeValidator` com allowlist
- **Dependências limpas** — `spring-boot-starter-web`, `spring-boot-actuator`, `caffeine` e `spring-cloud-starter` marcados `optional`
- **Beans condicionais ao classpath** — `CacheResource` e `RedisHealthCheck` condicionais

## Melhorias futuras

- Cache warmup on startup
- Invalidação distribuída via pub/sub (Redis)
- Métricas Micrometer para hit/miss ratio por provider
- Caffeine stats endpoint no Actuator
