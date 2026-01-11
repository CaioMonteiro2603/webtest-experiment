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
        
        // Find and click on "Examples" link using robust selector
        WebElement examplesLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/examples']"))
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
        
        // Find and click on "Documentation" link
        WebElement docsLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/docs']"))
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
        
        // Find and click on "API" link
        WebElement apiLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/api']"))
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
        
        // Find and click on "Embed" link
        WebElement embedLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/embed']"))
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
        
        // Find and click on "Blog" link
        WebElement blogLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://blog.jsfiddle.net/']"))
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
        
        // Find and click on Twitter link in footer
        WebElement twitterLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href='https://twitter.com/js_fiddle']"))
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
        
        // Find and click on GitHub link in footer
        WebElement githubLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href='https://github.com/jsfiddle/jsfiddle-issues']"))
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
        
        // Find and click on Discord link in footer
        WebElement discordLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href='https://discord.gg/9Uq3eC2']"))
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
        
        // Find and click on Patreon link in footer
        WebElement patreonLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href='https://www.patreon.com/jsfiddle']"))
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
        
        // Find and click on Privacy Policy link
        WebElement privacyLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/privacy']"))
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
        
        // Find and click on Terms of Service link
        WebElement termsLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/terms']"))
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
        
        // Find and click on Contact link
        WebElement contactLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/contact']"))
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
        
        // Find and click on Status link
        WebElement statusLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://status.jsfiddle.net/']"))
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
        
        // Find and click on Support link
        WebElement supportLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://support.jsfiddle.net/']"))
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
        
        // Find and click on Newsletter link
        WebElement newsletterLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://jsfiddle.substack.com/']"))
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
        
        // Find and click on Changelog link
        WebElement changelogLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://changelog.jsfiddle.net/']"))
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

    @Test
    @Order(18)
    public void testNavigationToRoadmapPage() {
        driver.get(BASE_URL);
        
        // Find and click on Roadmap link
        WebElement roadmapLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://roadmap.jsfiddle.net/']"))
        );
        roadmapLink.click();
        
        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        
        assertNotNull(newHandle, "New tab should open for Roadmap");
        driver.switchTo().window(newHandle);
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("roadmap.jsfiddle.net"), 
                   "Roadmap tab URL should contain roadmap.jsfiddle.net");
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(19)
    public void testNavigationToPricingPage() {
        driver.get(BASE_URL);
        
        // Find and click on Pricing link
        WebElement pricingLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/pricing']"))
        );
        pricingLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/pricing"));
        
        // Assert we are on pricing page
        assertTrue(driver.getCurrentUrl().contains("/pricing"), "Should navigate to /pricing page");
    }

    @Test
    @Order(20)
    public void testNavigationToProPage() {
        driver.get(BASE_URL);
        
        // Find and click on Pro link
        WebElement proLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/pro']"))
        );
        proLink.click();
        
        // Wait for URL to change
        wait.until(d -> d.getCurrentUrl().contains("/pro"));
        
        // Assert we are on pro page
        assertTrue(driver.getCurrentUrl().contains("/pro"), "Should navigate to /pro page");
    }
}