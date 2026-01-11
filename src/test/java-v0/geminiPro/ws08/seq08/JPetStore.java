package geminiPro.ws08.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    // User credentials will be generated for this test run
    private static String testUsername;
    private static String testPassword;

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By enterStoreLink = By.linkText("Enter the Store");
    private final By signInLink = By.linkText("Sign In");
    private final By usernameInput = By.name("username");
    private final By passwordInput = By.name("password");
    private final By loginButton = By.name("signon");
    private final By signOutLink = By.linkText("Sign Out");
    private final By myAccountLink = By.linkText("My Account");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);

        long timestamp = System.currentTimeMillis();
        testUsername = "testuser" + timestamp;
        testPassword = "password123";
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToCatalog() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(enterStoreLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Sidebar")));
    }

    private void login(String username, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).clear();
        driver.findElement(usernameInput).sendKeys(username);
        driver.findElement(passwordInput).clear();
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    @Test
    @Order(1)
    @DisplayName("Test User Registration")
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register Now!"))).click();

        // Account Information
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(testUsername);
        driver.findElement(By.name("password")).sendKeys(testPassword);
        driver.findElement(By.name("repeatedPassword")).sendKeys(testPassword);

        // Profile Information
        driver.findElement(By.name("account.firstName")).sendKeys("Gemini");
        driver.findElement(By.name("account.lastName")).sendKeys("Pro");
        driver.findElement(By.name("account.email")).sendKeys("gemini.pro@test.com");
        driver.findElement(By.name("account.phone")).sendKeys("555-1234");
        driver.findElement(By.name("account.address1")).sendKeys("123 AI Lane");
        driver.findElement(By.name("account.city")).sendKeys("Googleville");
        driver.findElement(By.name("account.state")).sendKeys("CA");
        driver.findElement(By.name("account.zip")).sendKeys("94043");
        driver.findElement(By.name("account.country")).sendKeys("USA");
        new Select(driver.findElement(By.name("account.languagePreference"))).selectByValue("english");
        new Select(driver.findElement(By.name("account.favouriteCategoryId"))).selectByValue("DOGS");
        
        driver.findElement(By.name("newAccount")).click();
        
        // After registration, user is logged in. Verify by checking for "Sign Out" link.
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(signOutLink)).isDisplayed(),
            "User should be logged in and see the Sign Out link after registration.");
        
        wait.until(ExpectedConditions.elementToBeClickable(signOutLink)).click();
    }

    @Test
    @Order(2)
    @DisplayName("Test Login with Invalid Credentials")
    void testLoginWithInvalidCredentials() {
        login("invalidUser", "wrongPassword");
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages")));
        assertTrue(message.getText().contains("Invalid username or password."), "Error message should be displayed for invalid login.");
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        assertNotNull(testUsername, "Registration test must run first.");
        login(testUsername, testPassword);
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(signOutLink)).isDisplayed(), "Sign Out link should be visible after login.");
        wait.until(ExpectedConditions.elementToBeClickable(signOutLink)).click();
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(signInLink)).isDisplayed(), "Sign In link should be visible after logout.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test Product Search")
    void testProductSearch() {
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.sendKeys("Dalmation");
        driver.findElement(By.name("searchProducts")).click();

        WebElement resultTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Catalog")));
        assertTrue(resultTable.getText().contains("Spotted Adult Female Dalmation"), "Search results should contain the searched pet.");
    }

    @Test
    @Order(5)
    @DisplayName("Test End-to-End Purchase Flow")
    void testEndToEndPurchaseFlow() {
        assertNotNull(testUsername, "Registration test must run first.");
        login(testUsername, testPassword);

        // 1. Select a pet category and product
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='SidebarContent']/a[contains(@href, 'FISH')]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-1"))).click();
        
        // 2. Add to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        
        // 3. Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();
        
        // 4. Continue through shipping (form is pre-filled for logged-in user)
        wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder"))).click();

        // 5. Confirm order
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Confirm"))).click();
        
        // 6. Assert success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(text(), 'Thank you, your order has been submitted.')]")));
        assertTrue(successMessage.isDisplayed(), "Order confirmation message should be displayed.");
    }

    @Test
    @Order(6)
    @DisplayName("Test My Account Information")
    void testMyAccountInformation() {
        assertNotNull(testUsername, "Registration test must run first.");
        login(testUsername, testPassword);
        wait.until(ExpectedConditions.elementToBeClickable(myAccountLink)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        
        // Verify that the user ID field on the account page matches the registered username
        WebElement userIdField = driver.findElement(By.name("username"));
        assertEquals(testUsername, userIdField.getAttribute("value"), "Username in account details should match the logged-in user.");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test External MyBatis Link in Footer")
    void testExternalMyBatisLink() {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("www.mybatis.org")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();

        driver.switchTo().window(newWindow);
        
        assertTrue(wait.until(ExpectedConditions.urlContains("mybatis.org")), "URL of the new tab should contain 'mybatis.org'");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertTrue(driver.findElement(By.id("Main")).isDisplayed(), "Should have switched back to the JPetStore main page.");
    }
}