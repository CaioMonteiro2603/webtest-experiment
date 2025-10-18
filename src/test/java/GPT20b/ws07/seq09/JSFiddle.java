package GPT20b.ws07.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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

@TestMethodOrder(OrderAnnotation.class)
public class JsFiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUpDriver() {
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

    /* ---------- Helper Methods ---------- */

    private String getCurrentHandle() {
        return driver.getWindowHandle();
    }

    private void closeOtherWindows(String originalHandle) {
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalHandle);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle"),
                "Home page title does not contain 'jsfiddle'");
    }

    @Test
    @Order(2)
    public void testAboutLinkNavigation() {
        driver.get(BASE_URL);
        By aboutLink = By.linkText("About");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        link.click();

        wait.until(ExpectedConditions.urlContains("/about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/about"),
                "Did not navigate to About page");
    }

    @Test
    @Order(3)
    public void testExternalLinksHandling() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[target='_blank']"));
        Assertions.assertTrue(!externalLinks.isEmpty(),
                "No external links found with target='_blank'");

        String originalHandle = getCurrentHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            link.click();

            Set<String> handles = driver.getWindowHandles();
            String newHandle = handles.stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No new window opened"));

            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(href));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "External link URL does not contain expected domain: " + href);

            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }
}