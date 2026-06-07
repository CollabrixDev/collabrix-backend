# GitHub Actions CI/CD Pipeline

This document explains the GitHub Actions workflows set up for automated build, test, and quality checks.

## Overview

The CI/CD pipeline ensures code quality and reliability through automated checks on every commit and pull request targeting `main`.

## Workflows

### 1. **Build & Test** (`build-and-test.yml`)
**Triggers:** Push to any branch, PR to main

**Steps:**
- ✅ Checkout code
- ✅ Setup JDK 21
- ✅ Compile project
- ✅ Run unit tests
- ✅ Run integration tests
- ✅ Generate test reports
- ✅ Upload coverage to Codecov

**Artifacts:**
- Test results (JUnit XML)
- Code coverage report (JaCoCo)

**Failure Handling:**
- Fails if tests fail
- Fails if compilation fails
- Generates artifacts even on failure for debugging

---

### 2. **Code Quality** (`code-quality.yml`)
**Triggers:** Push to any branch, PR to main

**Checks:**
- ✅ PMD (Static Analysis) - Detects bugs and bad practices
- ✅ SpotBugs (Bug Detection) - Finds potential bugs
- ✅ Code Formatting - Verifies code style
- ✅ Maven Analysis - Builds quality metrics

**PR Comment:**
- Adds quality report to PR
- Links to detailed artifacts

---

### 3. **Security Scan** (`security-scan.yml`)
**Triggers:** Push to main, PR to main, Weekly (Sunday)

**Scans:**
- ✅ OWASP Dependency Check - Vulnerable dependencies
- ✅ TruffleHog - Hardcoded secrets detection
- ✅ SARIF Upload - Security findings

**PR Comment:**
- Posts security status to PR
- Links to detailed reports

---

### 4. **PR Quality Gate** (`pr-quality-gate.yml`)
**Triggers:** PR opened/updated targeting main

**Enforces:**
- ✅ PR Title Format - Must follow conventional commits
- ✅ Commit Count - Max 50 commits (encourages squashing)
- ✅ Code Coverage - Minimum threshold
- ✅ All Tests Pass
- ✅ Code Compiles

**PR Comment:**
- Summary of all quality gate results
- Blocks merge if gates fail

---

### 5. **Release Build** (`release.yml`)
**Triggers:** Tag pushed (v*.*.*)

**Steps:**
- ✅ Build release JAR
- ✅ Create GitHub Release
- ✅ Upload artifacts
- ✅ Make available for download

**Usage:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## PR Title Format Requirements

PRs must have titles following conventional commits:

```
<type>(<scope>): <description>
```

**Valid Types:**
- `feat` - Feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Code style (no logic change)
- `refactor` - Code refactor
- `perf` - Performance improvement
- `test` - Test additions
- `chore` - Build/tool changes
- `ci` - CI/CD changes

**Examples:**
```
✅ feat(workspace): add member management
✅ fix(auth): handle expired token correctly
✅ docs: update API documentation
✅ test(workspace): add coverage for edge cases
```

---

## Failure Handling

### When Build Fails:
1. **Email Notification** - Sent to committers
2. **PR Comment** - Posted on affected PR
3. **Artifacts Generated** - Test reports still available for debugging
4. **Merge Blocked** - Cannot merge to main until fixed

### Debugging Failed Builds:
1. Check **Workflow Run** tab in GitHub
2. Look at specific job logs
3. Download **Artifacts** for test reports
4. Review code coverage
5. Fix issues and push again

---

## Test Coverage

### Minimum Coverage Requirements:
- **Overall:** 70%
- **Classes:** 70%
- **Methods:** 70%
- **Lines:** 70%

**View Coverage:**
```bash
mvn jacoco:report
# Report at: target/site/jacoco/index.html
```

---

## Local Testing Before Push

Always run these locally before pushing:

```bash
# Full build & test
mvn clean install

# Only tests
mvn test

# Code quality
mvn pmd:pmd spotbugs:spotbugs

# Coverage report
mvn clean test jacoco:report
```

---

## Commit Message Best Practices

Good commit messages:
```
feat(workspace): implement member management

- Add POST endpoint to add members
- Add DELETE endpoint to remove members
- Update WorkspaceService with authorization checks
- Add 4 new unit tests

Fixes #42
```

Avoid:
```
❌ fixed stuff
❌ wip
❌ asdf
❌ temp fix
```

---

## Workflow Status Badge

Add to README.md:

```markdown
[![Build & Test](https://github.com/CollabrixDev/collabrix-backend/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/CollabrixDev/collabrix-backend/actions/workflows/build-and-test.yml)
[![Code Quality](https://github.com/CollabrixDev/collabrix-backend/actions/workflows/code-quality.yml/badge.svg)](https://github.com/CollabrixDev/collabrix-backend/actions/workflows/code-quality.yml)
[![Security](https://github.com/CollabrixDev/collabrix-backend/actions/workflows/security-scan.yml/badge.svg)](https://github.com/CollabrixDev/collabrix-backend/actions/workflows/security-scan.yml)
```

---

## Configuration Files

### `.github/dependabot.yml`
Auto-updates dependencies (recommended):

```yaml
version: 2
updates:
  - package-ecosystem: maven
    directory: "/"
    schedule:
      interval: weekly
      day: monday
```

---

## Secrets Management

The following secrets should be configured in GitHub:
- `GITHUB_TOKEN` - Automatically provided by GitHub
- `CODECOV_TOKEN` - For codecov.io integration

Configure in: **Settings → Secrets and variables → Actions**

---

## Monitoring Builds

### GitHub Actions Dashboard:
- Go to: Actions tab in GitHub
- View all workflow runs
- Click on specific run for details
- Download artifacts

### Email Notifications:
- Enabled by default
- Sent on workflow failure
- Can be configured in Settings

---

## Troubleshooting

### Build Always Fails:
1. Check if Java version matches (21)
2. Verify Maven cache is not corrupted
3. Check for environment variable issues

### Tests Pass Locally But Fail in CI:
1. Check for environment-specific code
2. Verify database configuration
3. Check for flaky tests (timing issues)

### PR Comment Not Posting:
1. Verify `GITHUB_TOKEN` has correct permissions
2. Check workflow permissions in Settings
3. Ensure repository is public (for free tier)

---

## Future Enhancements

- [ ] Docker image build on release
- [ ] Automated deployment to staging
- [ ] Performance benchmarking
- [ ] Load testing
- [ ] E2E testing
- [ ] Sonarqube integration
- [ ] Slack notifications

---

**Last Updated:** June 7, 2026
**Author:** Prajeeth
