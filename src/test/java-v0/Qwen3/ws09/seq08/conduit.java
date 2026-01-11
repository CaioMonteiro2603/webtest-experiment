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
        assertEquals("Conduit", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"));
    }

    @Test
    @Order(2)
    public void testNavigationToArticles() {
        driver.get("https://demo.realworld.io/");
        WebElement articlesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Articles")));
        articlesLink.click();
        assertTrue(driver.getCurrentUrl().contains("/"));
        assertEquals("Conduit", driver.getTitle());
    }

    @Test
    @Order(3)
    public void testArticleListing() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> articleElements = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articleElements.size() > 0);
        
        for (WebElement article : articleElements) {
            assertTrue(article.isDisplayed());
            WebElement title = article.findElement(By.cssSelector(".article-title"));
            WebElement description = article.findElement(By.cssSelector(".article-description"));
            WebElement author = article.findElement(By.cssSelector(".article-author"));
            WebElement date = article.findElement(By.cssSelector(".article-date"));
            
            assertTrue(title.isDisplayed());
            assertTrue(description.isDisplayed());
            assertTrue(author.isDisplayed());
            assertTrue(date.isDisplayed());
        }
    }

    @Test
    @Order(4)
    public void testArticleSorting() {
        driver.get("https://demo.realworld.io/");
        
        WebElement sortDropdown = driver.findElement(By.cssSelector(".filter-options select"));
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
    }

    @Test
    @Order(5)
    public void testArticleTags() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag-list a"));
        assertTrue(tags.size() > 0);
        
        for (WebElement tag : tags) {
            assertTrue(tag.isDisplayed());
            String tagName = tag.getText();
            assertFalse(tagName.isEmpty());
        }
    }

    @Test
    @Order(6)
    public void testNavigationToArticle() {
        driver.get("https://demo.realworld.io/");
        WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".article-preview:first-child")));
        firstArticle.click();
        
        WebElement articleContent = driver.findElement(By.cssSelector(".article-page"));
        assertTrue(articleContent.isDisplayed());
        assertTrue(driver.getCurrentUrl().contains("/article/"));
        
        WebElement articleTitle = driver.findElement(By.cssSelector(".article-title"));
        assertTrue(articleTitle.isDisplayed());
    }

    @Test
    @Order(7)
    public void testLoginFunctionality() {
        driver.get("https://demo.realworld.io/");
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("test@example.com");
        passwordField.sendKeys("password");
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(8)
    public void testRegisterFunctionality() {
        driver.get("https://demo.realworld.io/");
        WebElement signUpLink = driver.findElement(By.linkText("Sign up"));
        signUpLink.click();
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Your Name']")));
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement signUpButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("testuser");
        emailField.sendKeys("test@example.com");
        passwordField.sendKeys("password");
        signUpButton.click();
        
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(errorElement.isDisplayed());
    }

    @Test
    @Order(9)
    public void testProfileNavigation() {
        driver.get("https://demo.realworld.io/");
        WebElement profileLink = driver.findElement(By.cssSelector(".nav-link[href='/profile']"));
        profileLink.click();
        
        assertTrue(driver.getCurrentUrl().contains("/profile"));
        assertEquals("Conduit", driver.getTitle());
    }

    @Test
    @Order(10)
    public void testFooterLinks() {
        driver.get("https://demo.realworld.io/");
        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertTrue(footerLinks.size() >= 3);
        
        String originalWindow = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                link.click();
                
                Set<String> windowHandles = driver.getWindowHandles();
                String newWindow = windowHandles.stream()
                        .filter(w -> !w.equals(originalWindow))
                        .findFirst()
                        .orElse(null);
                
                if (newWindow != null) {
                    driver.switchTo().window(newWindow);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("github.com") || 
                               currentUrl.contains("realworld.io") || 
                               currentUrl.contains("twitter.com"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(11)
    public void testUserProfilePage() {
        driver.get("https://demo.realworld.io/");
        WebElement userMenu = driver.findElement(By.cssSelector(".navbar-user"));
        userMenu.click();
        
        WebElement profileLink = driver.findElement(By.linkText("Your Profile"));
        profileLink.click();
        
        WebElement profilePage = driver.findElement(By.cssSelector(".profile-page"));
        assertTrue(profilePage.isDisplayed());
        
        WebElement profileName = driver.findElement(By.cssSelector(".profile-name"));
        assertTrue(profileName.isDisplayed());
    }
}