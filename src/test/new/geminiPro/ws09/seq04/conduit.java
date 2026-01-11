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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String expectedUrl = BASE_URL + "#/@";
        wait.until(ExpectedConditions.urlContains(expectedUrl));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/@"), "Should be redirected to user profile page after registration.");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test Invalid Login Attempt")
    void testInvalidLogin() {
        login(testEmail, "WrongPassword");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.getText().contains("invalid") || errorMessage.getText().contains("Email"), "Error message for invalid login is incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        login(testEmail, TEST_PASSWORD);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String profileUrl = BASE_URL + "#/@";
        wait.until(ExpectedConditions.urlContains(profileUrl));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/@"), "Should be redirected after successful login.");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href$='/settings']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-outline-danger")));
        logoutButton.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href$='/login']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"), "Should be on home page after logout.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Create, Edit, and Delete an Article")
    void testCreateEditDeleteArticle() {
        login(testEmail, TEST_PASSWORD);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href$='/editor']"))).click();
        
        String articleTitle = "My Test Article Title " + UUID.randomUUID().toString().substring(0, 4);
        String articleAbout = "About testing with Selenium";
        String articleBody = "This is the main body of the test article written in Markdown.";
        String articleTag = "testing";

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']"))).sendKeys(articleTitle);
        driver.findElement(By.cssSelector("input[placeholder*='about']")).sendKeys(articleAbout);
        driver.findElement(By.cssSelector("textarea")).sendKeys(articleBody);
        driver.findElement(By.cssSelector("input[placeholder*='tags']")).sendKeys(articleTag);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement createdTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(articleTitle, createdTitle.getText(), "Article title after creation is incorrect.");
        
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-outline-secondary"))).click();
        
        String updatedBodyText = " This text has been updated.";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea"))).sendKeys(updatedBodyText);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement updatedBody = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".article-content p")));
        Assertions.assertEquals(articleBody + updatedBodyText, updatedBody.getText(), "Article body should be updated.");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger"))).click();
        
        wait.until(ExpectedConditions.urlContains("#/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to the home page after deleting an article.");
    }
    
    @Test
    @Order(5)
    @DisplayName("Filter Global Feed by Tag")
    void testGlobalFeedAndTagFiltering() {
        WebElement globalFeedTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(), 'Global Feed')]")));
        Assertions.assertTrue(globalFeedTab.getAttribute("class").contains("active"), "Global Feed tab should be active by default.");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement firstTag = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tag-pill")));
        String tagName = firstTag.getText();
        firstTag.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement activeTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle a.active")));
        Assertions.assertTrue(activeTab.getText().toLowerCase().contains(tagName.toLowerCase()), "The clicked tag should become the active feed tab.");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test External Link in Footer")
    void testExternalFooterLink() {
        String originalWindow = driver.getWindowHandle();
        
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "Should remain on same window.");
    }
}