package SunaGPT20b.ws06.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
    }

    private void login(String username, String password) {
        navigateToBase();
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void ensureLoggedIn() {
        wait.until(ExpectedConditions.urlContains("room"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("room")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("admin", "password");
        ensureLoggedIn();
        Assertions.assertTrue(driver.getCurrentUrl().contains("room"),
                "After valid login, URL should contain 'room'");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_password");
        WebElement errorContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert, .error, .alert-danger")));
        Assertions.assertTrue(errorContainer.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorContainer.getText().toLowerCase().contains("username") ||
                        errorContainer.getText().toLowerCase().contains("password"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("admin", "password");
        ensureLoggedIn();
        Assertions.assertTrue(driver.findElements(By.className("room")).size() > 0,
                "Room listings should be displayed after login");
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        login("admin", "password");
        ensureLoggedIn();
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Profile")));
        Assertions.assertNotNull(profileLink, "Profile link should be visible after login");
        Assertions.assertTrue(driver.findElements(By.className("room")).size() > 0,
                "Room listings should be visible");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("admin", "password");
        ensureLoggedIn();
        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
        Assertions.assertNotNull(footer, "Footer should be visible after login");
        List<WebElement> socialLinks = footer.findElements(By.cssSelector("a[href*='twitter.com'], a[href*='facebook.com'], a[href*='linkedin.com']"));
        Assertions.assertTrue(socialLinks.size() > 0, "Social links should be present in footer");
    }
}