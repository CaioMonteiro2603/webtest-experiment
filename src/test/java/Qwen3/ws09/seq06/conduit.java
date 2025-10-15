package Qwen3.ws09.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class RealWorldTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testHomePageLoadsCorrectly() {
        driver.get("https://demo.realworld.io/");
        
        String currentPageTitle = driver.getTitle();
        assertTrue(currentPageTitle.contains("RealWorld"), "Page title should contain 'RealWorld'");
        
        WebElement mainHeader = driver.findElement(By.tagName("h1"));
        assertTrue(mainHeader.getText().contains("RealWorld"), "Main header should contain RealWorld");
        
        // Check if navigation menu is present
        WebElement navigationMenu = driver.findElement(By.cssSelector("nav"));
        assertTrue(navigationMenu.isDisplayed(), "Navigation menu should be displayed");
        
        // Check if articles section is present
        WebElement articlesSection = driver.findElement(By.cssSelector(".articles"));
        assertTrue(articlesSection.isDisplayed(), "Articles section should be displayed");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get("https://demo.realworld.io/");
        
        // Click on 'Sign In' link
        WebElement signInLink = driver.findElement(By.linkText("Sign In"));
        signInLink.click();
        
        // Wait for login form to appear
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[type='email']")));
        
        // Fill in login credentials
        emailField.sendKeys("jane@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("[type='password']"));
        passwordField.sendKeys("password");
        
        // Submit login form
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Verify successful login
        wait.until(ExpectedConditions.urlContains("home"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("home"), "Should be on home page after login");
        
        WebElement userGreeting = driver.findElement(By.cssSelector(".user-greeting"));
        assertTrue(userGreeting.isDisplayed(), "User greeting should be displayed");
    }

    @Test
    @Order(3)
    public void testInvalidLoginError() {
        driver.get("https://demo.realworld.io/");
        
        // Click on 'Sign In' link
        WebElement signInLink = driver.findElement(By.linkText("Sign In"));
        signInLink.click();
        
        // Wait for login form to appear
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[type='email']")));
        
        // Fill in invalid credentials
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("[type='password']"));
        passwordField.sendKeys("wrongpassword");
        
        // Submit login form
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Check for error message
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(4)
    public void testNavigationAndMenuFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Click on 'Sign In' and login for navigation tests
        WebElement signInLink = driver.findElement(By.linkText("Sign In"));
        signInLink.click();
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[type='email']")));
        emailField.sendKeys("jane@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("[type='password']"));
        passwordField.sendKeys("password");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for the page to load after login
        wait.until(ExpectedConditions.urlContains("home"));
        
        // Test menu navigation
        WebElement menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        
        // Click on 'Home' menu item
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("home"), "Should navigate to home page");
        
        // Navigate to 'Profile' page
        menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement profileLink = driver.findElement(By.linkText("Profile"));
        profileLink.click();
        
        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("profile"), "Should navigate to profile page");
        
        // Go back to home page
        driver.get("https://demo.realworld.io/home");
        
        // Click on 'Sign Out'
        menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement signOutLink = driver.findElement(By.linkText("Sign Out"));
        signOutLink.click();
        
        // Verify logout successful
        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("home"), "Should be back on home page after logout");
    }

    @Test
    @Order(5)
    public void testArticleFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Click on 'Sign In' and login
        WebElement signInLink = driver.findElement(By.linkText("Sign In"));
        signInLink.click();
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[type='email']")));
        emailField.sendKeys("jane@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("[type='password']"));
        passwordField.sendKeys("password");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for the page to load
        wait.until(ExpectedConditions.urlContains("home"));
        
        // Check articles are displayed
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articles.size() > 0, "Should have at least one article displayed");
        
        // Click on first article
        if (!articles.isEmpty()) {
            articles.get(0).click();
            
            // Verify we are on article page
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("article"), "Should navigate to article page");
            
            // Go back to home page
            driver.get("https://demo.realworld.io/home");
        }
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://demo.realworld.io/");
        
        // Wait for footer to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));
        
        // Check for external links in footer
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("github") || href.contains("twitter") || href.contains("facebook"))) {
                // These are external links we want to test
                String oldTab = driver.getWindowHandle();
                link.click();
                String winHandle = driver.getWindowHandle();
                driver.switchTo().window(winHandle);
                
                // Verify we navigated to expected domain
                if (href.contains("github")) {
                    assertTrue(driver.getCurrentUrl().contains("github.com"), 
                              "GitHub link should navigate to GitHub website");
                } else if (href.contains("twitter")) {
                    assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
                              "Twitter link should navigate to Twitter website");
                } else if (href.contains("facebook")) {
                    assertTrue(driver.getCurrentUrl().contains("facebook.com"), 
                              "Facebook link should navigate to Facebook website");
                }
                
                driver.close();
                driver.switchTo().window(oldTab);
            }
        }
    }

    @Test
    @Order(7)
    public void testUserProfileAndView() {
        driver.get("https://demo.realworld.io/");
        
        // Click on 'Sign In' and login
        WebElement signInLink = driver.findElement(By.linkText("Sign In"));
        signInLink.click();
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[type='email']")));
        emailField.sendKeys("jane@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("[type='password']"));
        passwordField.sendKeys("password");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for the page to load
        wait.until(ExpectedConditions.urlContains("home"));
        
        // Navigate to profile page
        WebElement menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement profileLink = driver.findElement(By.linkText("Profile"));
        profileLink.click();
        
        // Check profile page elements
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("profile"), "Should be on profile page");
        
        WebElement profileHeader = driver.findElement(By.tagName("h1"));
        assertTrue(profileHeader.getText().contains("Profile"), "Profile header should be displayed");
        
        // Go back to home page
        driver.get("https://demo.realworld.io/home");
    }
}