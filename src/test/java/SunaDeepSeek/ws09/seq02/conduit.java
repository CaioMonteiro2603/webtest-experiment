package SunaDeepSeek.ws09.seq02;

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
public class RealWorldWebTest {
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("testuser@example.com");
        passwordField.sendKeys("password");
        signInButton.click();
        
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.nav-link[href='/settings']")).isDisplayed());
        
        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#logout']")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        
        // Click on first article
        List<WebElement> articles = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("div.article-preview"), 0));
        String articleTitle = articles.get(0).findElement(By.tagName("h1")).getText();
        articles.get(0).click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        Assertions.assertEquals(articleTitle, driver.findElement(By.cssSelector("div.banner h1")).getText());
        
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
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://twitter.com/conduit']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://github.com/gothinkster/realworld']")));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testTagNavigation() {
        driver.get(BASE_URL);
        
        // Get first tag
        List<WebElement> tags = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("div.tag-list a.tag-pill"), 0));
        String tagText = tags.get(0).getText();
        tags.get(0).click();
        
        wait.until(ExpectedConditions.urlContains("/tag/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.feed-toggle li.active")).getText().contains(tagText));
        
        // Verify articles are filtered by tag
        List<WebElement> filteredArticles = driver.findElements(By.cssSelector("div.article-preview"));
        if (filteredArticles.size() > 0) {
            Assertions.assertTrue(filteredArticles.get(0).getText().contains(tagText));
        }
    }
}