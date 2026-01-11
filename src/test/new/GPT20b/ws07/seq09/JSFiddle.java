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
public class JSFiddle {

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
        By aboutLink = By.xpath("//a[contains(text(),'About') or contains(text(),'ABOUT') or contains(text(),'about')]");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        link.click();

        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
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
            
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
            wait.until(ExpectedConditions.elementToBeClickable(link));
            
            try {
                link.click();
            } catch (Exception e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
            }

            Set<String> handles = driver.getWindowHandles();
            if (handles.size() > 1) {
                String newHandle = handles.stream()
                        .filter(h -> !h.equals(originalHandle))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No new window opened"));

                driver.switchTo().window(newHandle);
                wait.until(ExpectedConditions.urlContains(href.substring(8)));
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.substring(8)),
                        "External link URL does not contain expected domain: " + href);

                driver.close();
                driver.switchTo().window(originalHandle);
            }
        }
    }
}