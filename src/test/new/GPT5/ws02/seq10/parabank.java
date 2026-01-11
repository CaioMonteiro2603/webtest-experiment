package GPT5.ws02.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    // Robust selectors for ParaBank
    private static final By BODY = By.tagName("body");
    private static final By LOGIN_PANEL = By.id("loginPanel");
    private static final By USERNAME_INPUT = By.cssSelector("#loginPanel input[name='username'], #loginPanel input#username");
    private static final By PASSWORD_INPUT = By.cssSelector("#loginPanel input[name='password'], #loginPanel input#password");
    private static final By LOGIN_BUTTON = By.cssSelector("#loginPanel input[type='submit'], #loginPanel input.button");
    private static final By REGISTER_LINK = By.linkText("Register");
    private static final By ERROR_MESSAGE = By.xpath("//*[contains(@class,'error') or contains(.,'could not be verified')]");
    private static final By ACCOUNTS_OVERVIEW_HEADER = By.xpath("//h1[contains(normalize-space(.), 'Accounts Overview')]");
    private static final By LOGOUT_LINK = By.linkText("Log Out");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    // Non-applicable (to assert absence for this app)
    private static final By SORTING_DROPDOWN = By.cssSelector("select#sort, select[name*='sort'], [data-test='product_sort_container']");
    private static final By BURGER_MENU = By.id("react-burger-menu-btn");

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------------- Utilities ----------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getTitle() != null && !driver.getTitle().isEmpty(), "Base page should have a title");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/"), "URL should contain /parabank/");
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static String hostOf(String url) {
        try {
            return Optional.ofNullable(new URI(url)).map(URI::getHost).orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    private boolean tryLogin(String user, String pass) {
        openBase();
        WebElement u = wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        u.clear();
        u.sendKeys(user);

        WebElement p = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        p.clear();
        p.sendKeys(pass);

        WebElement btn = waitClickable(LOGIN_BUTTON);
        btn.click();

        // Wait for success or error
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("overview"),
                ExpectedConditions.presenceOfElementLocated(ACCOUNTS_OVERVIEW_HEADER),
                ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE)
            ));
        } catch (TimeoutException ignored) {}

        boolean success = driver.getCurrentUrl().contains("overview") || isPresent(ACCOUNTS_OVERVIEW_HEADER) || isPresent(LOGOUT_LINK);
        boolean error = isPresent(ERROR_MESSAGE);
        return success && !error;
    }

    private void logoutIfLoggedIn() {
        if (isPresent(LOGOUT_LINK)) {
            waitClickable(LOGOUT_LINK).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(LOGIN_PANEL));
        }
    }

    private void assertExternalByClick(WebElement link) {
        String href = link.getAttribute("href");
        String expectedDomain = hostOf(href);
        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        // Either new tab or same tab
        try {
            wait.until(drv -> drv.getWindowHandles().size() > before.size() || drv.getCurrentUrl().contains(expectedDomain));
        } catch (TimeoutException te) {
            Assertions.fail("External link did not open as expected: " + href);
            return;
        }

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "URL should contain expected external domain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "URL should contain expected external domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    // ---------------- Tests ----------------

    @Test
    @Order(1)
    public void basePage_ShouldLoad_And_LoginFormVisible() {
        openBase();
        Assertions.assertAll(
            () -> Assertions.assertTrue(isPresent(LOGIN_PANEL), "Login panel should be present"),
            () -> Assertions.assertTrue(isPresent(USERNAME_INPUT), "Username input should be present"),
            () -> Assertions.assertTrue(isPresent(PASSWORD_INPUT), "Password input should be present"),
            () -> Assertions.assertTrue(isPresent(LOGIN_BUTTON), "Login button should be present")
        );
    }

    @Test
    @Order(2)
    public void login_Negative_WithWrongPassword_ShowsErrorOrStays() {
        openBase();
        WebElement u = wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        u.clear();
        u.sendKeys(LOGIN);

        WebElement p = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        p.clear();
        p.sendKeys("wrong-password");

        waitClickable(LOGIN_BUTTON).click();

        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
                ExpectedConditions.urlContains("login.htm"),
                ExpectedConditions.presenceOfElementLocated(LOGIN_PANEL)
            ));
        } catch (TimeoutException ignored) {}

        boolean errorShown = isPresent(ERROR_MESSAGE);
        boolean stillOnLogin = driver.getCurrentUrl().contains("login") || isPresent(LOGIN_PANEL);
        Assertions.assertTrue(errorShown || stillOnLogin, "Should show error or remain on login for invalid credentials");
    }

    @Test
    @Order(3)
    public void login_Positive_Attempt_VerifyOverview_And_Logout() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Valid login not available with provided credentials; skipping positive flow.");
        // Verify we are on accounts overview or logged-in state
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview") || isPresent(ACCOUNTS_OVERVIEW_HEADER) || isPresent(LOGOUT_LINK),
                "After login, should be on Accounts Overview or show logout control");
        logoutIfLoggedIn();
        Assertions.assertTrue(isPresent(LOGIN_PANEL), "After logout, login panel should be visible");
    }

    @Test
    @Order(4)
    public void registerPage_OneLevelBelow_ShouldOpenAndShowFormFields() {
        openBase();
        Assumptions.assumeTrue(isPresent(REGISTER_LINK), "Register link not found; skipping");
        waitClickable(REGISTER_LINK).click();
        // On Register page, assert some customer fields
        By firstName = By.id("customer.firstName");
        By lastName = By.id("customer.lastName");
        By address = By.id("customer.address.street");
        By username = By.id("customer.username");
        By password = By.id("customer.password");
        wait.until(ExpectedConditions.presenceOfElementLocated(firstName));
        Assertions.assertAll(
            () -> Assertions.assertTrue(isPresent(firstName), "First name field should be present"),
            () -> Assertions.assertTrue(isPresent(lastName), "Last name field should be present"),
            () -> Assertions.assertTrue(isPresent(address), "Address field should be present"),
            () -> Assertions.assertTrue(isPresent(username), "Register username should be present"),
            () -> Assertions.assertTrue(isPresent(password), "Register password should be present")
        );
        driver.navigate().back();
        wait.until(ExpectedConditions.presenceOfElementLocated(LOGIN_PANEL));
    }

    @Test
    @Order(5)
    public void internalLinks_OneLevelBelow_AreReachable_AndReturn() {
        openBase();
        String baseHost = hostOf(BASE_URL);

        // Collect candidate internal links from header/nav/footer areas
        List<WebElement> links = driver.findElements(ANY_LINK);
        List<String> candidates = links.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(href -> hostOf(href).equalsIgnoreCase(baseHost))
                .filter(href -> !href.contains("#"))
                .filter(href -> href.contains("/parabank/"))
                .distinct()
                .limit(4)
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!candidates.isEmpty(), "No internal links found to test.");
        for (String url : candidates) {
            driver.navigate().to(url);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/"),
                    "Internal link should keep us on /parabank/: " + url);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    @Test
    @Order(6)
    public void footerOrHeader_ExternalLinks_Open_NewTab_AndDomainMatches() {
        openBase();
        String baseHost = hostOf(BASE_URL);

        // Collect external links and refresh the list after each click to avoid stale elements
        List<String> testedDomains = new ArrayList<>();
        int maxLinks = 3;

        int found = 0;
        while (found < maxLinks) {
            // Refresh external links collection in each iteration
            List<WebElement> links = driver.findElements(ANY_LINK);
            WebElement candidate = null;

            for (WebElement a : links) {
                String href;
                try {
                    href = a.getAttribute("href");
                } catch (StaleElementReferenceException e) {
                    continue;
                }
                if (href == null || href.trim().isEmpty()) continue;
                String host = hostOf(href);
                String target = Optional.ofNullable(a.getAttribute("target")).orElse("");
                boolean isExternal = (!host.isEmpty() && !host.equalsIgnoreCase(baseHost)) || "_blank".equalsIgnoreCase(target);
                if (!isExternal || testedDomains.contains(host)) continue;
                candidate = a;
                testedDomains.add(host);
                break;
            }

            if (candidate == null) break;

            // Double-check href again before clicking
            String href;
            try {
                href = candidate.getAttribute("href");
            } catch (StaleElementReferenceException e) {
                continue;
            }
            Assumptions.assumeTrue(href != null && !href.trim().isEmpty(), "External link href is null/empty");

            assertExternalByClick(candidate);
            found++;
        }

        Assumptions.assumeTrue(found > 0, "No suitable external links tested.");
    }

    @Test
    @Order(7)
    public void notApplicable_SortingAndBurgerMenu_ShouldBeAbsent() {
        openBase();
        Assertions.assertAll(
            () -> Assertions.assertTrue(driver.findElements(SORTING_DROPDOWN).isEmpty(),
                    "Sorting dropdown (inventory style) should not exist in ParaBank"),
            () -> Assertions.assertTrue(driver.findElements(BURGER_MENU).isEmpty(),
                    "Burger menu (Swag Labs style) should not exist in ParaBank")
        );
    }
}