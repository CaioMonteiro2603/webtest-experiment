package SunaQwen3.ws07.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        
        // Assert page title
        String title = driver.getTitle();
        assertTrue(title.contains("JSFiddle"), "Page title should contain 'JSFiddle'");
        
        // Assert URL is correct
        assertTrue(driver.getCurrentUrl().equals(BASE_URL) || driver.getCurrentUrl().startsWith(BASE_URL), 
                   "Current URL should be base URL or its variant");
    }

    @Test
    @Order(2)
    public void testNavigationToExamplesPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on "Examples" link using xpath as alternative
        WebElement examplesLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'/examples')]"))
        );
        examplesLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/examples"));
        
        // Assert we are on examples page
        assertTrue(driver.getCurrentUrl().contains("/examples"), "Should navigate to /examples page");
    }

    @Test
    @Order(3)
    public void testNavigationToDocumentationPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on "Documentation" link using xpath
        WebElement docsLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'/docs')]"))
        );
        docsLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/docs"));
        
        // Assert we are on docs page
        assertTrue(driver.getCurrentUrl().contains("/docs"), "Should navigate to /docs page");
    }

    @Test
    @Order(4)
    public void testNavigationToApiPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on "API" link using xpath
        WebElement apiLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'/api')]"))
        );
        apiLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/api"));
        
        // Assert we are on api page
        assertTrue(driver.getCurrentUrl().contains("/api"), "Should navigate to /api page");
    }

    @Test
    @Order(5)
    public void testNavigationToEmbedPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on "Embed" link using xpath
        WebElement embedLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'/embed')]"))
        );
        embedLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/embed"));
        
        // Assert we are on embed page
        assertTrue(driver.getCurrentUrl().contains("/embed"), "Should navigate to /embed page");
    }

    @Test
    @Order(6)
    public void testNavigationToBlogPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on "Blog" link using xpath
        WebElement blogLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'blog.jsfiddle.net')]"))
        );
        blogLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for blog");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("blog.jsfiddle.net"), 
                   "Blog tab URL should contain blog.jsfiddle.net");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(7)
    public void testNavigationToTwitterPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Twitter link using xpath
        WebElement twitterLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'twitter.com/js_fiddle')]"))
        );
        twitterLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Twitter");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
                   "Twitter tab URL should contain twitter.com");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(8)
    public void testNavigationToGitHubPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on GitHub link using xpath
        WebElement githubLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'github.com/jsfiddle/jsfiddle-issues')]"))
        );
        githubLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for GitHub");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("github.com"), 
                   "GitHub tab URL should contain github.com");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(9)
    public void testNavigationToDiscordPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Discord link using xpath
        WebElement discordLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'discord.gg')]"))
        );
        discordLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Discord");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("discord.gg"), 
                   "Discord tab URL should contain discord.gg");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(10)
    public void testNavigationToPatreonPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Patreon link using xpath
        WebElement patreonLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'patreon.com/jsfiddle')]"))
        );
        patreonLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Patreon");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("patreon.com"), 
                   "Patreon tab URL should contain patreon.com");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(11)
    public void testNavigationToPrivacyPolicyPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Privacy Policy link using xpath
        WebElement privacyLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'/privacy')]"))
        );
        privacyLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/privacy"));
        
        // Assert we are on privacy page
        assertTrue(driver.getCurrentUrl().contains("/privacy"), "Should navigate to /privacy page");
    }

    @Test
    @Order(12)
    public void testNavigationToTermsOfServicePage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Terms of Service link using xpath
        WebElement termsLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'/terms')]"))
        );
        termsLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/terms"));
        
        // Assert we are on terms page
        assertTrue(driver.getCurrentUrl().contains("/terms"), "Should navigate to /terms page");
    }

    @Test
    @Order(13)
    public void testNavigationToContactPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Contact link using xpath
        WebElement contactLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'/contact')]"))
        );
        contactLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/contact"));
        
        // Assert we are on contact page
        assertTrue(driver.getCurrentUrl().contains("/contact"), "Should navigate to /contact page");
    }

    @Test
    @Order(14)
    public void testNavigationToStatusPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Status link using xpath
        WebElement statusLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'status.jsfiddle.net')]"))
        );
        statusLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Status");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("status.jsfiddle.net"), 
                   "Status tab URL should contain status.jsfiddle.net");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(15)
    public void testNavigationToSupportPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Support link using xpath
        WebElement supportLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'support.jsfiddle.net')]"))
        );
        supportLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Support");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("support.jsfiddle.net"), 
                   "Support tab URL should contain support.jsfiddle.net");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(16)
    public void testNavigationToNewsletterPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Newsletter link using xpath
        WebElement newsletterLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'substack.com')]"))
        );
        newsletterLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Newsletter");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("substack.com"), 
                   "Newsletter tab URL should contain substack.com");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(17)
    public void testNavigationToChangelogPage() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely - FIXED
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
        
        // Find and click on Changelog link using xpath
        WebElement changelogLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'changelog.jsfiddle.net')]"))
        );
        changelogLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Changelog");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("changelog.jsfiddle.net"), 
                   "Changelog tab URL should contain changelog.jsfiddle.net");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }
}