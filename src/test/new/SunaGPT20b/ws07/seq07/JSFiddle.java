package SunaGPT20b.ws07.seq07;

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
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
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

    /** Helper to navigate to base URL before each test */
    private void goToBase() {
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
    }

    /** Test that the home page loads and URL is correct */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        goToBase();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                "Home page URL should start with " + BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"),
                "Page title should contain 'jsfiddle'");
    }

    /** Test all internal links that are exactly one level below the base URL */
    @Test
    @Order(2)
    public void testInternalOneLevelLinks() {
        goToBase();

        List<WebElement> anchorElements = driver.findElements(By.cssSelector("a[href]"));
        List<String> internalLinks = anchorElements.stream()
                .map(e -> e.getAttribute("href"))
                .filter(href -> href != null && href.matches("^https://jsfiddle\\.net/[^/]+/?$"))
                .distinct()
                .collect(Collectors.toList());

        Assertions.assertFalse(internalLinks.isEmpty(),
                "There should be at least one internal one‑level link on the home page.");

        for (String link : internalLinks) {
            driver.navigate().to(link);
            wait.until(ExpectedConditions.urlContains(link));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(link),
                    "Navigated URL should start with the expected link: " + link);
            // Return to home for the next iteration
            driver.navigate().back();
            wait.until(ExpectedConditions.titleContains("JSFiddle"));
        }
    }

    /** Test external links present on the home page (one level only) */
    @Test
    @Order(3)
    public void testExternalLinks() {
        goToBase();

        List<WebElement> anchorElements = driver.findElements(By.cssSelector("a[href]"));
        List<WebElement> externalLinks = anchorElements.stream()
                .filter(e -> {
                    String href = e.getAttribute("href");
                    return href != null && href.startsWith("http") && !href.contains("jsfiddle.net");
                })
                .collect(Collectors.toList());

        Assertions.assertFalse(externalLinks.isEmpty(),
                "There should be at least one external link on the home page.");

        for (WebElement linkElement : externalLinks) {
            String href = linkElement.getAttribute("href");
            String expectedDomain = href.replaceAll("^(https?://[^/]+).*$", "$1");

            // Ensure the element is clickable before clicking
            wait.until(ExpectedConditions.elementToBeClickable(linkElement));

            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            linkElement.click();

            // Wait for a new window/tab if it opens
            wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());

            Set<String> allWindows = driver.getWindowHandles();
            allWindows.removeAll(existingWindows);
            String newWindowHandle = allWindows.iterator().next();

            driver.switchTo().window(newWindowHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));

            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External page URL should contain expected domain: " + expectedDomain);

            driver.close(); // close external tab
            driver.switchTo().window(originalWindow);
            // Return to base page state
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.titleContains("JSFiddle"));
        }
    }
}