package geminiPro.ws09.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

/**
 * A comprehensive JUnit 5 test suite for the RealWorld demo application "Conduit".
 * It uses Selenium WebDriver with Firefox in headless mode to test user registration,
 * login, article creation/editing/deletion, feed navigation, and external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

    // Unique user credentials for the test run to ensure test independence
    private static String testUsername;
    private static String testEmail;
    private static final String TEST_PASSWORD = "Password123!";

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Generate a unique user for each full test suite run
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        testUsername = "tester-" + uniqueId;
        testEmail = "tester-" + uniqueId + "@example.com";
    }

    @AfterAll
    static void teardownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
        // The app sometimes loads with a hash, wait for the main container.
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home-page")));
    }
    
    private void login(String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href$='/login']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']"))).sendKeys(email);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Test
    @Order(1)
    @DisplayName("Test New User Registration")
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href$='/register']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Username']"))).sendKeys(testUsername);
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(testEmail);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement userProfileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(@href, '#/@" + testUsername + "')]")));

        Assertions.assertTrue(userProfileLink.isDisplayed(), "User profile link should be visible in the nav bar after registration.");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test Invalid Login Attempt")
    void testInvalidLogin() {
        login(testEmail, "WrongPassword");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertEquals("Email or password is invalid", errorMessage.getText(), "Error message for invalid login is incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        login(testEmail, TEST_PASSWORD);
        WebElement userProfileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(@href, '#/@" + testUsername + "')]")));
        Assertions.assertTrue(userProfileLink.isDisplayed(), "User should be logged in successfully.");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href$='/settings']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1"))); // Wait for settings page to load
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-outline-danger")));
        logoutButton.click();

        WebElement signInLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href$='/login']")));
        Assertions.assertTrue(signInLink.isDisplayed(), "User should be logged out and Sign In link should be visible.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Create, Edit, and Delete an Article")
    void testCreateEditDeleteArticle() {
        // --- 1. Login ---
        login(testEmail, TEST_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href$='/editor']")));

        // --- 2. Create Article ---
        driver.findElement(By.cssSelector("a[href$='/editor']")).click();
        
        String articleTitle = "My Test Article Title " + UUID.randomUUID().toString().substring(0, 4);
        String articleAbout = "About testing with Selenium";
        String articleBody = "This is the main body of the test article written in Markdown.";
        String articleTag = "testing";

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']"))).sendKeys(articleTitle);
        driver.findElement(By.cssSelector("input[placeholder*='about']")).sendKeys(articleAbout);
        driver.findElement(By.cssSelector("textarea")).sendKeys(articleBody);
        driver.findElement(By.cssSelector("input[placeholder*='tags']")).sendKeys(articleTag);
        driver.findElement(By.cssSelector("button[type='button']")).click();
        
        // --- 3. Verify Creation ---
        WebElement createdTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(articleTitle, createdTitle.getText(), "Article title after creation is incorrect.");
        WebElement createdBody = driver.findElement(By.xpath("//div[contains(@class, 'article-content')]//p"));
        Assertions.assertEquals(articleBody, createdBody.getText(), "Article body after creation is incorrect.");
        
        // --- 4. Edit Article ---
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-outline-secondary"))).click();
        
        String updatedBodyText = " This text has been updated.";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea"))).sendKeys(updatedBodyText);
        driver.findElement(By.cssSelector("button[type='button']")).click();
        
        // --- 5. Verify Edit ---
        WebElement updatedBody = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'article-content')]//p")));
        Assertions.assertEquals(articleBody + updatedBodyText, updatedBody.getText(), "Article body should be updated.");

        // --- 6. Delete Article ---
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger"))).click();
        
        // Wait for redirection to the home page after deletion
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("#/"), "Should be redirected to the home page after deleting an article.");
    }
    
    @Test
    @Order(5)
    @DisplayName("Filter Global Feed by Tag")
    void testGlobalFeedAndTagFiltering() {
        // No login needed for this test
        WebElement globalFeedTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(), 'Global Feed')]")));
        Assertions.assertTrue(globalFeedTab.getAttribute("class").contains("active"), "Global Feed tab should be active by default.");
        
        // Find the first available tag and click it
        WebElement firstTag = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar .tag-pill")));
        String tagName = firstTag.getText();
        firstTag.click();

        // Assert that the tag is now the active feed tab
        WebElement activeTagTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@class, 'active') and contains(text(), '" + tagName + "')]")));
        Assertions.assertTrue(activeTagTab.isDisplayed(), "The clicked tag should become the active feed tab.");
        Assertions.assertFalse(globalFeedTab.getAttribute("class").contains("active"), "Global Feed tab should no longer be active.");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test External Link in Footer")
    void testExternalFooterLink() {
        String originalWindow = driver.getWindowHandle();
        
        WebElement thinksterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.logo-font[href='https://thinkster.io']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", thinksterLink);
        thinksterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String newWindow = driver.getWindowHandles().stream()
                .filter(handle -> !handle.equals(originalWindow))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New window did not open."));

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("thinkster.io"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("thinkster.io"), "New window URL should contain 'thinkster.io'.");

        driver.close();
        driver.switchTo().window(originalWindow);

        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should have returned to the original RealWorld app.");
    }
}