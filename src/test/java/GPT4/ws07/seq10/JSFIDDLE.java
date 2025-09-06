package GPT4.ws07.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;
import java.net.URI;

@TestMethodOrder(OrderAnnotation.class)
public class JsFiddleHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
    }

    private String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private void assertExternalOpens(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not an external link");
        String expectedHost = hostOf(href);
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();
        wait.until(d -> d.getWindowHandles().size() > before.size() || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String newWindow = after.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External link host mismatch");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External link host mismatch same tab");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        openBase();
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle") || title.toLowerCase().contains("fiddle"), "Title should mention JSFiddle");
        Assertions.assertTrue(driver.findElements(By.cssSelector("header")).size() > 0 || driver.findElements(By.cssSelector(".top")).size() > 0, "Header should exist");
        Assertions.assertTrue(driver.findElements(By.tagName("footer")).size() > 0, "Footer should exist");
    }

    @Test
    @Order(2)
    public void testInternalLinksOneLevel() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        int checked = 0;
        for (WebElement link : driver.findElements(By.cssSelector("a[href]"))) {
            String href = link.getAttribute("href");
            String host = hostOf(href);
            if (href.startsWith(BASE_URL) || href.startsWith("/") || host.isEmpty() || host.equalsIgnoreCase(baseHost)) {
                link.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                Assertions.assertTrue(hostOf(driver.getCurrentUrl()).equalsIgnoreCase(baseHost), "Should remain on same domain");
                checked++;
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                if (checked >= 5) break;
            }
        }
        Assertions.assertTrue(checked > 0, "Should test at least one internal link");
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        int tested = 0;
        for (WebElement link : driver.findElements(By.cssSelector("a[href]"))) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http") && !hostOf(href).equalsIgnoreCase(baseHost)) {
                assertExternalOpens(link);
                tested++;
                if (tested >= 3) break;
            }
        }
        Assertions.assertTrue(tested > 0, "Should test at least one external link");
    }
}
