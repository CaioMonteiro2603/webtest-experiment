package geminiPRO.ws08.seq03;

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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * A complete JUnit 5 test suite for the JPetStore demo website using Selenium WebDriver
 * with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreE2ETest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

    // --- Dynamic Test Data ---
    private static String testUserId;
    private static final String TEST_PASSWORD = "password123";
    private static final String FIRST_NAME = "Gemini";

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By SIGN_IN_LINK = By.linkText("Sign In");
    private static final By SIGN_OUT_LINK = By.linkText("Sign Out");
    private static final By REGISTER_NOW_LINK = By.linkText("Register Now!");
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[text()='Login']");
    private static final By WELCOME_CONTENT = By.id("WelcomeContent");
    private static final By SEARCH_INPUT = By.name("keyword");
    private static final By SEARCH_BUTTON = By.xpath("//button[text()='Search']");
    private static final By MYBATIS_LINK = By.xpath("//img[contains(@src, 'mybatis-logo.png')]");
    
    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        testUserId = "gemini" + System.currentTimeMillis();
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_LINK)).click();
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_NOW_LINK)).click();

        // User Information
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(testUserId);
        driver.findElement(PASSWORD_INPUT).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("repeatedPassword")).sendKeys(TEST_PASSWORD);

        // Account Information
        driver.findElement(By.name("account.firstName")).sendKeys(FIRST_NAME);
        driver.findElement(By.name("account.lastName")).sendKeys("Tester");
        driver.findElement(By.name("account.email")).sendKeys("gemini.tester@example.com");
        driver.findElement(By.name("account.phone")).sendKeys("1234567890");
        driver.findElement(By.name("account.address1")).sendKeys("123 Automation St");
        driver.findElement(By.name("account.city")).sendKeys("Testville");
        driver.findElement(By.name("account.state")).sendKeys("CA");
        driver.findElement(By.name("account.zip")).sendKeys("90210");
        driver.findElement(By.name("account.country")).sendKeys("USA");

        // Profile Information
        new Select(driver.findElement(By.name("account.languagePreference"))).selectByVisibleText("english");
        new Select(driver.findElement(By.name("account.favouriteCategoryId"))).selectByVisibleText("DOGS");
        driver.findElement(By.name("account.listOption")).click();

        driver.findElement(By.xpath("//button[text()='Save Account Information']")).click();
        
        // Assert successful registration by checking for the welcome message
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));
        Assertions.assertTrue(welcomeMessage.getText().contains(FIRST_NAME), "Welcome message not found after registration.");
        
        // Logout to set a clean state for the next test
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_OUT_LINK)).click();
    }
    
    @Test
    @Order(2)
    void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(testUserId);
        driver.findElement(PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(LOGIN_BUTTON).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password."), "Error message for invalid login is incorrect.");
    }
    
    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(testUserId);
        driver.findElement(PASSWORD_INPUT).sendKeys(TEST_PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));
        Assertions.assertTrue(welcomeMessage.getText().contains(FIRST_NAME), "Welcome message not found after login.");
        
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_OUT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_IN_LINK));
        Assertions.assertTrue(driver.findElement(SIGN_IN_LINK).isDisplayed(), "Sign In link not visible after logout.");
    }
    
    @Test
    @Order(4)
    void testSearchFunctionality() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT)).sendKeys("Goldfish");
        driver.findElement(SEARCH_BUTTON).click();
        
        WebElement productLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("FI-FW-02")));
        Assertions.assertTrue(productLink.isDisplayed(), "Product link for Goldfish (FI-FW-02) was not found in search results.");
    }

    @Test
    @Order(5)
    void testFullECommerceFlow() {
        // --- 1. Login ---
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(testUserId);
        driver.findElement(PASSWORD_INPUT).sendKeys(TEST_PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_CONTENT));

        // --- 2. Navigate and Add to Cart ---
        driver.get(BASE_URL + "catalog/categories/BIRDS");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("AV-CB-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        
        // --- 3. Verify and Update Cart ---
        WebElement quantityInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("EST-18")));
        Assertions.assertEquals("1", quantityInput.getAttribute("value"), "Initial cart quantity is incorrect.");
        quantityInput.clear();
        quantityInput.sendKeys("3");
        driver.findElement(By.xpath("//button[text()='Update Cart']")).click();
        
        // Wait for quantity to be updated
        wait.until(ExpectedConditions.attributeToBe(By.name("EST-18"), "value", "3"));
        WebElement subTotal = driver.findElement(By.xpath("//td[contains(text(), 'Sub Total: $')]/following-sibling::td"));
        Assertions.assertEquals("$58.50", subTotal.getText(), "Subtotal did not update correctly.");

        // --- 4. Proceed to Checkout ---
        driver.findElement(By.linkText("Proceed to Checkout")).click();
        
        // --- 5. Confirm Order ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Confirm']"))).click();
        
        // --- 6. Verify Success ---
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("info")));
        Assertions.assertTrue(successMessage.getText().contains("Thank you, your order has been submitted."), "Order confirmation message not found.");
    }

    @Test
    @Order(6)
    void testExternalMyBatisLink() {
        driver.get(BASE_URL + "catalog/");
        String originalWindow = driver.getWindowHandle();
        
        wait.until(ExpectedConditions.elementToBeClickable(MYBATIS_LINK)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("mybatis.org"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("mybatis.org"), "The MyBatis link did not navigate to the correct domain.");

        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"), "Did not return to the JPetStore page.");
    }
}