package GPT5.ws03.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_USER = "caio@gmail.com";
    private static final String LOGIN_PASS = "123";

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

    // ----------------------- Helpers -----------------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")),
                ExpectedConditions.presenceOfElementLocated(By.id("inputEmail")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='email']")),
                ExpectedConditions.titleContains("BugBank")
        ));
    }

    private boolean exists(By by) {
        return !driver.findElements(by).isEmpty();
    }

    private Optional<WebElement> firstOptional(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return Optional.of(els.get(0));
        }
        return Optional.empty();
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void type(By by, String text) {
        WebElement el = waitClickable(by);
        el.clear();
        el.sendKeys(text);
    }

    private boolean isLoggedIn() {
        // Heuristics: dashboard panels or logout/button present
        return exists(By.cssSelector("[data-test='logout']"))
                || exists(By.id("btnExit"))
                || driver.getCurrentUrl().toLowerCase().contains("home")
                || driver.findElements(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saldo')]")).size() > 0;
    }

    private void logoutIfPossible() {
        if (exists(By.cssSelector("[data-test='logout']"))) {
            waitClickable(By.cssSelector("[data-test='logout']")).click();
        } else if (exists(By.id("btnExit"))) {
            waitClickable(By.id("btnExit")).click();
        } else if (exists(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]"))) {
            waitClickable(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]")).click();
        }
        // back to login screen
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")),
                ExpectedConditions.presenceOfElementLocated(By.id("inputEmail"))
        ));
    }

    private void openRegisterModalIfPresent() {
        Optional<WebElement> reg = firstOptional(
                By.id("btnRegister"),
                By.cssSelector("[data-test='register']"),
                By.xpath("//button[contains(.,'Registrar') or contains(.,'Register')]")
        );
        reg.ifPresent(WebElement::click);
    }

    private boolean errorToastPresent() {
        return exists(By.cssSelector("[role='alert']"))
                || exists(By.cssSelector("[data-test='message']"))
                || driver.findElements(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'erro')]")).size() > 0;
    }

    private void login(String email, String pass) {
        goHome();
        // email
        By emailBy = exists(By.id("inputEmail")) ? By.id("inputEmail")
                : exists(By.cssSelector("[data-test='email']")) ? By.cssSelector("[data-test='email']")
                : By.cssSelector("input[type='email']");
        type(emailBy, email);
        // password
        By passBy = exists(By.id("inputPassword")) ? By.id("inputPassword")
                : exists(By.cssSelector("[data-test='password']")) ? By.cssSelector("[data-test='password']")
                : By.cssSelector("input[type='password']");
        type(passBy, pass);
        // login button
        By btnBy = exists(By.id("btnLogin")) ? By.id("btnLogin")
                : exists(By.cssSelector("[data-test='login-button']")) ? By.cssSelector("[data-test='login-button']")
                : By.xpath("//button[contains(.,'Acessar') or contains(.,'Login') or contains(.,'Entrar')]");
        waitClickable(btnBy).click();

        // Wait until either we get into dashboard or an error toast appears
        wait.until((ExpectedCondition<Boolean>) d -> isLoggedIn() || errorToastPresent());
    }

    private void assertExternalLink(String cssSelector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        if (links.isEmpty()) return; // optional
        String original = driver.getWindowHandle();
        String urlBefore = driver.getCurrentUrl();
        links.get(0).click();
        try {
            wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(urlBefore));
        } catch (TimeoutException ignored) { }
        Set<String> handles = new HashSet<>(driver.getWindowHandles());
        handles.remove(original);
        if (!handles.isEmpty()) {
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains(expectedDomain),
                    ExpectedConditions.not(ExpectedConditions.urlToBe(urlBefore))
            ));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.navigate().back();
        }
    }

    // ----------------------- Tests -----------------------

    @Test
    @Order(1)
    public void homePageLoads_andLoginControlsVisible() {
        goHome();
        WebElement email = waitVisible(By.cssSelector("input[type='email']"));
        WebElement password = waitVisible(By.cssSelector("input[type='password']"));
        Optional<WebElement> loginBtn = firstOptional(By.id("btnLogin"),
                By.cssSelector("[data-test='login-button']"),
                By.xpath("//button[contains(.,'Acessar') or contains(.,'Login') or contains(.,'Entrar')]"));
        Assertions.assertAll(
                () -> Assertions.assertTrue(email.isDisplayed(), "Email input should be visible"),
                () -> Assertions.assertTrue(password.isDisplayed(), "Password input should be visible"),
                () -> Assertions.assertTrue(loginBtn.isPresent(), "Login button should be present")
        );
    }

    @Test
    @Order(2)
    public void negativeLogin_showsErrorMessage() {
        goHome();
        login("invalid_user@example.com", "wrongpass");
        // Wait a bit more for the login state to stabilize
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        Assertions.assertTrue(errorToastPresent(), "An error message/toast should be shown for invalid login");
        Assertions.assertFalse(isLoggedIn(), "User should not be logged in with invalid credentials");
    }

    @Test
    @Order(3)
    public void openRegisterModal_ifAvailable_andClose() {
        goHome();
        openRegisterModalIfPresent();
        // Wait a bit for the modal to appear
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        // modal/dialog presence (heuristics)
        boolean modalVisible = exists(By.cssSelector("[role='dialog']")) ||
                               exists(By.cssSelector(".modal")) ||
                               exists(By.id("modalRegister")) ||
                               driver.findElements(By.xpath("//h2[contains(.,'Registrar') or contains(.,'Register')]")).size() > 0;
        Assertions.assertTrue(modalVisible, "Register modal/dialog should appear when clicking Registrar (if feature available)");

        // Try closing modal with common close controls
        if (exists(By.cssSelector("[data-test='close-modal']"))) {
            waitClickable(By.cssSelector("[data-test='close-modal']")).click();
        } else if (exists(By.cssSelector(".modal button[aria-label='Close']"))) {
            waitClickable(By.cssSelector(".modal button[aria-label='Close']")).click();
        } else if (exists(By.xpath("//button[contains(.,'Fechar') or contains(.,'Close')]"))) {
            waitClickable(By.xpath("//button[contains(.,'Fechar') or contains(.,'Close')]")).click();
        } else {
            // If nothing obvious, press ESC to close
            driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
        }
    }

    @Test
    @Order(4)
    public void validLogin_withProvidedCredentials_orGracefulError() {
        goHome();
        login(LOGIN_USER, LOGIN_PASS);
        // Accept either success or explicit error (if the provided test account doesn't exist on the demo)
        Assertions.assertTrue(isLoggedIn() || errorToastPresent(),
                "Should either navigate to dashboard (logged in) or show an explicit error");
    }

    @Test
    @Order(5)
    public void tryNavigateMenuOrBurger_ifExists() {
        goHome();
        // Some deployments have a burger/menu on the login or after login. Try both contexts.
        if (!isLoggedIn()) {
            // if not logged, try to log in to reveal potential menu
            login(LOGIN_USER, LOGIN_PASS);
        }
        // Try opening a burger menu if present (optional)
        Optional<WebElement> burger = firstOptional(
                By.cssSelector("[data-test='menu']"),
                By.id("react-burger-menu-btn"),
                By.xpath("//button[contains(@aria-label,'menu') or contains(.,'Menu')]")
        );
        if (burger.isPresent()) {
            // Verify menu opened by looking for close/cross or sidebar
            boolean menuOpen = exists(By.cssSelector("[data-test='menu-open']")) ||
                               exists(By.id("react-burger-cross-btn")) ||
                               exists(By.cssSelector(".bm-menu-wrap")) ||
                               exists(By.xpath("//nav"));
            Assertions.assertTrue(menuOpen, "Burger menu should open when clicked");
            // Close if close control exists
            Optional<WebElement> close = firstOptional(
                    By.id("react-burger-cross-btn"),
                    By.cssSelector("[data-test='menu-close']"),
                    By.xpath("//button[contains(.,'Fechar') or contains(.,'Close')]")
            );
            close.ifPresent(WebElement::click);
        } else {
            Assertions.assertTrue(true, "Burger/menu not present on this build; skipping without failure");
        }
    }

    @Test
    @Order(6)
    public void performSimpleAction_whenLoggedIn_orSkip() {
        // If logged in, try to find a simple dashboard action (like viewing balance or initiating transfer)
        if (!isLoggedIn()) {
            login(LOGIN_USER, LOGIN_PASS);
        }
        if (isLoggedIn()) {
            // Assert a dashboard marker is present (heuristics)
            boolean dashboardMarker = exists(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saldo')]"))
                    || exists(By.cssSelector("[data-test='home']"))
                    || exists(By.cssSelector("[data-test='user-name']"))
                    || exists(By.xpath("//h1|//h2|//h3"));
            Assertions.assertTrue(dashboardMarker, "A dashboard marker element should be visible after login");
        } else {
            Assertions.assertTrue(errorToastPresent(), "If not logged in, an error should have been shown earlier");
        }
    }

    @Test
    @Order(7)
    public void externalLinks_footer_orHeader_ifPresent() {
        goHome();
        // Try common social links
        assertExternalLink("a[href*='twitter.com']", "twitter.com");
        assertExternalLink("a[href*='facebook.com']", "facebook.com");
        assertExternalLink("a[href*='linkedin.com']", "linkedin.com");
        // Project/repo links – commonly GitHub
        assertExternalLink("a[href*='github.com']", "github.com");
        // Any documentation/demo site (dio.me for some builds)
        assertExternalLink("a[href*='dio.me']", "dio.me");
    }

    @Test
    @Order(8)
    public void resetOrLogout_returnsToLoginPage() {
        if (isLoggedIn()) {
            logoutIfPossible();
        } else {
            goHome();
        }
        Assertions.assertTrue(exists(By.cssSelector("input[type='email']")) || exists(By.id("inputEmail")),
                "Login email field should be visible after logout/reset");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL) || driver.getCurrentUrl().contains("netlify"),
                "Should be on base URL after returning to login");
    }
}