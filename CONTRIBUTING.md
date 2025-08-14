# Contributing to Axer

Thank you for your interest in contributing to Axer! This document provides guidelines and information for contributors to help maintain code quality and project consistency.

## 📋 Table of Contents

- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [Fork and Clone](#fork-and-clone)
- [Code Style and Guidelines](#code-style-and-guidelines)
- [Building and Testing](#building-and-testing)
- [Making Changes](#making-changes)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Community Guidelines](#community-guidelines)

## Getting Started

Axer is a Kotlin Multiplatform debugging library that provides HTTP request monitoring, logging, exception handling, and Room database inspection for Android, iOS, and JVM platforms.

### What You Can Contribute

- **Bug fixes**: Help us identify and fix issues
- **New features**: Propose and implement new debugging capabilities
- **Documentation**: Improve docs, add examples, or fix typos
- **Platform support**: Extend compatibility to new platforms
- **Performance improvements**: Optimize existing functionality
- **Tests**: Add or improve test coverage

## Development Environment Setup

### Prerequisites

- **JDK 17 or higher** (JDK 21 recommended for best compatibility)
- **Android SDK** (for Android targets)
- **Xcode** (for iOS targets, macOS only)
- **Git** for version control

### Recommended Tools

- **IntelliJ IDEA** or **Android Studio** (latest stable version)
- **Kotlin Multiplatform Plugin** (usually bundled)

### Platform-Specific Requirements

- **Android**: Android SDK with API level 21+ (minSdk = 21)
- **iOS**: Xcode with iOS deployment target as specified in project
- **JVM**: Compatible JDK installation

## Fork and Clone

1. **Fork the repository** on GitHub by clicking the "Fork" button
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/axer.git
   cd axer
   ```
3. **Add the upstream remote**:
   ```bash
   git remote add upstream https://github.com/orioneee/axer.git
   ```
4. **Verify your setup**:
   ```bash
   git remote -v
   # Should show both origin (your fork) and upstream (original repo)
   ```

## Code Style and Guidelines

### Kotlin Style

- Follow **Kotlin Coding Conventions** (Official Style Guide)
- Use `kotlin.code.style=official` (already configured in `gradle.properties`)
- Prefer explicit type declarations for public APIs
- Use meaningful variable and function names
- Add KDoc comments for public APIs

### Code Organization

- **Package structure**: Follow existing `io.github.orioneee.*` package naming
- **Internal APIs**: Use `internal` visibility for implementation details
- **File naming**: Use PascalCase for class files, follow Kotlin conventions

### Best Practices

- **Multiplatform compatibility**: Ensure code works across all supported platforms
- **Resource management**: Properly close resources and handle lifecycles
- **Error handling**: Use appropriate exception handling and logging
- **Performance**: Consider memory usage and processing efficiency
- **Testing**: Write unit tests for new functionality

## Building and Testing

### Building the Project

1. **Grant execute permissions** (Unix-like systems):
   ```bash
   chmod +x ./gradlew
   ```

2. **Build all modules**:
   ```bash
   ./gradlew build
   ```

3. **Build specific modules**:
   ```bash
   ./gradlew :axer:build                    # Main library
   ./gradlew :axer-no-op:build              # No-op variant
   ./gradlew :axer-remote-debugger:build    # Desktop debugger
   ./gradlew :sample:composeApp:build       # Sample app
   ```


### Building Sample Applications

```bash
# Android sample
./gradlew :sample:composeApp:assembleDebug

# Desktop remote debugger
./gradlew :axer-remote-debugger:createDistributable
```

## Making Changes

### Branch Naming

Use descriptive branch names with the following patterns:
- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `docs/description` - Documentation changes
- `refactor/description` - Code refactoring
- `test/description` - Test additions/improvements

Examples:
- `feature/okhttp-timeout-support`
- `bugfix/memory-leak-ktor-interceptor`
- `docs/installation-guide-update`

### Commit Messages

Follow conventional commit format:
```
type(scope): brief description

Detailed explanation if necessary

Fixes #issue-number (if applicable)
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or fixing tests
- `chore`: Build system or auxiliary tool changes

**Examples:**
```
feat(okhttp): add request timeout configuration support

fix(ktor): resolve memory leak in request interceptor

docs(readme): update installation instructions for v1.2.0
```

### Making Changes Workflow

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following the guidelines above

3. **Test your changes**:
   ```bash
   ./gradlew build
   ./gradlew test
   ```

4. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat(scope): your descriptive message"
   ```

5. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

## Pull Request Process

### Before Submitting

- [ ] Update documentation if needed
- [ ] Add tests for new functionality(optionally)
- [ ] Verify multiplatform compatibility
- [ ] Check that no existing functionality is broken

### Submitting a Pull Request

1. **Push your branch** to your fork
2. **Open a Pull Request** on GitHub
3. **Fill out the PR template** (if available)
4. **Provide clear description**:
   - What changes were made
   - Why the changes were necessary
   - How to test the changes
   - Any breaking changes

### PR Guidelines

- **Title**: Use descriptive title (follow commit message convention)
- **Description**: Explain the changes and their purpose
- **Scope**: Keep PRs focused on a single feature or fix
- **Size**: Prefer smaller, reviewable PRs over large ones
- **Testing**: Include relevant tests and verify existing tests pass

### Review Process

- Maintainers will review your PR
- Address feedback and requested changes
- Keep your branch updated with main:
  ```bash
  git fetch upstream
  git rebase upstream/main
  git push --force-with-lease origin your-branch
  ```

## Issue Reporting

### Before Opening an Issue

- [ ] Search existing issues to avoid duplicates
- [ ] Check if the issue exists in the latest version
- [ ] Try to reproduce the issue with minimal code

### Bug Reports

Include the following information:
- **Axer version** being used
- **Platform** (Android, iOS, JVM) and version details
- **Kotlin version** and multiplatform plugin version
- **Steps to reproduce** the issue
- **Expected behavior** vs **actual behavior**
- **Code samples** that demonstrate the issue
- **Stack traces** or error logs (if applicable)

### Feature Requests

For new features, please describe:
- **Use case**: Why is this feature needed?
- **Proposed solution**: How should it work?
- **Alternatives**: What other approaches were considered?
- **Platform considerations**: How should it work across platforms?

### Issue Labels

- `bug`: Something isn't working correctly
- `enhancement`: New feature request
- `documentation`: Documentation improvements
- `good first issue`: Good for newcomers
- `help wanted`: Community assistance requested

## Community Guidelines

### Code of Conduct

We follow the principles of respectful and inclusive collaboration:

- **Be respectful**: Treat all community members with respect
- **Be constructive**: Provide helpful feedback and suggestions
- **Be collaborative**: Work together towards common goals
- **Be patient**: Everyone has different experience levels

### Communication

- **Issues**: Use GitHub issues for bug reports and feature requests
- **Discussions**: Use GitHub Discussions for questions and general discussion
- **Pull Requests**: Use PR comments for code review discussions

### Recognition

Contributors will be recognized:
- In release notes for significant contributions
- In project documentation where appropriate
- Through GitHub's contribution tracking

## Additional Resources

- **[README.md](README.md)**: Project overview and basic usage
- **[License](LICENSE)**: Apache License 2.0 terms
- **[Releases](https://github.com/orioneee/axer/releases)**: Version history and downloads
- **[Kotlin Docs](https://orioneee.github.io/axer/-axer/io.github.orioneee/index.html)**: API documentation
- **[Kotlin Multiplatform Guide](https://kotlinlang.org/docs/multiplatform.html)**: Platform documentation

## Questions?

If you have questions not covered in this guide:
1. Check existing [GitHub Issues](https://github.com/orioneee/axer/issues)
2. Open a new issue with the `question` label
3. Start a [GitHub Discussion](https://github.com/orioneee/axer/discussions)

Thank you for contributing to Axer! 🚀