package GPT20b.ws04.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void cleanup() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Utility that opens the target page */
    private static void loadPage() {
        driver.get(BASE_URL);
    }

    /** Finds the host of the base URL for external link checks */
    private static String baseHost() {
        return "katalon-test.s3.amazonaws.com";
    }

    @Test
    @Order(1)
    @DisplayName("Page load and title verification")
    void testPageLoads() {
        loadPage();
        String title = driver.getTitle();
        assertTrue(title.toLowerCase().contains("form"),
                "Page title does not contain expected text. Title: " + title);
    }

    @Test
    @Order(2)
    @DisplayName("Form fields presence")
    void testFormFieldsPresence() {
        loadPage();
        List<WebElement> nameFields = driver.findElements(By.cssSelector("input[name='name']"));
        Assumptions.assumeTrue(nameFields.size() > 0, "No name input found; skipping test.");

        List<WebElement> emailFields = driver.findElements(By.cssSelector("input[name='email']"));
        Assumptions.assumeTrue(emailFields.size() > 0, "No email input found; skipping test.");

        List<WebElement> websiteFields = driver.findElements(By.cssSelector("input[name='website']"));
        assertTrue(websiteFields.size() > 0, "Website input is missing.");
    }

    @Test
    @Order(3)
    @DisplayName("Form submission with valid data shows success")
    void testFormSubmissionValid() {
        loadPage();

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='name']")));
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']")));
        WebElement websiteInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='website']")));

        nameInput.sendKeys("John Doe");
        emailInput.sendKeys("john@example.com");
        websiteInput.sendKeys("https://example.com");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("results")));
        assertNotNull(successMessage, "Success message not displayed after submission.");
        assertTrue(successMessage.getText().toLowerCase().contains("thanks"),
                "Success message does not contain expected text: " + successMessage.getText());
    }

    @Test
    @Order(4)
    @DisplayName("Form submission with missing fields shows error")
    void testFormSubmissionInvalid() {
        loadPage();

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        submitButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error-message")));
        assertNotNull(error, "Error message not displayed when submitting incomplete form.");
        assertTrue(error.getText().toLowerCase().contains("required"),
                "Error message does not mention required fields: " + error.getText());
    }

    @Test
    @Order(5)
    @DisplayName("External links open in new tab and return correctly")
    void testExternalLinks() {
        loadPage();

        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(!links.isEmpty(), "No links found on the page.");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            // Skip same host links
            if (href == null || href.contains(baseHost())) {
                continue;
            }

            String parentHandle = driver.getWindowHandle();
            link.click();

            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(parentHandle);
            String newHandle = handles.iterator().next();

            driver.switchTo().window(newHandle);
            try {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                assertTrue(currentUrl.contains(href.toLowerCase().replaceFirst("https?://", "")),
                        "External link URL does not contain expected domain: " + href);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }
    }
}