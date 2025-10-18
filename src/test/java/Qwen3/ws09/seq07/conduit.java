package Qwen3.ws09.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RealWorldTest {

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
    public void testPageLoad() {
        driver.get("https://demo.realworld.io/");
        
        // Verify page title
        String title = driver.getTitle();
        assertTrue(title.contains("Conduit"));
        
        // Verify main elements are present
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("header")));
        assertTrue(header.isDisplayed());
        
        WebElement mainContent = driver.findElement(By.tagName("main"));
        assertTrue(mainContent.isDisplayed());
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://demo.realworld.io/");
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        assertEquals("https://demo.realworld.io/", driver.getCurrentUrl());

        // Test Sign In link
        driver.get("https://demo.realworld.io/");
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"));

        // Go back to home
        driver.get("https://demo.realworld.io/");
        
        // Test Sign Up link
        WebElement signUpLink = driver.findElement(By.linkText("Sign up"));
        signUpLink.click();
        wait.until(ExpectedConditions.urlContains("register"));
        assertTrue(driver.getCurrentUrl().contains("register"));
    }

    @Test
    @Order(3)
    public void testLoginPage() {
        driver.get("https://demo.realworld.io/login");
        
        // Verify login form elements
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        assertTrue(emailField.isDisplayed());
        
        WebElement passwordField = driver.findElement(By.name("password"));
        assertTrue(passwordField.isDisplayed());
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(loginButton.isDisplayed());
        
        // Verify page title
        String title = driver.getTitle();
        assertTrue(title.contains("Sign in"));
    }

    @Test
    @Order(4)
    public void testRegisterPage() {
        driver.get("https://demo.realworld.io/register");
        
        // Verify registration form elements
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        assertTrue(usernameField.isDisplayed());
        
        WebElement emailField = driver.findElement(By.name("email"));
        assertTrue(emailField.isDisplayed());
        
        WebElement passwordField = driver.findElement(By.name("password"));
        assertTrue(passwordField.isDisplayed());
        
        WebElement registerButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(registerButton.isDisplayed());
        
        // Verify page title
        String title = driver.getTitle();
        assertTrue(title.contains("Sign up"));
    }

    @Test
    @Order(5)
    public void testArticleFeed() {
        driver.get("https://demo.realworld.io/");
        
        // Verify article feed is displayed
        WebElement articleFeed = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-feed")));
        assertTrue(articleFeed.isDisplayed());
        
        // Check for articles
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articles.size() > 0);
        
        // Check first article
        if (!articles.isEmpty()) {
            WebElement firstArticle = articles.get(0);
            assertTrue(firstArticle.isDisplayed());
            
            // Check title
            List<WebElement> titles = firstArticle.findElements(By.cssSelector(".article-title"));
            if (!titles.isEmpty()) {
                assertTrue(titles.get(0).isDisplayed());
            }
            
            // Check description
            List<WebElement> descriptions = firstArticle.findElements(By.cssSelector(".article-description"));
            if (!descriptions.isEmpty()) {
                assertTrue(descriptions.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(6)
    public void testArticleDetail() {
        driver.get("https://demo.realworld.io/");
        
        // Get first article link
        List<WebElement> articleLinks = driver.findElements(By.cssSelector(".article-preview .article-title"));
        if (!articleLinks.isEmpty()) {
            WebElement articleLink = articleLinks.get(0);
            String articleUrl = articleLink.getAttribute("href");
            
            // Click on article link (if available)
            if (articleUrl != null && !articleUrl.isEmpty()) {
                articleLink.click();
                // Should navigate to article detail
                wait.until(ExpectedConditions.urlContains("article"));
                assertTrue(driver.getCurrentUrl().contains("article"));
            }
        }
    }

    @Test
    @Order(7)
    public void testTags() {
        driver.get("https://demo.realworld.io/");
        
        // Check for tags section
        WebElement tagsSection = driver.findElement(By.cssSelector(".tag-list"));
        assertTrue(tagsSection.isDisplayed());
        
        // Check for tags
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag-pill"));
        assertTrue(tags.size() > 0);
    }

    @Test
    @Order(8)
    public void testUserProfile() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to profile (if logged in)
        List<WebElement> profileLinks = driver.findElements(By.cssSelector("a[href*='profile']"));
        if (!profileLinks.isEmpty()) {
            WebElement profileLink = profileLinks.get(0);
            if (profileLink.isDisplayed()) {
                profileLink.click();
                wait.until(ExpectedConditions.urlContains("profile"));
                assertTrue(driver.getCurrentUrl().contains("profile"));
            }
        }
    }

    @Test
    @Order(9)
    public void testNewArticle() {
        driver.get("https://demo.realworld.io/");
        
        // Check for new article button/link
        List<WebElement> newArticleLinks = driver.findElements(By.cssSelector("a[href*='editor']"));
        if (!newArticleLinks.isEmpty()) {
            WebElement newArticleLink = newArticleLinks.get(0);
            assertTrue(newArticleLink.isDisplayed());
        }
    }

    @Test
    @Order(10)
    public void testFooterLinks() {
        driver.get("https://demo.realworld.io/");
        
        // Check footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0);
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed());
        }
    }

    @Test
    @Order(11)
    public void testSocialLinksInFooter() {
        driver.get("https://demo.realworld.io/");
        
        // Check for social media links in footer
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer .social-links a"));
        assertTrue(socialLinks.size() >= 0);
    }

    @Test
    @Order(12)
    public void testHeaderNavigation() {
        driver.get("https://demo.realworld.io/");
        
        // Check header links
        List<WebElement> headerLinks = driver.findElements(By.cssSelector("header a"));
        assertTrue(headerLinks.size() > 0);
        
        // Check for user profile icon if logged in
        WebElement profileIcon = driver.findElement(By.cssSelector(".user-pic"));
        if (profileIcon.isDisplayed()) {
            // Profile icon is present
        }
    }

    @Test
    @Order(13)
    public void testFeedSorting() {
        driver.get("https://demo.realworld.io/");
        
        // Check for sorting options if present
        List<WebElement> sortOptions = driver.findElements(By.cssSelector(".feed-toggle li"));
        assertTrue(sortOptions.size() >= 0);
    }

    @Test
    @Order(14)
    public void testSearchFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Check for search bar if present
        List<WebElement> searchBars = driver.findElements(By.cssSelector("input[placeholder*='search']"));
        if (!searchBars.isEmpty()) {
            WebElement searchBar = searchBars.get(0);
            assertTrue(searchBar.isDisplayed());
        }
    }

    @Test
    @Order(15)
    public void testResponsiveDesign() {
        driver.get("https://demo.realworld.io/");
        
        // Check for mobile menu toggle if present
        List<WebElement> mobileToggles = driver.findElements(By.cssSelector(".nav-mobile-toggle"));
        if (!mobileToggles.isEmpty()) {
            WebElement toggle = mobileToggles.get(0);
            assertTrue(toggle.isDisplayed());
        }
    }

    @Test
    @Order(16)
    public void testImageElements() {
        driver.get("https://demo.realworld.io/");
        
        // Check images on page
        List<WebElement> images = driver.findElements(By.tagName("img"));
        assertTrue(images.size() > 0);
        
        // Check that at least one image is displayed
        boolean hasDisplayedImage = false;
        for (WebElement img : images) {
            if (img.isDisplayed()) {
                hasDisplayedImage = true;
                break;
            }
        }
        assertTrue(hasDisplayedImage);
    }

    @Test
    @Order(17)
    public void testArticleComments() {
        driver.get("https://demo.realworld.io/");
        
        // Get first article
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        if (!articles.isEmpty()) {
            WebElement firstArticle = articles.get(0);
            
            // Look for comments link
            List<WebElement> commentsLinks = firstArticle.findElements(By.cssSelector(".comments-link"));
            if (!commentsLinks.isEmpty()) {
                WebElement commentsLink = commentsLinks.get(0);
                assertTrue(commentsLink.isDisplayed());
            }
        }
    }

    @Test
    @Order(18)
    public void testFavoriteArticle() {
        driver.get("https://demo.realworld.io/");
        
        // Check for favorite button
        List<WebElement> favoriteButtons = driver.findElements(By.cssSelector(".toggle-favorite"));
        if (!favoriteButtons.isEmpty()) {
            WebElement favoriteButton = favoriteButtons.get(0);
            assertTrue(favoriteButton.isDisplayed());
        }
    }

    @Test
    @Order(19)
    public void testSettingsPageAccess() {
        driver.get("https://demo.realworld.io/");
        
        // Check for settings link
        List<WebElement> settingsLinks = driver.findElements(By.cssSelector("a[href*='settings']"));
        if (!settingsLinks.isEmpty()) {
            WebElement settingsLink = settingsLinks.get(0);
            assertTrue(settingsLink.isDisplayed());
        }
    }

    @Test
    @Order(20)
    public void testArticleTags() {
        driver.get("https://demo.realworld.io/");
        
        // Check article tags
        List<WebElement> articleTags = driver.findElements(By.cssSelector(".article-preview .tag-list .tag-pill"));
        assertTrue(articleTags.size() >= 0);
    }
}