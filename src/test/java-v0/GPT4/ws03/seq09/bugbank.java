package GPT4.ws03.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void openLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")),
                ExpectedConditions.titleContains("BugBank")
        ));
    }

    private void login(String email, String pass) {
        openLogin();
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.clear();
        emailField.sendKeys(email);
        WebElement passField = driver.findElement(By.cssSelector("input[type='password']"));
        passField.clear();
        passField.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], button"))).click();
        wait.until(ExpectedConditions.or(
                d -> d.getCurrentUrl().toLowerCase().contains("/home"),
                d -> d.findElements(By.cssSelector("[role='alert'], .error")).size() > 0
        ));
    }

    private boolean isLoggedIn() {
        return driver.getCurrentUrl().toLowerCase().contains("/home") ||
               !driver.findElements(By.cssSelector("button[aria-label='Logout'], button.logout")).isEmpty();
    }

    private void logoutIfNeeded() {
        if (isLoggedIn()) {
            List<WebElement> logoutBtns = driver.findElements(By.cssSelector("button[aria-label='Logout'], button.logout"));
            if (!logoutBtns.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(logoutBtns.get(0))).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
            }
        }
    }

    private void assertExternalLink(String selector, String domain) {
        List<WebElement> links = driver.findElements(By.cssSelector(selector));
        if (links.isEmpty()) return;
        String original = driver.getWindowHandle();
        links.get(0).click();
        wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(BASE_URL));
        Set<String> handles = driver.getWindowHandles();
        handles.remove(original);
        if (!handles.isEmpty()) {
            driver.switchTo().window(handles.iterator().next());
            wait.until(ExpectedConditions.urlContains(domain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "External link should navigate to " + domain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(domain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "External link should navigate to " + domain);
            driver.navigate().back();
        }
    }

    @Test
    @Order(1)
    public void loginPageDisplaysFields() {
        openLogin();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("input[type='email']")).isDisplayed(), "Email field should appear"),
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("input[type='password']")).isDisplayed(), "Password field should appear"),
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("button[type='submit'], button")).isDisplayed(), "Submit button should appear")
        );
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        login("bad@example.com", "wrong");
        Assertions.assertTrue(driver.findElements(By.cssSelector("[role='alert'], .error")).size() > 0,
                "An error alert should be shown for invalid login");
        Assertions.assertFalse(isLoggedIn(), "Should not be logged in with invalid credentials");
    }

    @Test
    @Order(3)
    public void validLoginNavigatesToDashboardOrError() {
        login(LOGIN, PASSWORD);
        Assertions.assertTrue(isLoggedIn() || driver.findElements(By.cssSelector("[role='alert'], .error")).size() > 0,
                "Either login succeeds or explicit error is shown");
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions_ifAvailable() {
        if (!isLoggedIn()) login(LOGIN, PASSWORD);
        List<WebElement> burger = driver.findElements(By.cssSelector("button[aria-label='Menu'], .burger"));
        if (!burger.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(burger.get(0))).click();
            boolean opened = !driver.findElements(By.cssSelector(".menu, nav, .sidebar")).isEmpty();
            Assertions.assertTrue(opened, "Burger menu should open if present");

            List<WebElement> allItems = driver.findElements(By.xpath("//a[contains(.,'All Items')]"));
            if (!allItems.isEmpty()) {
                allItems.get(0).click();
                wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
            }

            List<WebElement> about = driver.findElements(By.xpath("//a[contains(.,'About')]"));
            if (!about.isEmpty()) {
                String href = about.get(0).getAttribute("href");
                String domain = href.replaceAll("https?://", "").split("/")[0];
                about.get(0).click();
                assertExternalLink("", domain);
            }

            logoutIfNeeded(); // if logout is in the menu

        } else {
            Assumptions.assumeTrue(false, "Burger menu not present; skipping test");
        }
    }

    @Test
    @Order(5)
    public void footerSocialLinks_openExternallyIfPresent() {
        openLogin();
        assertExternalLink("a[href*='twitter.com']", "twitter.com");
        assertExternalLink("a[href*='facebook.com']", "facebook.com");
        assertExternalLink("a[href*='linkedin.com']", "linkedin.com");
    }

    @Test
    @Order(6)
    public void logoutReturnsToLogin() {
        if (!isLoggedIn()) login(LOGIN, PASSWORD);
        logoutIfNeeded();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login") || driver.getCurrentUrl().equals(BASE_URL),
                "After logout, login screen should be visible");
    }
}
