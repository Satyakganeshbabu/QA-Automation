# Section A1 — Manual test suite

## Questions for the product owner and assumptions

1. **What postal-code formats are supported?** Assumption: postal code is mandatory but any non-empty string is accepted until format rules are supplied.
2. **Should sorting be stable when two products have the same price?** Assumption: all items must be ordered by numeric price; equal-price items may retain their prior relative order.
3. **Should the cart persist after logout and login?** Assumption: the assessment only requires cart clearing after a successful order; persistence across sessions needs clarification.
4. **Can the same product be added more than once?** Assumption: quantity can increase if the UI supports it; the primary cart tests use two distinct products.
5. **What is the expected tax calculation and rounding rule?** Assumption: use the tax displayed by the application and require total = displayed item total + displayed tax to two decimal places.
6. **What should happen if a user opens a checkout URL directly without a cart?** Assumption: the app should redirect or show a safe empty-state; it must not allow a completed order without a valid flow.
7. **Which browsers and viewport sizes are supported?** Assumption: desktop Chromium is the baseline; check a narrow mobile-sized viewport for overflow and core usability.
8. **What accessibility standard is required?** Assumption: basic keyboard navigation, visible focus, labels, and meaningful accessible names are expected; formal WCAG conformance is not claimed without a stated target.
9. **Should Cancel on checkout overview preserve the cart?** Assumption: it returns to the prior cart/flow without placing the order, and cart contents remain.
10. **What is the expected session timeout and behavior after logout?** Assumption: logout invalidates access to inner pages; direct URL access must return to login.

## Test cases

