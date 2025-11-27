package SunaGPT20b.ws05.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class WebTestSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        // Wait for the body element to be present
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(body.isDisplayed(), "Home page body should be displayed");
        // Basic title check (if any)
        String title = driver.getTitle();
        Assertions.assertFalse(title.isBlank(), "Page title should not be blank");
    }

    @Test
    @Order(2)
    public void testAllLinksOneLevelDeep() {
        driver.get(BASE_URL);
        // Wait for page load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        List<WebElement> links = driver.findElements(By.tagName("a"));
        Assertions.assertFalse(links.isEmpty(), "There should be at least one link on the home page");

        String baseDomain = "cac-tat.s3.eu-central-1.amazonaws.com";

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isBlank()) {
                continue; // skip empty hrefs
            }

            // Resolve relative URLs
            if (href.startsWith("/")) {
                href = "https://cac-tat.s3.eu-central-1.amazonaws.com" + href;
            }

            if (href.contains(baseDomain)) {
                // Internal link – navigate and verify URL contains the path
                String originalWindow = driver.getWindowHandle();
                driver.navigate().to(href);
                wait.until(ExpectedConditions.urlContains(href));
                Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                        "Internal link should navigate to its URL");
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            } else {
                // External link – open in new tab, verify domain, then close
                String originalWindow = driver.getWindowHandle();
                // Open link in new tab using JavaScript
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
                // Switch to new tab
                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newTab = windows.iterator().next();
                driver.switchTo().window(newTab);
                // Verify domain
                String currentUrl = driver.getCurrentUrl();
                String expectedDomain = href.replaceAll("^(https?://)?([^/]+).*", "$2");
                Assertions.assertTrue(currentUrl.contains(expectedDomain),
                        "External link should contain expected domain: " + expectedDomain);
                // Close tab and switch back
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    // Example placeholder for login test – will run only if login form exists
    @Test
    @Order(3)
    public void testLoginIfPresent() {
        driver.get(BASE_URL);
        // Look for a typical login form
        List<WebElement> usernameFields = driver.findElements(By.id("user-name"));
        if (usernameFields.isEmpty()) {
            // No login form present – skip test
            return;
        }
        WebElement username = usernameFields.get(0);
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        username.clear();
        username.sendKeys("standard_user");
        password.clear();
        password.sendKeys("secret_sauce");
        loginBtn.click();

        // Wait for navigation to inventory page (example path contains /inventory)
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After login, URL should contain /inventory");
    }

    // Example placeholder for sorting dropdown – will run only if present
    @Test
    @Order(4)
    public void testSortingDropdownIfPresent() {
        driver.get(BASE_URL);
        // Attempt to locate a sorting dropdown
        List<WebElement> selects = driver.findElements(By.cssSelector("select[data-test='product_sort_container']"));
        if (selects.isEmpty()) {
            return; // No sorting dropdown on this site
        }
        WebElement dropdown = selects.get(0);
        Select select = new Select(dropdown);
        // Iterate over options
        List<WebElement> options = select.getOptions();
        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            // Verify that the first product name changes after sorting
            // This is a generic check – ensure at least one product element is present
            List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
            Assertions.assertFalse(items.isEmpty(), "Product list should not be empty after sorting");
        }
    }

    // Example placeholder for menu actions – will run only if menu exists
    @Test
    @Order(5)
    public void testMenuActionsIfPresent() {
        driver.get(BASE_URL);
        List<WebElement> menuButtons = driver.findElements(By.id("react-burger-menu-btn"));
        if (menuButtons.isEmpty()) {
            return; // No burger menu
        }
        WebElement menuBtn = menuButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        // Click All Items
        List<WebElement> allItems = driver.findElements(By.id("inventory_sidebar_link"));
        if (!allItems.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(allItems.get(0))).click();
            wait.until(ExpectedConditions.urlContains("/inventory"));
        }

        // Click About (external)
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            WebElement about = aboutLinks.get(0);
            // Open in new tab
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", about.getAttribute("href"));
            Set<String> windows = driver.getWindowHandles();
            String original = driver.getWindowHandle();
            windows.remove(original);
            String newTab = windows.iterator().next();
            driver.switchTo().window(newTab);
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains("about") || currentUrl.contains("About"),
                    "External About link should navigate to an about page");
            driver.close();
            driver.switchTo().window(original);
        }

        // Logout if present
        List<WebElement> logoutLinks = driver.findElements(By.id("logout_sidebar_link"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            // After logout, expect login page (e.g., URL contains /login)
            wait.until(ExpectedConditions.urlContains("login"));
        }

        // Reset App State if present
        List<WebElement> resetLinks = driver.findElements(By.id("reset_sidebar_link"));
        if (!resetLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLinks.get(0))).click();
            // Verify that cart badge is cleared (if present)
            List<WebElement> badge = driver.findElements(By.className("shopping_cart_badge"));
            if (!badge.isEmpty()) {
                Assertions.assertEquals("0", badge.get(0).getText(),
                        "Cart badge should be cleared after reset");
            }
        }
    }
}