package GPT5.ws10.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://brasilagri.com/login";
    private static final String LOGIN_EMAIL = "superadmin@brasilagri.com.br";
    private static final String LOGIN_PASSWORD = "10203040";

    @BeforeAll
    static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1400, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    /* ---------------- Helpers ---------------- */

    private WebElement waitClickable(WebElement el) {
        return wait.until(ExpectedConditions.elementToBeClickable(el));
    }

    private WebElement first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private static String getOrigin(String url) {
        try {
            URI u = new URI(url);
            if (u.getHost() == null) return "";
            return u.getScheme() + "://" + u.getHost() + (u.getPort() > -1 ? ":" + u.getPort() : "");
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private static String getHost(String url) {
        try {
            URI u = new URI(url);
            return u.getHost() == null ? "" : u.getHost();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private boolean isSameOrigin(String href) {
        return getOrigin(BASE_URL).equalsIgnoreCase(getOrigin(href));
    }

    private void set(WebElement el, String text) {
        wait.until(ExpectedConditions.visibilityOf(el));
        el.clear();
        el.sendKeys(text);
    }

    private boolean isLoggedInHeuristic() {
        if (!driver.getCurrentUrl().toLowerCase().contains("/login")) return true;
        return !driver.findElements(By.xpath("//*[contains(translate(.,'LOGOUT','logout'),'logout')]")).isEmpty();
    }

    private void login(String email, String password) {
        WebElement emailField = first(By.cssSelector("input[type='email'], input[name*='email' i]"));
        WebElement passField = first(By.cssSelector("input[type='password'], input[name*='pass' i]"));

        Assertions.assertNotNull(emailField);
        Assertions.assertNotNull(passField);

        set(emailField, email);
        set(passField, password);

        WebElement submit = first(By.cssSelector("button[type='submit'], input[type='submit']"));
        if (submit != null) {
            waitClickable(submit).click();
        } else {
            passField.sendKeys(Keys.ENTER);
        }

        wait.until(d ->
                !d.getCurrentUrl().toLowerCase().contains("/login")
                        || !d.findElements(By.cssSelector(".error, .alert, .invalid-feedback")).isEmpty()
        );
    }
}