# Changelog

## Unreleased

### Added

- SPDX license headers and class-level Javadocs for public Java types.
- Package-level Java documentation.
- Maven source and Javadoc JAR generation.
- MariaDB SELECT and MySQL transaction examples.
- Javadoc contribution and generation guide.

### Fixed

- Corrected the README connection cleanup example.
- Avoided setting timeout properties when connection properties are disabled.


## [Unreleased]

### Added
- Maven build and GitHub Actions CI.
- MariaDB runtime dependency and documentation.
- Security, contribution, architecture, API, review, and roadmap documentation.

### Changed
- Standard Maven project layout.
- Modern MySQL JDBC driver class name.
- JDBC resources use try-with-resources.

### Fixed
- Null-properties failure in MySQL and MariaDB timeout configuration.

### Removed
- Bundled JDBC drivers, compiled classes, Eclipse files, yGuard binary, and obsolete MySQL MXJ binary.
