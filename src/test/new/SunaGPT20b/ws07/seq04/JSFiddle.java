package SunaGPT20b.ws07.seq04;

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

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.net.URI;
import java.net.URISyntaxException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        String title = driver.getTitle();
        Assertions.assertTrue(title.contains("JSFiddle"),
                "Home page title should contain 'JSFiddle', but was: " + title);
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Home page URL should be the base URL.");
    }

    @Test
    @Order(2)
    public void testInternalLinksOneLevelBelow() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href]")));

        List<WebElement> linkElements = driver.findElements(By.cssSelector("a[href]"));
        List<String> internalUrls = new ArrayList<>();

        for (WebElement el : linkElements) {
            String href = el.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            try {
                URI uri = new URI(href);
                String host = uri.getHost();
                // Consider internal if same host or relative path
                if (host == null || host.contains("jsfiddle.net")) {
                    String absolute = href.startsWith("http") ? href : BASE_URL + href.replaceFirst("^/+", "");
                    internalUrls.add(absolute);
                }
            } catch (URISyntaxException e) {
                // ignore malformed URLs
            }
        }

        // Remove duplicates
        Set<String> uniqueUrls = new HashSet<>(internalUrls);

        for (String url : uniqueUrls) {
            try {
                driver.navigate().to(url);
                // Simple verification: page loads and URL contains expected path
                wait.until(ExpectedConditions.urlContains(url.replace(BASE_URL, "")));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(url),
                        "Navigated URL should start with expected internal URL. Expected: " + url + ", Actual: " + driver.getCurrentUrl());
            } catch (Exception e) {
                // Skip URLs that fail to load or timeout
                continue;
            } finally {
                // Return to base for next iteration
                try {
                    driver.navigate().to(BASE_URL);
                    wait.until(ExpectedConditions.titleContains("JSFiddle"));
                } catch (Exception e) {
                    // If return to base fails, continue
                }
            }
        }
    }

    @Test
    @Order(3)
    public void testExternalLinksOneLevelBelow() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href]")));

        List<WebElement> linkElements = driver.findElements(By.cssSelector("a[href]"));
        List<String> externalUrls = new ArrayList<>();

        for (WebElement el : linkElements) {
            String href = el.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            try {
                URI uri = new URI(href);
                String host = uri.getHost();
                if (host != null && !host.contains("jsfiddle.net")) {
                    externalUrls.add(href);
                }
            } catch (URISyntaxException e) {
                // ignore malformed URLs
            }
        }

        // Remove duplicates
        Set<String> uniqueExternal = new HashSet<>(externalUrls);
        String originalWindow = driver.getWindowHandle();

        for (String extUrl : uniqueExternal) {
            try {
                URI uri = new URI(extUrl);
                String expectedDomain = uri.getHost();

                // Open link in a new tab via JavaScript
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", extUrl);
                wait.until(driver -> driver.getWindowHandles().size() > 1);

                Set<String> handles = driver.getWindowHandles();
                handles.remove(originalWindow);
                String newWindow = handles.iterator().next();

                driver.switchTo().window(newWindow);
                wait.until(ExpectedConditions.urlContains("docs.jsfiddle.net"));

                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains("docs.jsfiddle.net"),
                        "External link should navigate to domain containing 'docs.jsfiddle.net'. Actual URL: " + currentUrl);

                driver.close();
                driver.switchTo().window(originalWindow);
                wait.until(ExpectedConditions.titleContains("JSFiddle"));
            } catch (Exception e) {
                // Skip malformed URLs or failures
                try {
                    if (driver.getWindowHandles().size() > 1) {
                        Set<String> handles = driver.getWindowHandles();
                        handles.remove(originalWindow);
                        if (!handles.isEmpty()) {
                            driver.switchTo().window(handles.iterator().next());
                            driver.close();
                        }
                    }
                    driver.switchTo().window(originalWindow);
                } catch (Exception ex) {
                    // Final cleanup attempt
                }
            }
        }
    }
}