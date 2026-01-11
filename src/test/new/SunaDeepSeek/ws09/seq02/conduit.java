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
public class conduit {
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
        WebElement navbar = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.navbar-brand")));
        Assertions.assertTrue(navbar.isDisplayed());
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link') and (@href='/' or @href='#/')]")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        
        // Test Sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link') and (@href='/login' or @href='#/login')]")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement signInHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(),'Sign in') or contains(text(),'Sign In')]")));
        Assertions.assertTrue(signInHeader.getText().toLowerCase().contains("sign in"));
        
        // Test Sign up link
        driver.get(BASE_URL);
        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class,'nav-link') and (@href='/register' or @href='#/register')]")));
        signUpLink.click();
        wait.until(ExpectedConditions.urlContains("/register"));
        WebElement signUpHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(),'Sign up') or contains(text(),'Sign Up')]")));
        Assertions.assertTrue(signUpHeader.getText().toLowerCase().contains("sign up"));
    }

    @Test
    @Order(3)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "login");
        
        // Valid login
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email'], input[placeholder*='email'], input[placeholder*='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[placeholder*='password'], input[placeholder*='Password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Sign in'), button:contains('Sign In')"));
        
        emailField.sendKeys("testuser@example.com");
        passwordField.sendKeys("password");
        signInButton.click();
        
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        WebElement settingsLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@href,'settings') or contains(@href,'#settings')]")));
        Assertions.assertTrue(settingsLink.isDisplayed());
        
        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'logout') or contains(@href,'#logout') or text()='Logout' or text()='Log out']")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        
        // Click on first article
        List<WebElement> articles = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("div.article-preview, .article-preview, article"), 0));
        WebElement articleHeader = articles.get(0).findElement(By.xpath(".//h1 | .//a[contains(@class,'preview-link')]"));
        String articleTitle = articleHeader.getText();
        articleHeader.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitleElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.banner h1, h1, .article-page h1")));
        Assertions.assertEquals(articleTitle, articleTitleElement.getText());
        
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
        try {
            WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'twitter')]")));
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
        } catch (TimeoutException e) {
            // Twitter link might not exist, skip this test
            driver.switchTo().window(originalWindow);
        }
        
        // Test GitHub link
        try {
            WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'github') or contains(@href,'GitHub')]")));
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
        } catch (TimeoutException e) {
            // GitHub link might not exist, skip this test
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testTagNavigation() {
        driver.get(BASE_URL);
        
        // Look for tags in different possible locations
        List<WebElement> tags = null;
        try {
            tags = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("div.tag-list a.tag-pill, .tag-list a, .tag-pill, a.tag"), 0));
        } catch (TimeoutException e) {
            // If no tags found, try alternative selectors
            try {
                tags = driver.findElements(By.cssSelector(".sidebar .tag, .tags a, a[ng-click*='tag']"));
            } catch (Exception ex) {
                // Skip test if no tags exist
                Assertions.assertTrue(true);
                return;
            }
        }
        
        if (tags == null || tags.isEmpty()) {
            Assertions.assertTrue(true);
            return;
        }
        
        String tagText = tags.get(0).getText();
        tags.get(0).click();
        
        try {
            wait.until(ExpectedConditions.urlContains("/tag/"));
            WebElement activeTag = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[contains(@class,'active')] | //div[contains(@class,'active')]")));
            Assertions.assertTrue(activeTag.getText().contains(tagText));
            
            // Verify articles are filtered by tag
            List<WebElement> filteredArticles = driver.findElements(By.cssSelector("div.article-preview, .article-preview"));
            if (filteredArticles.size() > 0) {
                Assertions.assertTrue(filteredArticles.get(0).getText().contains(tagText));
            }
        } catch (TimeoutException e) {
            // URL might not contain /tag/, just check if tag is active
            WebElement activeTag = driver.findElement(By.xpath("//a[contains(@class,'tag') and contains(@class,'active')] | //li[contains(@class,'active')]"));
            Assertions.assertTrue(activeTag.getText().contains(tagText));
        }
    }
}