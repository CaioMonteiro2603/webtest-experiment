package GPT20b.ws05.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class CacTatTest {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleIs("Cac Tat"));
        Assertions.assertTrue(driver.getTitle().contains("Cac Tat"),
                "Home page title should contain 'Cac Tat'.");
    }

    @Test
    @Order(2)
    public void testInternalLinks() throws Exception {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.tagName("a"));
        List<String> internalUrls = links.stream()
                .map(el -> el.getAttribute("href"))
                .filter(href -> href != null && href.startsWith(BASE_URL))
                .distinct()
                .collect(Collectors.toList());

        String baseHandle = driver.getWindowHandle();

        for (String url : internalUrls) {
            driver.navigate().to(url);
            wait.until(ExpectedConditions.titleIsNotEmpty());
            Assertions.assertFalse(driver.getTitle().isEmpty(),
                    "Page at " + url + " should have a non‑empty title.");

            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }

        driver.switchTo().window(baseHandle);
    }

    @Test
    @Order(3)
    public void testExternalLinks() throws Exception {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.tagName("a"));
        List<WebElement> externalLinks = links.stream()
                .filter(el -> {
                    String href = el.getAttribute("href");
                    if (href == null) return false;
                    try {
                        return !new URL(href).getHost().contains("s3.eu-central-1.amazonaws.com");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        String originalHandle = driver.getWindowHandle();

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            link.click();

            // Wait for either a new window or the URL to change
            wait.until(driver1 -> {
                Set<String> handles = driver1.getWindowHandles();
                return handles.size() > 1 || !driver1.getCurrentUrl().equals(BASE_URL);
            });

            Set<String> handles = driver.getWindowHandles();
            if (handles.size() > 1) {
                handles.remove(originalHandle);
                String newHandle = handles.iterator().next();
                driver.switchTo().window(newHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                        "External link should navigate to URL containing " + href);
                driver.close();
                driver.switchTo().window(originalHandle);
            } else {
                Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                        "External link should navigate to URL containing " + href);
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
            }
        }
    }
}