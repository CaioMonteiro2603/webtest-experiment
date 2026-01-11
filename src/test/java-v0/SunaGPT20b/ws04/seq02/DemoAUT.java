package SunaGPT20b.ws04.seq02;

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
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public void testFormLoads() {
        driver.get(BASE_URL);
        // Verify that the form element is present
        WebElement form = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("form")));
        Assertions.assertTrue(form.isDisplayed(),
                "The form should be visible on the page.");
    }

    @Test
    @Order(2)
    public void testValidFormSubmission() {
        driver.get(BASE_URL);

        // Fill text inputs (first name, last name, email)
        List<WebElement> textInputs = driver.findElements(By.cssSelector("input[type='text'], input[type='email']"));
        for (WebElement input : textInputs) {
            String type = input.getAttribute("type");
            if ("email".equalsIgnoreCase(type)) {
                input.clear();
                input.sendKeys("test@example.com");
            } else {
                input.clear();
                input.sendKeys("Test");
            }
        }

        // Fill numeric input (age) if present
        List<WebElement> numberInputs = driver.findElements(By.cssSelector("input[type='number']"));
        for (WebElement input : numberInputs) {
            input.clear();
            input.sendKeys("30");
        }

        // Select a gender radio button if present
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        if (!radios.isEmpty()) {
            radios.get(0).click();
        }

        // Select a country from dropdown if present
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (!selects.isEmpty()) {
            WebElement select = selects.get(0);
            select.click();
            // Choose the first option that is not the placeholder
            List<WebElement> options = select.findElements(By.tagName("option"));
            for (WebElement option : options) {
                if (!option.getAttribute("value").isEmpty()) {
                    option.click();
                    break;
                }
            }
        }

        // Fill textarea if present
        List<WebElement> textareas = driver.findElements(By.tagName("textarea"));
        if (!textareas.isEmpty()) {
            WebElement textarea = textareas.get(0);
            textarea.clear();
            textarea.sendKeys("Automation test message.");
        }

        // Click the submit button
        WebElement submitBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[type='submit'], input[type='submit']")));
        submitBtn.click();

        // Expect an alert indicating successful submission
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = alert.getText();
        Assertions.assertTrue(
                alertText.toLowerCase().contains("submitted") ||
                alertText.toLowerCase().contains("thank"),
                "Alert should indicate successful form submission.");
        alert.accept();
    }

    @Test
    @Order(3)
    public void testInvalidFormSubmission() {
        driver.get(BASE_URL);

        // Ensure required fields are empty
        List<WebElement> requiredInputs = driver.findElements(By.cssSelector("input[required], textarea[required]"));
        for (WebElement input : requiredInputs) {
            input.clear();
        }

        // Click the submit button
        WebElement submitBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[type='submit'], input[type='submit']")));
        submitBtn.click();

        // Expect an alert with validation error
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = alert.getText();
        Assertions.assertTrue(
                alertText.toLowerCase().contains("required") ||
                alertText.toLowerCase().contains("error"),
                "Alert should indicate validation error for missing required fields.");
        alert.accept();
    }

    @Test
    @Order(4)
    public void testLinksOneLevelDeep() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Collect all hrefs from anchor tags
        List<String> hrefs = driver.findElements(By.tagName("a")).stream()
                .map(e -> e.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> !h.trim().isEmpty())
                .filter(h -> !h.startsWith("javascript:"))
                .collect(Collectors.toList());

        for (String href : hrefs) {
            // Skip same-page anchors
            if (href.startsWith("#")) {
                continue;
            }

            boolean isExternal = !href.contains("katalon-test.s3.amazonaws.com");
            if (isExternal) {
                // Open external link in a new tab
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
                Set<String> handles = driver.getWindowHandles();
                // Switch to the newly opened tab
                for (String handle : handles) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                // Verify the URL contains the expected domain
                String expectedDomain = href.replaceFirst("^(https?://[^/]+).*$", "$1");
                wait.until(ExpectedConditions.urlContains(expectedDomain.replace("https://", "").replace("http://", "")));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "External link should navigate to its domain: " + expectedDomain);
                // Close the external tab and switch back
                driver.close();
                driver.switchTo().window(originalWindow);
            } else {
                // Internal link: navigate, verify load, then go back
                driver.navigate().to(href);
                wait.until(ExpectedConditions.urlContains(href));
                Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                        "Internal link should navigate to: " + href);
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
            }
        }
    }
}