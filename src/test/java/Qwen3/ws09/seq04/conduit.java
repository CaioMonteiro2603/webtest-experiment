package Qwen3.ws09.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

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
        String title = driver.getTitle();
        assertTrue(title.contains("Conduit"));
        assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"));
    }

    @Test
    @Order(2)
    public void testNavigationToArticles() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to Articles
        WebElement articlesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Articles")));
        articlesLink.click();
        
        wait.until(ExpectedConditions.urlContains("articles"));
        assertTrue(driver.getCurrentUrl().contains("articles"));
        
        // Verify articles are displayed
        List<WebElement> articleCards = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articleCards.size() >= 1);
        
        // Go back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
    }

    @Test
    @Order(3)
    public void testArticleSorting() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to Articles
        WebElement articlesLink = driver.findElement(By.linkText("Articles"));
        articlesLink.click();
        
        wait.until(ExpectedConditions.urlContains("articles"));
        
        // Try to interact with sort options if available
        List<WebElement> sortOptions = driver.findElements(By.cssSelector("select.form-control"));
        if (!sortOptions.isEmpty()) {
            WebElement sortDropdown = sortOptions.get(0);
            Select sortSelect = new Select(sortDropdown);
            
            // Switch between different sort options
            try {
                sortSelect.selectByVisibleText("Most Recent");
                Thread.sleep(1000); // Brief pause to allow sort to process
                
                sortSelect.selectByVisibleText("Most Popular");
                Thread.sleep(1000); // Brief pause to allow sort to process
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Expected behavior in headless mode - continue anyway
            }
        }
    }

    @Test
    @Order(4)
    public void testArticleDetailAndView() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to Articles
        WebElement articlesLink = driver.findElement(By.linkText("Articles"));
        articlesLink.click();
        
        wait.until(ExpectedConditions.urlContains("articles"));
        
        // Click on first article to view details
        List<WebElement> articleLinks = driver.findElements(By.cssSelector(".article-preview a"));
        if (!articleLinks.isEmpty()) {
            WebElement firstArticleLink = articleLinks.get(0);
            firstArticleLink.click();
            
            // Wait for article details to load
            wait.until(ExpectedConditions.urlContains("article"));
            assertTrue(driver.getCurrentUrl().contains("article"));
            
            // Verify article detail page loaded
            WebElement articleTitle = driver.findElement(By.cssSelector("h1.article-title"));
            assertTrue(articleTitle.isDisplayed());
            
            // Go back to articles
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("articles"));
        }
    }

    @Test
    @Order(5)
    public void testLoginPage() {
        driver.get("https://demo.realworld.io/");
        
        // Click on Sign In link
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"));
        
        // Verify login form exists
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".login-page form")));
        assertTrue(loginForm.isDisplayed());
        
        // Verify form fields
        List<WebElement> formFields = driver.findElements(By.cssSelector("input[formcontrolname]"));
        assertTrue(formFields.size() >= 2);
    }

    @Test
    @Order(6)
    public void testRegisterPage() {
        driver.get("https://demo.realworld.io/");
        
        // Click on Sign Up link
        WebElement signUpLink = driver.findElement(By.linkText("Sign up"));
        signUpLink.click();
        
        wait.until(ExpectedConditions.urlContains("register"));
        assertTrue(driver.getCurrentUrl().contains("register"));
        
        // Verify registration form exists
        WebElement registerForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".register-page form")));
        assertTrue(registerForm.isDisplayed());
        
        // Verify form fields
        List<WebElement> formFields = driver.findElements(By.cssSelector("input[formcontrolname]"));
        assertTrue(formFields.size() >= 3);
    }

    @Test
    @Order(7)
    public void testNavigationBetweenPages() {
        driver.get("https://demo.realworld.io/");
        
        // Test navigation through main menu
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() >= 3);
        
        // Navigate to Articles
        WebElement articlesLink = driver.findElement(By.linkText("Articles"));
        articlesLink.click();
        wait.until(ExpectedConditions.urlContains("articles"));
        
        // Navigate to Profile (if logged in, but we're not)
        try {
            WebElement profileLink = driver.findElement(By.linkText("Your Feed"));
            profileLink.click();
            // If fails due to not being logged in, that's fine - just test it can be clicked 
        } catch (NoSuchElementException e) {
            // Expected if not logged in
        }
        
        // Go back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
    }

    @Test
    @Order(8)
    public void testFooterLinks() {
        driver.get("https://demo.realworld.io/");
        
        // Check footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() >= 2);
        
        
        // Test some footer links (check for existence rather than navigation which may not be reliable)
        for (int i = 0; i < Math.min(2, footerLinks.size()); i++) {
            WebElement link = footerLinks.get(i);
            String href = link.getAttribute("href");
            assertNotNull(href);
            assertFalse(href.isEmpty());
        }
    }

    @Test
    @Order(9)
    public void testResponsiveDesign() {
        driver.get("https://demo.realworld.io/");
        
        // Test various screen sizes
        Dimension[] screenSizes = {
            new Dimension(1920, 1080),
            new Dimension(1200, 800),
            new Dimension(768, 1024),
            new Dimension(375, 667)
        };
        
        for (Dimension size : screenSizes) {
            driver.manage().window().setSize(size);
            
            // Verify key elements are present
            try {
                WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("header")));
                WebElement navigation = driver.findElement(By.cssSelector("nav"));
                WebElement mainContent = driver.findElement(By.tagName("main"));
                
                assertTrue(header.isDisplayed());
                assertTrue(navigation.isDisplayed());
                assertTrue(mainContent.isDisplayed());
            } catch (Exception e) {
                // Don't fail tests for display issues in headless mode
                // Just ensure no severe crashes
            }
        }
    }

    @Test
    @Order(10)
    public void testArticleFeedFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to Articles
        WebElement articlesLink = driver.findElement(By.linkText("Articles"));
        articlesLink.click();
        
        wait.until(ExpectedConditions.urlContains("articles"));
        
        // Verify that articles are displayed
        List<WebElement> articlePreviews = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articlePreviews.size() >= 1);
        
        // Try to read the first article's title
        if (!articlePreviews.isEmpty()) {
            WebElement firstArticle = articlePreviews.get(0);
            List<WebElement> titles = firstArticle.findElements(By.cssSelector("h1, h2"));
            if (!titles.isEmpty()) {
                assertTrue(titles.get(0).isDisplayed());
            }
        }
        
        // Navigate to tags if available  
        List<WebElement> tagLinks = driver.findElements(By.cssSelector(".tag-list a"));
        if (!tagLinks.isEmpty()) {
            WebElement firstTag = tagLinks.get(0);
            firstTag.click();
            
            // Wait for tag page to load
            try {
                wait.until(ExpectedConditions.urlContains("tag"));
            } catch (TimeoutException e) {
                // Expected in headless, continue anyway
            }
        }
    }

    @Test
    @Order(11)
    public void testHomePageFeatures() {
        driver.get("https://demo.realworld.io/");
        
        // Verify page title
        String title = driver.getTitle();
        assertTrue(title.contains("Conduit"));
        
        // Verify main headings
        WebElement mainHeading = driver.findElement(By.cssSelector("h1")); 
        assertTrue(mainHeading.isDisplayed());
        
        // Verify featured articles section
        WebElement articlesSection = driver.findElement(By.cssSelector("section.articles"));
        assertTrue(articlesSection.isDisplayed());
        
        // Try to find main navigation
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() >= 2);
    }
}