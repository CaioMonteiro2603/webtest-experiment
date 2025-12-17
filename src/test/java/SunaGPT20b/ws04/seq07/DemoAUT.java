package SunaGPT20b.ws04.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testFormPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().contains("Sample Form"),
                "Page title should contain 'Sample Form'");

        // Verify presence of form fields
        Assertions.assertTrue(isElementPresent(By.id("firstName")), "First Name field should be present");
        Assertions.assertTrue(isElementPresent(By.id("lastName")), "Last Name field should be present");
        Assertions.assertTrue(isElementPresent(By.id("email")), "Email field should be present");
        Assertions.assertTrue(isElementPresent(By.id("gender")), "Gender dropdown should be present");
        Assertions.assertTrue(isElementPresent(By.id("message")), "Message textarea should be present");
        Assertions.assertTrue(isElementPresent(By.cssSelector("input[type='submit']")), "Submit button should be present");
    }

    @Test
    @Order(2)
    public void testFormSubmissionNavigatesToSubmitPage() {
        driver.get(BASE_URL);

        // Fill the form
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        Select genderSelect = new Select(driver.findElement(By.id("gender")));
        genderSelect.selectByVisibleText("Male");
        driver.findElement(By.id("message")).sendKeys("Testing form submission.");

        // Submit the form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='submit']")));
        submitBtn.click();

        // Wait for navigation to submit.html
        wait.until(driver1 -> driver1.getCurrentUrl().endsWith("submit.html"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("submit.html"),
                "Should navigate to submit.html after form submission");

        // Basic verification on submit page (presence of a heading or success message)
        Assertions.assertTrue(isElementPresent(By.tagName("h1")) ||
                        isElementPresent(By.tagName("h2")) ||
                        isElementPresent(By.id("successMessage")),
                "Submit page should contain a heading or success message");
    }

    @Test
    @Order(3)
    public void testExternalLinksOnFormPage() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.xpath("//a[@href]"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            // Skip internal navigation to submit.html (already covered)
            if (href == null || href.isEmpty() || href.endsWith("submit.html")) {
                continue;
            }

            // Open link in a new tab using JavaScript to avoid interfering with the main page
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            // Switch to the new tab
            String originalWindow = driver.getWindowHandle();
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            // Verify the URL contains the expected domain (basic check)
            wait.until(driver1 -> driver1.getCurrentUrl().length() > 0);
            Assertions.assertTrue(driver.getCurrentUrl().contains(getDomainFromUrl(href)),
                    "External link should navigate to a URL containing its domain");

            // Close the external tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    // Helper method to check element presence without throwing exceptions
    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // Extract domain from a URL for external link verification
    private String getDomainFromUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return "";
        }
    }
}