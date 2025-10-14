package GPT20b.ws04.seq02;

import java.time.Duration;
import java.util.Set;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class FormPageTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL =
            "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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

    /* ---------- TEST SCAFFOLDING ---------- */

    @Test
    @Order(1)
    public void testPageLoads() {
        driver.navigate().to(BASE_URL);
        assertTrue(driver.getCurrentUrl().contains("form.html"),
                "URL should contain 'form.html' after navigation");
        String title = driver.getTitle();
        assertTrue(title.toLowerCase().contains("form"),
                "Page title should contain 'form'");
        // Verify form element is present
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        assertTrue(form.isDisplayed(), "Form should be visible on page");
    }

    @Test
    @Order(2)
    public void testFormValidationEmpty() {
        driver.navigate().to(BASE_URL);
        WebElement submit = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();

        // Collect all required inputs
        List<WebElement> requiredInputs = driver.findElements(By.cssSelector("input[required], textarea[required], select[required]"));
        assertFalse(requiredInputs.isEmpty(),
                "Page should contain required fields for validation test");

        for (WebElement input : requiredInputs) {
            String js = "return arguments[0].validationMessage;";
            String message = (String) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(js, input);
            assertFalse(message.isEmpty(),
                    "Required field should produce a validation message when empty");
        }
    }

    @Test
    @Order(3)
    public void testSuccessfulFormSubmission() {
        driver.navigate().to(BASE_URL);

        // Fill required fields with sample data
        Map<String, String> data = new java.util.HashMap<>();
        data.put("firstName", "John");
        data.put("lastName", "Doe");
        data.put("email", "john.doe@example.com");
        data.put("password", "Password123");
        data.put("confirmPassword", "Password123");
        data.put("age", "30");
        data.put("gender", "male");

        // Populate form
        for (var entry : data.entrySet()) {
            By locator = By.xpath("//*[@id='" + entry.getKey() + "'] | //*[@name='" + entry.getKey() + "'] | //*[@placeholder='" + entry.getKey() + "']");
            List<WebElement> elements = driver.findElements(locator);
            if (!elements.isEmpty()) {
                WebElement field = elements.get(0);
                wait.until(ExpectedConditions.elementToBeClickable(field));
                field.clear();
                field.sendKeys(entry.getValue());
            }
        }

        WebElement submit = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();

        // Verify success indicator
        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".success, #success, [data-testid='success-message']")));
        assertTrue(successMsg.isDisplayed(), "Success message should be displayed after valid submission");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.navigate().to(BASE_URL);

        // Find first external link (not on katalon-test.s3.amazonaws.com)
        List<WebElement> links = driver.findElements(By.cssSelector("a[href^='http']"));
        assertFalse(links.isEmpty(), "At least one external link should be present on the page");

        WebElement externalLink = null;
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (!href.contains("katalon-test.s3.amazonaws.com")) {
                externalLink = link;
                break;
            }
        }

        assertNotNull(externalLink, "No external link found on the page");

        switchToExternalLink(externalLink, externalLink.getAttribute("href"));
    }

    /* ---------- HELPER METHODS ---------- */

    private void switchToExternalLink(WebElement link, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link));
        link.click();

        // Wait until a new window is opened
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "External link should navigate to a URL containing '" + expectedDomain + "'");
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }
}