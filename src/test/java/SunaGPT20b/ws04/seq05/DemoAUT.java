package SunaGPT20b.ws04.seq05;

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
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testPageLoadsAndTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertNotNull(title, "Page title should not be null");
        Assertions.assertFalse(title.isEmpty(), "Page title should not be empty");
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);

        // Locate the form element
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        // Fill all text-like inputs with static data
        List<WebElement> inputs = form.findElements(By.cssSelector("input"));
        for (WebElement input : inputs) {
            String type = input.getAttribute("type");
            if (type == null) type = "";
            if (type.equalsIgnoreCase("submit") || type.equalsIgnoreCase("button") || type.equalsIgnoreCase("reset") || type.equalsIgnoreCase("hidden")) {
                continue;
            }
            // Use a simple static value based on type
            String value = "test";
            if (type.equalsIgnoreCase("email")) {
                value = "test@example.com";
            } else if (type.equalsIgnoreCase("password")) {
                value = "Password123";
            }
            input.clear();
            input.sendKeys(value);
        }

        // Click the submit button (first clickable button or input[type=submit])
        WebElement submitBtn = null;
        try {
            submitBtn = form.findElement(By.cssSelector("input[type='submit']"));
        } catch (Exception ignored) {
        }
        if (submitBtn == null) {
            try {
                submitBtn = form.findElement(By.cssSelector("button[type='submit']"));
            } catch (Exception ignored) {
            }
        }
        if (submitBtn == null) {
            // Fallback to any button inside the form
            submitBtn = form.findElement(By.tagName("button"));
        }

        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();

        // After submission, verify that the page either shows a success message or stays on the same URL
        // We wait for either a change in URL or presence of a typical success element
        boolean urlChanged = wait.until(driver -> !driver.getCurrentUrl().equals(BASE_URL));
        if (!urlChanged) {
            // Look for a generic success message container
            List<WebElement> successElements = driver.findElements(By.xpath("//*[contains(text(),'Thank you') or contains(text(),'Success') or contains(text(),'Submitted')]"));
            Assertions.assertFalse(successElements.isEmpty(), "Expected a success message after form submission");
        }
    }

    @Test
    @Order(3)
    public void testExternalLinksOneLevelDeep() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Find all external links (href starts with http and not containing the base domain)
        List<WebElement> externalLinks = driver.findElements(By.xpath("//a[starts-with(@href, 'http') and not(contains(@href, 'katalon-test.s3.amazonaws.com'))]"));

        Assertions.assertFalse(externalLinks.isEmpty(), "No external links found on the page");

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            Assertions.assertNotNull(href, "External link href should not be null");

            // Click the link
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();

            // Wait for possible new window/tab
            wait.until(driver -> driver.getWindowHandles().size() >= 1);

            Set<String> windows = driver.getWindowHandles();
            if (windows.size() > 1) {
                // Switch to the newly opened window
                String newWindow = windows.stream()
                        .filter(handle -> !handle.equals(originalWindow))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("New window not found"));
                driver.switchTo().window(newWindow);
            }

            // Verify URL contains the external domain (up to first slash after protocol)
            String domain = href.replaceFirst("^(https?://[^/]+).*", "$1");
            wait.until(ExpectedConditions.urlContains(domain.replaceFirst("https?://", "")));

            // Close external window if it was opened
            if (driver.getWindowHandles().size() > 1) {
                driver.close();
                driver.switchTo().window(originalWindow);
            } else {
                // If same window, navigate back to original page
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
            }
        }
    }

    @Test
    @Order(4)
    public void testFormResetButtonIfPresent() {
        driver.get(BASE_URL);
        // Attempt to locate a reset button
        List<WebElement> resetButtons = driver.findElements(By.xpath("//button[contains(translate(text(),'RESET','reset','RESET'),'reset') or @type='reset' or contains(@id,'reset')]"));
        if (resetButtons.isEmpty()) {
            // No reset button; test is considered passed as not applicable
            return;
        }
        WebElement resetBtn = resetButtons.get(0);
        // Fill a field to ensure reset has effect
        List<WebElement> textInputs = driver.findElements(By.cssSelector("input[type='text'], input[type='email'], input[type='password']"));
        if (!textInputs.isEmpty()) {
            WebElement sample = textInputs.get(0);
            sample.clear();
            sample.sendKeys("sample");
            Assertions.assertEquals("sample", sample.getAttribute("value"), "Input should contain the typed value before reset");
        }

        wait.until(ExpectedConditions.elementToBeClickable(resetBtn)).click();

        // Verify that the previously filled input is cleared
        if (!textInputs.isEmpty()) {
            WebElement sample = textInputs.get(0);
            Assertions.assertEquals("", sample.getAttribute("value"), "Input should be cleared after reset");
        }
    }
}