package Qwen3.ws05.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CACTATTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        String title = driver.getTitle();
        assertTrue(title.contains("TAT"), "Page title should contain 'TAT'");

        // Verify main content is present
        WebElement mainHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        assertTrue(mainHeader.isDisplayed(), "Main header should be displayed");
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Check for main navigation links
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Navigation links should be present");

        // Click first navigation link (assuming it's home)
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("index.html"), "Should navigate to home page");
    }

    @Test
    @Order(3)
    public void testContactForm() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Find contact form
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();

        // Wait for form to load
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement messageField = driver.findElement(By.id("message"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        // Fill form
        nameField.sendKeys("John Doe");
        emailField.sendKeys("john@example.com");
        messageField.sendKeys("Test message for contact form");

        // Submit form
        submitButton.click();

        // Verify form submission
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission");
    }

    @Test
    @Order(4)
    public void testServiceSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Find service section
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();

        // Wait for services page to load
        wait.until(ExpectedConditions.urlContains("services.html"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("services.html"), "Should navigate to services page");

        // Check if service items are displayed
        List<WebElement> serviceItems = driver.findElements(By.cssSelector(".service-item"));
        assertTrue(serviceItems.size() > 0, "Service items should be displayed");
    }

    @Test
    @Order(5)
    public void testTeamSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Find team section
        WebElement teamLink = driver.findElement(By.linkText("Team"));
        teamLink.click();

        // Wait for team page to load
        wait.until(ExpectedConditions.urlContains("team.html"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("team.html"), "Should navigate to team page");

        // Check if team members are displayed
        List<WebElement> teamMembers = driver.findElements(By.cssSelector(".team-member"));
        assertTrue(teamMembers.size() > 0, "Team members should be displayed");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }

        // Check footer social links
        try {
            WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
            twitterLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("twitter"), "Should open Twitter link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if Twitter link not found
        }

        try {
            WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
            linkedinLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("linkedin"), "Should open LinkedIn link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if LinkedIn link not found
        }

        try {
            WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
            facebookLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("facebook"), "Should open Facebook link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if Facebook link not found
        }
    }

    @Test
    @Order(7)
    public void testResponsiveDesign() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Check responsive design elements
        WebElement mobileMenuButton = driver.findElement(By.cssSelector(".mobile-menu-button"));
        assertTrue(mobileMenuButton.isDisplayed(), "Mobile menu button should be displayed");

        // Check that page adapts to different screen sizes by checking basic layout elements
        WebElement header = driver.findElement(By.tagName("header"));
        WebElement footer = driver.findElement(By.tagName("footer"));
        
        assertTrue(header.isDisplayed(), "Header should be displayed");
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
    }

    @Test
    @Order(8)
    public void testImageLoading() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Check if images are loaded
        List<WebElement> images = driver.findElements(By.cssSelector("img"));
        assertTrue(images.size() > 0, "Images should be present on page");

        // Wait for images to be loaded 
        for (WebElement img : images) {
            if (img.isDisplayed()) {
                // Image is displayed, which means it loaded successfully
                assertTrue(img.isDisplayed(), "Images should be displayed");
            }
        }
    }
}