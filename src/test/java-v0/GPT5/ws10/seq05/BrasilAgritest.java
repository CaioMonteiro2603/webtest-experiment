package GPT5.ws10.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;

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
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ---------------- Helpers ----------------

    private void openBase() {
        driver.get(BASE_URL);
        waitDocumentReady();
        dismissCookieOrWelcome();
    }

    private void waitDocumentReady() {
        try {
            wait.until(d ->
                    ((JavascriptExecutor) d)
                            .executeScript("return document.readyState")
                            .equals("complete")
            );
        } catch (Exception ignored) {
        }
    }

    private void dismissCookieOrWelcome() {
        List<By> candidates = Arrays.asList(
                By.cssSelector("button#onetrust-accept-btn-handler"),
                By.cssSelector("button[class*='accept'],button[class*='agree'],button[class*='ok']"),
                By.cssSelector(".cookie-accept,.cc-accept,.cc-dismiss,.btn-close,.close")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(els.get(0))).click();
                    break;
                } catch (Exception ignored) {
                }
            }
        }
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        return null;
    }

    private WebElement waitClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    private boolean isLoggedInHeuristic() {
        String url = driver.getCurrentUrl().toLowerCase();
        if (url.contains("/login")) return false;
        return !driver.findElements(By.cssSelector("nav,header,.sidebar,.navbar,a[href*='logout']")).isEmpty();
    }

    private void login(String user, String pass) {
        openBase();
        WebElement email = firstPresent(
                By.id("email"), By.name("email"),
                By.cssSelector("input[type='email']")
        );
        WebElement password = firstPresent(
                By.id("password"), By.name("password"),
                By.cssSelector("input[type='password']")
        );
        WebElement submit = firstPresent(
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Entrar') or contains(.,'Login') or contains(.,'Acessar')]")
        );

        Assertions.assertNotNull(email, "Email field not found");
        Assertions.assertNotNull(password, "Password field not found");
        Assertions.assertNotNull(submit, "Submit button not found");

        email.clear();
        email.sendKeys(user);
        password.clear();
        password.sendKeys(pass);
        waitClickable(submit).click();
        waitDocumentReady();
    }

    // ---------------- Tests ----------------

    @Test
    @Order(1)
    public void basePage_Loads_And_HasLoginForm() {
        openBase();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"));
        Assertions.assertNotNull(firstPresent(By.cssSelector("input[type='email']")));
        Assertions.assertNotNull(firstPresent(By.cssSelector("input[type='password']")));
    }

    @Test
    @Order(2)
    public void login_WithValidCredentials_NavigatesToApp() {
        login(LOGIN, PASSWORD);

        // Force primitive boolean to avoid JUnit's assertTrue(BooleanSupplier) overload issues with type inference
        boolean loggedIn = wait.until(d -> isLoggedInHeuristic());

        Assertions.assertTrue(loggedIn, "Login did not succeed (heuristic check failed)");
    }
}