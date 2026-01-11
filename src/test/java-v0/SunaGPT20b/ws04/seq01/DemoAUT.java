package SunaGPT20b.ws04.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.net.URL;
import java.net.MalformedURLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public void testBasePageLoads() {
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("form.html"),
                "Base URL should contain 'form.html', but was: " + currentUrl);
        String title = driver.getTitle();
        Assertions.assertFalse(title.isEmpty(),
                "Page title should not be empty on base page.");
    }

    @Test
    @Order(2)
    public void testFormElementsPresent() {
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        List<WebElement> inputs = driver.findElements(By.cssSelector("input, textarea, select"));
        Assertions.assertFalse(inputs.isEmpty(),
                "Form should contain at least one input, textarea, or select element.");
        // Example check: ensure a submit button exists
        List<WebElement> submitButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        Assertions.assertFalse(submitButtons.isEmpty(),
                "Form should contain a submit button.");
    }

    @Test
    @Order(3)
    public void testAllOneLevelLinks() throws MalformedURLException {
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // Collect hrefs from all anchor elements
        List<String> hrefs = driver.findElements(By.tagName("a"))
                .stream()
                .map(e -> e.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> !h.trim().isEmpty())
                .filter(h -> !h.startsWith("javascript:"))
                .filter(h -> !h.startsWith("#"))
                .collect(Collectors.toList());

        URL base = new URL(BASE_URL);
        String baseHost = base.getHost();

        for (String rawHref : hrefs) {
            String absoluteUrl;
            try {
                URL url = new URL(rawHref);
                absoluteUrl = url.toString();
            } catch (MalformedURLException e) {
                // Relative URL, resolve against base
                absoluteUrl = new URL(base, rawHref).toString();
            }

            URL targetUrl = new URL(absoluteUrl);
            String targetHost = targetUrl.getHost();

            // Navigate to the link
            driver.navigate().to(absoluteUrl);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            String currentUrl = driver.getCurrentUrl();

            if (targetHost.equalsIgnoreCase(baseHost)) {
                // Internal link: verify URL contains the path
                Assertions.assertTrue(currentUrl.contains(targetUrl.getPath()),
                        "Internal link should navigate to URL containing path '" + targetUrl.getPath() + "', but was: " + currentUrl);
            } else {
                // External link: verify domain presence
                Assertions.assertTrue(currentUrl.contains(targetHost),
                        "External link should navigate to domain '" + targetHost + "', but was: " + currentUrl);
            }

            // Return to base page for next iteration
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }
}