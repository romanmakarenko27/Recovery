# SchoolDay QA Automation

## Project Overview
Selenium WebDriver + JUnit 5 test automation for SchoolDay QA environment.
Covers MFA recovery code login flow.

## Tech Stack
- Java 21, Gradle 9, Selenium 4.20, WebDriverManager 5.8, JUnit 5.10

## Project Structure
```
src/test/java/com/schoolday/qa/
├── base/        → BaseTest (WebDriver lifecycle)
├── pages/       → Page Objects (LoginPage, MfaPage, RecoveryCodePage)
├── tests/       → Test classes (RecoveryCodeTest)
└── util/        → Utilities (PageInspector)
src/test/resources/
└── test.properties      → credentials & config (gitignored)

Project root:
├── credentials.txt          → test credentials (gitignored)
└── SchoolDay_reset_codes.txt → single-use recovery codes (gitignored)
```

## Architecture
- **Page Object Model**: each page = separate class with locators + actions
- **BaseTest**: all tests extend it; handles driver setup/teardown + config loading
- Page objects take `WebDriver` in constructor, no base class
- All tests extend `BaseTest`

## Running Tests
```
gradlew.bat test
gradlew.bat test --tests "com.schoolday.qa.tests.RecoveryCodeTest_Vendor"
gradlew.bat test --tests "*.RecoveryCodeTest.testMethodName"
```

## Key Conventions
- Use explicit waits (`WebDriverWait`), never `Thread.sleep()`
- Credentials in `test.properties` (gitignored), never hardcoded
- Recovery codes file (`SchoolDay_reset_codes.txt`) is in the **project root**, not in resources
- After a recovery code is consumed, mark it as `USED` in the file (do NOT delete it)
  - Format: `USED:PK9P%vuMbv` — prefix the code with `USED:` so it's skipped on next read
- Tests ordered: UI-only first (1-5), functional tests after (6+)

## Test Plan (RecoveryCodeTest)
| Order | Test | Details |
|-------|------|---------|
| 1 | MFA page elements displayed | UI-only verification |
| 2 | Navigate to Recovery Code page | UI-only, verify all elements |
| 3 | Navigate back to MFA from Recovery | UI-only |
| 4 | Empty submission validation | UI-only |
| 5 | Invalid recovery code | Enter a 10-char code like `PK9P%vuMbv`; assert UI error message AND 403 HTTP response |
| 6 | Contact Support link | Verify "Contact support" link navigates to `https://share.hsforms.com/` |
| 7 | Already-used recovery code | Submit a previously used (valid) code; assert UI error message AND 403 HTTP response |
| 8 | Successful recovery code login | Consume a fresh code, submit it, verify redirect to `https://connect-qa.gg4l.com/admin/institutions` and mark code as USED |

## HTTP Response Validation
- Tests 5 and 7 require intercepting the network response to assert **403** status code
- Use Selenium DevTools / CDP `Network.responseReceived` or a browser proxy to capture responses
- Also validate the UI error message shown to the user

## QA Environment
- URL: `https://connect-qa.gg4l.com/login/vendor`
- Post-login landing page: `https://connect-qa.gg4l.com/admin/institutions`
- Test user: configured in `test.properties`
