package GPT5.ws07.seq07;

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
 * Single-file JUnit 5 + Selenium 4 test suite for https://jsfiddle.net/
 *
 * - Uses FirefoxDriver in HEADLESS mode via options.addArguments("--headless")
 * - Uses WebDriverWait with Duration.ofSeconds(10)
 * - Tests base page, external links, and one-level internal links (limited to avoid flakiness)
 */
@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static URI baseUri;

    @BeforeAll
    public static void beforeAll() throws URISyntaxException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUri = new URI(BASE_URL);
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // Ignore if session is already closed
            }
        }
    }

    private void goToBaseAndWait() {
        try {
            driver.navigate().to(BASE_URL);
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(baseUri.getHost().toLowerCase()),
                    "Expected current URL to contain host: " + baseUri.getHost());
        } catch (Exception e) {
            // If session is invalid, recreate driver
            if (e.getMessage().contains("NoSuchSessionException") || driver == null) {
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--headless");
                driver = new FirefoxDriver(options);
                wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                driver.navigate().to(BASE_URL);
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            } else {
                throw e;
            }
        }
    }

    private boolean isExternalHref(String href) {
        if (href == null || href.trim().isEmpty()) return false;
        try {
            URI uri = new URI(href);
            String host = uri.getHost();
            if (host == null) return false; // relative -> internal
            return !host.equalsIgnoreCase(baseUri.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    private void closeTabAndSwitchBack(String originalHandle) {
        try {
            Set<String> handles = driver.getWindowHandles();
            if (handles.size() > 1) {
                driver.close();
            }
            driver.switchTo().window(originalHandle);
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (Exception e) {
            // If session is invalid, go back to base
            goToBaseAndWait();
        }
    }

    private List<String> collectOneLevelInternalLinks() {
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> result = new LinkedHashSet<>();
        for (WebElement a : anchors) {
            try {
                String href = a.getAttribute("href");
                if (href == null || href.trim().isEmpty()) continue;
                URI uri = new URI(href);
                if (uri.getHost() == null || uri.getHost().equalsIgnoreCase(baseUri.getHost())) {
                    // include internal or relative links; one-level limitation applied later when testing
                    result.add(href);
                }
            } catch (Exception e) {
                // treat as internal if parsing fails
                String href = a.getAttribute("href");
                if (href != null) result.add(href);
            }
        }
        return new ArrayList<>(result);
    }

    @Test
    @Order(1)
    public void testHomePageTitleAndHeader() {
        goToBaseAndWait();
        String title = driver.getTitle();
        Assertions.assertTrue(title != null && title.toLowerCase().contains("jsfiddle"),
                "Expected page title to contain 'JSFiddle', actual: " + title);

        // Look for main header/logo area (robust: look for element with role banner or header tags)
        List<WebElement> headers = driver.findElements(By.cssSelector("header, .header, .navbar, .top"));
        Assertions.assertTrue(headers.size() > 0 && headers.get(0).isDisplayed(),
                "Expected a visible header or navbar on the JSFiddle home page.");
    }

    @Test
    @Order(2)
    public void testCreateNewFiddleButtonOpensEditor() {
        goToBaseAndWait();
        // Try to find "New" or "Create" link/button; use several robust selectors
        List<By> selectors = Arrays.asList(
                By.cssSelector("a[href='/']"), // logo link
                By.cssSelector("a.logo"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new')]"),
                By.xpath("//a[contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new')]"),
                By.cssSelector("a[href*='fiddle']"),
                By.cssSelector("button[title*='New'], button[aria-label*='New']")
        );

        boolean opened = false;
        String originalUrl = driver.getCurrentUrl();
        for (By sel : selectors) {
            List<WebElement> elems = driver.findElements(sel);
            if (elems.isEmpty()) continue;
            for (WebElement e : elems) {
                try {
                    if (!e.isDisplayed()) continue;
                    wait.until(ExpectedConditions.elementToBeClickable(e));
                    e.click();
                    // Wait for either URL change or editor area present
                    wait.until(d -> !d.getCurrentUrl().equals(originalUrl)
                            || d.findElements(By.cssSelector(".panel, #panel, .result, #editor")).size() > 0);
                    // assert success: URL contains 'fiddle' or an editor panel exists
                    String current = driver.getCurrentUrl();
                    boolean hasEditor = driver.findElements(By.cssSelector(".panel, #panel, #editor, .CodeMirror")).size() > 0;
                    if (current.toLowerCase().contains("fiddle") || hasEditor) {
                        opened = true;
                        break;
                    } else {
                        // navigate back and try next
                        driver.navigate().back();
                        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                    }
                } catch (Exception ignored) {
                }
            }
            if (opened) break;
        }

        Assertions.assertTrue(opened, "Expected to open an editor or a fiddle page from the home page using a 'New' or related control.");
        // return to base for subsequent tests
        driver.navigate().to(BASE_URL);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    @Test
    @Order(3)
    public void testExternalLinksOpenAndContainDomain() {
        goToBaseAndWait();
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> hrefs = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        String originalHandle = driver.getWindowHandle();
        int tested = 0;
        for (String href : hrefs) {
            if (isExternalHref(href)) {
                tested++;
                if (tested > 4) break; // limit to avoid flakiness
                try {
                    // Open link in new tab
                    ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", href);
                    wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                    
                    // Switch to new window
                    for (String handle : driver.getWindowHandles()) {
                        if (!handle.equals(originalHandle)) {
                            driver.switchTo().window(handle);
                            break;
                        }
                    }
                    
                    try {
                        URI u = new URI(href);
                        String host = u.getHost();
                        if (host != null) {
                            wait.until(d -> d.getCurrentUrl().contains(host));
                            Assertions.assertTrue(driver.getCurrentUrl().contains(host),
                                    "External link should open a URL that contains host: " + host);
                        }
                    } catch (URISyntaxException ignored) {
                        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                    }
                } finally {
                    // close and switch back
                    closeTabAndSwitchBack(originalHandle);
                }
            }
        }
        if (hrefs.stream().anyMatch(this::isExternalHref)) {
            Assertions.assertTrue(tested > 0, "There are external links but none were tested.");
        }
    }

    @Test
    @Order(4)
    public void testOneLevelInternalLinksNavigateAndContainContent() {
        goToBaseAndWait();
        List<String> internalLinks = collectOneLevelInternalLinks().stream()
                .filter(h -> !h.equalsIgnoreCase(BASE_URL))
                .collect(Collectors.toList());

        int tested = 0;
        for (String href : internalLinks) {
            if (tested >= 6) break; // keep test suite fast and stable
            try {
                // Only navigate to links that keep us one level below root OR are relative
                URI uri = new URI(href);
                String path = uri.getPath() == null ? "" : uri.getPath();
                String normalized = path.replaceAll("^/+", "").replaceAll("/+$", "");
                int segments = normalized.isEmpty() ? 0 : normalized.split("/").length;
                if (segments > 2) continue; // skip deeper links

                driver.navigate().to(href);
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

                boolean hasHeader = driver.findElements(By.cssSelector("h1, h2, h3")).stream().anyMatch(WebElement::isDisplayed);
                boolean hasBodyText = driver.findElements(By.tagName("body")).stream()
                        .anyMatch(b -> b.getText() != null && b.getText().trim().length() > 20);

                Assertions.assertTrue(hasHeader || hasBodyText, "Navigated internal page " + href + " should contain header or body text.");

                tested++;
            } catch (URISyntaxException e) {
                // if URI can't be parsed, skip
            } catch (Exception e) {
                // register a failure for navigation but continue
                Assertions.fail("Failed to navigate or validate internal link: " + href + " -> " + e.getMessage());
            } finally {
                // return to base
                goToBaseAndWait();
            }
        }

        if (!internalLinks.isEmpty()) {
            Assertions.assertTrue(tested > 0, "Expected to test at least one one-level internal link.");
        }
    }
}