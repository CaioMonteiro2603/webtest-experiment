package Qwen3.ws09.seq02;

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
public class conduit {
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
    public void testPageLoad() {
        driver.get("https://demo.realworld.io/");
        
        wait.until(ExpectedConditions.titleContains("Conduit"));
        assertTrue(driver.getTitle().contains("Conduit"));
        assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"));
    }

    @Test
    @Order(2)
    public void testNavigation() {
        driver.get("https://demo.realworld.io/");
        
        // Test Home navigation
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.navbar-brand")));
        homeLink.click();
        assertEquals("https://demo.realworld.io/", driver.getCurrentUrl());
        
        // Test Sign In
        driver.get("https://demo.realworld.io/");
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        assertTrue(driver.getCurrentUrl().contains("login"));
        
        // Navigate back
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        
        // Test Sign Up
        WebElement signUpLink = driver.findElement(By.linkText("Sign up"));
        signUpLink.click();
        assertTrue(driver.getCurrentUrl().contains("register"));
        
        // Navigate back
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
    }

    @Test
    @Order(3)
    public void testArticleListing() {
        driver.get("https://demo.realworld.io/");
        
        // Verify articles are displayed
        WebElement articlesContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-preview")));
        assertTrue(articlesContainer.isDisplayed());
        
        // Get articles
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articles.size() > 0);
        
        // Verify first article details
        if (!articles.isEmpty()) {
            WebElement firstArticle = articles.get(0);
            assertTrue(firstArticle.isDisplayed());
            
            // Check article title
            WebElement title = firstArticle.findElement(By.cssSelector(".article-title"));
            assertTrue(title.isDisplayed());
            assertTrue(title.getText().length() > 0);
            
            // Check article description
            WebElement description = firstArticle.findElement(By.cssSelector(".article-description"));
            assertTrue(description.isDisplayed());
        }
    }

    @Test
    @Order(4)
    public void testArticleDetails() {
        driver.get("https://demo.realworld.io/");
        
        // Click on first article
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        if (!articles.isEmpty()) {
            WebElement firstArticleLink = articles.get(0).findElement(By.cssSelector("a.article-preview-link"));
            firstArticleLink.click();
            
            // Wait for article detail page
            wait.until(ExpectedConditions.urlContains("article"));
            assertTrue(driver.getCurrentUrl().contains("article"));
            
            // Verify article content
            WebElement articleTitle = driver.findElement(By.cssSelector("h1.article-title"));
            assertTrue(articleTitle.isDisplayed());
            
            WebElement articleContent = driver.findElement(By.cssSelector(".article-content"));
            assertTrue(articleContent.isDisplayed());
        }
    }

    @Test
    @Order(5)
    public void testSignInFunctionality() {
        driver.get("https://demo.realworld.io/login");
        
        // Fill sign in form
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys("demo@realworld.io");
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("password");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for redirect to home
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"));
        
        // Verify successfully logged in
        WebElement profileLink = driver.findElement(By.linkText("Your Name"));
        assertTrue(profileLink.isDisplayed());
    }

    @Test
    @Order(6)
    public void testInvalidSignIn() {
        driver.get("https://demo.realworld.io/login");
        
        // Fill with invalid credentials
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("wrongpass");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("email or password"));
    }

    @Test
    @Order(7)
    public void testRegistration() {
        driver.get("https://demo.realworld.io/register");
        
        // Fill registration form
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Username']")));
        usernameField.sendKeys("testuser" + System.currentTimeMillis());
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        emailField.sendKeys("testuser" + System.currentTimeMillis() + "@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("password123");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for redirect to home
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"));
    }

    @Test
    @Order(8)
    public void testCreateArticle() {
        driver.get("https://demo.realworld.io/");
        
        // Log in first
        driver.get("https://demo.realworld.io/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys("demo@realworld.io");
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("password");
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for home page
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        
        // Navigate to new article
        WebElement writeArticleLink = driver.findElement(By.linkText("New Article"));
        writeArticleLink.click();
        
        // Wait for new article page
        wait.until(ExpectedConditions.urlContains("editor"));
        assertTrue(driver.getCurrentUrl().contains("editor"));
        
        // Fill article form
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys("Test Article Title");
        WebElement descriptionField = driver.findElement(By.cssSelector("input[placeholder='What's this article about?']"));
        descriptionField.sendKeys("Test article description");
        WebElement bodyField = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        bodyField.sendKeys("This is the content of the test article.");
        WebElement tagsField = driver.findElement(By.cssSelector("input[placeholder='Enter tags']"));
        tagsField.sendKeys("test,article");
        
        // Publish article
        WebElement publishButton = driver.findElement(By.cssSelector("button[type='submit']"));
        publishButton.click();
        
        // Wait for article to be published
        wait.until(ExpectedConditions.urlContains("article"));
        assertTrue(driver.getCurrentUrl().contains("article"));
    }

    @Test
    @Order(9)
    public void testFollowUser() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to user profile
        WebElement userProfileLink = driver.findElement(By.cssSelector("a[href*='/profile']"));
        if (userProfileLink.isDisplayed()) {
            userProfileLink.click();
            wait.until(ExpectedConditions.urlContains("profile"));
            
            // Follow user
            WebElement followButton = driver.findElement(By.cssSelector("button.btn-outline-primary"));
            if (followButton.isDisplayed()) {
                followButton.click();
                // Wait for follow state to change
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button.btn-secondary")));
            }
        }
    }

    @Test
    @Order(10)
    public void testTagsFiltering() {
        driver.get("https://demo.realworld.io/");
        
        // Check tags exist
        WebElement tagsContainer = driver.findElement(By.cssSelector(".tag-list"));
        assertTrue(tagsContainer.isDisplayed());
        
        // Get tags
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag-pill"));
        assertTrue(tags.size() > 0);
        
        // Click first tag
        if (!tags.isEmpty()) {
            tags.get(0).click();
            // Verify tag filter applied
            wait.until(ExpectedConditions.urlContains("tag"));
            assertTrue(driver.getCurrentUrl().contains("tag"));
        }
    }

    @Test
    @Order(11)
    public void testSearchFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Verify search bar exists
        WebElement searchInput = driver.findElement(By.cssSelector("input[placeholder='Search articles']"));
        assertTrue(searchInput.isDisplayed());
        
        // Search for something
        searchInput.sendKeys("test");
        WebElement searchButton = driver.findElement(By.cssSelector("button.btn-primary"));
        searchButton.click();
        
        // Wait for search results
        wait.until(ExpectedConditions.urlContains("search"));
        assertTrue(driver.getCurrentUrl().contains("search"));
        
        // Verify search results page loaded
        WebElement searchResults = driver.findElement(By.cssSelector(".search-results"));
        assertTrue(searchResults.isDisplayed());
    }

    @Test
    @Order(12)
    public void testFooterLinks() {
        driver.get("https://demo.realworld.io/");
        
        // Test footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0);
        
        // Verify footer exists
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed());
    }
}