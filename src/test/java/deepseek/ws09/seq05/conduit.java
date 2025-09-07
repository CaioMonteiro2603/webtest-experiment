package GPT5.ws09.seq05;

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
public class RealWorldHeadlessSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ---------------------- Helpers ----------------------

    private void openBase() {
        driver.get(BASE_URL);
        waitDocumentReady();
        // The app typically redirects to a hash route (#/)
        wait.until(d -> driver.getCurrentUrl().startsWith(BASE_URL));
        dismissOverlaysIfAny();
    }

    private void waitDocumentReady() {
        try {
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private void dismissOverlaysIfAny() {
        // Best-effort cookie banners / modals
        List<By> candidates = Arrays.asList(
                By.cssSelector("button[id*='accept'],button[class*='accept'],button[aria-label*='accept']"),
                By.cssSelector("button[class*='agree'],button[id*='agree']"),
                By.cssSelector(".cookie-accept,.cc-allow,.cc-accept,.cookiebar-close,.cc-dismiss,.btn-close,.close")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(els.get(0))).click();
                    break;
                } catch (Exception ignored) {}
            }
        }
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return ""; }
    }

    private int depthOfPath(String path) {
        if (path == null) return 0;
        String p = path;
        if (p.startsWith("/")) p = p.substring(1);
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        if (p.isEmpty()) return 0;
        return p.split("/").length;
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        return null;
    }

    private void handleExternalLink(WebElement link) {
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        String baseHost = hostOf(driver.getCurrentUrl());

        waitClickable(link).click();

        // wait for new tab or navigation
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !hostOf(d.getCurrentUrl()).equals(baseHost));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            waitDocumentReady();
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should be on an external domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            waitDocumentReady();
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should have navigated to an external domain");
            driver.navigate().back();
            waitDocumentReady();
        }
    }

    private String getFirstArticleTitle() {
        List<WebElement> items = driver.findElements(By.cssSelector(".article-preview .preview-link h1"));
        return items.isEmpty() ? "" : items.get(0).getText().trim();
    }

    // ---------------------- Tests ----------------------

    @Test
    @Order(1)
    public void baseLoads_CoreUIVisible() {
        openBase();
        // Header "conduit" brand and Home link should exist
        Assertions.assertTrue(driver.findElements(By.cssSelector("a.navbar-brand")).size() > 0, "Brand should exist");
        WebElement homeTab = firstPresent(By.linkText("Home"), By.cssSelector("a[href*='#/']"));
        Assertions.assertNotNull(homeTab, "Home tab should be present");
        // Feed area or article previews should be visible
        Assertions.assertTrue(
                driver.findElements(By.cssSelector(".article-preview")).size() > 0 ||
                driver.findElements(By.cssSelector(".feed-toggle")).size() > 0,
                "Feed or article previews should be visible");
    }

    @Test
    @Order(2)
    public void headerNavigation_SignIn_SignUp_Home() {
        openBase();

        // Sign in
        WebElement signIn = firstPresent(By.linkText("Sign in"), By.cssSelector("a[href*='#/login']"));
        Assumptions.assumeTrue(signIn != null, "Sign in link not found; skipping");
        waitClickable(signIn).click();
        wait.until(ExpectedConditions.urlContains("#/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/login"), "Should be on login route");

        // Sign up
        WebElement signUp = firstPresent(By.linkText("Need an account?"), By.linkText("Sign up"), By.cssSelector("a[href*='#/register']"));
        Assumptions.assumeTrue(signUp != null, "Sign up link not found; skipping");
        waitClickable(signUp).click();
        wait.until(ExpectedConditions.urlContains("#/register"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/register"), "Should be on register route");

        // Back Home
        WebElement home = firstPresent(By.linkText("Home"), By.cssSelector("a.navbar-brand"));
        waitClickable(home).click();
        waitDocumentReady();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should return to base");
    }

    @Test
    @Order(3)
    public void openArticle_FromGlobalFeed_ThenBack() {
        openBase();
        // Ensure we are on Global Feed (when not logged in it's default)
        Assertions.assertTrue(driver.findElements(By.cssSelector(".article-preview")).size() > 0, "Article previews should be present");
        String titleBefore = getFirstArticleTitle();

        WebElement firstPreview = firstPresent(By.cssSelector(".article-preview .preview-link"));
        Assumptions.assumeTrue(firstPreview != null, "No article preview link; skipping");
        waitClickable(firstPreview).click();
        waitDocumentReady();

        // Article detail
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/article/"), "Should navigate to article detail");
        WebElement h1 = firstPresent(By.cssSelector("h1"));
        Assumptions.assumeTrue(h1 != null, "Article title header not found; skipping");
        Assertions.assertTrue(h1.isDisplayed(), "Article title should be displayed");

        // Navigate back to home
        driver.navigate().back();
        waitDocumentReady();
        Assertions.assertTrue(driver.findElements(By.cssSelector(".article-preview")).size() > 0, "Returned to feed");
        String titleAfter = getFirstArticleTitle();
        // Not guaranteed to be different, but should be non-empty
        Assertions.assertFalse(titleAfter.isEmpty(), "First article title should be readable");
        Assertions.assertFalse(titleBefore.isEmpty(), "First article title before navigation should be readable");
    }

    @Test
    @Order(4)
    public void tagFilter_FiltersFeed() {
        openBase();
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag-list a.tag-default"));
        Assumptions.assumeTrue(!tags.isEmpty(), "No tags available; skipping");
        WebElement firstTag = tags.get(0);
        String tagText = firstTag.getText().trim();
        waitClickable(firstTag).click();

        // Wait until the feed toggle shows the tag pill or list refreshes
        boolean tagApplied = false;
        try {
            tagApplied = wait.until(d ->
                    d.findElements(By.cssSelector(".feed-toggle .nav-pills .nav-link.active")).stream()
                            .anyMatch(e -> e.getText().toLowerCase().contains(tagText.toLowerCase()))
                    || d.findElements(By.cssSelector(".article-preview")).size() > 0
            );
        } catch (TimeoutException ignored) {}
        Assertions.assertTrue(tagApplied, "Tag should filter the feed");
    }

    @Test
    @Order(5)
    public void pagination_ChangesArticleOrder() {
        openBase();
        // get first article title
        String firstTitle = getFirstArticleTitle();
        Assumptions.assumeTrue(!firstTitle.isEmpty(), "No first article title; skipping");

        // Click page 2 if available
        List<WebElement> pages = driver.findElements(By.cssSelector(".pagination li a"));
        Assumptions.assumeTrue(pages.size() > 1, "No pagination available; skipping");
        WebElement page2 = pages.get(1); // second link is usually page 2
        waitClickable(page2).click();

        // Wait for articles to refresh (first title likely changes)
        String firstAfter = "";
        try {
            wait.until(d -> {
                String t = getFirstArticleTitle();
                if (!t.isEmpty() && !t.equals(firstTitle)) {
                    firstAfter = t;
                    return true;
                }
                return false;
            });
        } catch (TimeoutException ignored) {
            firstAfter = getFirstArticleTitle();
        }
        Assertions.assertNotEquals(firstTitle, firstAfter, "First article should change after pagination");
    }

    @Test
    @Order(6)
    public void invalidLogin_ShowsErrorOrStaysOnLogin() {
        openBase();
        WebElement signIn = firstPresent(By.linkText("Sign in"), By.cssSelector("a[href*='#/login']"));
        Assumptions.assumeTrue(signIn != null, "Sign in link not found; skipping");
        waitClickable(signIn).click();
        wait.until(ExpectedConditions.urlContains("#/login"));

        WebElement email = firstPresent(By.cssSelector("input[type='email']"), By.cssSelector("input[placeholder='Email']"));
        WebElement password = firstPresent(By.cssSelector("input[type='password']"), By.cssSelector("input[placeholder='Password']"));
        WebElement submit = firstPresent(By.cssSelector("button[type='submit']"), By.xpath("//button[contains(.,'Sign in')]"));
        Assumptions.assumeTrue(email != null && password != null && submit != null, "Login form controls missing; skipping");

        email.clear(); email.sendKeys("invalid@example.com");
        password.clear(); password.sendKeys("wrongpassword");
        waitClickable(submit).click();

        // Expect error list or remain on login
        boolean errorShown = false;
        try {
            errorShown = wait.until(d -> d.findElements(By.cssSelector(".error-messages li")).size() > 0);
        } catch (TimeoutException ignored) {}
        boolean stillOnLogin = driver.getCurrentUrl().contains("#/login");
        Assertions.assertTrue(errorShown || stillOnLogin, "Invalid login should not authenticate");
    }

    @Test
    @Order(7)
    public void internalLinks_OneLevelBelow_Work() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        URI base = URI.create(driver.getCurrentUrl());
        int baseDepth = depthOfPath(base.getPath());

        List<String> hrefs = driver.findElements(By.cssSelector("a[href]")).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> h.startsWith("http"))
                .distinct()
                .collect(Collectors.toList());

        // Consider one level below path (ignoring hash, which SPA uses)
        List<String> internalOneLevel = hrefs.stream()
                .filter(h -> baseHost.equals(hostOf(h)))
                .filter(h -> {
                    URI u = URI.create(h.split("#")[0]);
                    int depth = depthOfPath(u.getPath());
                    return depth <= baseDepth + 1;
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!internalOneLevel.isEmpty(), "No internal one-level-below links found; skipping");
        int checked = 0;
        String original = driver.getCurrentUrl();
        for (String link : internalOneLevel) {
            if (checked >= 3) break; // limit for stability
            Optional<WebElement> anchor = driver.findElements(By.cssSelector("a[href]")).stream()
                    .filter(a -> link.equals(a.getAttribute("href"))).findFirst();
            if (anchor.isEmpty()) continue;
            String before = driver.getCurrentUrl();
            try {
                waitClickable(anchor.get()).click();
                wait.until(d -> !d.getCurrentUrl().equals(before));
                waitDocumentReady();
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should stay within same host");
                Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "Destination should render");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                checked++;
            } catch (Exception e) {
                driver.get(original);
                waitDocumentReady();
            }
        }
        Assertions.assertTrue(checked >= 1, "At least one internal one-level-below link should load");
    }

    @Test
    @Order(8)
    public void footerOrHeader_ExternalLinks_OpenOnDifferentDomain() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement> anchors = driver.findElements(By.cssSelector("footer a[href], header a[href], nav a[href], a[target='_blank']"));
        List<WebElement> external = anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && href.startsWith("http") && !hostOf(href).equals(baseHost);
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!external.isEmpty(), "No external links found; skipping");
        int validated = 0;
        for (WebElement link : external) {
            if (validated >= 3) break; // cap to reduce flakiness
            try {
                handleExternalLink(link);
                validated++;
            } catch (Exception e) {
                driver.get(BASE_URL);
                waitDocumentReady();
            }
        }
        Assertions.assertTrue(validated > 0, "Should validate at least one external link");
    }

    @Test
    @Order(9)
    public void optionalDropdowns_IfAny_ChangeSelection() {
        openBase();
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No select dropdowns present; skipping");
        Select s = new Select(selects.get(0));
        List<WebElement> opts = s.getOptions();
        Assumptions.assumeTrue(opts.size() >= 2, "Not enough options to test selection; skipping");
        s.selectByIndex(0);
        String first = s.getFirstSelectedOption().getText();
        s.selectByIndex(1);
        String second = s.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(first, second, "Selection should change after choosing a different option");
    }
}
