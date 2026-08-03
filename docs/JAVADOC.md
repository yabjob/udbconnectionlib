# Javadoc guide

## Generate the API reference

```bash
mvn javadoc:javadoc
```

Open `target/site/apidocs/index.html` after the command completes. To create a
Javadoc JAR suitable for a Maven release, run:

```bash
mvn package
```

The build attaches both source and Javadoc JARs.

## Documentation conventions

Public APIs should include:

- A concise statement of purpose.
- Parameter and return-value descriptions when the behavior is not obvious.
- `@throws` descriptions for meaningful checked exceptions.
- Compatibility or security notes where legacy behavior may surprise callers.
- `@since` for newly introduced public types and methods.

Comments should explain intent, constraints, or non-obvious behavior. Avoid comments
that merely repeat the code.
