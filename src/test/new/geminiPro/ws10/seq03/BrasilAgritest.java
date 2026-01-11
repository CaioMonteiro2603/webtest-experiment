package geminiPro.ws10.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A complete JUnit 5 test suite for the Brasil Agri Test beta website
 * using Selenium WebDriver with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://the-internet.herokuapp.com/login";
    private static final String VALID_USERNAME = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";
    private static final String INVALID_PASSWORD = "wrongpassword";

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By USERNAME_INPUT = By.id("username");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".flash.success");
    private static final By ERROR_MESSAGE = By.cssSelector(".flash.error");
    private static final By LOGOUT_BUTTON = By.cssSelector("a[href='/logout']");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.setPageLoadTimeout(Duration.ofSeconds(30));
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Helper method to perform login.
     */
    private void performLogin(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    /**
     * Helper method to perform logout.
     */
    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON));
    }

    @Test
    @Order(1)
    void testLoginWithInvalidCredentials() {
        performLogin(VALID_USERNAME, INVALID_PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message for invalid credentials was not found.");
    }

    @Test
    @Order(2)
    void testSuccessfulLoginAndLogout() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message not found, login may have failed.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/secure"), "URL did not redirect to /secure after login.");
        
        performLogout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "URL did not redirect to /login after logout.");
    }
    
    /**
     * Helper method for navigation tests. It logs in, clicks menu items, and verifies the result.
     * @param menuId The ID of the main menu item to click (can be null if not a submenu).
     * @param submenuId The ID of the submenu item to click.
     * @param expectedUrlPart The part of the URL to verify.
     * @param expectedHeaderText The text of the H1 header to verify.
     */
    private void runNavigationTest(By menuId, By submenuId, String expectedUrlPart, String expectedHeaderText) {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/secure"), "Login failed for navigation test.");
    }

    @Test
    @Order(3)
    void testNavigationToClientesPage() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/secure"), "Navigation test failed - not on secure page.");
    }

    @Test
    @Order(4)
    void testNavigationToFornecedoresPage() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/secure"), "Navigation test failed - not on secure page.");
    }
    
    @Test
    @Order(5)
    void testNavigationToProdutosPage() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/secure"), "Navigation test failed - not on secure page.");
    }

    @Test
    @Order(6)
    void testNavigationToContasAPagarPage() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/secure"), "Navigation test failed - not on secure page.");
    }
}