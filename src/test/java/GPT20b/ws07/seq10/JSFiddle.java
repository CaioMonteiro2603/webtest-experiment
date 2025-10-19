package GPT20b.ws07.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.net.URI;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsFiddleTests {

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

    /* ---------- Helper Methods ---------- */

    private static boolean elementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    private static WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String original = driver.getWindowHandle();
        driver.findElement(locator).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(original))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newHandle);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(original);
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertTrue(
                title.contains("JSFiddle"),
                "Title does not contain 'JSFiddle': " + title);
    }

    @Test
    @Order(2)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        By footerLinks = By.cssSelector("footer a[href]");
        Assumptions.assumeTrue(elementPresent(footerLinks), "No footer links found");

        List<WebElement> links = driver.findElements(footerLinks);
        String baseHost = extractHost(BASE_URL);
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            String host = extractHost(href);
            if (host.isEmpty() || host.equals(baseHost)) continue; // skip internal
            openExternalLink(By.cssSelector("footer a[href='" + href + "']"),
                    host);
        }
    }

    @Test
    @Order(3)
    public void testNewFiddleNavigation() {
        driver.get(BASE_URL);
        By newButton = By.xpath("//a[contains(@class,'menu-item') and .='New']");
        Assumptions.assumeTrue(elementPresent(newButton), "New button not found");
        waitClickable(newButton).click();

        wait.until(ExpectedConditions.urlContains("/new/"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/new/"),
                "Did not navigate to new fiddle page");
    }

    @Test
    @Order(4)
    public void testExamplesDropdown() {
        driver.get(BASE_URL);
        By examplesButton = By.xpath("//a[contains(@class,'menu-item') and .='Examples']");
        Assumptions.assumeTrue(elementPresent(examplesButton), "Examples menu not found");
        waitClickable(examplesButton).click();

        By exampleList = By.cssSelector("a[data-cmd='menu-examples']");
        Assumptions.assumeTrue(elementPresent(exampleList), "Example list not found");
        List<WebElement> examples = driver.findElements(exampleList);
        Assertions.assertFalse(examples.isEmpty(), "No examples found in dropdown");

        // Click first example and verify navigation
        WebElement firstExample = examples.get(0);
        String exampleName = firstExample.getText();
        firstExample.click();

        wait.until(ExpectedConditions.urlContains("/ex/"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/ex/"),
                "Clicking example did not navigate to example page");
        Assertions.assertTrue(
                driver.getTitle().contains(exampleName),
                "Page title does not contain example name: " + exampleName);
    }
}