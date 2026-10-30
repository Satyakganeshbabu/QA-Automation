# Harvee QA Automation Assessment — Selenium + Java

## Stack
- Java 21
- Maven
- Selenium WebDriver 4
- JUnit 5
- Chrome browser

## Setup
1. Install Java 21, Maven, and Google Chrome.
2. From the project root, run `mvn test`.
3. Selenium Manager may resolve the compatible ChromeDriver automatically. If your environment blocks that, install a matching ChromeDriver and configure it through your standard Selenium setup.

The base URL and user credentials are configurable through Maven system properties or environment variables: `BASE_URL`, `STANDARD_USER`, `LOCKED_OUT_USER`, and `SAUCE_PASSWORD`. Defaults point only to the Sauce Demo practice site. Never put real credentials into source control.

## Reports and evidence
Maven Surefire writes JUnit XML/text reports under `target/surefire-reports/`. Failed test exceptions trigger screenshots before browser teardown under `target/screenshots/`. The test runner uses explicit WebDriverWait conditions and does not use fixed sleeps. A failed sorting assertion may be reported as a wait timeout if the page never reaches the selected order; screenshots and the displayed order should be inspected.

## Run
```bash
mvn test
```

## Traceability
| Manual case | Automated test |
|---|---|
| TC01 | `TC01_standardUserLogin` |
| TC02 | `TC02_lockedOutUser` |
| TC03 | `TC03_priceLowToHigh` |
| TC04 | `TC04_addTwoRemoveOne` |
| TC05 | `TC05_checkoutTotalAndEmptyCart` |
| TC06 | `TC06_missingCheckoutFields` (3 independent parameterized invocations) |
| TC07 | `TC07_protectedProductsUrl` |
| TC08 | `TC08_nameSorting` |
| TC09 | `TC09_priceHighToLow` |
| TC10–TC23 | Manual only / future automation candidates as described in ANSWERS.md |

## Growing to 200 tests
I would organize tests by feature and risk, build reusable page objects and test-data helpers, and separate smoke from full regression. I would keep the UI suite focused on critical user journeys while moving pure calculations, validation and authorization rules to unit/API tests if those layers are available. I would add cross-browser projects where supported, tags for priority/runtime, CI parallelism after confirming test isolation, and stable fixtures for repeatability. Every test should be independently runnable and should not depend on execution order. I would track flaky-test rates rather than relying on retries to conceal instability, and retain JUnit reports and screenshots as failure evidence.

## Notes
Only the specified Sauce Demo practice site is targeted.

## Verification status
The source has been reviewed but browser tests have **not** been executed in this delivery environment. Run `mvn test` locally and inspect the generated reports before submitting.

## Review limitations
This package was statically reviewed and revised, but Maven and Chrome integration tests were not run here. Browser/version-specific behavior must be verified locally. No test-pass claim is made.
