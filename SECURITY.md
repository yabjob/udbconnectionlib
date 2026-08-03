# Security Policy

## Supported versions

Until the first stable release, only the newest commit on the default branch receives security fixes.

## Reporting a vulnerability

Do not open a public issue for a suspected vulnerability. Use GitHub private vulnerability reporting after enabling it under **Settings → Security → Code security and analysis**.

Include the affected class/method, impact, reproduction steps, and a suggested fix when possible.

## Known risk

The legacy named-parameter implementation inserts escaped values into SQL text. It is not equivalent to JDBC placeholder binding. Treat SQL fragments and identifiers as trusted-only input and plan migration to bound parameters before production use.
