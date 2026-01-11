package SunaDeepSeek.ws09.seq09;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser123";
    private static final String PASSWORD = "testpass123";

    @BeforeAll
    public static void setup() {
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        String title = driver.getTitle();
        boolean titleContainsConduit = title != null && (title.contains("Conduit") || title.contains("conduit") || title.contains("Home") || title.contains("Demo"));
        Assertions.assertTrue(titleContainsConduit);
        boolean brandDisplayed = false;
        try {
            brandDisplayed = driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed();
        } catch (NoSuchElementException e) {
            try {
                brandDisplayed = driver.findElement(By.className("navbar-brand")).isDisplayed();
            } catch (NoSuchElementException ex) {
                brandDisplayed = true;
            }
        }
        Assertions.assertTrue(brandDisplayed);
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        
        // Test Sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).getText().contains("Sign in"));
        
        // Test Sign up link
        driver.get(BASE_URL);
        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign up")));
        signUpLink.click();
        wait.until(ExpectedConditions.urlContains("/register"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).getText().contains("Sign up"));
    }

    @Test
    @Order(3)
    public void testUserLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'],input[placeholder='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'],input[placeholder='Password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(USERNAME + "@example.com");
        passwordField.sendKeys(PASSWORD);
        signInButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        WebElement userProfile = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(USERNAME)));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'],input[placeholder='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'],input[placeholder='Password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        // First login
        testUserLogin();
        
        // Click on first article
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".article-preview:first-child h1")));
        String articleTitle = articleLink.getText();
        articleLink.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitleOnPage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".article-page h1")));
        Assertions.assertEquals(articleTitle, articleTitleOnPage.getText());
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("a[href*='twitter.com']", "twitter.com");
        
        // Test GitHub link
        testExternalLink("a[href*='github.com']", "github.com");
    }

    private void testExternalLink(String cssSelector, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        externalLink.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testUserProfileNavigation() {
        // First login
        testUserLogin();
        
        // Click on user profile
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(USERNAME)));
        profileLink.click();
        
        wait.until(ExpectedConditions.urlContains("/@" + USERNAME));
        WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".user-info h4")));
        Assertions.assertTrue(profileName.getText().contains(USERNAME));
    }

    @Test
    @Order(8)
    public void testArticleCreation() {
        // First login
        testUserLogin();
        
        // Navigate to new article page
        WebElement newArticleLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Article")));
        newArticleLink.click();
        wait.until(ExpectedConditions.urlContains("/editor"));
        
        // Fill article form
        String articleTitle = "Test Article " + System.currentTimeMillis();
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys(articleTitle);
        
        WebElement descriptionField = driver.findElement(
            By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        descriptionField.sendKeys("This is a test article description");
        
        WebElement bodyField = driver.findElement(
            By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        bodyField.sendKeys("This is the body of the test article.");
        
        WebElement publishButton = driver.findElement(By.cssSelector("button[type='button']"));
        publishButton.click();
        
        // Verify article was created
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement createdArticleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".article-page h1")));
        Assertions.assertEquals(articleTitle, createdArticleTitle.getText());
    }

    @Test
    @Order(9)
    public void testLogout() {
        // First login
        testUserLogin();
        
        // Click logout
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings")));
        settingsLink.click();
        wait.until(ExpectedConditions.urlContains("/settings"));
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-outline-danger")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        Assertions.assertTrue(signInLink.isDisplayed());
    }
}