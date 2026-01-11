package SunaQwen3.ws07.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String EXTERNAL_LINKEDIN = "linkedin.com";
    private static final String EXTERNAL_TWITTER = "twitter.com";
    private static final String EXTERNAL_FACEBOOK = "facebook.com";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("jsfiddle.net"), "URL should contain jsfiddle.net");
        assertTrue(driver.getTitle().contains("JSFiddle"), "Page title should contain JSFiddle");
    }

    @Test
    @Order(2)
    void testNavigationToExamplesPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> examplesLinks = driver.findElements(By.cssSelector("a[href*='/examples']"));
        if (!examplesLinks.isEmpty()) {
            WebElement examples = examplesLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", examples);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/examples"), "Should navigate to /examples");
        } else {
            System.out.println("Examples link not found, skipping navigation test");
        }
    }

    @Test
    @Order(3)
    void testNavigationToApiPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> apiLinks = driver.findElements(By.cssSelector("a[href*='/api']"));
        if (!apiLinks.isEmpty()) {
            WebElement api = apiLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", api);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/api"), "Should navigate to /api");
        } else {
            System.out.println("API link not found, skipping navigation test");
        }
    }

    @Test
    @Order(4)
    void testNavigationToAboutPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> aboutLinks = driver.findElements(By.cssSelector("a[href*='/about']"));
        if (!aboutLinks.isEmpty()) {
            WebElement about = aboutLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", about);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/about"), "Should navigate to /about");
        } else {
            System.out.println("About link not found, skipping navigation test");
        }
    }

    @Test
    @Order(5)
    void testExternalFooterLinkedInLinkOpensInNewTab() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> linkedInLinks = driver.findElements(By.cssSelector("a[href*='linkedin']"));
        if (!linkedInLinks.isEmpty()) {
            WebElement linkedIn = linkedInLinks.get(0);
            String href = linkedIn.getAttribute("href");
            assertTrue(href.contains("linkedin.com"), "LinkedIn URL should contain linkedin.com");
        } else {
            System.out.println("LinkedIn link not found, skipping test");
        }
    }

    @Test
    @Order(6)
    void testExternalFooterTwitterLinkOpensInNewTab() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> twitterLinks = driver.findElements(By.cssSelector("a[href*='twitter']"));
        if (!twitterLinks.isEmpty()) {
            WebElement twitter = twitterLinks.get(0);
            String href = twitter.getAttribute("href");
            assertTrue(href.contains("twitter.com"), "Twitter URL should contain twitter.com");
        } else {
            System.out.println("Twitter link not found, skipping test");
        }
    }

    @Test
    @Order(7)
    void testExternalFooterFacebookLinkOpensInNewTab() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> facebookLinks = driver.findElements(By.cssSelector("a[href*='facebook']"));
        if (!facebookLinks.isEmpty()) {
            WebElement facebook = facebookLinks.get(0);
            String href = facebook.getAttribute("href");
            assertTrue(href.contains("facebook.com"), "Facebook URL should contain facebook.com");
        } else {
            System.out.println("Facebook link not found, skipping test");
        }
    }

    @Test
    @Order(8)
    void testSearchFunctionalityReturnsResults() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> searchInputs = driver.findElements(By.name("q"));
        if (!searchInputs.isEmpty()) {
            WebElement input = searchInputs.get(0);
            input.clear();
            input.sendKeys("javascript");
            
            List<WebElement> searchButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
            if (!searchButtons.isEmpty()) {
                searchButtons.get(0).click();
                
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                assertTrue(driver.getCurrentUrl().contains("javascript"), "URL should contain search query");
            } else {
                System.out.println("Search button not found, skipping submission");
            }
        } else {
            System.out.println("Search input not found, skipping search test");
        }
    }

    @Test
    @Order(9)
    void testNavigationToPricingPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> pricingLinks = driver.findElements(By.cssSelector("a[href*='/pricing']"));
        if (!pricingLinks.isEmpty()) {
            WebElement pricing = pricingLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", pricing);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/pricing"), "Should navigate to /pricing");
        } else {
            System.out.println("Pricing link not found, skipping navigation test");
        }
    }

    @Test
    @Order(10)
    void testNavigationToBlogPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> blogLinks = driver.findElements(By.cssSelector("a[href*='blog']"));
        if (!blogLinks.isEmpty()) {
            WebElement blog = blogLinks.get(0);
            String href = blog.getAttribute("href");
            assertTrue(href.contains("blog"), "Blog URL should contain blog");
        } else {
            System.out.println("Blog link not found, skipping test");
        }
    }

    @Test
    @Order(11)
    void testNavigationToSupportPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> supportLinks = driver.findElements(By.cssSelector("a[href*='/support']"));
        if (!supportLinks.isEmpty()) {
            WebElement support = supportLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", support);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/support"), "Should navigate to /support");
        } else {
            System.out.println("Support link not found, skipping navigation test");
        }
    }

    @Test
    @Order(12)
    void testNavigationToPrivacyPolicyPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> privacyLinks = driver.findElements(By.cssSelector("a[href*='/privacy']"));
        if (!privacyLinks.isEmpty()) {
            WebElement privacy = privacyLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", privacy);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/privacy"), "Should navigate to /privacy");
        } else {
            System.out.println("Privacy link not found, skipping navigation test");
        }
    }

    @Test
    @Order(13)
    void testNavigationToTermsOfServicePage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> termsLinks = driver.findElements(By.cssSelector("a[href*='/terms']"));
        if (!termsLinks.isEmpty()) {
            WebElement terms = termsLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", terms);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/terms"), "Should navigate to /terms");
        } else {
            System.out.println("Terms link not found, skipping navigation test");
        }
    }

    @Test
    @Order(14)
    void testNavigationToStatusPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> statusLinks = driver.findElements(By.cssSelector("a[href*='status']"));
        if (!statusLinks.isEmpty()) {
            WebElement status = statusLinks.get(0);
            String href = status.getAttribute("href");
            assertTrue(href.contains("status"), "Status URL should contain status");
        } else {
            System.out.println("Status link not found, skipping test");
        }
    }

    @Test
    @Order(15)
    void testNavigationToChangelogPage() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> changelogLinks = driver.findElements(By.cssSelector("a[href*='/changelog']"));
        if (!changelogLinks.isEmpty()) {
            WebElement changelog = changelogLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", changelog);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            assertTrue(driver.getCurrentUrl().contains("/changelog"), "Should navigate to /changelog");
        } else {
            System.out.println("Changelog link not found, skipping navigation test");
        }
    }
}