| ID | Area | Type | Title | Pre-conditions | Steps | Test data | Expected result | Priority | Automate? and why |
|---|---|---|---|---|---|---|---|---|---|
| TC01 | Login | Positive | Standard user logs in | Login page open | Enter valid credentials; submit | `standard_user` / shared password | Products page is shown | P1 | Yes — core smoke journey |
| TC02 | Login | Negative | Locked-out user cannot log in | Login page open | Enter locked-out credentials; submit | `locked_out_user` / shared password | Clear locked-out error; remains logged out | P1 | Yes — deterministic negative path |
| TC03 | Products | Positive | Sort price low to high | Logged in | Select Price (low to high); inspect every product price | All listed prices | Entire list is non-decreasing | P1 | Yes — regression-prone sorting rule |
| TC04 | Cart | Positive | Add two products and remove one | Logged in; empty cart | Add two different products; check badge; remove one from products page; open cart | Backpack, Bike Light | Badge changes 0→2→1; only remaining item appears in cart | P1 | Yes — key cart behavior |
| TC05 | Checkout | Positive | Complete checkout and verify arithmetic | Logged in; empty cart | Add two products; checkout; enter details; continue; compare amounts; finish | Valid names and postal code | Total equals item total + tax; confirmation shown; cart badge absent | P1 | Yes — end-to-end revenue-critical path |
| TC06 | Checkout | Negative/boundary | Each required field is validated | Logged in; at least one item in cart | Submit checkout info with first name missing, then last name missing, then postal code missing | One field blank per attempt | Correct field-specific validation error; cannot continue | P1 | Yes — parameterized deterministic validation |
| TC07 | Security/session | Negative | Direct products URL after logout is blocked | Logged in | Log out; navigate directly to `/inventory.html` | No active session | Redirects to login or denies access; products are not exposed | P1 | Yes — protects authenticated page access |
| TC08 | Products | Positive | Sort names A–Z and Z–A | Logged in | Select each name sort; inspect complete names list | All product names | Whole list is ascending/descending alphabetically | P2 | Yes — repeatable ordering logic |
| TC09 | Products | Positive | Sort price high to low | Logged in | Select Price (high to low); inspect every price | All listed prices | Entire list is non-increasing | P2 | Yes — counterpart to ascending sort |
| TC10 | Cart | Positive | Remove item from cart page | Logged in; two products in cart | Open cart; remove one item | Two distinct products | Removed item disappears and badge decrements | P2 | Yes — alternate removal entry point |
| TC11 | Checkout | Negative | Cancel from overview does not place order | Logged in; cart has item; overview open | Click Cancel | Backpack | No order confirmation; no order placed; cart remains available with item | P1 | Yes — business transaction safety; not included in initial 9 to keep suite focused |
| TC12 | Checkout | Boundary | Checkout with one item | Logged in; empty cart | Add one product and complete checkout | Lowest-priced product | Item total matches item price; total equals subtotal + tax | P2 | Yes — single-item arithmetic boundary |
| TC13 | Products | Boundary | Empty cart badge is absent or zero | Logged in; no items added | Observe cart badge | Empty cart | No misleading positive count is shown | P2 | Yes — badge state |
| TC14 | Login | Negative | Invalid password is rejected | Login page open | Enter valid username and wrong password | `standard_user` / incorrect password | Error shown; user remains logged out | P1 | Yes — authentication negative path |
| TC15 | Products | Usability | Product page remains usable at narrow viewport | Logged in | Set viewport to 375×812; inspect product cards, sort, and cart navigation | Mobile-sized viewport | No critical overlap/clipping; controls remain usable; horizontal scroll not required for core tasks | P2 | No — visual judgment; can add visual regression later |
| TC16 | Accessibility | Accessibility | Core flow can be operated by keyboard | Login page open | Use Tab/Shift+Tab and Enter through login, sort, add, cart and checkout controls | Keyboard only | Focus is visible and order is logical; controls can be activated without a mouse | P2 | Partly — automated accessibility checks help, but keyboard usability needs human review |
| TC17 | Browser | Compatibility | Core shopping flow works in a second supported browser | Test account available | Repeat login, add item, cart and checkout in Firefox or WebKit | Same standard data | No browser-specific functional failure | P2 | Yes — cross-browser CI project once supported |
| TC18 | Security/session | Negative | Back button after logout does not reveal authenticated content | Logged in; visit products page | Log out; press browser Back; refresh if needed | No active session | Protected content is not usable; session is invalidated | P1 | Yes — session security |
| TC19 | Products | Positive | Product details open and return to list | Logged in | Open a product detail; return to products | Any product | Correct product details shown; returning preserves a sensible list state | P3 | Yes — lower-risk navigation regression |
| TC20 | Cart | Boundary | Remove the last item | Logged in; one item in cart | Open cart; remove item | One item | Empty-state shown; badge removed; checkout cannot submit a nonexistent order | P2 | Yes — empty-cart transition |
| TC21 | Checkout | Negative | Direct checkout URL without session is blocked | Logged out | Navigate directly to checkout URL | No session | Redirect to login or safe denial; no customer/order data exposed | P1 | Yes — protected route coverage |
| TC22 | Checkout | Negative | Attempt checkout with empty cart | Logged in; empty cart | Navigate through cart/checkout URLs if available | Empty cart | App prevents placing an order with no items | P1 | Yes — transaction invariant |
| TC23 | Exploratory | Exploratory | Explore known-problem user behavior for 15 minutes | Use only the provided public practice site and documented test users | See charter below; record observations with steps and evidence | `problem_user`, `performance_glitch_user`, and `error_user` where available | Identify and document inconsistent UI, slow interactions, broken images/actions, or unexpected errors without assuming every known issue is a release blocker | P2 | No — exploratory discovery is human-led; discovered regressions can later become automated |

### 15-minute exploratory charter — known-problem users

**Mission:** Explore whether known-problem accounts can complete the core shopping journey and identify user-visible inconsistencies.

- **Minutes 0–3:** Log in as each documented problem user one at a time; note load time, error messages, layout and image rendering.
- **Minutes 3–7:** Browse products, sort both names and prices, and compare displayed order against the selected sort.
- **Minutes 7–11:** Add/remove products, check the badge and cart contents, and look for stale or mismatched state.
- **Minutes 11–14:** Attempt checkout with valid details; observe validation, transitions and whether totals remain internally consistent. Do not place repeated orders unnecessarily.
- **Minute 14–15:** Capture one concise note per finding with user, steps, expected/actual behavior, screenshot/trace if useful, and severity rationale.

**Boundaries:** Use only Sauce Demo, avoid load/stress testing, and do not assume a known-problem user’s behavior is representative of the standard user. Report observed facts separately from hypotheses.

## Test strategy (under 150 words)

