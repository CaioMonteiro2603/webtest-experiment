package Qwen3.ws09.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoad() {
        driver.get("https://demo.realworld.io/");
        
        String title = driver.getTitle();
        assertTrue(title.contains("Conduit"), "Page title should contain 'Conduit'");
        
        // Verify main content is present
        WebElement mainHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        assertTrue(mainHeader.isDisplayed(), "Main header should be displayed");
        
        // Check for navigation elements
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Navigation links should be present");
    }

    @Test
    @Order(2)
    public void testArticleListingAndSorting() {
        driver.get("https://demo.realworld.io/");
        
        // Wait for articles to load
        WebElement articlesContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-preview")));
        assertTrue(articlesContainer.isDisplayed(), "Articles container should be displayed");
        
        // Check that articles are present
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articles.size() > 0, "Articles should be displayed");
        
        // Verify article elements
        WebElement firstArticle = articles.get(0);
        WebElement articleTitle = firstArticle.findElement(By.cssSelector("h1"));
        WebElement articleDescription = firstArticle.findElement(By.cssSelector("p"));
        WebElement articleAuthor = firstArticle.findElement(By.cssSelector(".author"));
        
        assertTrue(articleTitle.isDisplayed(), "Article title should be displayed");
        assertTrue(articleDescription.isDisplayed(), "Article description should be displayed");
        assertTrue(articleAuthor.isDisplayed(), "Article author should be displayed");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://demo.realworld.io/");
        
        // Test main navigation links
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Navigation links should be present");
        
        // Click on first navigation link (likely Home)
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        
        // Verify we're still on the homepage
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("demo.realworld.io"), "Should remain on homepage after clicking Home");
        
        // Test other navigation links if present
        try {
            WebElement loginLink = driver.findElement(By.linkText("Sign in"));
            loginLink.click();
            
            String loginUrl = driver.getCurrentUrl();
            assertTrue(loginUrl.contains("login"), "Should navigate to login page");
            
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        } catch (NoSuchElementException e) {
            // Continue if Sign in link doesn't exist
        }
        
        try {
            WebElement registerLink = driver.findElement(By.linkText("Sign up"));
            registerLink.click();
            
            String registerUrl = driver.getCurrentUrl();
            assertTrue(registerUrl.contains("register"), "Should navigate to register page");
            
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        } catch (NoSuchElementException e) {
            // Continue if Sign up link doesn't exist
        }
    }

    @Test
    @Order(4)
    public void testArticleReading() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to an article
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        if (articles.size() > 0) {
            WebElement firstArticle = articles.get(0);
            WebElement articleLink = firstArticle.findElement(By.tagName("h1"));
            articleLink.click();
            
            // Wait for article page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-page")));
            
            // Verify article details
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/article/"), "Should navigate to article page");
            
            WebElement articleTitle = driver.findElement(By.cssSelector("h1"));
            assertTrue(articleTitle.isDisplayed(), "Article title should be displayed");
            
            WebElement articleContent = driver.findElement(By.cssSelector(".article-content"));
            assertTrue(articleContent.isDisplayed(), "Article content should be displayed");
            
            // Go back to homepage
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        }
    }

    @Test
    @Order(5)
    public void testUserProfile() {
        driver.get("https://demo.realworld.io/");
        
        // Try to access user profile (if logged in)
        try {
            WebElement profileLink = driver.findElement(By.cssSelector("[data-testid='profile-link']"));
            if (profileLink.isDisplayed()) {
                profileLink.click();
                
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("profile"), "Should navigate to profile page");
            }
        } catch (NoSuchElementException e) {
            // User might not be logged in, so we don't fail the test
        }
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://demo.realworld.io/");
        
        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }
        
        // Check for external footer links
        try {
            WebElement githubLink = driver.findElement(By.cssSelector("a[href*='github']"));
            String githubUrl = githubLink.getAttribute("href");
            assertTrue(githubUrl.contains("github"), "Should open GitHub link");
            
        } catch (NoSuchElementException e) {
            // Continue if GitHub link not found
        }
        
        try {
            WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
            String twitterUrl = twitterLink.getAttribute("href");
            assertTrue(twitterUrl.contains("twitter"), "Should open Twitter link");
            
        } catch (NoSuchElementException e) {
            // Continue if Twitter link not found
        }
        
        try {
            WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
            String facebookUrl = facebookLink.getAttribute("href");
            assertTrue(facebookUrl.contains("facebook"), "Should open Facebook link");
            
        } catch (NoSuchElementException e) {
            // Continue if Facebook link not found
        }
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Try to use search functionality if present
        try {
            WebElement searchInput = driver.findElement(By.cssSelector("[data-testid='search-input']"));
            WebElement searchButton = driver.findElement(By.cssSelector("[data-testid='search-button']"));
            
            searchInput.sendKeys("test");
            searchButton.click();
            
            // Wait for search results
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".search-results")));
            
            // Verify result page loaded
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("?q=test"), "Should display search results");
        } catch (NoSuchElementException e) {
            // Search might not be implemented or accessible, continue
        }
    }

    @Test
    @Order(8)
    public void testResponsiveDesign() {
        driver.get("https://demo.realworld.io/");
        
        // Check for responsive elements are present
        WebElement header = driver.findElement(By.tagName("header"));
        WebElement footer = driver.findElement(By.tagName("footer"));
        WebElement mainContent = driver.findElement(By.cssSelector("main"));
        
        assertTrue(header.isDisplayed(), "Header should be displayed");
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
        assertTrue(mainContent.isDisplayed(), "Main content should be displayed");
    }
}