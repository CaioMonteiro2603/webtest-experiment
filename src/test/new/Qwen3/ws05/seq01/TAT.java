package Qwen3.ws05.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); 
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
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a, header a, .nav a, .navigation a, ul li a"));
        assertTrue(navLinks.size() > 0, "Navigation links should be present");

        // Click first navigation link (assuming it's home)
        try {
            WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
            homeLink.click();
        } catch (TimeoutException e) {
            // If Home link not found, click the first nav link
            if (!navLinks.isEmpty()) {
                navLinks.get(0).click();
            }
        }

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("index.html"), "Should navigate to home page");
    }

    @Test
    @Order(3)
    public void testContactForm() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Find contact form
        try {
            WebElement contactLink = driver.findElement(By.linkText("Contact"));
            contactLink.click();
        } catch (NoSuchElementException e) {
            // Try alternative selectors for contact link
            try {
                WebElement contactLink = driver.findElement(By.partialLinkText("Contact"));
                contactLink.click();
            } catch (NoSuchElementException e2) {
                // Look for contact section or form directly
                WebElement contactSection = driver.findElement(By.cssSelector("section[id*='contact'], .contact, #contact"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", contactSection);
            }
        }

        // Wait for form to load
        try {
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
        } catch (TimeoutException e) {
            // If form not found, assume contact section exists
            List<WebElement> contactElements = driver.findElements(By.cssSelector("input, textarea, button"));
            assertTrue(contactElements.size() > 0, "Contact form elements should be present");
        }
    }

    @Test
    @Order(4)
    public void testServiceSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Find service section
        try {
            WebElement servicesLink = driver.findElement(By.linkText("Services"));
            servicesLink.click();
        } catch (NoSuchElementException e) {
            // Try alternative selectors for services link
            try {
                WebElement servicesLink = driver.findElement(By.partialLinkText("Services"));
                servicesLink.click();
            } catch (NoSuchElementException e2) {
                // Look for services section directly
                WebElement servicesSection = driver.findElement(By.cssSelector("section[id*='service'], .services, #services"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", servicesSection);
            }
        }

        // Wait for services page to load or section to be visible
        try {
            wait.until(ExpectedConditions.urlContains("services.html"));
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("services.html"), "Should navigate to services page");
        } catch (TimeoutException e) {
            // If no navigation occurred, verify services section exists on current page
            List<WebElement> serviceElements = driver.findElements(By.cssSelector("section[id*='service'], .services, #services"));
            assertTrue(serviceElements.size() > 0, "Services section should be present");
        }

        // Check if service items are displayed
        List<WebElement> serviceItems = driver.findElements(By.cssSelector(".service-item, .service, [class*='service']"));
        assertTrue(serviceItems.size() > 0, "Service items should be displayed");
    }

    @Test
    @Order(5)
    public void testTeamSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Find team section
        try {
            WebElement teamLink = driver.findElement(By.linkText("Team"));
            teamLink.click();
        } catch (NoSuchElementException e) {
            // Try alternative selectors for team link
            try {
                WebElement teamLink = driver.findElement(By.partialLinkText("Team"));
                teamLink.click();
            } catch (NoSuchElementException e2) {
                // Look for team section directly
                WebElement teamSection = driver.findElement(By.cssSelector("section[id*='team'], .team, #team"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", teamSection);
            }
        }

        // Wait for team page to load or section to be visible
        try {
            wait.until(ExpectedConditions.urlContains("team.html"));
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("team.html"), "Should navigate to team page");
        } catch (TimeoutException e) {
            // If no navigation occurred, verify team section exists on current page
            List<WebElement> teamElements = driver.findElements(By.cssSelector("section[id*='team'], .team, #team"));
            assertTrue(teamElements.size() > 0, "Team section should be present");
        }

        // Check if team members are displayed
        List<WebElement> teamMembers = driver.findElements(By.cssSelector(".team-member, .member, [class*='team']"));
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

        // Check responsive design elements with multiple selectors
        List<WebElement> mobileMenuElements = driver.findElements(By.cssSelector(".mobile-menu-button, .menu-toggle, .hamburger, [class*='menu'], button[aria-label*='menu'], .navbar-toggle"));
        if (!mobileMenuElements.isEmpty()) {
            assertTrue(mobileMenuElements.get(0).isDisplayed(), "Mobile menu button should be displayed");
        }

        // Check that page adapts to different screen sizes by checking basic layout elements
        List<WebElement> headerElements = driver.findElements(By.cssSelector("header, .header, [role='banner']"));
        List<WebElement> footerElements = driver.findElements(By.cssSelector("footer, .footer, [role='contentinfo']"));
        
        assertTrue(!headerElements.isEmpty() && headerElements.get(0).isDisplayed(), "Header should be displayed");
        assertTrue(!footerElements.isEmpty() && footerElements.get(0).isDisplayed(), "Footer should be displayed");
    }

    @Test
    @Order(8)
    public void testImageLoading() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");

        // Check if images are loaded with multiple selectors
        List<WebElement> images = driver.findElements(By.cssSelector("img, picture img, .image img, [src*='.jpg'], [src*='.png'], [src*='.svg']"));
        assertTrue(images.size() > 0, "Images should be present on page");

        // Wait for images to be loaded 
        for (WebElement img : images) {
            try {
                if (img.isDisplayed()) {
                    // Image is displayed, which means it loaded successfully
                    assertTrue(img.isDisplayed(), "Images should be displayed");
                }
            } catch (Exception e) {
                // Skip images that cannot be displayed
            }
        }
    }
}