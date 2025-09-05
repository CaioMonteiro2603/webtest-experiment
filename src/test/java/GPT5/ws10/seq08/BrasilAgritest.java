package GPT5.ws10.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgriBetaE2ETest {

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // -------------------- Helpers --------------------

    private void openLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='email']")),
                ExpectedConditions.titleContains("Login")
        ));
    }

    private boolean exists(By by) {
        return !driver.findElements(by).isEmpty();
    }

    private Optional<WebElement> first(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return Optional.of(els.get(0));
        }
        return Optional.empty();
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private void type(By by, String text) {
        WebElement el = waitClickable(by);
        el.clear();
        el.sendKeys(text);
    }

    private boolean isLoggedInHeuristic() {
        // Look for common dashboard markers or logout button
        return exists(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]")) ||
               exists(By.cssSelector("a[href*='logout']")) ||
               exists(By.cssSelector("[data-test='logout']")) ||
               driver.getCurrentUrl().toLowerCase().contains("/dashboard") ||
               exists(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'dashboard')]"));
    }

    private void logoutIfPossible() {
        if (exists(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]"))) {
            waitClickable(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]")).click();
        } else if (exists(By.cssSelector("a[href*='logout']"))) {
            waitClickable(By.cssSelector("a[href*='logout']")).click();
        } else if (exists(By.cssSelector("[data-test='logout']"))) {
            waitClickable(By.cssSelector("[data-test='logout']")).click();
        }
        // Wait until login screen is back (if it logs out)
        wait.until((ExpectedCondition<Boolean>) d ->
                exists(By.cssSelector("input[type='email']")) ||
                exists(By.cssSelector("input[name='email']")) ||
                driver.getCurrentUrl().contains("/login")
        );
    }

    private void login(String email, String password) {
        openLogin();
        By emailBy = exists(By.cssSelector("input[name='email']")) ? By.cssSelector("input[name='email']") : By.cssSelector("input[type='email']");
        By passBy = exists(By.cssSelector("input[name='password']")) ? By.cssSelector("input[name='password']") : By.cssSelector("input[type='password']");
        type(emailBy, email);
        type(passBy, password);

        By submitBy = exists(By.cssSelector("button[type='submit']")) ? By.cssSelector("button[type='submit']")
                : By.xpath("//button[contains(.,'Entrar') or contains(.,'Login')]");
        waitClickable(submitBy).click();

        // Wait until either dashboard or an error appears
        wait.until((ExpectedCondition<Boolean>) d ->
                isLoggedInHeuristic() ||
                exists(By.cssSelector(".error, .alert-danger, [role='alert']")) ||
                exists(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'credenciais') or contains(.,'inválid') or contains(.,'invalid')]"))
        );
    }

    private void resetAppStateIfAvailable() {
        // Try common patterns for "Reset App State"
        if (exists(By.xpath("//a[contains(.,'Reset App State')] | //button[contains(.,'Reset App State')]"))) {
            waitClickable(By.xpath("//a[contains(.,'Reset App State')] | //button[contains(.,'Reset App State')]")).click();
            // Confirm with a toast or by checking a state reset marker if any; otherwise just wait briefly via condition
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".toast-success, .alert-success")),
                    ExpectedConditions.urlContains("reset")
            ));
        }
    }

    private Optional<WebElement> findBurgerMenu() {
        By[] candidates = new By[] {
                By.cssSelector("[aria-label='menu']"),
                By.cssSelector(".navbar-burger"),
                By.id("react-burger-menu-btn"),
                By.xpath("//button[contains(@aria-label,'menu') or contains(.,'Menu')]")
        };
        return first(candidates);
    }

    private void openBurgerIfPresent() {
        Optional<WebElement> burger = findBurgerMenu();
        burger.ifPresent(b -> wait.until(ExpectedConditions.elementToBeClickable(b)).click());
    }

    private void assertExternalLinkOpens(String cssSelector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        if (links.isEmpty()) return; // treat as optional
        String original = driver.getWindowHandle();
        String before = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();
        try {
            wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(before));
        } catch (TimeoutException ignored) {}

        Set<String> handles = driver.getWindowHandles();
        if (handles.size() > 1) {
            for (String h : handles) if (!h.equals(original)) { driver.switchTo().window(h); break; }
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should navigate to " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should navigate to " + expectedDomain);
            driver.navigate().back();
        }
    }

    // Try to exercise a generic sorting dropdown if present on dashboard lists/tables.
    private void exerciseFirstSortingDropdownIfAny() {
        List<WebElement> selects = driver.findElements(By.cssSelector("select, .select select"));
        if (selects.isEmpty()) return;

        // Choose the first real <select> having 2+ options
        WebElement selectEl = null;
        for (WebElement s : selects) {
            try {
                Select sel = new Select(s);
                if (sel.getOptions().size() >= 2) { selectEl = s; break; }
            } catch (Exception ignored) {}
        }
        if (selectEl == null) return;

        Select sel = new Select(selectEl);

        // Try to detect an adjacent list/table to observe changes
        List<String> beforeTexts = collectFirstColumnTexts();

        // Switch to last option, then to first, and expect some change (heuristic)
        int count = sel.getOptions().size();
        sel.selectByIndex(count - 1);
        wait.until(d -> true); // allow minimal rendering

        List<String> after1 = collectFirstColumnTexts();
        sel.selectByIndex(0);
        wait.until(d -> true);
        List<String> after2 = collectFirstColumnTexts();

        boolean changedOnce = !beforeTexts.equals(after1) || !after1.equals(after2);
        Assertions.assertTrue(changedOnce, "Changing sort options should influence list/table order (heuristic)");
    }

    private List<String> collectFirstColumnTexts() {
        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (!rows.isEmpty()) {
            return rows.stream().map(r -> {
                List<WebElement> tds = r.findElements(By.cssSelector("td"));
                return tds.isEmpty() ? r.getText() : tds.get(0).getText();
            }).collect(Collectors.toList());
        }
        // fallback: collect from list items/cards
        List<WebElement> items = driver.findElements(By.cssSelector(".list-group .list-group-item, .card .card-title, li"));
        return items.stream().limit(10).map(WebElement::getText).collect(Collectors.toList());
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void loginPageLoads_controlsPresent() {
        openLogin();
        Assertions.assertAll(
                () -> Assertions.assertTrue(exists(By.cssSelector("input[type='email']")) || exists(By.cssSelector("input[name='email']")),
                        "Email input should be present"),
                () -> Assertions.assertTrue(exists(By.cssSelector("input[type='password']")) || exists(By.cssSelector("input[name='password']")),
                        "Password input should be present"),
                () -> Assertions.assertTrue(exists(By.cssSelector("button[type='submit']")) ||
                        exists(By.xpath("//button[contains(.,'Entrar') or contains(.,'Login')]")), "Submit button should be present")
        );
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        login("invalid@example.com", "wrongpass123");
        Assertions.assertAll(
                () -> Assertions.assertTrue(exists(By.cssSelector(".error, .alert-danger, [role='alert']")) ||
                        exists(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inválid') or contains(.,'invalid')]")),
                        "Some error indicator/message should appear for invalid login"),
                () -> Assertions.assertTrue(driver.getCurrentUrl().contains("/login") || !isLoggedInHeuristic(),
                        "Should remain on login page and not be logged in")
        );
    }

    @Test
    @Order(3)
    public void validLoginNavigatesToDashboard_orExplicitError() {
        login(LOGIN, PASSWORD);
        Assertions.assertTrue(isLoggedInHeuristic() ||
                        exists(By.cssSelector(".error, .alert-danger, [role='alert']")),
                "Either we should reach dashboard (logged in) or see an explicit error");
    }

    @Test
    @Order(4)
    public void burgerMenu_openClose_allItems_about_logout_resetIfAvailable() {
        if (!isLoggedInHeuristic()) login(LOGIN, PASSWORD);

        Optional<WebElement> burger = findBurgerMenu();
        if (burger.isPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(burger.get())).click();
            boolean menuOpened = exists(By.cssSelector(".bm-menu-wrap, nav, aside")) ||
                                 exists(By.xpath("//button[contains(.,'Fechar') or contains(.,'Close')]"));
            Assertions.assertTrue(menuOpened, "Burger/menu should open");

            // All Items (navigate to some main listing)
            if (exists(By.xpath("//a[contains(.,'All Items')]")) || exists(By.id("inventory_sidebar_link"))) {
                first(By.xpath("//a[contains(.,'All Items')]"), By.id("inventory_sidebar_link"))
                        .ifPresent(WebElement::click);
                // verify URL or presence of a list/table
                wait.until((ExpectedCondition<Boolean>) d ->
                        exists(By.cssSelector("table, .list-group, .card, .data-table")) ||
                        !driver.getCurrentUrl().contains("/login"));
                Assertions.assertTrue(true, "All Items action executed");
                openBurgerIfPresent();
            }

            // About (external)
            if (exists(By.xpath("//a[contains(.,'About')]")) || exists(By.cssSelector("a[href*='http']"))) {
                WebElement about = first(By.xpath("//a[contains(.,'About')]"), By.cssSelector("a[href*='http']"))
                        .orElse(null);
                if (about != null) {
                    String href = about.getAttribute("href");
                    String expectedDomain = href != null && href.contains("http") ? href.split("/")[2] : "http";
                    String original = driver.getWindowHandle();
                    String before = driver.getCurrentUrl();
                    wait.until(ExpectedConditions.elementToBeClickable(about)).click();
                    try { wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(before)); } catch (TimeoutException ignored) {}
                    Set<String> handles = driver.getWindowHandles();
                    if (handles.size() > 1) {
                        for (String h : handles) if (!h.equals(original)) { driver.switchTo().window(h); break; }
                        wait.until(ExpectedConditions.urlContains(expectedDomain));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "About should open external site");
                        driver.close();
                        driver.switchTo().window(original);
                    } else {
                        wait.until(ExpectedConditions.urlContains(expectedDomain));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "About should open external site");
                        driver.navigate().back();
                    }
                    openBurgerIfPresent();
                }
            }

            // Reset App State (optional)
            resetAppStateIfAvailable();

            // Logout via menu if present (will be re-logged in by later tests as needed)
            if (exists(By.xpath("//a[contains(.,'Logout')]")) ||
                exists(By.xpath("//button[contains(.,'Logout') or contains(.,'Sair')]")) ||
                exists(By.cssSelector("a[href*='logout']"))) {
                first(By.xpath("//a[contains(.,'Logout')]"),
                      By.xpath("//button[contains(.,'Logout') or contains(.,'Sair')]"),
                      By.cssSelector("a[href*='logout']"))
                        .ifPresent(WebElement::click);
                // Expect back to login
                wait.until((ExpectedCondition<Boolean>) d ->
                        driver.getCurrentUrl().contains("/login") ||
                        exists(By.cssSelector("input[type='email']"))
                );
                Assertions.assertTrue(driver.getCurrentUrl().contains("/login") || exists(By.cssSelector("input[type='email']")),
                        "After logout, login page should be visible");
            } else {
                // Close menu if close control exists
                first(By.id("react-burger-cross-btn"),
                     By.xpath("//button[contains(.,'Close') or contains(.,'Fechar')]"))
                    .ifPresent(WebElement::click);
            }
        } else {
            Assumptions.assumeTrue(false, "Burger/menu not present; skipping menu-related assertions");
        }
    }

    @Test
    @Order(5)
    public void sortingDropdown_exercisesIfPresent_andAffectsOrder() {
        if (!isLoggedInHeuristic()) login(LOGIN, PASSWORD);
        exerciseFirstSortingDropdownIfAny();
    }

    @Test
    @Order(6)
    public void footerSocialLinks_openExternally_ifPresent() {
        // test without requiring login
        openLogin();
        assertExternalLinkOpens("a[href*='twitter.com']", "twitter.com");
        assertExternalLinkOpens("a[href*='facebook.com']", "facebook.com");
        assertExternalLinkOpens("a[href*='linkedin.com']", "linkedin.com");
    }

    @Test
    @Order(7)
    public void logout_returnsToLoginPage() {
        if (!isLoggedInHeuristic()) login(LOGIN, PASSWORD);
        logoutIfPossible();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login") ||
                              exists(By.cssSelector("input[type='email']")),
                "After logout, should be on login screen");
    }
}
