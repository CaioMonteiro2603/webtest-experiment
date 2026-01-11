package GPT5.ws07.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED by prompt
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ====================== Helpers ======================

    private void openBase() {
        driver.get(BASE_URL);
        waitDocumentReady();
        dismissOverlaysIfAny();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
        Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "Body element should exist");
    }

    private void waitDocumentReady() {
        try {
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private void dismissOverlaysIfAny() {
        // Generic cookie/consent banners (best-effort and guarded)
        String[] acceptTexts = new String[]{"accept", "agree", "allow", "got it"};
        for (String t : acceptTexts) {
            By xpath = By.xpath("//*[self::button or self::a][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + t + "')]");
            List<WebElement> els = driver.findElements(xpath);
            if (!els.isEmpty()) {
                try {
                    WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(els.get(0)));
                    btn.click();
                    waitDocumentReady();
                    break;
                } catch (Exception ignored) {}
            }
        }
        // Close icons common patterns
        By closeBtn = By.cssSelector("[data-dismiss], .cookiebar-close, .cc-dismiss, .close, .btn-close");
        List<WebElement> closes = driver.findElements(closeBtn);
        if (!closes.isEmpty()) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(closes.get(0))).click();
            } catch (Exception ignored) {}
        }
    }

    private boolean present(By by) {
        return driver.findElements(by).size() > 0;
    }

    private String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return ""; }
    }

    private int depthOfPath(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) return 0;
        String p = path;
        if (p.startsWith("/")) p = p.substring(1);
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        if (p.isEmpty()) return 0;
        return p.split("/").length;
    }

    private void handleExternalLink(WebElement link) {
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        String baseHost = hostOf(driver.getCurrentUrl());

        // open link
        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(link));
        web.click();

        // determine if new window opened
        try {
            wait.until(d -> driver.getWindowHandles().size() != before.size() || !hostOf(d.getCurrentUrl()).equals(baseHost));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            waitDocumentReady();
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "External link should navigate to another domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            waitDocumentReady();
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "External link should navigate to another domain (same tab)");
            driver.navigate().back();
            waitDocumentReady();
        }
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        return null;
    }

    // ====================== Tests ======================

    @Test
    @Order(1)
    public void baseLoads_HeaderFooterCoreAreasPresent() {
        openBase();
        Assertions.assertTrue(present(By.cssSelector("header, .navbar, nav")), "Header/nav should be present");
        Assertions.assertTrue(present(By.tagName("footer")), "Footer should be present");
        // Top search bar should exist on JSFiddle
        Assertions.assertTrue(present(By.cssSelector("input[type='search'], input[name='q'], input[placeholder*='Search']")), "Search input should be present");
    }

    @Test
    @Order(2)
    public void searchFromHeaderNavigatesToSearchResults() {
        openBase();
        WebElement search = firstPresent(
                By.cssSelector("input[type='search']"),
                By.cssSelector("input[name='q']"),
                By.cssSelector("input[placeholder*='Search']")
        );
        Assumptions.assumeTrue(search != null, "Search input not found; skipping test");
        String startUrl = driver.getCurrentUrl();
        search.clear();
        search.sendKeys("react");
        search.sendKeys(Keys.ENTER);

        // Assert navigation to a search page or query in URL
        boolean urlChanged = false;
        try {
            urlChanged = wait.until(d ->
                    !d.getCurrentUrl().equals(startUrl) &&
                    (d.getCurrentUrl().contains("/search") || d.getCurrentUrl().contains("?q=") || d.getCurrentUrl().contains("&q=")));
        } catch (TimeoutException ignored) {}
        Assertions.assertTrue(urlChanged || present(By.cssSelector("[class*='search']")), "Search should navigate to results or update URL with query");

        // Go back to keep state clean
        driver.navigate().back();
        waitDocumentReady();
    }

    @Test
    @Order(3)
    public void visitInternalLinks_OneLevelBelow() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        URI base = URI.create(driver.getCurrentUrl());
        int baseDepth = depthOfPath(base.getPath());

        // collect internal links
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> hrefs = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> h.startsWith("http"))
                .distinct()
                .collect(Collectors.toList());

        List<String> candidates = hrefs.stream()
                .filter(h -> baseHost.equals(hostOf(h)))
                .filter(h -> {
                    URI u = URI.create(h);
                    int depth = depthOfPath(u.getPath());
                    return depth <= baseDepth + 1; // one level below the base
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!candidates.isEmpty(), "No internal links one-level-below found; skipping");

        int visited = 0;
        String originalUrl = driver.getCurrentUrl();
        for (String h : candidates) {
            if (visited >= 3) break; // limit to reduce flakiness
            try {
                String before = driver.getCurrentUrl();
                // find the anchor again to avoid stale reference
                Optional<WebElement> linkOpt = driver.findElements(By.cssSelector("a[href]")).stream()
                        .filter(a -> h.equals(a.getAttribute("href"))).findFirst();
                if (linkOpt.isEmpty()) continue;
                WebElement web = wait.until(ExpectedConditions.elementToBeClickable(linkOpt.get()));
                web.click();
                wait.until(d -> !d.getCurrentUrl().equals(before));
                waitDocumentReady();
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should remain on jsfiddle.net host");
                Assertions.assertTrue(present(By.tagName("body")), "Destination page should render content");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                visited++;
            } catch (Exception e) {
                // Recover and continue
                driver.get(originalUrl);
                waitDocumentReady();
            }
        }

        Assertions.assertTrue(visited >= 1, "Should be able to visit at least one internal link one-level-below");
    }

    @Test
    @Order(4)
    public void docsLink_OpensDocsSubdomainAsExternal() {
        openBase();
        // Many builds link to docs.jsfiddle.net or help pages
        WebElement docsLink = firstPresent(
                By.cssSelector("a[href*='docs.']"),
                By.cssSelector("a[href*='documentation']"),
                By.partialLinkText("Docs")
        );
        Assumptions.assumeTrue(docsLink != null, "Docs link not found; skipping");

        String baseHost = hostOf(driver.getCurrentUrl());
        handleExternalLink(docsLink);
        Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "After external check we should be back on base host");
    }

    @Test
    @Order(5)
    public void externalLinksInFooterOrHeader_OpenOnDifferentDomain() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement> anchors = driver.findElements(By.cssSelector("footer a[href], header a[href], nav a[href], a[target='_blank']"));
        List<WebElement> external = anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    if (href == null || !href.startsWith("http")) return false;
                    String host = hostOf(href);
                    return !baseHost.equals(host);
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!external.isEmpty(), "No external links detected; skipping");
        int validated = 0;
        for (WebElement link : external) {
            if (validated >= 3) break; // keep it lean
            try {
                handleExternalLink(link);
                validated++;
            } catch (Exception e) {
                // If popup blocked or CSP issues, attempt to continue
                driver.get(BASE_URL);
                waitDocumentReady();
            }
        }
        Assertions.assertTrue(validated > 0, "At least one external link should be verified");
    }

    @Test
    @Order(6)
    public void signInOrCreateLink_VisibleAndNavigable() {
        openBase();
        // Try to locate a Sign in / Login / Create button/link
        WebElement authLink = firstPresent(
                By.partialLinkText("Sign in"),
                By.partialLinkText("Log in"),
                By.cssSelector("a[href*='/login'], a[href*='/signin']"),
                By.partialLinkText("Create"),
                By.cssSelector("a[href*='/user/']")
        );
        Assumptions.assumeTrue(authLink != null, "Auth-related link not found; skipping");

        String before = driver.getCurrentUrl();
        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(authLink));
        web.click(); 
        try {
            wait.until(d -> !d.getCurrentUrl().equals(before));
        } catch (TimeoutException ignored) {}
        waitDocumentReady();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("login") ||
                driver.getCurrentUrl().contains("signin") ||
                hostOf(driver.getCurrentUrl()).contains("jsfiddle.net"),
                "Auth navigation should occur or remain on host with different path");
        // Return to base
        driver.get(BASE_URL);
        waitDocumentReady();
    }

    @Test
    @Order(7)
    public void optionalDropdowns_ChangeSelectionIfPresent() {
        openBase();
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No <select> dropdowns on page; skipping");

        WebElement selectEl = selects.get(0);
        Select select = new Select(selectEl);
        List<WebElement> opts = select.getOptions();
        Assumptions.assumeTrue(opts.size() >= 2, "Not enough options to test selection; skipping");

        select.selectByIndex(0);
        String first = select.getFirstSelectedOption().getText();
        select.selectByIndex(1);
        String second = select.getFirstSelectedOption().getText();

        Assertions.assertNotEquals(first, second, "Changing dropdown selection should reflect new option");
    }
}
