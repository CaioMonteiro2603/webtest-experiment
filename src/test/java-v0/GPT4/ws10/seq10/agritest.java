package GPT4.ws10.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class agritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN_USERNAME = "superadmin@brasilagritest.com.br";
    private static final String LOGIN_PASSWORD = "10203040";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on login page");
    }

    private void login(String user, String pass) {
        openBase();
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement btn = driver.findElement(By.cssSelector("button[type='submit']"));
        email.clear(); email.sendKeys(user);
        password.clear(); password.sendKeys(pass);
        btn.click();
    }

    private String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return ""; }
    }

    private void assertExternalLink(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not external link");
        String expectedHost = hostOf(href);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();
        wait.until(d -> d.getWindowHandles().size() > before.size() || driver.getCurrentUrl().contains(expectedHost));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String nw = after.iterator().next();
            driver.switchTo().window(nw);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "Expected host in URL");
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "Expected host in same-tab URL");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        }
    }

    @Test
    @Order(1)
    public void testSuccessfulLogin() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should land on dashboard after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        login("bad@user.com", "wrongpass");
        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(err.getText().toLowerCase().contains("invalid"), "Should show invalid credentials error");
    }

    @Test
    @Order(3)
    public void testInternalLinksOneLevel() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        int tested = 0;
        String baseHost = hostOf(BASE_URL);
        for (WebElement link : driver.findElements(By.cssSelector("a[href]"))) {
            String href = link.getAttribute("href");
            String host = hostOf(href);
            if (href.startsWith(BASE_URL) || host.equalsIgnoreCase(baseHost) || host.isEmpty()) {
                link.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                Assertions.assertTrue(hostOf(driver.getCurrentUrl()).equalsIgnoreCase(baseHost), "Should stay within domain");
                tested++;
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                if (tested >= 5) break;
            }
        }
        Assertions.assertTrue(tested > 0, "Should test at least one internal link");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        int tested = 0;
        String baseHost = hostOf(BASE_URL);
        for (WebElement link : driver.findElements(By.cssSelector("a[href]"))) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http") && !hostOf(href).equalsIgnoreCase(baseHost)) {
                assertExternalLink(link);
                tested++;
                if (tested >= 3) break;
            }
        }
        Assertions.assertTrue(tested > 0, "Should test at least one external link");
    }

    @Test
    @Order(5)
    public void testLogout() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu']")));
        menu.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logout.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should return to login page after logout");
    }
}
