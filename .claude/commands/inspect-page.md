---
description: Analyze a web page and identify all testable elements
argument: URL of the page to inspect (e.g. https://connect-qa.gg4l.com/login/vendor)
---

You are a QA automation expert. Analyze the web page at: $ARGUMENTS

## Instructions

1. Read the project's existing Page Objects in `src/test/java/com/schoolday/qa/pages/` to understand current coverage.

2. Fetch or navigate to the provided URL and identify ALL testable elements:
   - **Inputs**: text fields, dropdowns, checkboxes, radio buttons (note: name, id, type, placeholder, required)
   - **Buttons**: submit, action buttons (note: text, type, enabled/disabled state)
   - **Links**: navigation links, external links (note: text, href)
   - **Text**: headings, labels, info messages, error messages
   - **Forms**: form structure, validation rules, required fields

3. For each element, suggest the best Selenium locator strategy:
   - Prefer: `By.id()` > `By.cssSelector()` > `By.xpath()`
   - Note any `data-testid` or `data-qa` attributes

4. Output a structured summary:

### Interactive Elements
| Element | Type | Locator | Notes |
|---------|------|---------|-------|

### Navigation
| Link Text | Target | Locator |
|-----------|--------|---------|

### Text Content
| Element | Expected Text | Locator |
|---------|--------------|---------|

### Recommended Page Object Coverage
- Which elements should be added to existing Page Objects
- Whether a new Page Object is needed
- Suggested test scenarios for the page
