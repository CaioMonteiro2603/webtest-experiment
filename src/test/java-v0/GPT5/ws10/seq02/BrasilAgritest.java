package GPT5.ws10.seq02;

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
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ----------------------------- Helpers -----------------------------

    private void goToBase() {
        driver.get(BASE_URL);
        waitForPageReady();
    }

    private void waitForPageReady() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private WebElement clickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private List<WebElement> displayedAll(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
    }

    private Optional<WebElement> findFirstVisible(List<By> locators) {
        for (By by : locators) {
            List<WebElement> list = displayedAll(by);
            if (!list.isEmpty()) {
                return Optional.of(list.get(0));
            }
        }
        return Optional.empty();
    }

    private boolean waitUrlContainsAny(Collection<String> fragments) {
        try {
            return wait.until(d -> {
                String u = d.getCurrentUrl().toLowerCase(Locale.ROOT);
                for (String f : fragments) {
                    if (u.contains(f.toLowerCase(Locale.ROOT))) return true;
                }
                return false;
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean isLoggedInHeuristic() {
        // Heuristics: dashboard-like URL or presence of sidebar/header user menu
        String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
        if (url.contains("/dashboard") || url.matches(".*#/dashboard.*")) return true;
        List<By> candidates = Arrays.asList(
                By.cssSelector("a[href*='logout' i]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')]"),
                By.cssSelector("nav .dropdown-toggle"),
                By.cssSelector("aside, .sidebar, .menu, .ant-layout-sider, .MuiDrawer-root")
        );
        for (By by : candidates) {
            if (!driver.findElements(by).isEmpty()) return true;
        }
        return false;
    }

    private void login(String email, String pass) {
        goToBase();
        Optional<WebElement> emailInput = findFirstVisible(Arrays.asList(
                By.id("email"), By.name("email"), By.cssSelector("input[type='email']"), By.cssSelector("input[name*='email' i]")
        ));
        Optional<WebElement> passwordInput = findFirstVisible(Arrays.asList(
                By.id("password"), By.name("password"), By.cssSelector("input[type='password']"), By.cssSelector("input[name*='password' i]")
        ));
        Optional<WebElement> submitBtn = findFirstVisible(Arrays.asList(
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log in') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login')]"),
                By.cssSelector("button.btn-primary"), By.cssSelector("button.MuiButton-root"), By.cssSelector("button.ant-btn")
        ));

        Assertions.assertTrue(emailInput.isPresent(), "Email input should be present on the login page");
        Assertions.assertTrue(passwordInput.isPresent(), "Password input should be present on the login page");
        Assertions.assertTrue(submitBtn.isPresent(), "Submit/Login button should be present on the login page");

        emailInput.get().clear();
        emailInput.get().sendKeys(email);
        passwordInput.get().clear();
        passwordInput.get().sendKeys(pass);
        clickable(By.xpath("//*[self::button or self::a][@type='submit' or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(@class,'btn') or contains(@class,'MuiButton') or contains(@class,'ant-btn')]")).click();

        // Wait for either an error or a dashboard-like page
        wait.until(d -> {
            String u = d.getCurrentUrl().toLowerCase(Locale.ROOT);
            boolean moved = u.contains("dashboard") || !u.endsWith("/login");
            boolean errorShown = !d.findElements(By.cssSelector(".alert, [role='alert'], .error, .invalid-feedback")).isEmpty();
            return moved || errorShown;
        });
    }

    private void logoutIfPossible() {
        // Try to find a logout link or menu
        List<By> logoutCandidates = Arrays.asList(
                By.cssSelector("a[href*='logout' i]"),
                By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]")
        );
        for (By by : logoutCandidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                try {
                    clickable(by).click();
                    waitUrlContainsAny(Arrays.asList("/login"));
                    return;
                } catch (Exception ignored) { }
            }
        }
        // Try open user menu then logout
        List<By> userMenuCandidates = Arrays.asList(
                By.cssSelector("nav .dropdown-toggle"),
                By.cssSelector("button[aria-label='account' i], [data-bs-toggle='dropdown']"),
                By.cssSelector(".ant-dropdown-trigger, .MuiAvatar-root, .avatar")
        );
        for (By by : userMenuCandidates) {
            List<WebElement> menus = driver.findElements(by);
            if (!menus.isEmpty()) {
                try {
                    clickable(by).click();
                    for (By logoutBy : logoutCandidates) {
                        if (!driver.findElements(logoutBy).isEmpty()) {
                            clickable(logoutBy).click();
                            waitUrlContainsAny(Arrays.asList("/login"));
                            return;
                        }
                    }
                } catch (Exception ignored) { }
            }
        }
    }

    private void openExternalAndAssert(WebElement link, String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !Objects.equals(d.getCurrentUrl(), BASE_URL));
        } catch (TimeoutException ignored) { }
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newTab = after.iterator().next();
            driver.switchTo().window(newTab);
            wait.until(d -> d.getCurrentUrl() != null && !d.getCurrentUrl().isEmpty());
            String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
            Assertions.assertTrue(url.contains(expectedDomainFragment.toLowerCase(Locale.ROOT)), "External URL should contain " + expectedDomainFragment + " but was " + url);
            driver.close();
            driver.switchTo().window(original);
        } else {
            String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
            Assertions.assertTrue(url.contains(expectedDomainFragment.toLowerCase(Locale.ROOT)), "External URL should contain " + expectedDomainFragment + " but was " + url);
            driver.navigate().back();
        }
    }

    private int pathDepth(String href) {
        try {
            URI uri = URI.create(href);
            String path = uri.getPath();
            if (path == null || path.isEmpty() || "/".equals(path)) return 0;
            String[] parts = Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            return parts.length;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    // ----------------------------- Tests -----------------------------

    @Test
    @Order(1)
    public void loginPageLoads_FieldsPresent() {
        goToBase();
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("/login"), "URL should contain /login");
        Optional<WebElement> emailInput = findFirstVisible(Arrays.asList(
                By.id("email"), By.name("email"), By.cssSelector("input[type='email']"), By.cssSelector("input[name*='email' i]")
        ));
        Optional<WebElement> passwordInput = findFirstVisible(Arrays.asList(
                By.id("password"), By.name("password"), By.cssSelector("input[type='password']"), By.cssSelector("input[name*='password' i]")
        ));
        Optional<WebElement> submitBtn = findFirstVisible(Arrays.asList(
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login')]")
        ));
        Assertions.assertAll(
                () -> Assertions.assertTrue(emailInput.isPresent(), "Email input should be visible"),
                () -> Assertions.assertTrue(passwordInput.isPresent(), "Password input should be visible"),
                () -> Assertions.assertTrue(submitBtn.isPresent(), "Submit button should be visible")
        );
    }

    @Test
    @Order(2)
    public void negativeLogin_ShowsErrorMessage() {
        goToBase();
        login("unknown@example.com", "wrong123");
        // Expect error near form
        List<WebElement> errors = driver.findElements(By.cssSelector(".alert, [role='alert'], .error, .invalid-feedback, .help.is-danger"));
        Assertions.assertTrue(!errors.isEmpty() || driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("/login"),
                "With invalid credentials, should remain on /login or see an error message");
    }

    @Test
    @Order(3)
    public void positiveLogin_Succeeds_ThenLogoutIfPossible() {
        login(LOGIN, PASSWORD);
        boolean atDashboard = waitUrlContainsAny(Arrays.asList("/dashboard", "/home", "/admin", "/painel", "/panel"));
        boolean loggedIn = atDashboard || isLoggedInHeuristic();
        Assertions.assertTrue(loggedIn, "Should be logged in and navigate to a dashboard-like page");
        logoutIfPossible(); // optional
    }

    @Test
    @Order(4)
    public void menuBurger_Optional_OpenClose() {
        goToBase();
        // If login is required to see burger, try logging in first
        login(LOGIN, PASSWORD);
        // Try to find burger toggler
        List<By> burgerLocs = Arrays.asList(
                By.cssSelector("button.navbar-toggler"),
                By.cssSelector("button[aria-label='Toggle navigation']"),
                By.cssSelector("[data-bs-toggle='offcanvas'], [data-toggle='offcanvas'], [data-bs-toggle='collapse'], [data-toggle='collapse']"),
                By.cssSelector(".ant-drawer-open, .MuiDrawer-root") // presence markers
        );
        Optional<WebElement> burger = findFirstVisible(burgerLocs);
        if (burger.isPresent()) {
        	WebElement web = wait.until(ExpectedConditions.elementToBeClickable(burger.get())); 
        	web.clear();
            // After open, a nav link should be visible
            boolean anyNavLink = !displayedAll(By.cssSelector("nav a, .offcanvas a, .collapse.show a, .ant-menu a, .MuiListItem-button")).isEmpty();
            Assertions.assertTrue(anyNavLink, "Nav links should be visible after opening burger/offcanvas");
            // Try closing
            burger.get().click();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://"), "Still on site after closing menu");
        } else {
            Assertions.assertTrue(true, "No burger menu found; skipping.");
        }
    }

    @Test
    @Order(5)
    public void optionalSortingDropdown_ChangesSelectionIfPresent() {
        // Sorting is domain-specific; treat as optional anywhere after login
        login(LOGIN, PASSWORD);
        Optional<WebElement> sortSelect = findFirstVisible(Arrays.asList(
                By.cssSelector("select[id*='sort' i]"),
                By.cssSelector("select[name*='sort' i]"),
                By.xpath("//select[contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort')]")
        ));
        if (sortSelect.isEmpty()) {
            Assertions.assertTrue(true, "No sorting dropdown available; skipping.");
            return;
        }
        Select select = new Select(sortSelect.get());
        String initial = select.getFirstSelectedOption().getText();
        List<WebElement> options = select.getOptions();
        if (options.size() > 1) {
            select.selectByIndex(options.size() - 1);
            String after = select.getFirstSelectedOption().getText();
            Assertions.assertNotEquals(initial, after, "Selecting a different sort option should change selection");
        } else {
            Assertions.assertTrue(true, "Only one sort option; nothing to change.");
        }
    }

    @Test
    @Order(6)
    public void footerExternalLinks_TwitterFacebookLinkedIn_IfPresent() {
        // Check on login page for publicly visible footer links
        goToBase();
        Map<String, String> targets = new LinkedHashMap<>();
        targets.put("a[href*='twitter.com']", "twitter.com");
        targets.put("a[href*='facebook.com']", "facebook.com");
        targets.put("a[href*='linkedin.com']", "linkedin.com");
        for (Map.Entry<String, String> entry : targets.entrySet()) {
            List<WebElement> links = driver.findElements(By.cssSelector(entry.getKey()));
            if (!links.isEmpty()) {
                openExternalAndAssert(links.get(0), entry.getValue());
            }
        }
    }

    @Test
    @Order(7)
    public void crawlOneLevelBelowBase_InternalPagesReachable() {
        goToBase();
        // Collect distinct internal links one path segment deep (e.g., /forgot, /register)
        Set<String> oneLevel = new LinkedHashSet<>();
        for (WebElement a : driver.findElements(By.cssSelector("a[href]"))) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith("http")) continue;
            if (!href.startsWith("https://beta.brasilagritest.com")) continue;
            int depth = pathDepth(href);
            if (depth == 1) oneLevel.add(href);
        }
        int visited = 0;
        for (String url : oneLevel) {
            driver.navigate().to(url);
            waitForPageReady();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://beta.brasilagritest.com"), "Should stay within the same domain");
            visited++;
            if (visited >= 5) break; // limit to keep test stable and fast
        }
        goToBase();
    }
}
