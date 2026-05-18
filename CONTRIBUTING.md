# Contributing

This project follows shared MDS contribution standards.

## Branch Naming

Prefer these branch patterns:

- `feature/<name>`
- `fix/<name>`
- `refactor/<name>`
- `docs/<name>`
- `build/<name>`

Examples:

- `feature/initial-structure`
- `fix/docker-image-source`
- `build/maven-standardization`

## Commit Standard

Use the commit convention documented in:

- [`docs/COMMIT_PATTERN.md`](docs/COMMIT_PATTERN.md)

## Pull Request Standard

Use the default GitHub PR template and, when helpful, the additional templates available in:

- [`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md)
- [`.github/PULL_REQUEST_TEMPLATE/feature.md`](.github/PULL_REQUEST_TEMPLATE/feature.md)
- [`.github/PULL_REQUEST_TEMPLATE/fix.md`](.github/PULL_REQUEST_TEMPLATE/fix.md)
- [`.github/PULL_REQUEST_TEMPLATE/build.md`](.github/PULL_REQUEST_TEMPLATE/build.md)
- [`docs/PULL_REQUEST_TEMPLATE.md`](docs/PULL_REQUEST_TEMPLATE.md)

## General Rules

- Keep one logical change per commit.
- Keep PRs focused and easy to review.
- Avoid including build artifacts such as `target/`, generated jars, or temporary files.
- Update documentation when the public contract, setup, or contribution flow changes.
