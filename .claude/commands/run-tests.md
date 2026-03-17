---
description: Execute tests via Gradle and interpret results
argument: Optional test filter (e.g. RecoveryCodeTest, or specific method name)
---

You are a QA automation assistant. Run the project's tests and analyze the results.

## Instructions

1. Determine the Gradle command based on the argument:

   - **No argument** (run all): `gradlew.bat test`
   - **Class name** (e.g. `RecoveryCodeTest`): `gradlew.bat test --tests "com.schoolday.qa.tests.$ARGUMENTS"`
   - **Method name** (e.g. `RecoveryCodeTest.testMfaPageElementsDisplayed`): `gradlew.bat test --tests "com.schoolday.qa.tests.$ARGUMENTS"`

   Run the command from the project root directory: `C:\Users\Roman\IdeaProjects\schoolday-automation`

2. Parse the test output and provide:

   ### Test Results Summary
   | Status | Count |
   |--------|-------|
   | Passed | N |
   | Failed | N |
   | Skipped | N |
   | Total | N |

3. For each **failure**, provide:

   ### Failed: testMethodName
   - **Error**: The assertion or exception message
   - **Root Cause**: Your analysis of why it failed
   - **Suggested Fix**: Concrete code changes to fix it

4. Remind the user about the HTML test report:
   - Path: `build/reports/tests/test/index.html`

5. If ALL tests pass, confirm success and note any warnings from the build output.

6. Important notes:
   - Test 6 (`testSuccessfulRecoveryCodeLogin`) consumes a recovery code — warn the user before running it
   - If recovery_codes.txt is empty, test 6 will fail with "No recovery codes remaining"
   - Use `--info` flag if more debug output is needed for failures
