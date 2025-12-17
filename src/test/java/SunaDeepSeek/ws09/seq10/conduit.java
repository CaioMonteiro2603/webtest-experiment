package SunaDeepSeek.ws09.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser123";
    private static final String PASSWORD = "test123";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("realworld"));
        Assertions.assertTrue(driver.getTitle().contains("Conduit"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/']")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("/"));
        
        // Test Sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/login']")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        
        // Test Sign up link
        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/register']")));
        signUpLink.click();
        wait.until(ExpectedConditions.urlContains("/register"));
    }

    @Test
    @Order(3)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        WebElement userProfile = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/@" + USERNAME + "']")));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        testSuccessfulLogin(); // Ensure logged in
        
        List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("article-preview")));
        if (articles.size() > 0) {
            WebElement firstArticle = articles.get(0).findElement(By.cssSelector("a.preview-link"));
            String articleTitle = firstArticle.findElement(By.tagName("h1")).getText();
            firstArticle.click();
            
            wait.until(ExpectedConditions.urlContains("/article/"));
            WebElement articleHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
            Assertions.assertEquals(articleTitle, articleHeader.getText());
        }
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("a[href*='twitter']", "twitter.com");
        
        // Test GitHub link
        testExternalLink("a[href*='github']", "github.com");
    }

    private void testExternalLink(String linkSelector, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(linkSelector)));
        externalLink.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
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
        testSuccessfulLogin(); // Ensure logged in
        
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/@" + USERNAME + "']")));
        profileLink.click();
        
        wait.until(ExpectedConditions.urlContains("/@" + USERNAME));
        WebElement profileHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h4")));
        Assertions.assertTrue(profileHeader.getText().contains(USERNAME));
    }

    @Test
    @Order(8)
    public void testLogout() {
        testSuccessfulLogin(); // Ensure logged in
        
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/settings']")));
        settingsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/settings"));
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/login']")));
        Assertions.assertTrue(signInLink.isDisplayed());
    }
}