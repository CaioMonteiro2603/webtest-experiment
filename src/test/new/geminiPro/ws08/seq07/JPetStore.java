package geminiPro.ws08.seq07;

import org.junit.jupiter.api.AfterAll;
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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for the JPetStore demo application.
 * This suite covers user registration, login, product searching, category navigation,
 * and the complete end-to-end checkout process.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // Unique user credentials generated once for the entire test suite
    private static final String UNIQUE_RUN_ID = String.valueOf(System.currentTimeMillis()).substring(6);
    private static final String USERNAME = "gemini" + UNIQUE_RUN_ID;
    private static final String PASSWORD = "password123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register Now!"))).click();

        // Fill User Information
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.name("repeatedPassword")).sendKeys(PASSWORD);

        // Fill Account Information
        driver.findElement(By.name("account.firstName")).sendKeys("Gemini");
        driver.findElement(By.name("account.lastName")).sendKeys("Tester");
        driver.findElement(By.name("account.email")).sendKeys("gemini.tester@example.com");
        driver.findElement(By.name("account.phone")).sendKeys("555-1234");
        driver.findElement(By.name("account.address1")).sendKeys("123 Test Ave");
        driver.findElement(By.name("account.city")).sendKeys("Testville");
        driver.findElement(By.name("account.state")).sendKeys("TS");
        driver.findElement(By.name("account.zip")).sendKeys("12345");
        driver.findElement(By.name("account.country")).sendKeys("USA");
        
        // Language and Category Preferences
        new Select(driver.findElement(By.name("account.languagePreference"))).selectByVisibleText("english");
        new Select(driver.findElement(By.name("account.favouriteCategoryId"))).selectByVisibleText("DOGS");
        
        driver.findElement(By.name("newAccount")).click();

        // Assert successful login after registration
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));
        assertTrue(welcomeMessage.getText().contains("Welcome Gemini!"), "Welcome message should be displayed after registration.");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        performLogoutIfLoggedIn();
        driver.get(BASE_URL + "account/signonForm");
        performLogin(USERNAME, "wrong-password");

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".messages li")));
        assertEquals("Invalid username or password. Signon failed.", errorMessage.getText(), "Error message for invalid login should be displayed.");
    }

    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        driver.get(BASE_URL + "account/signonForm");
        performLogin(USERNAME, PASSWORD);

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));
        assertTrue(welcomeMessage.getText().contains("Welcome Gemini!"), "Welcome message should be visible after login.");
        assertTrue(driver.findElement(By.linkText("Sign Out")).isDisplayed(), "'Sign Out' link should be visible.");

        driver.findElement(By.linkText("Sign Out")).click();

        WebElement signInLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign In")));
        assertTrue(signInLink.isDisplayed(), "'Sign In' link should be visible after logout.");
    }

    @Test
    @Order(4)
    void testProductSearch() {
        driver.get(BASE_URL + "catalog/");
        String searchTerm = "Goldfish";
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.sendKeys(searchTerm);
        driver.findElement(By.xpath("//input[@value='Search']")).click();

        WebElement resultsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Catalog")));
        assertTrue(resultsTable.getText().contains(searchTerm), "Search results table should contain the search term.");
    }

    @Test
    @Order(5)
    void testNavigateByCategory() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='SidebarContent']/a[contains(@href, 'DOGS')]"))).click();

        wait.until(ExpectedConditions.urlContains("categoryId=DOGS"));
        WebElement categoryHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Catalog")));
        assertTrue(categoryHeader.getText().startsWith("Dogs"), "Page header should indicate the 'Dogs' category.");
    }
    
    @Test
    @Order(6)
    void testFullCheckoutFlow() {
        // Step 1: Login
        driver.get(BASE_URL + "account/signonForm");
        performLogin(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));

        // Step 2: Search for an item and add to cart
        driver.findElement(By.name("keyword")).sendKeys("Manx");
        driver.findElement(By.xpath("//input[@value='Search']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-14"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        
        // Step 3: Proceed to Checkout
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Shopping Cart']")));
        driver.findElement(By.linkText("Proceed to Checkout")).click();
        
        // Step 4: Continue through payment details
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("newOrder"))).click();
        
        // Step 5: Confirm the order
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[contains(text(),'Payment Details')]")));
        driver.findElement(By.linkText("Confirm")).click();
        
        // Step 6: Assert success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".messages li")));
        assertEquals("Thank you, your order has been submitted.", successMessage.getText(), "Order confirmation message should be displayed.");
    }
    
    // --- Helper Methods ---

    private void performLogin(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.clear();
        usernameField.sendKeys(username);
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.clear();
        passwordField.sendKeys(password);
        driver.findElement(By.name("signon")).submit();
    }
    
    private void performLogoutIfLoggedIn() {
        // Use findElements to avoid an exception if the element doesn't exist.
        if (!driver.findElements(By.linkText("Sign Out")).isEmpty()) {
            driver.findElement(By.linkText("Sign Out")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign In")));
        }
    }
}