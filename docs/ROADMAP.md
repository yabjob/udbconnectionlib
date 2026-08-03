# Roadmap

## 2.x compatibility line

- Add unit and integration tests for every adapter.
- Add pool concurrency and timeout tests.
- Add Javadocs without changing signatures.
- Deprecate unsafe SQL text substitution APIs.
- Replace standard-output debug messages with `java.util.logging` or SLF4J.

## 3.0 redesign

- Introduce Java-standard class and method naming.
- Use generics throughout the API.
- Implement named parameters by compiling to `?` placeholders and binding typed values.
- Adopt `AutoCloseable` for query/session objects.
- Replace the custom pool or make pooling pluggable.
- Use `java.time` and explicit timezone semantics.
- Add typed mappers and functional row-mapper interfaces.
- Publish semantic-versioning and migration policies.
