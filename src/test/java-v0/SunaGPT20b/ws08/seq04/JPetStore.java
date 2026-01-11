package SunaGPT20b.ws08.seq04;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore{

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    @BeforeEach
    public void setUp() {
        driver.get(BASE_URL);
        // Ensure a clean state by resetting the app via the menu if present
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuBtn.click();
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            // close the menu
            WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeBtn.click();
        } catch (Exception ignored) {
        }
    }

    // ---------- Helper Methods ----------
    private void login(String user, String pass) {
        driver.get(BASE_URL + "login");
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
        loginBtn.click();
    }

    private void openMenuAndClick(String linkId) {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement target = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        target.click();
        // close menu if still open
        List<WebElement> closeButtons = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeButtons.isEmpty()) {
            closeButtons.get(0).click();
        }
    }

    // ---------- Tests ----------
    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After valid login, URL should contain /inventory");
        WebElement inventoryHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.title")));
        Assertions.assertEquals("PRODUCTS", inventoryHeader.getText(),
                "Inventory page should display PRODUCTS title");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalidUser", "invalidPass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));

        By sortSelect = By.cssSelector("select[data-test='product_sort_container']");
        Select select = new Select(wait.until(ExpectedConditions.elementToBeClickable(sortSelect)));

        // Capture first product name for each sort option
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        String previousFirstItem = null;

        for (String option : options) {
            select.selectByVisibleText(option);
            // wait for sorting to apply
            wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".inventory_item"))));
            List<WebElement> items = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.cssSelector(".inventory_item_name")));
            Assertions.assertFalse(items.isEmpty(), "Sorted list should contain items");
            String firstItem = items.get(0).getText();
            Assertions.assertNotNull(firstItem, "First item name should not be null");
            if (previousFirstItem != null) {
                Assertions.assertNotEquals(previousFirstItem, firstItem,
                        "First item should change after sorting by " + option);
            }
            previousFirstItem = firstItem;
        }
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login(USERNAME, PASSWORD);
        // All Items (should stay on inventory)
        openMenuAndClick("inventory_sidebar_link");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items link should navigate to inventory page");

        // About (external)
        openMenuAndClick("about_sidebar_link");
        String originalWindow = driver.getWindowHandle();
        Set<String> windows = driver.getWindowHandles();
        Assertions.assertEquals(2, windows.size(), "About link should open a new tab");
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"),
                "About page should be on GitHub domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Logout
        openMenuAndClick("logout_sidebar_link");
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout should redirect to login page");

        // Reset App State (no visible effect but should not error)
        login(USERNAME, PASSWORD);
        openMenuAndClick("reset_sidebar_link");
        // Verify we are still on inventory page after reset
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Reset App State should keep user on inventory page");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        // Footer social links selectors (example IDs)
        String[] linkIds = {"twitter_link", "facebook_link", "linkedin_link"};
        String[] expectedDomains = {"twitter.com", "facebook.com", "linkedin.com"};

        for (int i = 0; i < linkIds.length; i++) {
            List<WebElement> links = driver.findElements(By.id(linkIds[i]));
            if (links.isEmpty()) continue; // skip if not present
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new window/tab
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomains[i]),
                    "External link should navigate to domain " + expectedDomains[i]);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testOneLevelInternalLinks() {
        login(USERNAME, PASSWORD);
        driver.get(BASE_URL);
        // Collect all internal links on the base page (one level)
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> internalUrls = new HashSet<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                // Ensure only one level deeper (no further slashes after base path)
                String path = href.substring(BASE_URL.length());
                if (!path.isEmpty() && !path.contains("/")) {
                    internalUrls.add(href);
                }
            }
        }

        for (String url : internalUrls) {
            driver.navigate().to(url);
            // Simple verification: page loads and has a body element
            WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
            Assertions.assertTrue(body.isDisplayed(), "Page body should be displayed for " + url);
            // Return to base page for next iteration
            driver.navigate().back();
        }
    }
}