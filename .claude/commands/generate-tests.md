---
description: Generate JUnit 5 test cases from an existing Page Object
argument: Path to the Page Object file (e.g. src/test/java/com/schoolday/qa/pages/LoginPage.java)
---

You are a QA automation expert. Generate comprehensive JUnit 5 tests for the Page Object at: $ARGUMENTS

## Instructions

1. Read the specified Page Object file to understand its locators and action methods.

2. Read `src/test/java/com/schoolday/qa/base/BaseTest.java` to follow the project's test lifecycle and conventions.

3. Read existing test classes in `src/test/java/com/schoolday/qa/tests/` to match the style and patterns:
   - Import conventions
   - `@BeforeEach` setup pattern (login → navigate flow)
   - Assertion style
   - Test naming conventions

4. Generate a complete test class with these categories:

   **UI Validation Tests** (no side effects, run first):
   - Elements are displayed/visible
   - Correct text content (headings, labels, descriptions)
   - Enabled/disabled states of buttons
   - Required field indicators

   **Navigation Tests**:
   - Links navigate to correct pages
   - Back navigation works correctly

   **Negative Tests**:
   - Empty field submission → validation error
   - Invalid input → error message
   - Boundary cases (very long input, special characters)

   **Positive/Functional Tests** (run last):
   - Happy path for each action method
   - Expected state changes after actions

5. Follow these rules:
   - Use `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` with `@Order`
   - UI-only tests first, functional tests last
   - Use explicit waits (`WebDriverWait`), never `Thread.sleep()`
   - Meaningful assertion messages in every `assert*()` call
   - Each test should be independent (fresh login in `@BeforeEach`)

6. Output the complete Java test class file ready to save.
