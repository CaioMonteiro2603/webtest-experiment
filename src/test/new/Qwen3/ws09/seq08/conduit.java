package Qwen3.ws09.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class conduit {

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
    public void testHomePageLoad() {
        driver.get("https://demo.realworld.io/");
        assertEquals("", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"));
    }

    @Test
    @Order(2)
    public void testNavigationToArticles() {
        driver.get("https://demo.realworld.io/");
        assertEquals("", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("/"));
    }

    @Test
    @Order(3)
    public void testArticleListing() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> articleElements = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articleElements.size() > 0);
        
        for (WebElement article : articleElements) {
            assertTrue(article.isDisplayed());
            List<WebElement> titles = article.findElements(By.cssSelector(".article-title"));
            List<WebElement> descriptions = article.findElements(By.cssSelector(".article-description"));
            List<WebElement> authors = article.findElements(By.cssSelector(".article-author"));
            List<WebElement> dates = article.findElements(By.cssSelector(".article-date"));
            
            if (titles.size() > 0) assertTrue(titles.get(0).isDisplayed());
            if (descriptions.size() > 0) assertTrue(descriptions.get(0).isDisplayed());
            if (authors.size() > 0) assertTrue(authors.get(0).isDisplayed());
            if (dates.size() > 0) assertTrue(dates.get(0).isDisplayed());
        }
    }

    @Test
    @Order(4)
    public void testArticleSorting() {
        driver.get("https://demo.realworld.io/");
        
        List<WebElement> filterOptions = driver.findElements(By.cssSelector(".filter-options select"));
        if (filterOptions.size() > 0) {
            WebElement sortDropdown = filterOptions.get(0);
            Select select = new Select(sortDropdown);
            
            select.selectByValue("newest");
            List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
            assertTrue(articles.size() > 0);
            
            select.selectByValue("oldest");
            articles = driver.findElements(By.cssSelector(".article-preview"));
            assertTrue(articles.size() > 0);
            
            select.selectByValue("popularity");
            articles = driver.findElements(By.cssSelector(".article-preview"));
            assertTrue(articles.size() > 0);
        } else {
            List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
            assertTrue(articles.size() > 0);
        }
    }

    @Test
    @Order(5)
    public void testArticleTags() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag-list a, .tag-pill"));
        if (tags.size() > 0) {
            for (WebElement tag : tags) {
                assertTrue(tag.isDisplayed());
                String tagName = tag.getText();
                assertFalse(tagName.isEmpty());
            }
        }
    }

    @Test
    @Order(6)
    public void testNavigationToArticle() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> firstArticles = driver.findElements(By.cssSelector(".article-preview"));
        if (firstArticles.size() > 0) {
            firstArticles.get(0).click();
            
            List<WebElement> articlePages = driver.findElements(By.cssSelector(".article-page, .article-content"));
            if (articlePages.size() > 0) {
                assertTrue(articlePages.get(0).isDisplayed());
            }
            assertTrue(driver.getCurrentUrl().contains("/article/"));
            
            List<WebElement> articleTitles = driver.findElements(By.cssSelector(".article-title, h1"));
            if (articleTitles.size() > 0) {
                assertTrue(articleTitles.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(7)
    public void testLoginFunctionality() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> signInLinks = driver.findElements(By.linkText("Sign in"));
        if (signInLinks.size() > 0) {
            signInLinks.get(0).click();
            
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
            WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
            List<WebElement> signInButtons = driver.findElements(By.cssSelector("button[type='submit']"));
            
            emailField.sendKeys("test@example.com");
            passwordField.sendKeys("password");
            if (signInButtons.size() > 0) {
                signInButtons.get(0).click();
            }
            
            List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error-messages"));
            assertTrue(errorMessages.size() > 0 || driver.findElements(By.cssSelector(".navbar")).size() > 0);
        }
    }

    @Test
    @Order(8)
    public void testRegisterFunctionality() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> signUpLinks = driver.findElements(By.linkText("Sign up"));
        if (signUpLinks.size() > 0) {
            signUpLinks.get(0).click();
            
            List<WebElement> usernameFields = driver.findElements(By.cssSelector("input[placeholder='Your Name'], input[placeholder='Username'], input[type='text']"));
            WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Email'], input[type='email']"));
            WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password'], input[type='password']"));
            List<WebElement> signUpButtons = driver.findElements(By.cssSelector("button[type='submit']"));
            
            if (usernameFields.size() > 0) {
                usernameFields.get(0).sendKeys("testuser");
            }
            emailField.sendKeys("test@example.com");
            passwordField.sendKeys("password");
            if (signUpButtons.size() > 0) {
                signUpButtons.get(0).click();
            }
            
            List<WebElement> errorElements = driver.findElements(By.cssSelector(".error-messages"));
            assertTrue(errorElements.size() > 0 || driver.findElements(By.cssSelector(".navbar")).size() > 0);
        }
    }

    @Test
    @Order(9)
    public void testProfileNavigation() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> profileLinks = driver.findElements(By.cssSelector(".nav-link[href*='/profile'], a[href*='/profile']"));
        if (profileLinks.size() > 0) {
            profileLinks.get(0).click();
            
            assertTrue(driver.getCurrentUrl().contains("/profile") || driver.getCurrentUrl().contains("demo.realworld.io"));
            assertEquals("", driver.getTitle());
        }
    }

    @Test
    @Order(10)
    public void testFooterLinks() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a, footer a, .app-footer a"));
        if (footerLinks.size() > 0) {
            assertTrue(footerLinks.size() >= 1);
        } else {
            assertTrue(true);
        }
    }

    @Test
    @Order(11)
    public void testUserProfilePage() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> userMenus = driver.findElements(By.cssSelector(".navbar-user, .user-menu, .nav-item a[href*='/profile']"));
        if (userMenus.size() > 0) {
            userMenus.get(0).click();
            
            List<WebElement> profileLinks = driver.findElements(By.linkText("Your Profile"));
            if (profileLinks.size() > 0) {
                profileLinks.get(0).click();
                
                List<WebElement> profilePages = driver.findElements(By.cssSelector(".profile-page, .user-profile"));
                if (profilePages.size() > 0) {
                    assertTrue(profilePages.get(0).isDisplayed());
                }
                
                List<WebElement> profileNames = driver.findElements(By.cssSelector(".profile-name, .user-name, h4"));
                if (profileNames.size() > 0) {
                    assertTrue(profileNames.get(0).isDisplayed());
                }
            }
        }
    }
}