Automate P1 user journeys and deterministic rules at the UI layer: standard/locked-out login, complete-list sorting, cart add/remove state, required-field errors, checkout arithmetic, order confirmation, and protected-route behavior. Run a small smoke subset on each change and the broader suite in CI. If the client exposes unit/API layers, cover tax arithmetic, field validation, cart calculations, sorting functions, and authorization rules there because those checks are faster and isolate failures better; do not invent an API where none is documented. Keep keyboard/accessibility review, responsive layout, exploratory testing with known-problem users, and visual usability checks manual or supported by tools plus human review. The biggest remaining risk is that a green UI suite may not prove server-side order integrity or payment/order persistence under real service failures; I would report that risk and request API/service-level coverage and monitoring.

# Section B — AI output and client communication

## B1. Review of the AI-generated test

Problems:
1. It uses Playwright syntax but does not identify the test runner setup or ensure imports/fixtures are configured.
2. It hard-codes the live practice URL and credentials inside the test rather than using configuration/environment variables.
3. `text=Add to cart` is ambiguous because multiple products have an “Add to cart” button; it may click an unintended product and is not scoped to a product.
4. It uses a fixed three-second sleep, which is slow and flaky; Playwright auto-waits for actionable elements and assertions.
5. It does not assert that login succeeded or that the cart contains the intended product.
6. It checks only that the total text is truthy; it never verifies subtotal + tax = total.
7. It does not parse currency safely or compare to the cent.
8. It does not assert a successful order confirmation.
9. It does not check that the cart is empty after finishing the order.
10. It has no Page Object Model or equivalent abstraction; selectors and workflow are all in the test body.
11. It uses very weak checkout data (`A`, `B`, `1`) without explaining whether those values satisfy any format rule.
12. It does not check the page state after Continue or Finish, so an error page could still satisfy the weak truthiness assertion.
13. It provides no traceability to a manual test case ID.
14. It omits useful failure evidence and does not itself establish HTML/JUnit reporting.
15. The Markdown link syntax around the URL may not be a valid URL string if copied literally into the code.

**Rewrite:** See `TC05 checkout total equals item total plus tax and successful order empties cart` in `tests/shop-checkout.spec.js`. It uses page objects, waits through Playwright’s built-in behavior, checks the displayed arithmetic to the cent, confirms order completion, and verifies that the cart badge is absent afterward.

## B2. AI feature evaluation — Test case generator

I would evaluate the generator that converts user stories into fixed-schema JSON because malformed output can break the downstream test-management import.

| Check | Evaluation design | Scoring method |
|---|---|---|
| Coverage of requirements | Build a reviewed gold set of stories with positive, negative, boundary, and role/permission rules. Compare generated cases against a requirement-to-test coverage matrix. | Rubric: score each required behavior covered, partially covered, or missed. |
| Invented requirements | Include stories with deliberate ambiguity and compare each generated assertion/test-data rule with the source story and approved assumptions. | Human review: flag unsupported requirements and record a hallucination rate. |
| Duplicate or near-duplicate cases | Use stories with overlapping wording and check exact duplicates plus semantically equivalent tests. | Automatically: exact normalized duplicates; rubric/human review for semantic duplicates. |
| Strict JSON validity and schema | Validate output with a JSON parser and JSON Schema for required keys, types, allowed enums, and non-empty fields. | Automatically: pass/fail, plus percentage of outputs valid on first generation. |

I would report per-check scores, not only one average, because schema failure can block an import while missing a critical negative test can create product risk.

## B3. Jira defect report

**Title:** [4.2.0][Products] Price ascending sort places $29.99 before $15.99

**Environment:** Release 4.2.0; Chrome; Sauce Demo practice store; test account: standard user.

**Preconditions:** User is logged in and the Products page is displayed.

**Steps to reproduce:**
1. Open the Products page.
2. Select **Price (low to high)** in the sort control.
3. Compare the displayed prices from top to bottom.
4. Repeat the steps to confirm consistency.

**Expected result:** All products are sorted by numeric price in ascending order; no higher-priced item appears before a lower-priced item.

**Actual result:** An item priced at **$29.99** appears before an item priced at **$15.99**. The issue has reproduced on every run since release 4.2.0.

**Severity:** Major (proposed). Sorting is a visible core shopping feature and can mislead customers comparing prices. It does not currently appear to block checkout, so Critical is not justified by the evidence provided. Final priority should be agreed with product/business owners.

