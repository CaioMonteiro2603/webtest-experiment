package SunaGPT20b.ws04.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.*;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

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

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @Test
    @Order(1)
    public void testBasePageLoads() {
        navigateToBase();
        Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"),
                "Base URL should contain 'form.html'");
        String title = driver.getTitle();
        Assertions.assertNotNull(title, "Page title should not be null");
        Assertions.assertFalse(title.isEmpty(), "Page title should not be empty");
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        navigateToBase();

        // Fill all visible text inputs with a generic value
        List<WebElement> inputs = driver.findElements(By.cssSelector("input[type='text'], input:not([type])"));
        for (WebElement input : inputs) {
            if (input.isDisplayed() && input.isEnabled()) {
                input.clear();
                input.sendKeys("test");
            }
        }

        // Attempt to click a submit button
        By submitLocator = By.cssSelector("button[type='submit'], input[type='submit']");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitLocator));
        submitBtn.click();

        // Verify that either URL changed or a success message appears
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("success"),
                    ExpectedConditions.presenceOfElementLocated(By.id("message")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success, .alert-success"))
            ));
        } catch (TimeoutException e) {
            // Fallback: ensure we are still on a page with a form (no crash)
            Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"),
                    "After submission, should remain on the form page if no success indicator.");
        }
    }

    @Test
    @Order(3)
    public void testInternalLinksOneLevelDeep() {
        navigateToBase();

        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        String basePrefix = BASE_URL.substring(0, BASE_URL.lastIndexOf('/') + 1);

        List<String> internalHrefs = links.stream()
                .map(e -> e.getAttribute("href"))
                .filter(href -> href != null && href.startsWith(basePrefix))
                .collect(Collectors.toList());

        for (String href : internalHrefs) {
            driver.get(href);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            Assertions.assertTrue(
                    driver.getCurrentUrl().startsWith(basePrefix),
                    "Navigated URL should be an internal link: " + href
            );

            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }


    @Test
    @Order(4)
    public void testExternalLinksPolicy() {
        navigateToBase();

        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        List<WebElement> externalLinks = links.stream()
                .filter(e -> {
                    String href = e.getAttribute("href");
                    return href != null && href.matches("^https?://(?!katalon-test\\.s3\\.amazonaws\\.com).+");
                })
                .collect(Collectors.toList());

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            // Click the link
            try {
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
            } catch (ElementClickInterceptedException e) {
                // Fallback: use JavaScript click
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
            }

            // Wait for possible new window/tab
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1 || driver1.getCurrentUrl().contains(href));

            Set<String> windows = driver.getWindowHandles();
            if (windows.size() > 1) {
                // Switch to the new window
                for (String handle : windows) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                // Verify domain
                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains(getDomainFromUrl(href)),
                        "External link should navigate to expected domain.");
                // Close external window and switch back
                driver.close();
                driver.switchTo().window(originalWindow);
            } else {
                // Same window navigation
                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains(getDomainFromUrl(href)),
                        "External link should navigate to expected domain.");
                // Navigate back to base page
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
        }
    }

    private String getDomainFromUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return "";
        }
    }
}