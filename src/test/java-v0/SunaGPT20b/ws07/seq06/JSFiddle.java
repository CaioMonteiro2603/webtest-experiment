package SunaGPT20b.ws07.seq06;

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
import org.openqa.selenium.WindowType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
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

        // Load base page and collect links
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));

        List<WebElement> anchors = driver.findElements(By.tagName("a"));
        for (WebElement anchor : anchors) {
            String href = anchor.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            try {
                URL url = new URL(href);
                URL base = new URL(BASE_URL);
                if (url.getHost().equalsIgnoreCase(base.getHost())) {
                    // Internal link – check depth (one level below)
                    String path = url.getPath(); // e.g., "/abc"
                    String[] segments = path.split("/");
                    int depth = 0;
                    for (String seg : segments) {
                        if (!seg.isEmpty()) {
                            depth++;
                        }
                    }
                    if (depth <= 1) {
                        internalLinks.add(href);
                    }
                } else {
                    // External link
                    externalLinks.add(href);
                }
            } catch (MalformedURLException e) {
                // Skip malformed URLs
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
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"),
                "Base page title should contain 'jsfiddle'");
    }

    @Test
    @Order(2)
    public void testInternalLinks() {
        for (String link : internalLinks) {
            driver.get(link);
            // Wait for the page title to be non‑empty
            String pageTitle = wait.until(driver -> driver.getTitle());
            Assertions.assertFalse(pageTitle.isEmpty(),
                    "Internal page at " + link + " should have a non‑empty title");
            // Return to base page for next iteration
            driver.get(BASE_URL);
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));
        }
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        String originalWindow = driver.getWindowHandle();
        for (String link : externalLinks) {
            // Open a new tab
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(link);
            // Verify URL contains expected domain
            String currentUrl = wait.until(driver -> driver.getCurrentUrl());
            try {
                URL url = new URL(currentUrl);
                URL expected = new URL(link);
                Assertions.assertEquals(expected.getHost().toLowerCase(),
                        url.getHost().toLowerCase(),
                        "External link should navigate to the expected domain");
            } catch (MalformedURLException e) {
                Assertions.fail("Malformed URL encountered: " + currentUrl);
            }
            // Close the tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}
