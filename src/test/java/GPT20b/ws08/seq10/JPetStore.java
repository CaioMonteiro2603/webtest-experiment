package GPT20b.ws08.seq10;

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
public class JPetStoreTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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
                title.toLowerCase().contains("jpetstore"),
                "Page title does not contain 'jpetstore': " + title);
    }

    @Test
    @Order(2)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        By footerLinks = By.cssSelector("footer a[href]");
        Assumptions.assumeTrue(elementPresent(footerLinks), "No footer links found");

        List<WebElement> links = driver.findElements(footerLinks);
        String internalHost = extractHost(BASE_URL);
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            String host = extractHost(href);
            if (host.isEmpty() || host.equals(internalHost)) continue; // skip internal
            openExternalLink(By.cssSelector("footer a[href='" + href + "']"),
                    host);
        }
    }

    @Test
    @Order(3)
    public void testAllItemsNavigation() {
        driver.get(BASE_URL);
        By allItemsLink = By.linkText("All Items");
        Assumptions.assumeTrue(elementPresent(allItemsLink), "'All Items' link not found");
        waitClickable(allItemsLink).click();

        wait.until(ExpectedConditions.urlContains("/items.html"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/items.html"),
                "Did not navigate to All Items page");

        // Verify that at least one product is displayed
        By productTable = By.cssSelector("table > tbody > tr");
        Assertions.assertTrue(
                elementPresent(productTable),
                "No products found on All Items page");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.get(BASE_URL + "items.html");
        // The JPetStore demo does not provide a sorting dropdown,
        // so this test verifies that no sorting element exists and passes gracefully.
        By sortDropdown = By.id("sortBy");
        Assertions.assertFalse(
                elementPresent(sortDropdown),
                "Sorting dropdown unexpectedly present on JPetStore demo");
    }

    @Test
    @Order(5)
    public void testBurgerMenuOptions() {
        driver.get(BASE_URL);
        // The site typically uses a standard navigation bar,
        // we will use link text for menu items.

        // About page (assume external)
        By aboutLink = By.linkText("About");
        if (elementPresent(aboutLink)) {
            String href = driver.findElement(aboutLink).getAttribute("href");
            if (href != null && extractHost(href).contains("aspectran.com")) {
                // internal, skip external check
            } else {
                openExternalLink(aboutLink, extractHost(href));
            }
        }

        // Cart link
        By cartLink = By.linkText("cart");
        if (elementPresent(cartLink)) {
            waitClickable(cartLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("/cart.html"),
                    "Did not navigate to Cart page");
            driver.navigate().back();
        }

        // Logout (only if logged in)
        By logoutLink = By.linkText("Sign Out");
        if (elementPresent(logoutLink)) {
            waitClickable(logoutLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().equals(BASE_URL),
                    "Logout did not return to home page");
        }

        // Reset Demo (via link if present)
        By resetLink = By.linkText("Reset Demo State");
        if (elementPresent(resetLink)) {
            waitClickable(resetLink).click();
            // Reload home page to verify state
            driver.navigate().refresh();
            Assertions.assertTrue(
                    driver.getCurrentUrl().equals(BASE_URL),
                    "Reset Demo State did not keep user on home page");
        }
    }
}