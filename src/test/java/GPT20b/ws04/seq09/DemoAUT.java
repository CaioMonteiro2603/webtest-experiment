package GPT20b.ws04.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class KatalonFormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setUpDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driverDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper Methods ---------- */

    private void reloadPage() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void closeOtherWindows(String originalHandle) {
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalHandle);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testPageLoads() {
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("form"),
                "Page title does not contain 'Form'");
    }

    @Test
    @Order(2)
    public void testFormFieldsPresence() {
        reloadPage();
        String[] fieldIds = {"name", "email", "phone", "message"};
        for (String id : fieldIds) {
            List<WebElement> elems = driver.findElements(By.id(id));
            Assertions.assertFalse(elems.isEmpty(),
                    "Expected input field with id '" + id + "' not found");
        }
    }

    @Test
    @Order(3)
    public void testEmptySubmissionError() {
        reloadPage();
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitBtn.click();

        Web error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("please fill"),
                "Expected error message for missing fields not displayed");
    }

    @Test
    @Order(4)
    public void testSuccessfulSubmission() {
        reloadPage();

        driver.findElement(By.id("name")).sendKeys("John Doe");
        driver.findElement(By.id("email")).sendKeys("john@example.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        driver.findElement(By.id("message")).sendKeys("This is a test message.");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitBtn.click();

        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("thank you"),
                "Success message not displayed after valid submission");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        reloadPage();
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[target='_blank']"));
       .assertFalse(externalLinks.isEmpty(),
                "No external links found on the page");

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            link.click();

            // Wait for new tab
            Set<String> handles = driver.getWindowHandles();
            String newHandle = handles.stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("New window did not open"));
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(href));

            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "External link URL does not contain expected domain");
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }
}