**Priority:** P1 for release triage, subject to the team's severity/priority definitions.

**Evidence to attach:**
- Screenshot showing the selected ascending sort and the two out-of-order prices.
- Short screen recording or Playwright trace showing the reproduction.
- Browser/version, release/build identifier, timestamp and test account type.
- Test execution result and any console errors relevant to the behavior.

**Frequency:** 100% of observed runs since 4.2.0.  
**Workaround:** Select another sort or manually compare prices; this is not an acceptable long-term fix.

## B4. Message to the product manager (under 150 words)

Hi — our release checks found two risks before Friday’s planned release. Price sorting is consistently incorrect in 4.2.0: a $29.99 item appears before a $15.99 item when customers choose “Price (low to high).” Separately, checkout fails around 1 in 10 runs, although a retry passes. That second issue may indicate an intermittent problem and needs investigation rather than being dismissed as a passing rerun.

**My recommendation is to hold the release until we understand the checkout failure and fix or explicitly accept the sorting defect.** A practical alternative is to delay only the affected release, fix sorting, and use a short focused regression run on checkout and order totals before rescheduling. I’ll share reproducible steps and evidence for both issues, and provide an update with the likely cause and retest results as soon as we have them.

## Section C — Live discussion preparation

### C1. How I would analyze “Checkout has been failing for some users since this morning”

1. **Clarify impact and urgency.** Ask when it started, which users/regions/browsers are affected, exact error messages, whether all payment methods or only certain orders fail, approximate failure rate, business impact, and whether a workaround exists. Confirm whether orders are failing before submission or whether duplicate/uncertain orders are possible.
2. **Establish scope.** Check monitoring, error rates, traces, application/service logs, browser/network errors, support tickets, and failed automated tests. Compare affected and unaffected sessions without exposing personal data.
3. **Check recent changes.** Review deployments, feature flags, configuration, dependency/service incidents, database changes, and test-data changes around the start time.
4. **Reproduce safely.** Use a non-production environment or approved test account with the same browser, workflow and relevant data. Capture timestamps, correlation/request IDs, network responses and logs. Avoid repeated real transactions.
5. **Narrow the layer.** Determine whether the failure is UI validation, API response, business rule, service dependency, data-specific, permissions, or environment-related. Compare a known-good scenario with a failing one and change one variable at a time.
6. **Distinguish defect from data/environment.** A reproducible failure tied to application logic or a recent code change points toward a product defect. Failures isolated to invalid/stale data, credentials, unavailable dependencies, or one environment may indicate data/configuration/environment issues; verify before concluding.
7. **Communicate while investigating.** Acknowledge quickly, share what is known versus unknown, impact, mitigation and next update time. Do not wait for root cause before sending a status update. Escalate immediately if orders or customer data may be at risk.
8. **Confirm recovery.** Retest the original failing case and related paths, monitor the failure rate, verify order state and duplicate handling, then send a concise root-cause/impact/prevention summary.

### C2. Recent production issue — personalize before the live discussion

**Important:** Use a real incident you personally worked on. The outline below is a fill-in structure, not a claim that a specific incident happened.

- **What happened and discovery:** “In my Cognizant QA work on a Pega application, I noticed [specific behavior] while [executing a test / reviewing a report / receiving a user report]. The impact was [specific user or business consequence].”
- **Root cause:** “I reproduced it using [inputs/steps], compared the expected and actual results, and checked [logs / data / related workflow / recent change] with [developer or lead]. We identified [confirmed cause].”
- **Client communication:** “I informed [lead/client contact] through [approved channel] at [time relative to discovery]. I explained the user impact in plain language, attached [steps/screenshots/test data], and separated confirmed facts from the suspected cause.”
- **Prevention:** “We [added a regression case / updated test data / improved validation / added a review or monitoring check]. Later, [say whether it caught a recurrence, or be clear if there has not yet been a recurrence].”
- **Looking back:** “I would [specific improvement, such as adding the regression test earlier, checking an adjacent workflow, or escalating impact sooner].”

Keep the story honest and concise. Be ready to explain your exact contribution, how the root cause was confirmed, and what evidence supported it. Do not claim a production fix or monitoring change unless you personally know it happened.
