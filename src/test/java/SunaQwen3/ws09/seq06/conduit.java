package SunaQwen3.ws09.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("Demo", driver.getTitle(), "Page title should be 'Demo'");

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(signInButton).click();

        By homeLink = By.xpath("//a[contains(text(), 'conduit')]");
        wait.until(ExpectedConditions.elementToBeClickable(homeLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after login");
        By globalFeed = By.xpath("//a[contains(text(), 'Global Feed')]");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(globalFeed)).isDisplayed(), "Global Feed should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);
        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(signInButton).click();

        By errorDiv = By.cssSelector("div.error-messages");
        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorDiv)).getText();

        assertTrue(errorMessage.contains("email or password is invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationMenuAllItems() {
        // Try to find menu button, but if not present, assume menu is already visible
        List<WebElement> menuButtons = driver.findElements(By.cssSelector("button.navbar-toggler"));
        if (menuButtons.size() > 0) {
            WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButtons.get(0)));
            menuButtonEl.click();
        }

        By allItemsLink = By.xpath("//a[contains(text(), 'All Articles') or contains(text(), 'Home')]");
        WebElement allItemsEl = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItemsEl.click();

        By globalFeedHeader = By.xpath("//a[contains(text(), 'Global Feed')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(globalFeedHeader));

        assertTrue(driver.getCurrentUrl().contains("#/"), "URL should contain #/ after clicking All Articles");
    }
}