package GPT5.ws10.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String ORIGIN = "https://beta.brasilagritest.com";
    private static final String LOGIN_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String LOGIN_PASSWORD = "10203040";

    @BeforeAll
    public static void setUpClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().window().setSize(new Dimension(1400, 1000));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- Helper utilities ----------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    private boolean isElementPresent(By by) {
        return driver.findElements(by).size() > 0;
    }

    private WebElement clickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void login() {
        goHome();
        By emailBy = By.cssSelector("input[name='email'], input#email, input[type='email']");
        By passBy = By.cssSelector("input[name='password'], input#password, input[type='password']");
        By submitBy = By.cssSelector("button[type='submit'], button.MuiButton-root, button:has(span:contains('Login'))");

        WebElement email = clickable(emailBy);
        email.clear();
        email.sendKeys(LOGIN_EMAIL);

        WebElement pass = clickable(passBy);
        pass.clear();
        pass.sendKeys(LOGIN_PASSWORD);

        WebElement submit = clickable(submitBy);
        submit.click();

        // Consider login successful once we leave /login and see something dashboard-ish
        wait.until(d -> !d.getCurrentUrl().contains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(ORIGIN), "After login we should remain on same origin");
    }

    private void logoutIfLoggedIn() {
        if (!driver.getCurrentUrl().contains("/login")) {
            // Try to find a common logout path via menu/drawer
            // Open drawer/burger if present
            List<By> menuCandidates = Arrays.asList(
                    By.cssSelector("button[aria-label='open drawer']"),
                    By.cssSelector("button[aria-label='menu']"),
                    By.cssSelector("button[aria-label='Menu']"),
                    By.cssSelector("button.MuiIconButton-root")
            );
            for (By by : menuCandidates) {
                if (isElementPresent(by)) {
                    try {
                        clickable(by).click();
                        break;
                    } catch (Exception ignored) {}
                }
            }
            // Click a logout-like entry if available
            List<By> logoutCandidates = Arrays.asList(
                    By.xpath("//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'logout')]"),
                    By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'logout')]"),
                    By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'logout')]")
            );
            for (By by : logoutCandidates) {
                if (isElementPresent(by)) {
                    try {
                        clickable(by).click();
                        break;
                    } catch (Exception ignored) {}
                }
            }
            wait.until(ExpectedConditions.urlContains("/login"));
        }
    }

    private void openDrawerIfPresent() {
        List<By> menuButtons = Arrays.asList(
                By.cssSelector("button[aria-label='open drawer']"),
                By.cssSelector("button[aria-label='menu']"),
                By.cssSelector("button[aria-label='Menu']")
        );
        for (By by : menuButtons) {
            if (isElementPresent(by)) {
                try {
                    clickable(by).click();
                    // drawer typically appears as an aside/nav; just wait for presence of some nav items
                    wait.withTimeout(Duration.ofSeconds(3));
                    return;
                } catch (Exception ignored) {}
            }
        }
    }

    private void handleExternalLink(WebElement link, String expectedDomainFragment) {
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        link.click();
        try {
            // Wait for either a new window or a navigation in the same tab
            wait.until(d -> d.getWindowHandles().size() > oldWindows.size() || !d.getCurrentUrl().startsWith(ORIGIN));
        } catch (TimeoutException ignored) {
        }

        Set<String> newWindows = driver.getWindowHandles();
        if (newWindows.size() > oldWindows.size()) {
            // switched opened window
            for (String w : newWindows) {
                if (!oldWindows.contains(w)) {
                    driver.switchTo().window(w);
                    break;
                }
            }
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains(expectedDomainFragment.toLowerCase()));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment.toLowerCase()),
                    "External link should contain expected domain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // same tab navigation
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains(expectedDomainFragment.toLowerCase()));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment.toLowerCase()),
                    "External link should contain expected domain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(d -> d.getWindowHandle().equals(originalWindow));
        }
    }

    private List<WebElement> findFooterSocialLinks() {
        List<String> domains = Arrays.asList("twitter", "facebook", "linkedin", "x.com");
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        return anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    if (href == null) return false;
                    String lower = href.toLowerCase();
                    for (String d : domains) {
                        if (lower.contains(d)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    // ---------- Tests ----------

    @Test
    @Order(1)
    public void basePageLoadsAndExternalLinksFromBase() {
        goHome();
        // Assert basic presence of login form elements
        Assertions.assertTrue(isElementPresent(By.cssSelector("input[name='email'], input[type='email']")), "Email input should be present on login page");
        Assertions.assertTrue(isElementPresent(By.cssSelector("input[name='password'], input[type='password']")), "Password input should be present on login page");
        Assertions.assertTrue(isElementPresent(By.cssSelector("button[type='submit']")), "Submit button should be present on login page");

        // One-level crawl from base page (same-origin links on the page)
        List<String> sameOriginLinks = driver.findElements(By.cssSelector("a[href]")).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> h.startsWith(ORIGIN))
                .map(h -> {
                    try {
                        URI u = URI.create(h);
                        return u.getPath();
                    } catch (Exception e) { return ""; }
                })
                .filter(p -> !p.isEmpty() && !p.equals("/") && !p.equals("/login"))
                .distinct()
                .limit(5) // keep it small to reduce flakiness
                .collect(Collectors.toList());

        String original = driver.getCurrentUrl();
        for (String path : sameOriginLinks) {
            driver.get(ORIGIN + path);
            // Basic assertion: page loads and remains same-origin
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(ORIGIN), "Navigated page should remain on origin");
        }
        driver.get(original);

        // External links on base (e.g., footer socials) - handle if present
        List<WebElement> socials = findFooterSocialLinks();
        for (WebElement a : socials) {
            String href = a.getAttribute("href").toLowerCase();
            String domain = href.contains("linkedin") ? "linkedin" : href.contains("facebook") ? "facebook" : href.contains("twitter") ? "twitter" : href.contains("x.com") ? "x.com" : "";
            if (!domain.isEmpty()) {
                handleExternalLink(a, domain);
            }
        }
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        goHome();
        WebElement email = clickable(By.cssSelector("input[name='email'], input[type='email']"));
        WebElement pass = clickable(By.cssSelector("input[name='password'], input[type='password']"));
        WebElement submit = clickable(By.cssSelector("button[type='submit']"));

        email.clear();
        email.sendKeys("wrong@example.com");
        pass.clear();
        pass.sendKeys("wrongpassword");
        submit.click();

        // Expect an error alert/message; guard for common patterns
        By errorBy = By.cssSelector(".MuiAlert-root, .Toastify__toast, [role='alert'], .MuiFormHelperText-root");
        WebElement err = visible(errorBy);
        Assertions.assertTrue(err.isDisplayed(), "An error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void validLoginNavigatesToDashboard() {
        logoutIfLoggedIn();
        login();
        // Check for URL not containing /login and presence of common dashboard elements
        Assertions.assertFalse(driver.getCurrentUrl().contains("/login"), "URL should not contain /login after successful login");
        // Try to assert a common dashboard header/identifier if present
        if (isElementPresent(By.cssSelector("h1, h2, h3, h4, h5, h6"))) {
            WebElement header = driver.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6")).get(0);
            Assertions.assertTrue(header.isDisplayed(), "A dashboard header should be visible");
        }
    }

    @Test
    @Order(4)
    public void menuOpenCloseAboutExternalAndResetIfPresent() {
        login();
        // Open drawer/burger
        openDrawerIfPresent();

        // Click dashboard/All Items equivalent if present
        List<By> dashboardCandidates = Arrays.asList(
                By.xpath("//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'dashboard')]"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'dashboard')]"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'dashboard')]")
        );
        for (By by : dashboardCandidates) {
            if (isElementPresent(by)) {
                clickable(by).click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(ORIGIN), "After menu navigation, still on same origin");

        // About (external) if present
        openDrawerIfPresent();
        List<By> aboutCandidates = Arrays.asList(
                By.xpath("//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'sobre')]"),
                By.xpath("//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'about')]"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'sobre') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'about')]"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'sobre') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'about')]")
        );
        boolean aboutHandled = false;
        for (By by : aboutCandidates) {
            if (isElementPresent(by)) {
                WebElement about = clickable(by);
                handleExternalLink(about, "brasilagritest");
                aboutHandled = true;
                break;
            }
        }
        if (!aboutHandled) {
            // As a fallback, try any anchor with target external from drawer section
            List<WebElement> anchors = driver.findElements(By.cssSelector("a[target='_blank']"));
            for (WebElement a : anchors) {
                String href = a.getAttribute("href");
                if (href != null && !href.startsWith(ORIGIN)) {
                    String expected = href.replace("https://", "").replace("http://", "");
                    expected = expected.contains("/") ? expected.substring(0, expected.indexOf('/')) : expected;
                    handleExternalLink(a, expected);
                    break;
                }
            }
        }

        // Reset App State if present
        openDrawerIfPresent();
        List<By> resetCandidates = Arrays.asList(
                By.xpath("//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'reset')]"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'reset')]"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'reset')]")
        );
        for (By by : resetCandidates) {
            if (isElementPresent(by)) {
                clickable(by).click();
                // No error should occur; remain on origin
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(ORIGIN), "After reset, URL remains on origin");
                break;
            }
        }
    }

    @Test
    @Order(5)
    public void sortingDropdownIfPresentChangesOrder() {
        login();
        // Look for a generic select that likely controls sorting
        List<WebElement> selects = driver.findElements(By.cssSelector("select, .MuiSelect-select"));
        boolean exercised = false;
        for (WebElement sel : selects) {
            try {
                if (!sel.isDisplayed()) continue;
                // Try using HTML select first
                if (sel.getTagName().equalsIgnoreCase("select")) {
                    Select s = new Select(sel);
                    List<WebElement> options = s.getOptions();
                    if (options.size() >= 2) {
                        // Capture some list items before and after
                        List<String> before = driver.findElements(By.cssSelector("li, .MuiListItem-root, .item, .card"))
                                .stream().limit(5).map(WebElement::getText).collect(Collectors.toList());
                        s.selectByIndex(options.size() - 1);
                        wait.withTimeout(Duration.ofSeconds(2));
                        List<String> after = driver.findElements(By.cssSelector("li, .MuiListItem-root, .item, .card"))
                                .stream().limit(5).map(WebElement::getText).collect(Collectors.toList());
                        // If we managed to read content, assert some change (best-effort)
                        if (!before.isEmpty() && !after.isEmpty()) {
                            exercised = true;
                            Assertions.assertNotEquals(before, after, "Selecting a different sort option should change the visible order/content");
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        // If no dropdown present, just assert we are still on a valid same-origin page
        if (!exercised) {
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(ORIGIN), "No sorting control found; still on app");
        }
    }

    @Test
    @Order(6)
    public void logoutViaMenuIfAvailable() {
        login();
        openDrawerIfPresent();
        List<By> logoutCandidates = Arrays.asList(
                By.xpath("//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'logout')]"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'logout')]"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'logout')]"),
                By.xpath("//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'sair')]"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'sair')]")
        );
        boolean clicked = false;
        for (By by : logoutCandidates) {
            if (isElementPresent(by)) {
                clickable(by).click();
                clicked = true;
                break;
            }
        }
        if (clicked) {
            wait.until(ExpectedConditions.urlContains("/login"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "After logout we should be back on /login");
        } else {
            // If no logout entry, try visiting a profile menu first if present
            List<WebElement> candidates = driver.findElements(By.cssSelector("button[aria-label*='account'], button:has(svg), .MuiAvatar-root"));
            if (!candidates.isEmpty()) {
                try {
                    candidates.get(0).click();
                    for (By by : logoutCandidates) {
                        if (isElementPresent(by)) {
                            clickable(by).click();
                            wait.until(ExpectedConditions.urlContains("/login"));
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
            // Ensure we end at login one way or another
            if (!driver.getCurrentUrl().contains("/login")) {
                logoutIfLoggedIn();
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "We should end at /login");
        }
    }
}
