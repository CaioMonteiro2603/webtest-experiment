package SunaDeepSeek.ws09.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RealWorldIOTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains(BASE_URL));
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
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        
        // Test Sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/login']")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).getText().contains("Sign in"));
        
        // Test Sign up link
        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/register']")));
        signUpLink.click();
        wait.until(ExpectedConditions.urlContains("/register"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).getText().contains("Sign up"));
    }

    @Test
    @Order(3)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "login");
        
        // Valid login
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.nav-link[href*='@']")).isDisplayed());
        
        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#settings']")));
        logoutLink.click();
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger"));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("/"));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        
        // Click on first article
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.preview-link:first-child h1")));
        String articleTitle = articleLink.getText();
        articleLink.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        Assertions.assertEquals(articleTitle, 
            driver.findElement(By.cssSelector("h1")).getText());
        
        // Go back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testTagNavigation() {
        driver.get(BASE_URL);
        
        // Get first tag
        WebElement firstTag = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.tag-pill:first-child")));
        String tagText = firstTag.getText();
        firstTag.click();
        
        wait.until(ExpectedConditions.urlContains("/tag/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".feed-toggle li.active"))
            .getText().contains(tagText));
    }
}