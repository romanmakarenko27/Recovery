---
description: Review test automation code for quality and best practices
argument: Path to the file(s) to review (e.g. src/test/java/com/schoolday/qa/tests/RecoveryCodeTest.java)
---

You are a senior QA automation engineer. Review the code at: $ARGUMENTS

## Instructions

1. Read the specified file(s).

2. Read related files for context:
   - `src/test/java/com/schoolday/qa/base/BaseTest.java`
   - All Page Objects in `src/test/java/com/schoolday/qa/pages/`
   - Other test classes in `src/test/java/com/schoolday/qa/tests/`

3. Check for these issues:

### Critical (must fix)
- Locators in test classes (should be in Page Objects only)
- Assertions in Page Objects (should be in test classes only)
- `Thread.sleep()` usage (use explicit waits instead)
- Hardcoded credentials (should be in test.properties)
- Shared mutable state between tests
- Missing driver.quit() or resource cleanup

### Warning (should fix)
- Flaky test patterns: timing-dependent assertions, order-dependent tests
- Overly broad CSS/XPath selectors that may match multiple elements
- Missing assertion messages
- Implicit waits mixed with explicit waits
- Redundant waits (waiting for something already waited for)
- Tests that are too large (testing multiple unrelated things)

### Suggestion (nice to have)
- Code duplication that could be extracted to helpers
- Naming improvements (test methods, variables)
- Missing test scenarios or edge cases
- Page Object methods that could be more reusable
- Fluent API patterns (return `this` for chaining)

4. Output a structured review:

## Review Summary
- Files reviewed: (list)
- Critical: N issues
- Warning: N issues
- Suggestion: N items

## Issues

### [CRITICAL/WARNING/SUGGESTION] Issue title
**File**: path:line
**Problem**: description
**Fix**: code suggestion
