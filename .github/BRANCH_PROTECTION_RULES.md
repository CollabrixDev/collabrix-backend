# GitHub Branch Protection Rules for Main

This document explains how to set up branch protection rules to enforce CI/CD policies.

## Setup Instructions

### 1. Navigate to Branch Protection
1. Go to GitHub repository
2. Settings → Branches
3. Click "Add rule" under "Branch protection rules"

### 2. Configure Main Branch Protection

**Branch name pattern:** `main`

### Required Status Checks

Enable these checks:
- ✅ **build-and-test** - Must pass all tests
- ✅ **code-quality** - Must pass code quality gates
- ✅ **security-scan** - Must pass security checks
- ✅ **pr-quality-gate** - Must pass PR validation

**Settings:**
- ✅ Require status checks to pass before merging
- ✅ Require branches to be up to date before merging
- ✅ Dismiss stale pull request approvals when new commits are pushed

### Pull Request Requirements

- ✅ Require pull request reviews before merging
  - Number of required reviewers: **1**
  - Dismiss stale pull request approvals: **Yes**
  - Require code owner reviews: **Yes** (if CODEOWNERS exists)
  
- ✅ Require approval of the most recent reviewable push
- ✅ Require conversation resolution before merging

### Restrictions

- ✅ Require administrators to follow the same rules
- ✅ Restrict who can push to matching branches
  - Allow only specific users/teams (optional)

### Danger Zone

- ✅ Allow auto-merge
  - Allow squash merging (recommended)
  - Allow rebase merging
- ✅ Delete head branch after merge

---

## Example Branch Protection Rule JSON

```json
{
  "enforce_admins": true,
  "require_linear_history": true,
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "build-and-test",
      "code-quality",
      "security-scan",
      "pr-quality-gate"
    ]
  },
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": true,
    "required_approving_review_count": 1
  }
}
```

---

## Access Control

### Who Can Push to Main?
- Only through approved PRs
- Requires all status checks to pass
- Requires at least 1 approval

### Who Can Merge?
- Users with "Write" or "Admin" permissions
- After all status checks pass
- After PR is approved

### Who Can Dismiss Reviews?
- Only repository admins

---

## Status Check Details

### Build & Test
- **Requirement:** All JUnit tests must pass
- **Retry:** 1 automatic retry on failure
- **Timeout:** 15 minutes
- **Failure Impact:** Blocks merge

### Code Quality
- **Requirement:** No critical PMD/SpotBugs issues
- **Coverage:** ≥ 70% code coverage
- **Timeout:** 10 minutes
- **Failure Impact:** Blocks merge

### Security Scan
- **Requirement:** No known vulnerable dependencies
- **Requirement:** No hardcoded secrets found
- **Timeout:** 10 minutes
- **Failure Impact:** Blocks merge

### PR Quality Gate
- **Requirement:** Title follows conventional commits
- **Requirement:** Max 50 commits (encourages squashing)
- **Requirement:** All tests pass
- **Timeout:** 10 minutes
- **Failure Impact:** Blocks merge

---

## Merge Strategy

### Recommended: Squash and Merge
```
Pros:
✅ Clean history on main
✅ One commit per PR
✅ Easy to revert
✅ Clear feature tracking

Example:
- Feature branch: 23 commits
- Merged to main: 1 squashed commit
```

### Alternative: Rebase and Merge
```
Pros:
✅ Linear history
✅ No merge commits
✅ Individual commits preserved

Cons:
❌ Can be confusing with multiple commits
```

### Not Recommended: Create a Merge Commit
```
Cons:
❌ Creates noise in history
❌ Harder to understand feature scope
```

---

## Handling CI/CD Failures

### Test Failures
1. Pull latest main: `git pull origin main`
2. Fix failing tests locally
3. Run `mvn test` to verify
4. Push fix - workflow re-runs automatically
5. Once green, merge is enabled

### Code Quality Failures
1. Review PMD/SpotBugs report
2. Fix issues in code
3. Push again - re-runs automatically

### Security Scan Failures
1. Review security report
2. Update vulnerable dependencies: `mvn versions:update-parent`
3. Or add exceptions (rare)
4. Push again

### Rebuilding Failed Workflow
If infrastructure issue (not code):
1. Go to Actions tab
2. Find failed workflow
3. Click "Re-run jobs" or "Re-run all jobs"

---

## Exceptions & Overrides

### When to Bypass (Emergency Only)
**Never recommended**, but if absolutely necessary:

1. Temporarily disable branch protection
   - Settings → Branches → Edit rule → Disable
2. Make emergency fix
3. **IMMEDIATELY** re-enable protection

### Post-Incident Review
1. Schedule review meeting
2. Document what happened
3. Implement preventive measures
4. Update procedures

---

## Monitoring & Alerts

### GitHub Notifications
- Enable: Settings → Notifications
- Action workflows trigger notifications
- PR reviews send notifications

### Slack Integration (Optional)
```
GitHub App → Slack:
- Workflow failures → Slack #deployments
- PR approvals → Slack #code-review
- Releases → Slack #releases
```

---

## Troubleshooting

### PR Shows "Checks Failed"
1. Click "Details" on check
2. Review workflow logs
3. Check for environment issues
4. Re-run workflow if infrastructure issue

### PR Marked as "Changes Requested"
1. Address review comments
2. Push new commits
3. Dismiss reviews if fixed
4. Request new review

### Cannot Push to Main (Locally)
```bash
# This is expected - push to feature branch instead
git checkout -b feature/my-feature
git push origin feature/my-feature
# Then create PR on GitHub
```

---

## Best Practices

✅ **Always work on feature branches**
```bash
git checkout -b feature/workspace-deletion
```

✅ **Write good commit messages**
```
feat(workspace): implement soft delete
```

✅ **Keep commits clean**
```bash
git rebase -i HEAD~5  # Squash before PR
```

✅ **Review your own PR first**
- Check diff
- Verify all tests pass
- Read through changes

✅ **Test locally before pushing**
```bash
mvn clean test jacoco:report
```

---

**Last Updated:** June 7, 2026
**Author:** Prajeeth
