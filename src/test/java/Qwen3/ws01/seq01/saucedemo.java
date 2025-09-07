package Qwen3.ws01.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL = "https://www.saucedemo.com/v1/inventory.html";
    private static final String LOGIN_USERNAME = "standard_user";
    private static final String LOGIN_PASSWORD = "secret_sauce";

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name"))).sendKeys(LOGIN_USERNAME);
        driver.findElement(By.id("password")).sendKeys(LOGIN_PASSWORD);
        driver.findElement(By.id("login-button")).click();

        wait.until(ExpectedConditions.urlToBe(INVENTORY_URL));
        assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed(), "Inventory list not displayed after login.");
    }

    @Test
    @Order(2)
    public void testSortingFunctionality() {
        driver.get(INVENTORY_URL); // Ensure we're on the inventory page

        // Sort by Name (A to Z) - Default
        Select sortSelect = new Select(driver.findElement(By.className("product_sort_container")));
        sortSelect.selectByValue("az");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item:first-child .inventory_item_name")));
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item .inventory_item_name"));
        List<String> actualNames = new ArrayList<>();
        for (WebElement item : items) {
            actualNames.add(item.getText());
        }
        List<String> sortedNames = new ArrayList<>(actualNames);
        sortedNames.sort(String.CASE_INSENSITIVE_ORDER);
        assertEquals(sortedNames, actualNames, "Items are not sorted by Name (A to Z).");

        // Sort by Name (Z to A)
        sortSelect.selectByValue("za");
        wait.until(ExpectedConditions.stalenessOf(items.get(0)));
        items = driver.findElements(By.cssSelector(".inventory_item .inventory_item_name"));
        actualNames.clear();
        for (WebElement item : items) {
            actualNames.add(item.getText());
        }
        sortedNames.sort(String.CASE_INSENSITIVE_ORDER.reversed());
        assertEquals(sortedNames, actualNames, "Items are not sorted by Name (Z to A).");

        // Sort by Price (Low to High)
        sortSelect.selectByValue("lohi");
        wait.until(ExpectedConditions.stalenessOf(items.get(0)));
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item .inventory_item_price"));
        List<Double> actualPrices = new ArrayList<>();
        for (WebElement price : prices) {
            actualPrices.add(Double.parseDouble(price.getText().replace("$", "")));
        }
        List<Double> sortedPrices = new ArrayList<>(actualPrices);
        sortedPrices.sort(Double::compareTo);
        assertEquals(sortedPrices, actualPrices, "Items are not sorted by Price (Low to High).");

        // Sort by Price (High to Low)
        sortSelect.selectByValue("hilo");
        wait.until(ExpectedConditions.stalenessOf(prices.get(0)));
        prices = driver.findElements(By.cssSelector(".inventory_item .inventory_item_price"));
        actualPrices.clear();
        for (WebElement price : prices) {
            actualPrices.add(Double.parseDouble(price.getText().replace("$", "")));
        }
        sortedPrices.sort(Double::compareTo.reversed());
        assertEquals(sortedPrices, actualPrices, "Items are not sorted by Price (High to Low).");
    }

    @Test
    @Order(3)
    public void testAddAndRemoveItem() {
        driver.get(INVENTORY_URL);

        // Reset App State to ensure clean slate
        openAndResetAppState();

        // Add first item
        WebElement firstAddButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".inventory_item:first-child button")));
        firstAddButton.click();

        // Verify cart count is 1
        WebElement cartBadge = wait.until(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.className("shopping_cart_badge")), "1"));
        assertNotNull(cartBadge, "Cart badge should show 1 item.");

        // Remove the item
        WebElement firstRemoveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".inventory_item:first-child button")));
        firstRemoveButton.click();

        // Verify cart is empty (badge should not be present)
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        assertEquals(0, badges.size(), "Cart badge should not be present after removing item.");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        driver.get(INVENTORY_URL);

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));

        // Click All Items (should stay on inventory page)
        driver.findElement(By.id("inventory_sidebar_link")).click();
        assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "URL should remain on inventory page after 'All Items'.");

        // Click About (external link)
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link"))).click();

        String originalWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);

        assertNotNull(newWindow, "A new window should have been opened.");
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "New window should be on saucelabs.com.");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Click Reset App State
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();

        // Ensure inventory is back
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("inventory_list")));

        // Click Logout
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Should be back on login page after logout.");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(INVENTORY_URL);

        String originalWindow = driver.getWindowHandle();

        // Click Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.social_twitter a")));
        twitterLink.click();
        assertExternalLinkAndReturn(originalWindow, "twitter.com");

        // Click Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.social_facebook a")));
        facebookLink.click();
        assertExternalLinkAndReturn(originalWindow, "facebook.com");

        // Click LinkedIn
        WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.social_linkedin a")));
        linkedInLink.click();
        assertExternalLinkAndReturn(originalWindow, "linkedin.com");
    }

    @Test
    @Order(6)
    public void testCheckoutFlow() {
        // Log back in
        testValidLogin();

        // Add an item
        driver.get(INVENTORY_URL);
        openAndResetAppState(); // Ensure clean state
        WebElement firstAddButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".inventory_item:first-child button")));
        firstAddButton.click();
        wait.until(ExpectedConditions.textToBePresentInElement(
                driver.findElement(By.className("shopping_cart_badge")), "1"));

        // Go to cart
        driver.findElement(By.className("shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Proceed to checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill in info
        driver.findElement(By.id("first-name")).sendKeys("Test");
        driver.findElement(By.id("last-name")).sendKeys("User");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));

        // Finish checkout
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));

        // Assert success
        assertTrue(driver.findElement(By.className("complete-header")).isDisplayed(), "Checkout completion header should be visible.");
        assertEquals("Thank you for your order!", driver.findElement(By.className("complete-header")).getText());
    }

    @Test
    @Order(7)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3[data-test='error']")));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid login.");
        assertTrue(errorElement.getText().contains("Username and password do not match"), "Error message text is incorrect.");
    }


    // --- Helper Methods ---

    private void openAndResetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));
    }

    private void assertExternalLinkAndReturn(String originalWindow, String expectedDomain) {
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        assertNotNull(newWindow, "A new window should have been opened for " + expectedDomain);
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "New window URL should contain " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}