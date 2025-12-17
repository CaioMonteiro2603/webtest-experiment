package SunaGPT20b.ws05.seq02;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Set<String> visitedInternal = new HashSet<>();

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testAllPagesOneLevelDeep() throws MalformedURLException {
        // Load base page
        driver.get(BASE_URL);
        waitForBody();

        // Verify base page title is not empty
        Assertions.assertFalse(driver.getTitle().isEmpty(),
                "Base page title should not be empty");

        // Collect all links on base page
        List<WebElement> baseLinks = driver.findElements(By.tagName("a"));
        for (WebElement link : baseLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            URL absoluteUrl = new URL(new URL(BASE_URL), href);
            String domain = absoluteUrl.getHost();
            String baseDomain = new URL(BASE_URL).getHost();

            if (domain.equals(baseDomain)) {
                // Internal link - one level below
                String normalized = absoluteUrl.toString();
                if (!visitedInternal.contains(normalized) && !normalized.equals(BASE_URL)) {
                    visitedInternal.add(normalized);
                    testInternalPage(normalized);
                }
            } else {
                // External link on base page
                testExternalLink(absoluteUrl);
            }
        }
    }

    private void testInternalPage(String url) throws MalformedURLException {
        driver.get(url);
        waitForBody();

        // Verify page title is not empty
        Assertions.assertFalse(driver.getTitle().isEmpty(),
                "Internal page title should not be empty for URL: " + url);

        // Collect links on this internal page
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            URL absoluteUrl = new URL(new URL(url), href);
            String domain = absoluteUrl.getHost();
            String baseDomain = new URL(BASE_URL).getHost();

            if (!domain.equals(baseDomain)) {
                // External link on internal page
                testExternalLink(absoluteUrl);
            }
        }
    }

    private void testExternalLink(URL externalUrl) {
        // Open external link in a new tab
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(externalUrl.toString());

        // Wait for page load
        waitForBody();

        // Verify URL contains expected domain
        Assertions.assertTrue(driver.getCurrentUrl().contains(externalUrl.getHost()),
                "External URL should contain its domain: " + externalUrl);

        // Close the tab and switch back
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());
    }

    private void waitForBody() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }
}