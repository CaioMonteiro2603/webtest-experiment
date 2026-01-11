package SunaDeepSeek.ws09.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser123";
    private static final String PASSWORD = "test123";

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
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div.banner h1")));
        Assertions.assertEquals("Home — Conduit", driver.getTitle());
        Assertions.assertTrue(driver.getCurrentUrl().contains(BASE_URL));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.nav-link[routerLink='/']")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        
        // Test Sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.nav-link[href='#/login']")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "#/login");
        
        // Valid login
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/#/"));
        WebElement userProfile = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.nav-link[href='#/profile")));
        Assertions.assertTrue(userProfile.isDisplayed());
        
        // Logout
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.nav-link[href='#/settings']")));
        settingsLink.click();
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("/#/"));
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("ul.error-messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        // Login first
        driver.get(BASE_URL + "#/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/#/"));
        
        // Navigate to global feed
        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.nav-link[href='#/']")));
        globalFeed.click();
        wait.until(ExpectedConditions.urlContains("/#/"));
        
        // Click on first article
        List<WebElement> articles = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector("div.article-preview"), 0));
        if (!articles.isEmpty()) {
            WebElement firstArticle = articles.get(0).findElement(By.cssSelector("h1"));
            String articleTitle = firstArticle.getText();
            firstArticle.click();
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.article-page")));
            WebElement articleTitleOnPage = driver.findElement(By.cssSelector("div.banner h1"));
            Assertions.assertEquals(articleTitle, articleTitleOnPage.getText());
        }
        
        // Logout
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.nav-link[href='#/settings']")));
        settingsLink.click();
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("/#/"));
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
        
        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(linkSelector)));
        externalLink.click();
        
        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close tab
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("about:blank")));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}