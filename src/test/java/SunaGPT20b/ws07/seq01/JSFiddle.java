package SunaGPT20b.ws07.seq01;

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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private static void waitForTitleNotEmpty() {
        wait.until(d -> {
            String title = d.getTitle();
            return title != null && !title.trim().isEmpty();
        });
    }

    private static boolean isExternal(String href) {
        if (href == null) return false;
        if (href.startsWith("mailto:") || href.startsWith("javascript:")) return false;
        return !href.startsWith(BASE_URL);
    }

    private static String getDomain(String urlStr) {
        try {
 URL url = new URL(urlStr);
            return url.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private static void collectExternalLinksFromCurrentPage(Set<String> collector) {
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (isExternal(href)) {
                collector.add(href);
            }
        }
    }

    @Test
    @Order(1)
    public void testHomePage() {
    	driver.get(BASE_URL);
    	waitForTitleNotEmpty(); 
    	Assertions.assertTrue(driver.getTitle().length() > 0);
    }

    @Test
    @Order(2)
    public void testInternalLinks() {
        driver.get(BASE_URL);
        waitForTitleNotEmpty();

        // Gather internal hrefs from the home page
        List<String> internalHrefs = new ArrayList<>();
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                internalHrefs.add(href);
            }
        }

        // Visit each internal link and verify the page loads
        for (String href : internalHrefs) {
            driver.navigate().to(href);
            waitForTitleNotEmpty();
            Assertions.assertTrue(driver.getTitle().length() > 0,
                    "Internal page title should be non‑empty for " + href);
            driver.navigate().back();
            waitForTitleNotEmpty();
        }
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        Set<String> externalLinks = new HashSet<>();

        // Collect external links from the home page
        driver.get(BASE_URL);
        waitForTitleNotEmpty();
        collectExternalLinksFromCurrentPage(externalLinks);

        // Collect internal pages to also scan for external links
        List<String> internalPages = new ArrayList<>();
        List<WebElement> homeLinks = driver.findElements(By.tagName("a"));
        for (WebElement link : homeLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                internalPages.add(href);
            }
        }

        // Scan each internal page for external links
        for (String pageUrl : internalPages) {
            driver.navigate().to(pageUrl);
            waitForTitleNotEmpty();
            collectExternalLinksFromCurrentPage(externalLinks);
            driver.navigate().back();
            waitForTitleNotEmpty();
        }

        // Verify each external link opens a new tab/window with the expected domain
        String originalWindow = driver.getWindowHandle();
        for (String extUrl : externalLinks) {
            if (extUrl.startsWith("mailto:") || extUrl.startsWith("javascript:")) {
                continue;
            }
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", extUrl);
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(originalWindow);
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && !d.getCurrentUrl().isEmpty());
            String currentUrl = driver.getCurrentUrl();
            String expectedDomain = getDomain(extUrl);
            Assertions.assertTrue(currentUrl.contains(expectedDomain),
                    "External link " + extUrl + " should contain domain " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}