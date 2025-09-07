package GTP5.ws04.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Single-file JUnit 5 + Selenium 4 test suite.
 *
 * Notes:
 * - Uses FirefoxDriver with headless argument via addArguments("--headless").
 * - Tests operate from the BASE_URL and exercise one-level links and external links.
 * - Designed to be resilient: guards optional elements and uses explicit waits.
 *
 * File/class name must match. Package as requested.
 */
@TestMethodOrder(OrderAnnotation.class)
public class KatalonFormTest {
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static URI baseUri;

    @BeforeAll
    public static void setup() throws URISyntaxException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED by spec
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUri = new URI(BASE_URL);
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Helper: navigate to base url and wait for basic load (document.readyState = complete)
    private void goToBaseAndWait() {
        driver.navigate().to(BASE_URL);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        // assert we are on the expected host/path
        Assertions.assertTrue(driver.getCurrentUrl().contains(baseUri.getHost()),
                "After navigation, expected current URL to contain base host: " + baseUri.getHost());
    }

    // Helper: open a URL in a new tab and switch to it; returns the new window handle.
    private String openUrlInNewTab(String url) {
        String original = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", url);
        // wait for additional window
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                return h;
            }
        }
        return original;
    }

    // Helper: close current tab and switch back to original
    private void closeTabAndSwitchBack(String originalHandle) {
        driver.close();
        driver.switchTo().window(originalHandle);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    // Helper: determine if an href is external to the base host
    private boolean isExternalHref(String href) {
        try {
            URI uri = new URI(href);
            String host = uri.getHost();
            if (host == null) return false; // relative link -> internal
            return !host.equalsIgnoreCase(baseUri.getHost());
        } catch (Exception e) {
            // malformed URIs are treated as internal to avoid flaky behavior
            return false;
        }
    }

    // Collect one-level internal links (same host and path depth 1 relative to base path)
    private List<String> collectOneLevelInternalLinks() {
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> result = new LinkedHashSet<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.trim().isEmpty()) continue;
            try {
                URI uri = new URI(href);
                if (uri.getHost() == null || uri.getHost().equalsIgnoreCase(baseUri.getHost())) {
                    // same host or relative; compute path depth relative to base
                    String path = uri.getPath();
                    if (path == null) path = "";
                    String basePath = baseUri.getPath();
                    if (basePath == null) basePath = "";
                    // Normalize: remove leading/trailing slashes
                    String p = path.replaceAll("^/+", "").replaceAll("/+$", "");
                    // count segments
                    int segments = p.isEmpty() ? 0 : p.split("/").length;
                    // Consider one-level below as exactly 1 segment different than base root,
                    // but to be permissive include any single-segment paths or same-file links.
                    if (segments <= 1) {
                        result.add(href);
                    } else {
                        // if path is exactly one additional segment relative to basePath
                        String bp = basePath.replaceAll("^/+", "").replaceAll("/+$", "");
                        int baseSegments = bp.isEmpty() ? 0 : bp.split("/").length;
                        if (segments == baseSegments + 1) result.add(href);
                    }
                }
            } catch (URISyntaxException ignored) {
                // treat as internal and include
                result.add(href);
            }
        }
        return new ArrayList<>(result);
    }

    @Test
    @Order(1)
    public void testBasePageLoadsAndHasForm() {
        goToBaseAndWait();
        // Prefer robust locators: look for <form>, or common named inputs
        List<WebElement> forms = driver.findElements(By.tagName("form"));
        Assertions.assertTrue(forms.size() > 0, "Expected at least one <form> element on the base page.");

        // Check some expected inputs exist (guarded)
        boolean hasTextInput = driver.findElements(By.cssSelector("input[type='text'], input[type='email'], textarea")).size() > 0;
        Assertions.assertTrue(hasTextInput, "Expected at least one text input or textarea on form page.");

        // Check for a submit button (type=submit or button with text submit)
        boolean hasSubmit = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']")).size() > 0
                || driver.findElements(By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'submit')]")).size() > 0;
        Assertions.assertTrue(hasSubmit, "Expected a submit button on the form page.");
    }

    @Test
    @Order(2)
    public void testFillAndSubmitFormIfPossible() {
        goToBaseAndWait();

        // Attempt to fill the form in a robust manner: find visible text inputs and fill predictable values.
        List<WebElement> textInputs = driver.findElements(By.cssSelector("input[type='text'], input[type='email'], textarea"));
        for (int i = 0; i < textInputs.size(); i++) {
            WebElement inp = textInputs.get(i);
            if (!inp.isDisplayed() || !inp.isEnabled()) continue;
            try {
                wait.until(ExpectedConditions.elementToBeClickable(inp));
                String value = "TestValue" + (i + 1);
                inp.clear();
                inp.sendKeys(value);
            } catch (Exception ignore) {
                // continue if can't interact
            }
        }

        // If there are checkboxes/radios, toggle the first of each type
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (!checkboxes.isEmpty()) {
            WebElement cb = checkboxes.get(0);
            if (cb.isDisplayed() && cb.isEnabled()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(cb));
                    cb.click();
                } catch (Exception ignored) { }
            }
        }

        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        if (!radios.isEmpty()) {
            WebElement r = radios.get(0);
            if (r.isDisplayed() && r.isEnabled()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(r));
                    r.click();
                } catch (Exception ignored) { }
            }
        }

        // Find a submit control robustly
        List<WebElement> submitButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        if (submitButtons.isEmpty()) {
            // fallback: look for button with text containing 'submit' (case-insensitive)
            submitButtons = driver.findElements(By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'submit')]"));
        }

        Assertions.assertTrue(submitButtons.size() > 0, "No submit button available to test form submission.");

        String originalUrl = driver.getCurrentUrl();
        WebElement submit = submitButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(submit));
        submit.click();

        // After submit: wait for either navigation (URL change), alert, or appearance of a "success" like text.
        boolean success = false;
        try {
            wait.until(d -> !d.getCurrentUrl().equals(originalUrl) || d.switchTo().alert() != null);
            success = !driver.getCurrentUrl().equals(originalUrl);
        } catch (Exception ignored) {
            // no URL change or alert within wait
        }

        if (!success) {
            // look for success messages in body (case-insensitive)
            WebElement body = driver.findElement(By.tagName("body"));
            String bodyText = body.getText().toLowerCase(Locale.ROOT);
            if (bodyText.contains("success") || bodyText.contains("thank you") || bodyText.contains("submitted")) {
                success = true;
            }
        }

        Assertions.assertTrue(success, "Form submission did not cause a navigational change, alert, or a visible success message.");
    }

    @Test
    @Order(3)
    public void testExternalLinksOpenAndContainDomain() {
        goToBaseAndWait();

        // Collect anchors with hrefs that are external
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> hrefs = anchors.stream()
                .map(a -> {
                    try { return a.getAttribute("href"); } catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .filter(h -> !h.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // Limit number of external links to avoid flakiness; test up to 5 external links
        int externalTested = 0;
        String originalHandle = driver.getWindowHandle();

        for (String href : hrefs) {
            if (isExternalHref(href)) {
                externalTested++;
                if (externalTested > 5) break;

                // Open in new tab to avoid losing the base page
                String newHandle = openUrlInNewTab(href);
                try {
                    // Wait for URL to contain the host of href
                    URI hrefUri;
                    try {
                        hrefUri = new URI(href);
                        String expectedHost = hrefUri.getHost();
                        // Wait for current URL to contain expected host
                        wait.until(ExpectedConditions.urlContains(expectedHost));
                        String current = driver.getCurrentUrl();
                        Assertions.assertTrue(current.contains(expectedHost),
                                "External link opened URL should contain expected host. Expected: " + expectedHost + " but was: " + current);
                    } catch (URISyntaxException ue) {
                        // If href can't be parsed, assert that page loaded (document ready)
                        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                    }
                } finally {
                    // Close the new tab and switch back
                    driver.close();
                    driver.switchTo().window(originalHandle);
                    wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                }
            }
        }

        // It's acceptable if no external links were present, but if there are any, we should have tested at least one.
        long totalExternal = hrefs.stream().filter(this::isExternalHref).count();
        if (totalExternal > 0) {
            Assertions.assertTrue(externalTested > 0, "There were external links but none were tested due to limits.");
        }
    }

    @Test
    @Order(4)
    public void testOneLevelInternalLinksNavigateAndHaveContent() {
        goToBaseAndWait();

        List<String> internalLinks = collectOneLevelInternalLinks();

        // Remove the base URL itself from the list
        internalLinks = internalLinks.stream().filter(h -> !h.equalsIgnoreCase(BASE_URL)).collect(Collectors.toList());

        // Limit to avoid heavy runs
        int tested = 0;
        for (String href : internalLinks) {
            if (tested >= 6) break; // keep suite fast and stable
            try {
                // Use normal navigation (same tab) to validate the link target is reachable and has content
                driver.navigate().to(href);
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

                // Assert that page has at least some visible textual content or a header
                boolean hasHeader = driver.findElements(By.cssSelector("h1, h2, h3")).stream().anyMatch(WebElement::isDisplayed);
                boolean hasBodyText = driver.findElements(By.tagName("body")).stream()
                        .anyMatch(b -> b.getText() != null && b.getText().trim().length() > 20);

                Assertions.assertTrue(hasHeader || hasBodyText, "Navigated page " + href + " should contain a header or body text.");

                tested++;
            } catch (Exception e) {
                // Record a failure for this particular link but continue testing others
                Assertions.fail("Failed to navigate or validate internal link: " + href + " -> " + e.getMessage());
            } finally {
                // return to base for next iteration
                driver.navigate().to(BASE_URL);
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            }
        }

        // If there are internal links, at least one should have been tested
        if (!internalLinks.isEmpty()) {
            Assertions.assertTrue(tested > 0, "Expected to test at least one one-level internal link.");
        }
    }
}
