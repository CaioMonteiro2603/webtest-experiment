package SunaGPT20b.ws04.seq06;

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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.net.MalformedURLException;
import java.net.URL;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebFormTest {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String BASE_DOMAIN;
    private static WebDriver driver;
    private static WebDriverWait wait;

    static {
        String domain = "";
        try {
            domain = new URL(BASE_URL).getHost();
        } catch (MalformedURLException e) {
            // fallback to empty string; tests will fail if URL is malformed
        }
        BASE_DOMAIN = domain;
    }

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

    /** Helper to wait for page load completion */
    private void waitForPageLoad() {
        ExpectedCondition<Boolean> pageLoadCondition = webDriver ->
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                        .equals("complete");
        wait.until(pageLoadCondition);
    }

    /** Helper to extract domain from a URL string */
    private String getDomain(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    @Test
    @Order(1)
    public void testBasePageLoads() {
        driver.get(BASE_URL);
        waitForPageLoad();

        // Verify that a <form> element is present on the page
        List<WebElement> forms = driver.findElements(By.tagName("form"));
        Assertions.assertFalse(forms.isEmpty(),
                "Base page should contain at least one <form> element.");
    }

    @Test
    @Order(2)
    public void testInternalLinks() {
        driver.get(BASE_URL);
        waitForPageLoad();

        List<WebElement> linkElements = driver.findElements(By.xpath("//a[@href]"));
        Set<String> internalUrls = new HashSet<>();

        for (WebElement link : linkElements) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            // Filter internal links (same domain) and ignore anchors or javascript:
            if (href.startsWith("http") && getDomain(href).equalsIgnoreCase(BASE_DOMAIN)
                    && !href.equalsIgnoreCase(BASE_URL)
                    && !href.contains("#")
                    && !href.toLowerCase().startsWith("javascript:")) {
                internalUrls.add(href);
            }
        }

        Assertions.assertFalse(internalUrls.isEmpty(),
                "No internal links found on the base page to test.");

        for (String url : internalUrls) {
            driver.navigate().to(url);
            waitForPageLoad();

            String current = driver.getCurrentUrl();
            Assertions.assertTrue(current.startsWith(url),
                    "Navigated URL should start with expected internal URL. Expected: " + url + " but was: " + current);
        }

        // Return to base page for subsequent tests
        driver.get(BASE_URL);
        waitForPageLoad();
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        waitForPageLoad();

        List<WebElement> linkElements = driver.findElements(By.xpath("//a[@href]"));
        Set<String> externalUrls = new HashSet<>();

        for (WebElement link : linkElements) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            // Filter external links (different domain) and ignore anchors or javascript:
            if (href.startsWith("http") && !getDomain(href).equalsIgnoreCase(BASE_DOMAIN)
                    && !href.contains("#")
                    && !href.toLowerCase().startsWith("javascript:")) {
                externalUrls.add(href);
            }
        }

        Assertions.assertFalse(externalUrls.isEmpty(),
                "No external links found on the base page to test.");

        for (String url : externalUrls) {
            // Locate the link element again (in case DOM changed)
            WebElement link = driver.findElement(By.xpath("//a[@href='" + url + "']"));
            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            // Click the link
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();

            // Wait for a new window/tab if it opens
            String newWindowHandle = null;
            try {
                wait.until(driver -> {
                    Set<String> handles = driver.getWindowHandles();
                    return handles.size() > existingWindows.size();
                });
                Set<String> handlesAfter = driver.getWindowHandles();
                handlesAfter.removeAll(existingWindows);
                newWindowHandle = handlesAfter.iterator().next();
            } catch (Exception ignored) {
                // No new window opened; stay in the same window
            }

            if (newWindowHandle != null) {
                driver.switchTo().window(newWindowHandle);
                waitForPageLoad();
                String currentDomain = getDomain(driver.getCurrentUrl());
                String expectedDomain = getDomain(url);
                Assertions.assertEquals(expectedDomain, currentDomain,
                        "External link should navigate to expected domain.");
                driver.close();
                driver.switchTo().window(originalWindow);
            } else {
                // Link opened in the same window
                waitForPageLoad();
                String currentDomain = getDomain(driver.getCurrentUrl());
                String expectedDomain = getDomain(url);
                Assertions.assertEquals(expectedDomain, currentDomain,
                        "External link should navigate to expected domain.");
                // Navigate back to base page for next iteration
                driver.navigate().back();
                waitForPageLoad();
            }
        }

        // Ensure we are back on the base page
        driver.get(BASE_URL);
        waitForPageLoad();
    }
}