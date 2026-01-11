package SunaGPT20b.ws08.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com";

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

    /** Helper: reset app state via the side‑menu */
    private void resetAppState() {
        // Open menu (burger) if present
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[aria-label='Menu'], button#menuButton, button#nav-toggle, button.menu")));
            menuBtn.click();
        } catch (TimeoutException ignored) { /* menu not present */ }

        // Click Reset App State if present
        try {
            WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='Reset App State']")));
            reset.click();
        } catch (TimeoutException ignored) { /* ignore */ }

        // Close menu if still open
        try {
            WebElement close = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[aria-label='Close'], button#menuClose")));
            close.click();
        } catch (TimeoutException ignored) { /* ignore */ }
    }

    /** Helper: perform login */
    private void login(String username, String password) {
        driver.get(BASE_URL + "/account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
    }

    /** Helper: verify we are logged in by checking presence of the logout link */
    private void assertLoggedIn() {
        // The logout link usually contains the text "Logout"
        boolean loggedIn = driver.findElements(By.xpath("//a[normalize-space()='Logout']")).size() > 0;
        Assertions.assertTrue(loggedIn, "User should be logged in, but logout link not found");
    }

    /** Helper: open the side‑menu and click a link by its visible text */
    private void clickMenuItem(String linkText) {
        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[aria-label='Menu'], button#menuButton, button#nav-toggle, button.menu")));
        menuBtn.click();

        // Click the desired link
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space()='" + linkText + "']")));
        link.click();

        // Wait for navigation to complete
        wait.until(ExpectedConditions.urlContains(linkText.replaceAll(" ", "").toLowerCase()));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("j2ee", "j2ee");
        // Verify we are on the catalog page (URL contains /catalog)
        wait.until(ExpectedConditions.urlContains("/catalog"));
        assertLoggedIn();
        // Return to a clean state for other tests
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalidUser", "wrongPass");
        // Expect an error message element (common id "errorMessage")
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Invalid')]")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid login");
        // Ensure we are still on the sign‑in page
        Assertions.assertTrue(driver.getCurrentUrl().contains("/account/signonForm"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        // Ensure we are logged in first
        login("j2ee", "j2ee");
        // Navigate to a product list (e.g., Fish category)
        driver.get(BASE_URL + "/catalog");
        // Wait for product list
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[name='sort']")));

        // Locate the sorting dropdown
        WebElement sortSelect = driver.findElement(By.cssSelector("select[name='sort']"));
        List<WebElement> options = sortSelect.findElements(By.tagName("option"));
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should have options");

        String previousFirst = "";
        for (WebElement option : options) {
            // Select each option
            new Select(sortSelect).selectByVisibleText(option.getText());
            // Wait for list to refresh
            wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".product-name"))));// first product element may be refreshed
            // Capture first product name
            List<WebElement> products = driver.findElements(By.cssSelector(".product-name"));
            Assertions.assertFalse(products.isEmpty(), "Product list should not be empty after sorting");
            String currentFirst = products.get(0).getText();
            // Ensure the order changes when option changes (simple check)
            if (!previousFirst.isEmpty()) {
                Assertions.assertNotEquals(previousFirst, currentFirst,
                        "First product should change after selecting a different sort option");
            }
            previousFirst = currentFirst;
        }
        // Clean up
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        // Ensure logged in
        login("j2ee", "j2ee");
        // Open menu and click All Items
        clickMenuItem("All Items");
        wait.until(ExpectedConditions.urlContains("/catalog"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog"));

        // About (external) – open and verify domain
        clickMenuItem("About");
        // New tab handling
        String original = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com") ||
                driver.getCurrentUrl().contains("about"), "External About link should open a new tab with a valid domain");
        driver.close();
        driver.switchTo().window(original);

        // Logout
        clickMenuItem("Logout");
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        // Verify we are logged out
        Assertions.assertTrue(driver.findElements(By.name("username")).size() > 0,
                "Username field should be present after logout");

        // Log back in for further tests
        login("j2ee", "j2ee");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        // Ensure logged in
        login("j2ee", "j2ee");
        // Scroll to footer
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Define expected external domains
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + domain + "']"));
            Assertions.assertFalse(links.isEmpty(), "Expected a link to " + domain);
            // Open first matching link
            String original = driver.getWindowHandle();
            links.get(0).click();
            // Switch to new window
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(original)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "External link should navigate to " + domain);
            driver.close();
            driver.switchTo().window(original);
        }
        // Return to clean state
        resetAppState();
    }
}