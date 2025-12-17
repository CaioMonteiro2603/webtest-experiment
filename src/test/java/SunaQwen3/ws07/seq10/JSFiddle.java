package SunaQwen3.ws07.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String EXTERNAL_LINKEDIN = "linkedin.com";
    private static final String EXTERNAL_TWITTER = "twitter.com";
    private static final String EXTERNAL_FACEBOOK = "facebook.com";

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
    public void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);
        wait.until(titleContains("JSFiddle"));

        Assertions.assertTrue(driver.getTitle().contains("JSFiddle"), "Page title should contain 'JSFiddle'");
        Assertions.assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"), "Current URL should contain jsfiddle.net");

        // Verify main elements are present
        Assertions.assertNotNull(driver.findElement(By.id("result")), "Result frame should be present");
        Assertions.assertNotNull(driver.findElement(By.cssSelector("textarea[aria-label='HTML']")), "HTML editor should be present");
        Assertions.assertNotNull(driver.findElement(By.cssSelector("textarea[aria-label='CSS']")), "CSS editor should be present");
        Assertions.assertNotNull(driver.findElement(By.cssSelector("textarea[aria-label='JavaScript']")), "JavaScript editor should be present");
    }

    @Test
    @Order(2)
    public void testNavigationToAboutPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/about']")));

        WebElement aboutLink = driver.findElement(By.cssSelector("a[href='/about']"));
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        aboutLink.click();

        wait.until(titleContains("About"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/about"), "Should navigate to /about page");
        Assertions.assertTrue(driver.getTitle().contains("About"), "Page title should contain 'About'");
    }

    @Test
    @Order(3)
    public void testExternalFooterLinksOpenInNewTab() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("footer .social a")));

        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer .social a"));
        String originalWindow = driver.getWindowHandle();
        Set<String> originalWindows = driver.getWindowHandles();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");

            // Click link that opens in new tab
            link.click();

            // Wait for new window to appear
            wait.until((d) -> d.getWindowHandles().size() > originalWindows.size());
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(originalWindows);
            String newWindow = newWindows.iterator().next();
            driver.switchTo().window(newWindow);

            // Validate URL contains expected domain
            if (href.contains("linkedin.com")) {
                Assertions.assertTrue(driver.getCurrentUrl().contains(EXTERNAL_LINKEDIN), "LinkedIn tab should open with correct domain");
            } else if (href.contains("twitter.com")) {
                Assertions.assertTrue(driver.getCurrentUrl().contains(EXTERNAL_TWITTER), "Twitter tab should open with correct domain");
            } else if (href.contains("facebook.com")) {
                Assertions.assertTrue(driver.getCurrentUrl().contains(EXTERNAL_FACEBOOK), "Facebook tab should open with correct domain");
            }

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(4)
    public void testLoginFunctionalityWithInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/login']")));

        WebElement loginLink = driver.findElement(By.cssSelector("a[href='/login']"));
        wait.until(ExpectedConditions.elementToBeClickable(loginLink));
        loginLink.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Should be on login page");

        // Find login form elements
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        // Enter invalid credentials
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");

        loginButton.click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        Assertions.assertTrue(errorMessage.getText().contains("incorrect") || errorMessage.getText().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(5)
    public void testNavigationToResourcesPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/resources']")));

        WebElement resourcesLink = driver.findElement(By.cssSelector("a[href='/resources']"));
        wait.until(ExpectedConditions.elementToBeClickable(resourcesLink));
        resourcesLink.click();

        wait.until(titleContains("Resources"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/resources"), "Should navigate to /resources page");
        Assertions.assertTrue(driver.getTitle().contains("Resources"), "Page title should contain 'Resources'");

        // Verify main content is loaded
        WebElement mainContent = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("main .resources")));
        Assertions.assertTrue(mainContent.isDisplayed(), "Resources content should be visible");
    }

    @Test
    @Order(6)
    public void testNavigationToFAQPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/faq']")));

        WebElement faqLink = driver.findElement(By.cssSelector("a[href='/faq']"));
        wait.until(ExpectedConditions.elementToBeClickable(faqLink));
        faqLink.click();

        wait.until(titleContains("FAQ"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/faq"), "Should navigate to /faq page");
        Assertions.assertTrue(driver.getTitle().contains("FAQ"), "Page title should contain 'FAQ'");

        // Verify FAQ sections are present
        List<WebElement> faqItems = driver.findElements(By.cssSelector(".faq-item"));
        Assertions.assertTrue(faqItems.size() > 0, "At least one FAQ item should be present");
    }

    @Test
    @Order(7)
    public void testNavigationToBlogPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://blog.jsfiddle.net/']")));

        WebElement blogLink = driver.findElement(By.cssSelector("a[href='https://blog.jsfiddle.net/']"));
        String originalWindow = driver.getWindowHandle();
        Set<String> originalWindows = driver.getWindowHandles();

        wait.until(ExpectedConditions.elementToBeClickable(blogLink));
        blogLink.click();

        // Wait for new window
        wait.until((d) -> d.getWindowHandles().size() > originalWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(originalWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        // Validate blog URL
        Assertions.assertTrue(driver.getCurrentUrl().contains("blog.jsfiddle.net"), "Blog URL should contain blog.jsfiddle.net");
        Assertions.assertTrue(driver.getTitle().contains("JSFiddle Blog"), "Blog page title should contain 'JSFiddle Blog'");

        // Close blog tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testNavigationToPricingPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/pricing']")));

        WebElement pricingLink = driver.findElement(By.cssSelector("a[href='/pricing']"));
        wait.until(ExpectedConditions.elementToBeClickable(pricingLink));
        pricingLink.click();

        wait.until(titleContains("Pricing"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/pricing"), "Should navigate to /pricing page");
        Assertions.assertTrue(driver.getTitle().contains("Pricing"), "Page title should contain 'Pricing'");

        // Verify pricing plans are visible
        List<WebElement> plans = driver.findElements(By.cssSelector(".pricing-plan"));
        Assertions.assertTrue(plans.size() >= 2, "At least two pricing plans should be visible");
    }

    @Test
    @Order(9)
    public void testNavigationToContactPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/contact']")));

        WebElement contactLink = driver.findElement(By.cssSelector("a[href='/contact']"));
        wait.until(ExpectedConditions.elementToBeClickable(contactLink));
        contactLink.click();

        wait.until(titleContains("Contact"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/contact"), "Should navigate to /contact page");
        Assertions.assertTrue(driver.getTitle().contains("Contact"), "Page title should contain 'Contact'");

        // Verify contact form is present
        WebElement contactForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("contact-form")));
        Assertions.assertTrue(contactForm.isDisplayed(), "Contact form should be visible");
    }

    @Test
    @Order(10)
    public void testNavigationToPrivacyPolicyPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/privacy']")));

        WebElement privacyLink = driver.findElement(By.cssSelector("a[href='/privacy']"));
        wait.until(ExpectedConditions.elementToBeClickable(privacyLink));
        privacyLink.click();

        wait.until(titleContains("Privacy"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/privacy"), "Should navigate to /privacy page");
        Assertions.assertTrue(driver.getTitle().contains("Privacy"), "Page title should contain 'Privacy'");

        // Verify policy content is present
        WebElement content = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".policy-content")));
        Assertions.assertTrue(content.isDisplayed(), "Privacy policy content should be visible");
    }

    @Test
    @Order(11)
    public void testNavigationToTermsOfServicePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='/terms']")));

        WebElement termsLink = driver.findElement(By.cssSelector("a[href='/terms']"));
        wait.until(ExpectedConditions.elementToBeClickable(termsLink));
        termsLink.click();

        wait.until(titleContains("Terms"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/terms"), "Should navigate to /terms page");
        Assertions.assertTrue(driver.getTitle().contains("Terms"), "Page title should contain 'Terms'");

        // Verify terms content is present
        WebElement content = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".terms-content")));
        Assertions.assertTrue(content.isDisplayed(), "Terms content should be visible");
    }

    @Test
    @Order(12)
    public void testNavigationToStatusPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://status.jsfiddle.net/']")));

        WebElement statusLink = driver.findElement(By.cssSelector("a[href='https://status.jsfiddle.net/']"));
        String originalWindow = driver.getWindowHandle();
        Set<String> originalWindows = driver.getWindowHandles();

        wait.until(ExpectedConditions.elementToBeClickable(statusLink));
        statusLink.click();

        // Wait for new window
        wait.until((d) -> d.getWindowHandles().size() > originalWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(originalWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        // Validate status page URL
        Assertions.assertTrue(driver.getCurrentUrl().contains("status.jsfiddle.net"), "Status URL should contain status.jsfiddle.net");

        // Close status tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    public void testNavigationToGitHubPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://github.com/jsfiddle/jsfiddle-issues']")));

        WebElement githubLink = driver.findElement(By.cssSelector("a[href='https://github.com/jsfiddle/jsfiddle-issues']"));
        String originalWindow = driver.getWindowHandle();
        Set<String> originalWindows = driver.getWindowHandles();

        wait.until(ExpectedConditions.elementToBeClickable(githubLink));
        githubLink.click();

        // Wait for new window
        wait.until((d) -> d.getWindowHandles().size() > originalWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(originalWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        // Validate GitHub URL
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub URL should contain github.com");

        // Close GitHub tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
    public void testNavigationToTwitterPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://twitter.com/js_fiddle']")));

        WebElement twitterLink = driver.findElement(By.cssSelector("a[href='https://twitter.com/js_fiddle']"));
        String originalWindow = driver.getWindowHandle();
        Set<String> originalWindows = driver.getWindowHandles();

        wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        twitterLink.click();

        // Wait for new window
        wait.until((d) -> d.getWindowHandles().size() > originalWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(originalWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        // Validate Twitter URL
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter URL should contain twitter.com");

        // Close Twitter tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    public void testNavigationToRSSFeed() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://feeds.feedburner.com/jsfiddle']")));

        WebElement rssLink = driver.findElement(By.cssSelector("a[href='https://feeds.feedburner.com/jsfiddle']"));
        String originalWindow = driver.getWindowHandle();
        Set<String> originalWindows = driver.getWindowHandles();

        wait.until(ExpectedConditions.elementToBeClickable(rssLink));
        rssLink.click();

        // Wait for new window
        wait.until((d) -> d.getWindowHandles().size() > originalWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(originalWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        // Validate RSS feed URL
        Assertions.assertTrue(driver.getCurrentUrl().contains("feeds.feedburner.com"), "RSS feed URL should contain feeds.feedburner.com");

        // Close RSS tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}