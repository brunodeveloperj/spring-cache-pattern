# Commit Pattern

This document defines the commit message pattern used across the MDS projects.

## Objective

Keep the history:

- clear for reviewers
- searchable over time
- consistent across libraries and APIs
- easy to reuse when opening pull requests

## Default Format

Use this structure:

```text
<type>: <short summary>

<context line 1>
<context line 2>
<context line 3>
```

Example:

```text
feat: add initial cache pattern structure

Add the initial cache pattern module structure and sources.
Document the Redis and Caffeine caching responsibilities of the library.
Prepare the repository baseline files for GitHub publishing.
```

## Allowed Types

Use these prefixes by default:

- `feat`: new feature, new module structure, new behavior
- `fix`: bug fix, regression fix, runtime correction
- `refactor`: internal improvement without behavior change
- `docs`: README, guides, comments, documentation-only changes
- `test`: test creation, test adjustment, test stabilization
- `build`: Maven, Docker, Gradle, plugin, dependency, pipeline changes
- `chore`: maintenance change not directly tied to feature/fix

## Summary Rules

The first line should:

- be written in English
- use imperative style
- stay concise
- describe the main outcome, not the implementation detail

Good examples:

- `feat: add initial token pattern structure`
- `fix: correct Docker base image source`
- `build: standardize Maven pom organization`
- `docs: expand platform starter usage guide`

Avoid:

- `update files`
- `changes in project`
- `fix stuff`
- `commit final`

## Body Rules

The body should explain:

1. what was introduced or changed
2. why this change matters
3. what context helps future maintainers

Prefer 2 to 4 short lines.

Good body lines:

- `Add the initial starter module structure and sources.`
- `Document how the module should be adopted in Spring Boot services.`
- `Prepare the repository baseline files for GitHub publishing.`

## Scope Guidance

When the repository contains one library only, keep the scope in the summary text itself:

```text
feat: add initial security pattern structure
```

When the change is highly specific, mention the subject directly:

```text
fix: correct Redis health check assertion
build: align Maven comments with platform starter pattern
docs: expand shared core utility examples
```

## Branch Guidance

Recommended branch naming:

- `feature/<name>`
- `fix/<name>`
- `refactor/<name>`
- `docs/<name>`
- `build/<name>`

Examples:

- `feature/initial-structure`
- `fix/docker-image-source`
- `build/maven-standardization`

## Patterns By Scenario

### Initial project publication

```text
feat: add initial <module> structure

Add the initial module structure and sources.
Document the library responsibilities and adoption flow.
Prepare the repository baseline files for GitHub publishing.
```

### Bug fix

```text
fix: correct <problem>

Adjust the affected implementation to remove the runtime failure.
Align the behavior with the expected contract used by consumers.
Keep the module ready for validation in local and container execution.
```

### Build or Maven update

```text
build: standardize Maven project structure

Align the pom organization with the platform commit and documentation standard.
Group dependencies and plugins with consistent section comments.
Keep the build configuration easier to review and maintain.
```

### Test stabilization

```text
test: stabilize integration coverage for <module>

Update the test suite to reflect the current contract exposed by the module.
Remove outdated expectations that no longer match runtime behavior.
Preserve confidence for future refactors and releases.
```

## Practical Rule

If the commit title feels generic, rewrite it until someone outside the project can infer the change from the first line alone.

## Current Team Convention

For MDS projects, prefer:

- English commit messages
- one logical change per commit
- contextual body when the commit affects architecture, onboarding, or platform structure
- commit summaries that can be reused as PR titles when appropriate
