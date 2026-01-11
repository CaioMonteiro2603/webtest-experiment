package GPT4.ws06.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class RESTFULL {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on BASE_URL");
    }

    private static String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private void assertExternalLink(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not an external link");
        String expectedHost = hostOf(href);
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();

        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String newWindow = after.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External host mismatch");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External host mismatch same tab");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    @Test
    @Order(1)
    public void testHomeLoad() {
        openBase();
        Assertions.assertAll("Basic check",
            () -> Assertions.assertTrue(driver.getTitle().toLowerCase().contains("automation"), "Title should contain 'automation'"),
            () -> Assertions.assertTrue(driver.findElements(By.tagName("header")).size() > 0 || driver.findElements(By.cssSelector("nav")).size() > 0, "Header or nav must be present"),
            () -> Assertions.assertTrue(driver.findElements(By.tagName("footer")).size() > 0, "Footer must be present")
        );
    }

    @Test
    @Order(2)
    public void testInternalLinksOneLevel() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        int tested = 0;
        for (WebElement link : driver.findElements(By.cssSelector("a[href]"))) {
            String href = link.getAttribute("href");
            String host = hostOf(href);
            if (href.startsWith(BASE_URL) || href.startsWith("/") || host.isEmpty() || baseHost.equalsIgnoreCase(host)) {
                link.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                Assertions.assertTrue(hostOf(driver.getCurrentUrl()).equalsIgnoreCase(baseHost), "Should remain within domain");
                tested++;
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                if (tested >= 5) break;
            }
        }
        Assertions.assertTrue(tested > 0, "Should test at least one internal link");
    }

    @Test
    @Order(3)
    public void testExternalLinksFooter() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        int tested = 0;
        for (WebElement link : driver.findElements(By.cssSelector("footer a[href], a[href]"))) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http") && !baseHost.equalsIgnoreCase(hostOf(href))) {
                assertExternalLink(link);
                tested++;
                if (tested >= 3) break;
            }
        }
        Assertions.assertTrue(tested > 0, "At least one external footer link should be tested");
    }
}
