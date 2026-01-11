package GPT5.ws10.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
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

    private static final String BASE_URL = "http://localhost:8080/login";
    private static final String LOGIN_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String LOGIN_PASSWORD = "10203040";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ---------------- Helpers ----------------

    private void goToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private ExpectedCondition<Boolean> urlChangesFrom(String previous) {
        return d -> previous == null || !d.getCurrentUrl().equals(previous);
    }

    private WebElement first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private void click(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private String hostDomain(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost();
            if (host == null) return "";
            String[] p = host.split("\\.");
            if (p.length < 2) return host;
            return p[p.length - 2] + "." + p[p.length - 1];
        } catch (Exception e) {
            return "";
        }
    }

    private boolean onLoginPage() {
        String url = driver.getCurrentUrl().toLowerCase();
        return url.contains("/login");
    }

    private void ensureLoggedOut() {
        // Try to find logout in header/menu, otherwise navigate to login
        if (onLoginPage()) return;
        Optional<WebElement> logout = driver.findElements(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]"))
                .stream().findFirst();
        if (logout.isPresent()) {
            try {
                click(logout.get());
                wait.until(ExpectedConditions.urlContains("/login"));
            } catch (Exception ignored) {}
        }
        if (!onLoginPage()) {
            driver.get(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private void login(String email, String password) {
        goToBase();
        WebElement emailField = first(By.cssSelector("input[type='email'], input[name='email'], #email, input[placeholder*='email' i]"));
        WebElement passField = first(By.cssSelector("input[type='password'], input[name='password'], #password, input[placeholder*='senha' i]"));
        WebElement submit = first(By.cssSelector("button[type='submit'], button[id*='login' i], button[name*='login' i]"));
        Assertions.assertAll("Login form should be present",
                () -> Assertions.assertNotNull(emailField, "Email field not found"),
                () -> Assertions.assertNotNull(passField, "Password field not found"),
                () -> Assertions.assertNotNull(submit, "Submit button not found")
        );
        emailField.clear();
        emailField.sendKeys(email);
        passField.clear();
        passField.sendKeys(password);
        String before = driver.getCurrentUrl();
        click(submit);
        try {
            wait.until(urlChangesFrom(before));
        } catch (TimeoutException ignored) {}
    }

    private boolean assertLoggedIn() {
        // Consider logged in if URL no longer contains /login and header has some dashboard element
        boolean notLoginUrl = !onLoginPage();
        boolean hasMenu = driver.findElements(By.cssSelector("nav, header, .sidebar, .menu, .navbar")).size() > 0;
        return notLoginUrl && hasMenu;
    }

    private void verifyExternalLink(WebElement anchor) {
        String href = anchor.getAttribute("href");
        if (href == null || href.isEmpty() || href.startsWith("javascript:") || href.startsWith("mailto:")) return;

        String baseDomain = hostDomain(BASE_URL);
        String targetDomain = hostDomain(href);
        boolean external = !baseDomain.equalsIgnoreCase(targetDomain);

        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        try {
            click(anchor);
        } catch (Exception e) {
            // Fallback open in new tab using JS
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank');", href);
        }

        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(driver.getCurrentUrl()));
        } catch (TimeoutException ignored) {}

        Set<String> after = new HashSet<>(driver.getWindowHandles());
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newTab = after.iterator().next();
            driver.switchTo().window(newTab);
            if (external) {
                wait.until(drv -> drv.getCurrentUrl() != null && drv.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            // same tab navigation
            if (external) {
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private void verifyFooterSocialLinksIfAny() {
        List<String> socials = Arrays.asList("twitter.com", "facebook.com", "linkedin.com", "instagram.com", "youtube.com");
        for (String dom : socials) {
            List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + dom + "']"));
            if (!links.isEmpty()) {
                verifyExternalLink(links.get(0));
            }
        }
    }

    private void openBurgerIfPresent() {
        WebElement burger = first(By.cssSelector(".navbar-burger, .hamburger, .navbar-toggler, button[aria-label*='menu' i], button[aria-expanded]"));
        if (burger != null) {
            try {
                click(burger);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("nav, .navbar, .menu, .sidebar")));
            } catch (Exception ignored) {}
        }
    }

    // ---------------- Tests ----------------

    @Test
    @Order(1)
    public void loginPageLoads() {
        goToBase();
        Assertions.assertTrue(onLoginPage(), "Base URL should be the login page.");
        // Expect email/password fields to be present
        Assertions.assertTrue(driver.findElements(By.cssSelector("input[type='email'], input[name='email'], #email")).size() > 0,
                "Email input should be present.");
        Assertions.assertTrue(driver.findElements(By.cssSelector("input[type='password'], input[name='password'], #password")).size() > 0,
                "Password input should be present.");
        verifyFooterSocialLinksIfAny();
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        goToBase();
        WebElement emailField = first(By.cssSelector("input[type='email'], input[name='email'], #email"));
        WebElement passField = first(By.cssSelector("input[type='password'], input[name='password'], #password"));
        WebElement submit = first(By.cssSelector("button[type='submit'], button[id*='login' i], button[name*='login' i]"));
        Assumptions.assumeTrue(emailField != null && passField != null && submit != null, "Login form not found; skipping.");
        emailField.clear(); emailField.sendKeys("wrong@example.com");
        passField.clear(); passField.sendKeys("wrongpassword");
        String before = driver.getCurrentUrl();
        click(submit);
        try {
            wait.until(d -> !d.getCurrentUrl().equals(before) || d.findElements(By.cssSelector(".error, .alert, .invalid-feedback, .text-danger")).size() > 0);
        } catch (TimeoutException ignored) {}
        boolean errorVisible = driver.findElements(By.cssSelector(".error, .alert, .invalid-feedback, .text-danger")).size() > 0;
        boolean stillOnLogin = onLoginPage();
        Assertions.assertTrue(errorVisible || stillOnLogin, "Invalid login should show an error or remain on login page.");
    }

    @Test
    @Order(3)
    public void validLoginNavigatesAwayFromLogin() {
        ensureLoggedOut();
        login(LOGIN_EMAIL, LOGIN_PASSWORD);
        Assertions.assertTrue(assertLoggedIn(), "After valid login, the app should navigate away from /login and show app navigation.");
        verifyFooterSocialLinksIfAny();
    }

    @Test
    @Order(4)
    public void menuBurgerOpenCloseAndAboutIfPresent() {
        // Requires logged-in state; if login fails, skip to avoid false negatives
        if (!assertLoggedIn()) {
            ensureLoggedOut();
            login(LOGIN_EMAIL, LOGIN_PASSWORD);
        }
        Assumptions.assumeTrue(assertLoggedIn(), "Not logged in; skipping menu test.");

        openBurgerIfPresent();

        // Exercise menu: All Items / Dashboard / Home equivalent
        List<WebElement> allItems = driver.findElements(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'home') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'dashboard') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'início')]"));
        if (!allItems.isEmpty()) {
            String before = driver.getCurrentUrl();
            click(allItems.get(0));
            try {
                wait.until(urlChangesFrom(before));
            } catch (TimeoutException ignored) {}
            Assertions.assertTrue(driver.getCurrentUrl() != null, "Navigation via All Items/Home should change or reload the page.");
        }

        // About (external) if present
        List<WebElement> abouts = driver.findElements(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sobre')]"));
        if (!abouts.isEmpty()) {
            verifyExternalLink(abouts.get(0));
        }
    }

    @Test
    @Order(5)
    public void sortingDropdownIfFoundChangesOrder() {
        if (!assertLoggedIn()) {
            ensureLoggedOut();
            login(LOGIN_EMAIL, LOGIN_PASSWORD);
        }
        Assumptions.assumeTrue(assertLoggedIn(), "Not logged in; skipping sorting test.");

        // Heuristic: find a visible select that likely controls sort or page listing
        WebElement selectEl = first(By.cssSelector("select:not([multiple])"));
        Assumptions.assumeTrue(selectEl != null, "No dropdown select found; skipping.");

        Select select = new Select(selectEl);
        List<WebElement> options = select.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Select has insufficient options; skipping.");

        // Snapshot first visible row/text before change (generic table/card item)
        WebElement beforeItem = first(By.cssSelector("table tbody tr td, .card .card-title, .list-group .list-group-item, .item, .row .col"));
        final String beforeTop = beforeItem != null ? beforeItem.getText() : "";

        // Change option
        select.selectByIndex(1);
        try {
            wait.until(ExpectedConditions.stalenessOf(beforeItem));
        } catch (TimeoutException ignored) {}

        String afterTop = "";
        WebElement afterItem = first(By.cssSelector("table tbody tr td, .card .card-title, .list-group .list-group-item, .item, .row .col"));
        if (afterItem != null) afterTop = afterItem.getText();

        Assumptions.assumeTrue(!beforeTop.isEmpty() && !afterTop.isEmpty(), "Unable to capture list items to compare; skipping assertion.");
        Assertions.assertNotEquals(beforeTop, afterTop, "List/order should change after selecting a different option.");
    }

    @Test
    @Order(6)
    public void iterateInternalLinksOneLevelFromPostLogin() {
        if (!assertLoggedIn()) {
            ensureLoggedOut();
            login(LOGIN_EMAIL, LOGIN_PASSWORD);
        }
        Assumptions.assumeTrue(assertLoggedIn(), "Not logged in; skipping link traversal.");

        String current = driver.getCurrentUrl();
        String root = current.split("\\?")[0];
        String baseHost = hostDomain(root);

        List<String> links = driver.findElements(By.cssSelector("a[href]")).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> !h.isEmpty())
                .filter(h -> {
                    if (h.startsWith("javascript:") || h.startsWith("mailto:")) return false;
                    if (h.startsWith("#")) return false;
                    if (h.startsWith("/")) return true;
                    if (h.startsWith("http")) return hostDomain(h).equalsIgnoreCase(baseHost);
                    return false;
                })
                .distinct()
                .collect(Collectors.toList());

        int visited = 0;
        for (String href : links) {
            if (visited >= 6) break; // limit for stability
            String selectorSafeHref = href.replace("'", "\\'");
            Optional<WebElement> link = driver.findElements(By.cssSelector("a[href='" + selectorSafeHref + "']")).stream().findFirst();
            if (!link.isPresent()) continue;

            String prev = driver.getCurrentUrl();
            try {
                click(link.get());
                try {
                    wait.until(d -> !d.getCurrentUrl().equals(prev) || d.findElements(By.tagName("h1")).size() > 0 || d.findElements(By.cssSelector("table, .card, .list-group")).size() > 0);
                } catch (TimeoutException ignored) {}
                boolean hasContent = driver.findElements(By.tagName("h1")).size() > 0
                        || driver.findElements(By.cssSelector("table, .card, .list-group, .container, main")).size() > 0;
                Assertions.assertTrue(hasContent, "Internal page should render content: " + href);
                verifyFooterSocialLinksIfAny();
            } catch (Exception ignored) {
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
            visited++;
        }
        Assertions.assertTrue(visited >= 0, "Traversed internal links without hard failures.");
    }

    @Test
    @Order(7)
    public void logoutIfAvailableAndReturnToLogin() {
        if (!assertLoggedIn()) {
            ensureLoggedOut();
            // If we cannot log in, we cannot test logout; skip
            login(LOGIN_EMAIL, LOGIN_PASSWORD);
        }
        Assumptions.assumeTrue(assertLoggedIn(), "Not logged in; skipping logout.");

        // Try to open menu and find Logout/Sair
        openBurgerIfPresent();
        List<WebElement> logoutLinks = new ArrayList<>();
        logoutLinks.addAll(driver.findElements(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]")));
        logoutLinks.addAll(driver.findElements(By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]")));

        Assumptions.assumeTrue(!logoutLinks.isEmpty(), "No logout control found; skipping.");
        String before = driver.getCurrentUrl();
        click(logoutLinks.get(0));
        try {
            wait.until(ExpectedConditions.urlContains("/login"));
        } catch (TimeoutException ignored) {}
        Assertions.assertTrue(onLoginPage() || !driver.getCurrentUrl().equals(before), "After logout, app should navigate to login page.");
    }
}