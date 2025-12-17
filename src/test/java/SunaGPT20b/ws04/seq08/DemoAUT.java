package SunaGPT20b.ws04.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final List<String> internalLinks = new ArrayList<>();
    private static final List<String> externalLinks = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(BASE_URL);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));

        List<WebElement> anchors = driver.findElements(By.tagName("a"));
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty() || href.startsWith("javascript")) {
                continue;
            }
            if (href.startsWith(BASE_URL) ||
                href.startsWith("https://katalon-test.s3.amazonaws.com") ||
                href.startsWith("/")) {
                if (!href.equals(BASE_URL)) {
                    internalLinks.add(href);
                }
            } else {
                externalLinks.add(href);
            }
        }
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
        driver.get(BASE_URL);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        Assertions.assertTrue(form.isDisplayed(), "Form should be displayed on the base page");
    }

    @Test
    @Order(2)
    public void testFormSubmissionValid() {
        driver.get(BASE_URL);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));

        List<WebElement> inputs = driver.findElements(By.cssSelector("input"));
        for (WebElement input : inputs) {
            String type = input.getAttribute("type");
            if ("text".equalsIgnoreCase(type) ||
                "email".equalsIgnoreCase(type) ||
                "password".equalsIgnoreCase(type) ||
                "search".equalsIgnoreCase(type) ||
                "tel".equalsIgnoreCase(type) ||
                "url".equalsIgnoreCase(type)) {
                input.clear();
                input.sendKeys("test");
            } else if ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) {
                if (!input.isSelected()) {
                    input.click();
                }
            }
        }

        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button, input[type='submit']")));
        submit.click();

        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        String bodyText = body.getText().toLowerCase();
        Assertions.assertTrue(bodyText.contains("thank") || bodyText.contains("success"),
                "Submission should display a success or thank‑you message");
    }

    @Test
    @Order(3)
    public void testFormSubmissionInvalid() {
        driver.get(BASE_URL);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));

        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button, input[type='submit']")));
        submit.click();

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertEquals(BASE_URL, currentUrl,
                "Invalid submission should keep the user on the same page");
    }

    @Test
    @Order(4)
    public void testInternalLinksNavigation() {
        for (String link : internalLinks) {
            driver.get(link);
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertEquals(link, currentUrl,
                    "Should navigate to internal link: " + link);
            String title = driver.getTitle();
            Assertions.assertFalse(title.isEmpty(),
                    "Page title should not be empty for internal link: " + link);
        }
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        String originalWindow = driver.getWindowHandle();

        for (String extLink : externalLinks) {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", extLink);
            wait.until(d -> d.getWindowHandles().size() > 1);

            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();

            driver.switchTo().window(newWindow);
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            String currentUrl = driver.getCurrentUrl();
            try {
                URI uri = new URI(extLink);
                String expectedHost = uri.getHost();
                Assertions.assertTrue(currentUrl.contains(expectedHost),
                        "External link should navigate to expected domain: " + extLink);
            } catch (Exception e) {
                Assertions.fail("Failed to parse external URL: " + extLink);
            }

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}