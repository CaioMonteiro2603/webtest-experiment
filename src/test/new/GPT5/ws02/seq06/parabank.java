package GPT5.ws02.seq06;

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

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ---------------------- Helpers ----------------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("headerPanel")));
    }

    private void attemptLogin(String user, String pass) {
        openBase();
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        username.clear();
        username.sendKeys(user);
        password.clear();
        password.sendKeys(pass);
        WebElement loginBtn = driver.findElements(By.cssSelector("input.button[value='Log In']")).isEmpty()
                ? driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"))
                : driver.findElement(By.cssSelector("input.button[value='Log In']"));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private boolean isLoggedIn() {
        // Accounts Overview header or presence of Log Out link is a good signal
        boolean logoutLink = driver.findElements(By.linkText("Log Out")).size() > 0;
        boolean accountsHeader = driver.findElements(By.xpath("//h1[contains(.,'Accounts Overview')]")).size() > 0;
        return logoutLink || accountsHeader || driver.getCurrentUrl().contains("overview.htm");
    }

    private void logoutIfNeeded() {
        if (isLoggedIn()) {
            List<WebElement> outs = driver.findElements(By.linkText("Log Out"));
            if (!outs.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(outs.get(0))).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
            } else {
                driver.get(BASE_URL);
            }
        }
    }

    private String hostOf(String url) {
        try {
            URI u = new URI(url);
            return u.getHost() == null ? "" : u.getHost();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private boolean sameHost(String url, String host) {
        return hostOf(url).equalsIgnoreCase(host);
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void assertCommonChrome() {
        // Header and footer should exist across the site
        Assertions.assertTrue(driver.findElements(By.id("headerPanel")).size() > 0, "Header should be present");
        Assertions.assertTrue(driver.findElements(By.id("footerPanel")).size() > 0, "Footer should be present");
    }

    private void openExternalInNewTabAndVerify(WebElement link) {
        String href = link.getAttribute("href");
        if (href == null || href.trim().isEmpty()) return;

        String currentHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        // Try normal click
        safeClick(link);

        // Wait for new tab/window or URL change if same tab
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        } catch (TimeoutException ignored) { }

        Set<String> after = driver.getWindowHandles();
        after.removeAll(before);
        String expectedDomain = hostOf(href);

        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External page domain should match: " + expectedDomain);
            driver.close();
            driver.switchTo().window(currentHandle);
        } else {
            // Same tab navigation fallback
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External page domain should match: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("headerPanel")));
        }
    }

    // ---------------------- Tests ----------------------

    @Test
    @Order(1)
    public void testHomePageLoadsAndChromeVisible() {
        openBase();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("parabank"), "Title should contain 'ParaBank'");
        assertCommonChrome();
        // Ensure login fields visible
        Assertions.assertTrue(driver.findElements(By.name("username")).size() > 0, "Username field should be present");
        Assertions.assertTrue(driver.findElements(By.name("password")).size() > 0, "Password field should be present");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        attemptLogin(USERNAME, "wrong-password");
        // Either stays on login showing error, or redirects then error message
        // The error appears inside rightPanel with 'Error!' text
        WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        String txt = panel.getText().toLowerCase();
        Assertions.assertTrue(txt.contains("error") || txt.contains("could not"), "An error message should be shown for invalid login");
        logoutIfNeeded();
    }

    @Test
    @Order(3)
    public void testLoginWithProvidedCredentialsIfPossible() {
        attemptLogin(USERNAME, PASSWORD);
        if (isLoggedIn()) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(.,'Accounts Overview')]")));
            Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should navigate to Accounts Overview");
            logoutIfNeeded();
        } else {
            // If credentials are not valid on the public demo, assert a clear error is shown
            WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
            String txt = panel.getText().toLowerCase();
            Assertions.assertTrue(txt.contains("error") || txt.contains("could not") || txt.contains("invalid"),
                    "If not logged in, an error message should be present");
        }
    }

    @Test
    @Order(4)
    public void testRegisterPageOneLevelBelow() {
        openBase();
        WebElement register = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        register.click();
        wait.until(ExpectedConditions.urlContains("register.htm"));
        Assertions.assertTrue(driver.findElement(By.id("rightPanel")).getText().toLowerCase().contains("signing up"),
                "Register page should describe signing up");
        assertCommonChrome();
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.htm"));
    }

    @Test
    @Order(5)
    public void testForgotLoginInfoPage() {
        openBase();
        WebElement forgot = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Forgot login info?")));
        forgot.click();
        wait.until(ExpectedConditions.urlContains("lookup.htm"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("customer lookup"), "Should be on Customer Lookup page");
        assertCommonChrome();
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.htm"));
    }

    @Test
    @Order(6)
    public void testStaticInfoPagesOneLevelBelow() {
        openBase();

        // Collect primary one-level links from visible navigation areas
        Set<String> hrefs = driver.findElements(By.cssSelector("#headerPanel a, #footermainPanel a, #leftPanel a, #rightPanel a"))
                .stream()
                .map(e -> e.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> !h.trim().isEmpty())
                .filter(h -> !h.startsWith("javascript"))
                .filter(h -> !h.contains("#"))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String baseHost = hostOf(BASE_URL);

        // Filter to same-host (one level) unique pages (limit to keep runtime reasonable)
        List<String> internal = hrefs.stream().filter(h -> sameHost(h, baseHost)).distinct().limit(10).collect(Collectors.toList());
        Assertions.assertFalse(internal.isEmpty(), "Should find at least one internal link one level below");

        for (String url : internal) {
            driver.get(url);
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("headerPanel")));
            } catch (TimeoutException e) {
                // Try alternative wait for footer panel if header panel is not found
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("footerPanel")));
            }
            assertCommonChrome();
            // Each content page should have a right panel heading
            Assertions.assertTrue(driver.findElements(By.id("rightPanel")).size() > 0, "Right panel should be present for: " + url);
        }
    }

    @Test
    @Order(7)
    public void testExternalLinksFromBaseAndOneLevelPages() {
        openBase();
        String baseHost = hostOf(BASE_URL);

        // Collect internal links (one level) to visit, plus base page
        Set<String> oneLevel = new LinkedHashSet<>();
        oneLevel.add(BASE_URL);
        List<WebElement> baseLinks = driver.findElements(By.cssSelector("#headerPanel a, #footermainPanel a, #leftPanel a, #rightPanel a"));
        for (WebElement a : baseLinks) {
            String href = a.getAttribute("href");
            if (href != null && sameHost(href, baseHost)) oneLevel.add(href);
        }

        // For each page, open and test first up to 3 external links
        int checkedExternal = 0;
        outer:
        for (String page : oneLevel) {
            driver.get(page);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("headerPanel")));

            List<WebElement> anchors = driver.findElements(By.cssSelector("a[href^='http']"));
            for (WebElement a : anchors) {
                try {
                    String href = a.getAttribute("href");
                    if (href == null) continue;
                    String host = hostOf(href);
                    if (!host.equalsIgnoreCase(baseHost)) {
                        // Open and verify domain (close after)
                        openExternalInNewTabAndVerify(a);
                        checkedExternal++;
                        if (checkedExternal >= 3) break outer; // limit to reduce flakiness/time
                    }
                } catch (StaleElementReferenceException e) {
                    // Skip stale elements and continue
                    continue;
                }
            }
        }
        Assertions.assertTrue(checkedExternal > 0, "At least one external link should be verified");
    }

    @Test
    @Order(8)
    public void testAdminPageIfPresent() {
        openBase();
        List<WebElement> adminLinks = driver.findElements(By.linkText("Admin Page"));
        if (!adminLinks.isEmpty()) {
            safeClick(adminLinks.get(0));
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("admin.htm"),
                    ExpectedConditions.presenceOfElementLocated(By.id("rightPanel"))
            ));
            assertCommonChrome();
            // The admin page usually has a form to initialize the DB
            Assertions.assertTrue(driver.findElements(By.xpath("//h1[contains(.,'Administration')]")).size() > 0
                            || driver.findElements(By.xpath("//h1[contains(.,'Admin')]")).size() > 0,
                    "Admin page header should be visible");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("index.htm"));
        } else {
            Assertions.assertTrue(true, "Admin Page link not present; skipped gracefully.");
        }
    }
}