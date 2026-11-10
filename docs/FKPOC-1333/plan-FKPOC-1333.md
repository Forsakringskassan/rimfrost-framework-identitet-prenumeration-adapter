# Plan FKPOC-1333 — Initial adapter implementation

## Overview

Implement `rimfrost-framework-identitet-prenumeration-adapter` following the established pattern from `rimfrost-framework-handlaggning-adapter` and `rimfrost-framework-oul-adapter`.

The adapter wraps `PrenumerationControllerApi` from `rimfrost-service-identitet-prenumeration-openapi-jaxrs-spec` (v0.0.1) and exposes two operations:
- `createPrenumeration(CreatePrenumerationRequest)` → `void`
- `deletePrenumeration(UUID prenumerationId)` → `void`

**Package root:** `se.fk.rimfrost.framework.identitetprenumeration`  
**Config key:** `identitetprenumeration.api.base-url`

---

## Steps

- [x] **1. Domain models**

  Create in `src/main/java/se/fk/rimfrost/framework/identitetprenumeration/model/`:
  - `Idtyp.java` — `@Value.Immutable` interface: `String typId()`, `String varde()`
  - `CreatePrenumerationRequest.java` — `@Value.Immutable` interface: `UUID prenumerationId()`, `List<Idtyp> identiteter()`, `String replyTo()`

- [x] **2. Exception**

  Create `src/main/java/se/fk/rimfrost/framework/identitetprenumeration/exception/IdentitetPrenumerationException.java`:
  - Extends `Exception`
  - `ErrorType` enum: `BAD_REQUEST`, `NOT_FOUND`, `SERVICE_UNAVAILABLE`, `UNEXPECTED_ERROR`
  - Two constructors: `(ErrorType, String)` and `(ErrorType, String, Throwable)`

- [x] **3. Mapper**

  Create `src/main/java/se/fk/rimfrost/framework/identitetprenumeration/adapter/IdentitetPrenumerationMapper.java`:
  - `@ApplicationScoped`
  - One direction only (domain → API, no response bodies to map back):
    - `toApiCreatePrenumerationRequest(CreatePrenumerationRequest)` → generated `CreatePrenumerationRequest`
    - `toApiIdtyp(Idtyp)` → generated `Idtyp` (private helper)

- [x] **4. Adapter**

  Create `src/main/java/se/fk/rimfrost/framework/identitetprenumeration/adapter/IdentitetPrenumerationAdapter.java`:
  - `@ApplicationScoped`, `@PostConstruct init()`, `@PreDestroy destroy()`
  - `@ConfigProperty(name = "identitetprenumeration.api.base-url")`
  - Proxy client: `PrenumerationControllerApi` via `WebResourceFactory` + `Apache5ConnectorProvider`
  - `createPrenumeration(CreatePrenumerationRequest request) throws IdentitetPrenumerationException`
    - Maps request via mapper, calls API, handles `ProcessingException` → `SERVICE_UNAVAILABLE`, `400` → `BAD_REQUEST`, other → `UNEXPECTED_ERROR`
  - `deletePrenumeration(UUID prenumerationId) throws IdentitetPrenumerationException`
    - Calls API directly, handles `ProcessingException` → `SERVICE_UNAVAILABLE`, `404` → `NOT_FOUND`, `400` → `BAD_REQUEST`, other → `UNEXPECTED_ERROR`

- [x] **5. Tests**

  Create in `src/test/java/se/fk/rimfrost/framework/identitetprenumeration/`:
  - `IdentitetPrenumerationAdapterTest.java` — `@QuarkusComponentTest`, WireMock, covers all error paths + happy path for each operation
  - `IdentitetPrenumerationMapperTest.java` — plain unit test, verifies field mapping for `toApiCreatePrenumerationRequest`

---

## Notes

- Both API operations return `void` (201/204) — no null-response check needed, no reverse mapping in mapper
- `quarkus-junit-mockito` included for `@InjectMock HandlaggningMapper` pattern parity, though mapper is simple enough that the adapter test may not need mocking
- No `krav.md` — consistent with all other framework adapters
