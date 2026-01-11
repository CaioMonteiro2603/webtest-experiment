package SunaGPT20b.ws07.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
    wait.until(ExpectedConditions.titleContains("JSFiddle"));
    Assertions.assertTrue(driver.getTitle().contains("JSFiddle"),
            "Home page title should contain 'JSFiddle'");
    Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
            "Home page URL should be the base URL");
}

@Test
@Order(2)
public void testCreateNewFiddle() {
    driver.get(BASE_URL);
    WebElement newBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[@title='Create new fiddle']")));
    newBtn.click();

    wait.until(d -> d.getCurrentUrl().matches("https://jsfiddle\\.net/.+/"));
    String newUrl = driver.getCurrentUrl();
    Assertions.assertTrue(newUrl.matches("https://jsfiddle\\.net/.+/"),
            "URL after creating new fiddle should match pattern");
}

@Test
@Order(3)
public void testRunButtonExists() {
    driver.get(BASE_URL);
    if (!driver.getCurrentUrl().matches("https://jsfiddle\\.net/.+/")) {
        WebElement newBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@title='Create new fiddle']")));
        newBtn.click();
        wait.until(d -> d.getCurrentUrl().matches("https://jsfiddle\\.net/.+/"));
    }

    WebElement runBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.run, button#run, button[data-action='run']")));
    Assertions.assertNotNull(runBtn, "Run button should be present");
    runBtn.click();

    WebElement resultIframe = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("iframe.result, iframe#result")));
    driver.switchTo().frame(resultIframe);
    WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    Assertions.assertNotNull(body, "Result iframe body should be present after running");
    driver.switchTo().defaultContent();
}

@Test
@Order(4)
public void testExternalFooterLinks() {
    driver.get(BASE_URL);
    Map<String, String> links = new LinkedHashMap<>();
    links.put("Twitter", "twitter.com");
    links.put("GitHub", "github.com");
    links.put("Facebook", "facebook.com");

    for (Map.Entry<String, String> entry : links.entrySet()) {
        String expectedDomain = entry.getValue();

        List<WebElement> elems = driver.findElements(
                By.xpath("//footer//a[contains(@href,'" + expectedDomain + "')]"));
        Assertions.assertFalse(elems.isEmpty(),
                "Footer should contain a link to " + expectedDomain);

        String originalWindow = driver.getWindowHandle();
        elems.get(0).click();
        
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should navigate to domain containing " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
}