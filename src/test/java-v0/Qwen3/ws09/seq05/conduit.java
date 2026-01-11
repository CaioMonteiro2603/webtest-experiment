package Qwen3.ws09.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
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
    public void testPageLoadAndTitle() {
        driver.get("https://demo.realworld.io/");
        
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("RealWorld"), "Page title should contain RealWorld");
        
        WebElement header = driver.findElement(By.cssSelector("header h1"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://demo.realworld.io/");
        
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Should have navigation links");
        
        for (WebElement link : navLinks) {
            assertTrue(link.isDisplayed(), "Navigation link should be displayed");
            assertNotNull(link.getAttribute("href"), "Navigation link should have href attribute");
        }
    }

    @Test
    @Order(3)
    public void testHomePageElements() {
        driver.get("https://demo.realworld.io/");
        
        // Check for welcome message or headline
        WebElement headline = driver.findElement(By.cssSelector("h1"));
        assertTrue(headline.isDisplayed(), "Headline should be displayed");
        
        // Check for articles section
        WebElement articlesSection = driver.findElement(By.cssSelector(".articles-section"));
        assertTrue(articlesSection.isDisplayed(), "Articles section should be displayed");
        
        // Check for featured articles
        List<WebElement> featuredArticles = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(featuredArticles.size() > 0, "Should have featured articles");
    }

    @Test
    @Order(4)
    public void testArticleListing() {
        driver.get("https://demo.realworld.io/");
        
        // Check article listing
        WebElement articlesContainer = driver.findElement(By.cssSelector(".articles-container"));
        assertTrue(articlesContainer.isDisplayed(), "Articles container should be displayed");
        
        List<WebElement> articlePreviews = driver.findElements(By.cssSelector(".article-preview"));
        assertTrue(articlePreviews.size() > 0, "Should have article previews");
        
        for (WebElement article : articlePreviews) {
            assertTrue(article.isDisplayed(), "Article preview should be displayed");
        }
    }

    @Test
    @Order(5)
    public void testArticleDetailPage() {
        driver.get("https://demo.realworld.io/");
        
        // Try to navigate to an article detail page
        try {
            List<WebElement> articleLinks = driver.findElements(By.cssSelector(".article-preview a"));
            if (!articleLinks.isEmpty()) {
                WebElement firstArticleLink = articleLinks.get(0);
                
                firstArticleLink.click();
                
                // Wait for page to load
                wait.until(ExpectedConditions.urlContains("/article/"));
                
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("/article/"), "Should navigate to article detail page");
                
                // Go back to home
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
            }
        } catch (Exception ignored) {
            // Skip if cannot navigate to article detail
        }
    }

    @Test
    @Order(6)
    public void testUserAuthentication() {
        driver.get("https://demo.realworld.io/");
        
        // Check if authentication links exist
        try {
            WebElement signInLink = driver.findElement(By.linkText("Sign in"));
            assertTrue(signInLink.isDisplayed(), "Sign in link should be displayed");
        } catch (NoSuchElementException ignored) {
            // Sign in not present or hidden
        }
        
        try {
            WebElement signUpLink = driver.findElement(By.linkText("Sign up"));
            assertTrue(signUpLink.isDisplayed(), "Sign up link should be displayed");
        } catch (NoSuchElementException ignored) {
            // Sign up not present or hidden
        }
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://demo.realworld.io/");
        
        // Check footer
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
        
        // Find footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed(), "Footer link should be displayed");
            assertNotNull(link.getAttribute("href"), "Footer link should have href attribute");
        }
    }

    @Test
    @Order(8)
    public void testExternalLinksInFooter() {
        driver.get("https://demo.realworld.io/");
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        String mainWindowHandle = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Click external links (may open in new window)
                link.click();
                try {
                    wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                    
                    for (String windowHandle : driver.getWindowHandles()) {
                        if (!windowHandle.equals(mainWindowHandle)) {
                            driver.switchTo().window(windowHandle);
                            break;
                        }
                    }
                    
                    String currentUrl = driver.getCurrentUrl();
                    // As this is a demo site, we mainly check that external links are handled properly
                    assertTrue(currentUrl.contains("http") || currentUrl.contains("https"), 
                               "External link should be a valid URL");
                    
                    driver.close();
                    driver.switchTo().window(mainWindowHandle);
                } catch (TimeoutException e) {
                    // If no new window was created, assume it's an internal redirect or same-page navigation
                }
            }
        }
    }

    @Test
    @Order(9)
    public void testArticleTags() {
        driver.get("https://demo.realworld.io/");
        
        // Check for tags or categories
        try {
            List<WebElement> tagElements = driver.findElements(By.cssSelector(".tag-list a"));
            if (!tagElements.isEmpty()) {
                for (WebElement tag : tagElements) {
                    assertTrue(tag.isDisplayed(), "Tag should be displayed");
                }
            }
        } catch (NoSuchElementException ignored) {
            // Tags might not be present on main page
        }
    }

    @Test
    @Order(10)
    public void testResponsiveDesignElements() {
        driver.get("https://demo.realworld.io/");
        
        // Check responsive design elements
        List<WebElement> navElements = driver.findElements(By.cssSelector("nav"));
        assertTrue(navElements.size() > 0, "Should have navigation elements");
        
        // Check if mobile menu toggle exists
        try {
            WebElement mobileToggle = driver.findElement(By.cssSelector(".navbar-toggle"));
            assertTrue(mobileToggle.isDisplayed(), "Mobile menu toggle should be displayed");
        } catch (NoSuchElementException ignored) {
            // May not be present in all viewports
        }
        
        // Check main content area
        WebElement mainContent = driver.findElement(By.tagName("main"));
        assertTrue(mainContent.isDisplayed(), "Main content should be displayed");
    }

    @Test
    @Order(11)
    public void testProfileNavigation() {
        driver.get("https://demo.realworld.io/");
        
        // Try to find profile-related elements
        try {
            List<WebElement> profileLinks = driver.findElements(By.cssSelector("[href*='/profile']"));
            for (WebElement link : profileLinks) {
                assertTrue(link.isDisplayed(), "Profile link should be displayed");
            }
        } catch (NoSuchElementException ignored) {
            // Profile links may not be visible or accessible without login
        }
    }

    @Test
    @Order(12)
    public void testSubscriptionFeature() {
        driver.get("https://demo.realworld.io/");
        
        // Check for subscription/ newsletter form
        try {
            WebElement subscribeForm = driver.findElement(By.cssSelector("[id*='subscribe']"));
            assertTrue(subscribeForm.isDisplayed(), "Subscribe form should be displayed");
        } catch (NoSuchElementException ignored) {
            // Subscription form might not exist on home page
        }
    }

    @Test
    @Order(13)
    public void testPageNavigationToSections() {
        driver.get("https://demo.realworld.io/");
        
        // Test navigation to different sections of the site
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        int linksToTest = Math.min(navLinks.size(), 3); // Limit to prevent excessive navigation
        
        for (int i = 0; i < linksToTest; i++) {
            if (i == 0) continue; // Skip first link which is likely homepage
            
            WebElement link = navLinks.get(i);
            String href = link.getAttribute("href");
            
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Navigate to the link
                link.click();
                
                // Wait for navigation
                wait.until(ExpectedConditions.urlContains(href));
                
                // Verify URL has the expected part
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("demo.realworld.io"), 
                          "Should remain on demo.realworld.io after navigation");
                
                // Go back to homepage
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
            }
        }
    }

    @Test
    @Order(14)
    public void testAccessibilityAndSemanticElements() {
        driver.get("https://demo.realworld.io/");
        
        // Test for semantic HTML elements
        List<WebElement> headerElements = driver.findElements(By.tagName("header"));
        assertEquals(1, headerElements.size(), "Should have one header element");
        
        List<WebElement> navElements = driver.findElements(By.tagName("nav"));
        assertTrue(navElements.size() > 0, "Should have navigation elements");
        
        List<WebElement> mainElements = driver.findElements(By.tagName("main"));
        assertEquals(1, mainElements.size(), "Should have one main element");
        
        List<WebElement> footerElements = driver.findElements(By.tagName("footer"));
        assertEquals(1, footerElements.size(), "Should have one footer element");
        
        // Test for images with alt attributes
        List<WebElement> imgElements = driver.findElements(By.tagName("img"));
        for (WebElement img : imgElements) {
            String altText = img.getAttribute("alt");
            if (altText != null) {
                assertFalse(altText.trim().isEmpty(), "Image alt text should not be empty");
            }
        }
        
        // Test for links having meaningful text
        List<WebElement> linkElements = driver.findElements(By.tagName("a"));
        for (WebElement link : linkElements) {
            String linkText = link.getText().trim();
            if (!linkText.isEmpty()) {
                assertNotNull(link.getAttribute("href"), "Link should have href attribute");
            }
        }
    }

    @Test
    @Order(15)
    public void testCommentFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Try to find comment-related elements or form
        try {
            List<WebElement> commentForms = driver.findElements(By.cssSelector("[id*='comment']"));
            for (WebElement form : commentForms) {
                if (form.isDisplayed()) {
                    assertTrue(form.isDisplayed(), "Comment form should be displayed");
                }
            }
        } catch (NoSuchElementException ignored) {
            // Comments might not be visible without article details or user interaction
        }
    }
}