package GPT5.ws10.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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

    @AfterEach
    public void cleanup() {
        // Try to return to a known state after each test
        try {
            // If a logout button/link is present, click it
            if (clickIfPresent(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]"))
                || clickIfPresent(By.cssSelector("a[href*='logout']"))) {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("/login"),
                        ExpectedConditions.presenceOfElementLocated(anyLoginLocator())
                ));
            }
        } catch (Exception ignored) {
        } finally {
            driver.manage().deleteAllCookies();
        }
    }

    /* ============================ Helpers ============================ */

    private static By anyLoginLocator() {
        return By.cssSelector("input[type='email'], input[name*='email'], input[id*='email']");
    }

    private void openLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Wait for any email/password field to ensure login page is loaded
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(anyLoginLocator()),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='password']"))
        ));
    }

    private static boolean clickIfPresent(By by) {
        List<WebElement> els = driver.findElements(by);
        if (!els.isEmpty()) {
            WebElement el = els.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(el)).click();
            return true;
        }
        return false;
    }

    private void type(By by, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
        el.clear();
        el.sendKeys(text);
    }

    private void doLogin(String email, String password) {
        openLogin();
        type(anyLoginLocator(), email);
        type(By.cssSelector("input[type='password'], input[name*='password'], input[id*='password']"), password);

        boolean clicked = clickIfPresent(By.cssSelector("button[type='submit']"))
                || clickIfPresent(By.xpath("//button[contains(.,'Entrar') or contains(.,'Login') or contains(.,'Sign in')]"))
                || clickIfPresent(By.cssSelector("input[type='submit']"));
        Assertions.assertTrue(clicked, "Login submit control should be clickable.");

        // Wait for either dashboard/home or an indication that we're no longer on /login
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        wait.until(d -> !driver.getCurrentUrl().endsWith("/login") && !driver.getCurrentUrl().contains("/login")
                || driver.findElements(By.xpath("//*[contains(.,'Sair') or contains(.,'Logout')]")).size() > 0
                || driver.findElements(By.cssSelector("nav, header")).size() > 0);
    }

    private boolean isLoggedIn() {
        if (!driver.getCurrentUrl().contains("/login") && !driver.getCurrentUrl().endsWith("/login")) {
            // Heuristic: presence of a nav + absence of the login form
            boolean hasNav = driver.findElements(By.cssSelector("nav, header")).size() > 0;
            boolean hasLogout = driver.findElements(By.xpath("//*[contains(.,'Sair') or contains(.,'Logout')]")).size() > 0;
            boolean hasLoginFields = !driver.findElements(anyLoginLocator()).isEmpty()
                    && !driver.findElements(By.cssSelector("input[type='password']")).isEmpty();
            return (hasNav || hasLogout) && !hasLoginFields;
        }
        return false;
    }

    private void ensureMenuOpenIfExists() {
        // Try different burger/menu candidates
        By[] candidates = new By[] {
                By.cssSelector("button[aria-label*='menu' i], button[aria-label*='menu'], button#menu, button.hamburger"),
                By.cssSelector(".hamburger, .menu-button, .drawer-toggle, [data-testid*='menu']"),
                By.xpath("//button[contains(.,'Menu')]")
        };
        for (By by : candidates) {
            if (clickIfPresent(by)) {
                // after click, wait a bit for a sidebar/nav to appear
                wait.until(d -> driver.findElement(By.tagName("body")) != null);
                break;
            }
        }
    }

    private void switchToNewTabAndVerify(String expectedDomain) {
        String original = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String w : driver.getWindowHandles()) {
            if (!w.equals(original)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected external domain in URL: " + expectedDomain + " but was " + driver.getCurrentUrl());
                driver.close();
                driver.switchTo().window(original);
                return;
            }
        }
        Assertions.fail("No new tab opened for external link.");
    }

    private void clickExternalAndAssert(String expectedDomain, By... candidates) {
        int before = driver.getWindowHandles().size();
        boolean clicked = false;
        for (By by : candidates) {
            if (clickIfPresent(by)) { clicked = true; break; }
        }
        Assertions.assertTrue(clicked, "Expected external link not found/clickable.");

        if (driver.getWindowHandles().size() > before) {
            switchToNewTabAndVerify(expectedDomain);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Expected URL to contain external domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private List<String> collectInternalLinksOneLevel(int maxCount) {
        Set<String> urls = new LinkedHashSet<>();
        String origin;
        try {
            origin = new java.net.URL(driver.getCurrentUrl()).getProtocol() + "://" + new java.net.URL(driver.getCurrentUrl()).getHost();
        } catch (Exception e) {
            origin = "";
        }
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.trim().isEmpty()) continue;
            if (href.startsWith("mailto:") || href.startsWith("tel:") || href.startsWith("javascript:")) continue;
            // Internal if same origin or relative
            boolean internal = href.startsWith(origin) || (!href.startsWith("http://") && !href.startsWith("https://"));
            if (internal) {
                // Avoid deep routes - allow one level below root or current section
                // Keep it simple: allow up to one extra path segment beyond origin
                try {
                    String path = href.startsWith("http") ? new java.net.URL(href).getPath() : href;
                    if (path == null) path = "/";
                    // basic filter: limit path depth
                    int depth = (int) Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).count();
                    if (depth <= 2) urls.add(href);
                } catch (Exception e) {
                    urls.add(href);
                }
            }
            if (urls.size() >= maxCount) break;
        }
        return new ArrayList<>(urls);
    }

    private void visitAndSmokeAssert(String url) {
        driver.navigate().to(url);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Generic smoke assertions
        Assertions.assertTrue(driver.getTitle() != null, "Visited page should have a title (can be empty).");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith("http"), "Visited page should have a valid URL.");
    }

    /* ============================ Tests ============================ */

    @Test
    @Order(1)
    public void loginPageLoadsAndFormVisible() {
        openLogin();
        Assertions.assertTrue(driver.getTitle() != null, "Login page title should not be null.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "URL should contain /login.");

        Assertions.assertTrue(driver.findElements(anyLoginLocator()).size() > 0,
                "Email input should be present.");
        Assertions.assertTrue(driver.findElements(By.cssSelector("input[type='password']")).size() > 0,
                "Password input should be present.");

        // Check submit button present
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("button[type='submit']")).size() > 0
                || driver.findElements(By.xpath("//button[contains(.,'Entrar') or contains(.,'Login') or contains(.,'Sign in')]")).size() > 0
                || driver.findElements(By.cssSelector("input[type='submit']")).size() > 0,
                "A submit control should be present on the login form.");
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        openLogin();
        type(anyLoginLocator(), "invalid@example.com");
        type(By.cssSelector("input[type='password'], input[name*='password'], input[id*='password']"), "wrong-pass");
        boolean clicked = clickIfPresent(By.cssSelector("button[type='submit']"))
                || clickIfPresent(By.xpath("//button[contains(.,'Entrar') or contains(.,'Login') or contains(.,'Sign in')]"))
                || clickIfPresent(By.cssSelector("input[type='submit']"));
        Assertions.assertTrue(clicked, "Login submit should be clickable.");

        // Expect an error of some sort
        WebElement errorContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(@class,'error') or contains(@class,'alert') or contains(.,'inválid') or contains(.,'invalid') or contains(.,'erro') or contains(.,'error')]")
        ));
        Assertions.assertTrue(errorContainer.isDisplayed(), "An error message should be displayed for invalid credentials.");
        // Stay (or return) on login
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Should remain on /login after invalid credentials.");
    }

    @Test
    @Order(3)
    public void validLoginSucceeds() {
        doLogin(LOGIN, PASSWORD);
        Assertions.assertTrue(isLoggedIn(), "User should be considered logged in (heuristic).");
        Assertions.assertFalse(driver.getCurrentUrl().contains("/login"),
                "After successful login, URL should not be /login.");
    }

    @Test
    @Order(4)
    public void crawlInternalLinksOneLevelAndExternalFromThosePages() {
        // Ensure logged in to access more routes (if applicable)
        if (!isLoggedIn()) {
            doLogin(LOGIN, PASSWORD);
            Assertions.assertTrue(isLoggedIn(), "Login must succeed before crawling.");
        }

        // Collect internal links one level deep from current page
        List<String> internalLinks = collectInternalLinksOneLevel(10);
        Assertions.assertTrue(internalLinks.size() > 0, "Should collect at least one internal link from the page.");

        // Visit each (one level)
        for (String link : internalLinks) {
            visitAndSmokeAssert(link.startsWith("http") ? link : link.startsWith("/")
                    ? (driver.getCurrentUrl().replaceAll("(https?://[^/]+).*", "$1") + link)
                    : (driver.getCurrentUrl().replaceAll("/+$", "") + "/" + link.replaceAll("^/+", "")));

            // On each visited page, test up to 2 external links if present
            List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
            int visited = 0;
            for (WebElement a : anchors) {
                String href = a.getAttribute("href");
                if (href == null) continue;
                String h = href.toLowerCase();
                boolean external = h.startsWith("http") && !h.contains("brasilagritest.com");
                if (external) {
                    int before = driver.getWindowHandles().size();
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", a);
                    wait.until(ExpectedConditions.elementToBeClickable(a)).click();
                    String expectedDomain;
                    try {
                        expectedDomain = new java.net.URL(href).getHost();
                    } catch (Exception e) {
                        expectedDomain = "http";
                    }
                    if (driver.getWindowHandles().size() > before) {
                        switchToNewTabAndVerify(expectedDomain);
                    } else {
                        wait.until(ExpectedConditions.urlContains(expectedDomain));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                                "Expected navigation to external domain: " + expectedDomain);
                        driver.navigate().back();
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                    }
                    visited++;
                    if (visited >= 2) break;
                }
            }
        }
    }

    @Test
    @Order(5)
    public void menuInteractionsIfPresentAndLogout() {
        // Ensure logged in
        if (!isLoggedIn()) {
            doLogin(LOGIN, PASSWORD);
            Assertions.assertTrue(isLoggedIn(), "Login must succeed for menu test.");
        }

        // Try opening a burger/menu if any
        ensureMenuOpenIfExists();

        // Try the common actions if they exist in this app's menu
        // 1) "All Items" / "Início" / "Dashboard"
        boolean navigatedAllItems = clickIfPresent(By.xpath("//a[contains(.,'All Items') or contains(.,'Início') or contains(.,'Dashboard')]"))
                || clickIfPresent(By.cssSelector("a[href*='dashboard'], a[href='/'], a.nav-home"));
        if (navigatedAllItems) {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Assertions.assertFalse(driver.getCurrentUrl().contains("/login"),
                    "After All Items/Home, should not be on /login.");
        }

        // 2) "About" (external)
        boolean aboutClicked = clickIfPresent(By.xpath("//a[contains(.,'About')]"))
                || clickIfPresent(By.cssSelector("a[href*='about']"));
        if (aboutClicked) {
            // If it opened new tab, verify and close; otherwise ensure URL changed to some domain
            String original = driver.getWindowHandle();
            if (driver.getWindowHandles().size() > 1) {
                for (String w : driver.getWindowHandles()) {
                    if (!w.equals(original)) {
                        driver.switchTo().window(w);
                        wait.until(ExpectedConditions.urlContains("http"));
                        Assertions.assertTrue(driver.getCurrentUrl().startsWith("http"),
                                "About link should navigate to an HTTP(S) URL.");
                        driver.close();
                        driver.switchTo().window(original);
                        break;
                    }
                }
            } else {
                wait.until(ExpectedConditions.urlContains("http"));
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
        }

        // 3) "Reset App State" (optional/app-specific). If present, click it and verify no error
        boolean resetClicked = clickIfPresent(By.xpath("//a[contains(.,'Reset App State') or contains(.,'Reset')]"))
                || clickIfPresent(By.cssSelector("a[href*='reset']"));
        if (resetClicked) {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            // A lightweight assertion: still logged in or redirected appropriately
            Assertions.assertTrue(isLoggedIn() || driver.getCurrentUrl().contains("/login"),
                    "After reset, should be either logged in or back to login.");
            if (!isLoggedIn() && !driver.getCurrentUrl().contains("/login")) {
                Assertions.fail("Reset action produced an unexpected state.");
            }
        }

        // 4) Logout (required)
        boolean loggedOut = clickIfPresent(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]"))
                || clickIfPresent(By.cssSelector("a[href*='logout']"));
        Assertions.assertTrue(loggedOut, "Logout control should be accessible.");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.presenceOfElementLocated(anyLoginLocator())
        ));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login") || !driver.findElements(anyLoginLocator()).isEmpty(),
                "After logout, should be on login page.");
    }

    @Test
    @Order(6)
    public void optionalSortingIfPresentOnAnyList() {
        // If the application has a sorting dropdown on a list page, exercise it.
        // We'll log in to increase the chance of reaching listing pages.
        if (!isLoggedIn()) {
            doLogin(LOGIN, PASSWORD);
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // Collect items (generic cards/rows) before sort
        List<WebElement> itemsBefore = collectListItems();
        List<String> snapshotBefore = itemsBefore.stream()
                .map(WebElement::getText)
                .map(t -> t == null ? "" : t.trim())
                .filter(t -> !t.isEmpty())
                .limit(10)
                .collect(Collectors.toList());

        // Find a select element that looks like sort control
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        boolean changed = false;
        if (!selects.isEmpty()) {
            Select sort = new Select(selects.get(0));
            if (sort.getOptions().size() > 1) {
                String initial = sort.getFirstSelectedOption().getText();
                int altIndex = sort.getOptions().size() > 2 ? 2 : 1;
                sort.selectByIndex(altIndex);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                List<WebElement> itemsAfter = collectListItems();
                List<String> snapshotAfter = itemsAfter.stream()
                        .map(WebElement::getText)
                        .map(t -> t == null ? "" : t.trim())
                        .filter(t -> !t.isEmpty())
                        .limit(10)
                        .collect(Collectors.toList());
                changed = !snapshotBefore.equals(snapshotAfter) || !sort.getFirstSelectedOption().getText().equals(initial);
            }
        }

        if (!selects.isEmpty()) {
            Assertions.assertTrue(changed, "List/order should change after selecting a different sort option.");
        } else {
            // If no sorting, assert that we at least see a list/grid content
            Assertions.assertTrue(!snapshotBefore.isEmpty() || driver.findElements(By.cssSelector("table, ul, .card, .list, .grid")).size() > 0,
                    "No sorting control found; expected some list/grid content to be present.");
        }
    }

    @Test
    @Order(7)
    public void footerSocialLinksIfPresent() {
        // Whether logged in or not, attempt on current page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Scroll to bottom to reveal footer
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

        // Check common social links and open them
        Map<String, By[]> socials = new LinkedHashMap<>();
        socials.put("twitter.com", new By[]{ By.cssSelector("a[href*='twitter.com']") });
        socials.put("facebook.com", new By[]{ By.cssSelector("a[href*='facebook.com']") });
        socials.put("linkedin.com", new By[]{ By.cssSelector("a[href*='linkedin.com']") });

        int found = 0;
        for (Map.Entry<String, By[]> e : socials.entrySet()) {
            String domain = e.getKey();
            By[] candidates = e.getValue();
            if (driver.findElements(candidates[0]).size() > 0) {
                clickExternalAndAssert(domain, candidates);
                found++;
            }
        }
        // It's acceptable if no social links exist; assert that page has some anchors at least
        if (found == 0) {
            Assertions.assertTrue(driver.findElements(By.cssSelector("a[href]")).size() > 0,
                    "No social links found; page should still contain anchors.");
        }
    }

    /* ============================ Utilities ============================ */

    private List<WebElement> collectListItems() {
        // Try common list/card/table patterns
        List<By> candidates = Arrays.asList(
                By.cssSelector(".card, .item, .list-item, .table tbody tr"),
                By.cssSelector("ul li"),
                By.xpath("//table//tr[td or th]")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els;
        }
        return Collections.emptyList();
    }
}
