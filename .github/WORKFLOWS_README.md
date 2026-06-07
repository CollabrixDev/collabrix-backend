# GitHub Actions Workflows Quick Reference

## Workflows at a Glance

| Workflow | Triggers | Purpose | Status |
|----------|----------|---------|--------|
| `build-and-test.yml` | Push, PR | Compile & test | ✅ |
| `code-quality.yml` | Push, PR | PMD, SpotBugs, formatting | ✅ |
| `security-scan.yml` | Push, PR, Schedule | Dependency check, secrets | ✅ |
| `pr-quality-gate.yml` | PR to main | PR validation & blocking | ✅ |
| `release.yml` | v*.*.* tag | Build release | ✅ |

## Common Scenarios

### Scenario 1: Commit to Feature Branch
```
1. Commit code
2. ✅ build-and-test runs automatically
3. ✅ code-quality runs automatically
4. View results in Actions tab
```

### Scenario 2: Create PR to Main
```
1. Create PR with conventional commit title
2. ✅ All workflows run
3. ✅ pr-quality-gate enforces standards
4. ✅ security-scan checks for vulnerabilities
5. Comment with results added to PR
6. Fix any failures
7. Merge when all checks pass ✅
```

### Scenario 3: Build Fails
```
1. Check workflow logs
2. Download test results artifact
3. Review failure details
4. Fix code locally
5. Push fix - workflows re-run automatically
6. Once pass, merge is allowed
```

### Scenario 4: Release New Version
```
1. Create tag: git tag v1.2.3
2. Push tag: git push origin v1.2.3
3. ✅ release.yml builds JAR
4. ✅ Creates GitHub Release
5. JAR available for download
```

## Environment Setup

Required for local development:
```bash
# Java 21
java -version

# Maven (auto-installed)
mvn -version

# Git
git --version
```

## Quick Commands

```bash
# Run all tests locally (before push)
mvn clean test

# Run quality checks
mvn pmd:pmd spotbugs:spotbugs

# Generate coverage report
mvn clean test jacoco:report

# Full build
mvn clean install

# Skip tests (CI will run them)
mvn clean package -DskipTests
```

## PR Checklist Before Merge

- [ ] PR title follows conventional commits format
- [ ] All GitHub Actions pass ✅
- [ ] Code coverage ≥ 70%
- [ ] No hardcoded secrets (scanned by workflow)
- [ ] No vulnerable dependencies
- [ ] Commits are clean (max 50)
- [ ] Documentation updated (if needed)
- [ ] Tests added for new features

## Status Check Requirements for Main

Before code can merge to `main`, these must pass:
1. ✅ build-and-test
2. ✅ code-quality
3. ✅ security-scan
4. ✅ pr-quality-gate

**All 4 must be green!**

---

For detailed documentation, see: [GITHUB_ACTIONS_GUIDE.md](.github/GITHUB_ACTIONS_GUIDE.md)
