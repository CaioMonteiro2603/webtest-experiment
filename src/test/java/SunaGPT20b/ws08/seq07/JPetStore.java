package SunaGPT20b.ws08.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
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
    public void navigateToBase() {
        driver.get(BASE_URL);
        // Ensure we are logged out before each test (except login tests)
        if (driver.getCurrentUrl().contains("logout") || driver.findElements(By.id("logout-link")).size() > 0) {
            // already logged out or logout link present
        } else if (driver.findElements(By.id("logout-link")).size() > 0) {
            driver.findElement(By.id("logout-link")).click();
            wait.until(ExpectedConditions.urlContains("login"));
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL + "login");
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
        wait.until(ExpectedConditions.urlContains("catalog"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("catalog"),
                "Login should navigate to catalog page.");
    }

    private void logout() {
        openMenu();
        WebElement logoutItem = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Logout']")));
        logoutItem.click();
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "Logout should navigate to login page.");
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetItem = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Reset App State']")));
        resetItem.click();
        // The page may stay on current view; verify cart badge cleared
        List<WebElement> badge = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badge.isEmpty(), "Cart badge should be cleared after reset.");
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("j2ee", "j2ee");
        // Verify presence of inventory list
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be displayed after login.");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.sendKeys("invalidUser");
        passField.sendKeys("wrongPass");
        loginBtn.click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials.");
    }

    @Test
    @Order(3)
    public void testInventorySorting() {
        login("j2ee", "j2ee");
        // Locate sorting dropdown
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        // Capture first item name before sorting
        List<WebElement> productNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        String firstBefore = productNames.get(0).getText();

        // Test each option
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String opt : options) {
            sortSelect.click();
            WebElement option = sortSelect.findElement(By.xpath(".//option[text()='" + opt + "']"));
            option.click();
            // Wait for sorting to apply
            wait.until(ExpectedConditions.stalenessOf(productNames.get(0)));
            productNames = driver.findElements(By.cssSelector(".inventory_item_name"));
            String firstAfter = productNames.get(0).getText();
            Assertions.assertNotEquals(firstBefore, firstAfter,
                    "Sorting option '" + opt + "' should change the order of items.");
            firstBefore = firstAfter;
        }
        logout();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login("j2ee", "j2ee");
        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='All Items']")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("catalog"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("catalog"),
                "All Items should navigate to catalog page.");
        logout();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login("j2ee", "j2ee");
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='About']")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("about"),
                "About link should open an external page containing 'about' in URL.");
        driver.close();
        driver.switchTo().window(originalWindow);
        logout();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login("j2ee", "j2ee");
        logout();
        // Verify we are back on login page
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "After logout, URL should contain 'login'.");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login("j2ee", "j2ee");
        // Add an item to cart to create state
        WebElement firstAddToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        firstAddToCart.click();
        // Verify badge appears
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 item.");

        // Reset state
        resetAppState();

        // Verify badge cleared
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after reset.");
        logout();
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login("j2ee", "j2ee");
        // Footer links selectors (example)
        String[][] links = {
                {"Twitter", "twitter.com"},
                {"Facebook", "facebook.com"},
                {"LinkedIn", "linkedin.com"}
        };
        for (String[] linkInfo : links) {
            String linkText = linkInfo[0];
            String expectedDomain = linkInfo[1];
            WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//footer//a[contains(@href,'" + expectedDomain + "')]")));
            String originalWindow = driver.getWindowHandle();
            socialLink.click();

            // Switch to new window
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(expectedDomain),
                    linkText + " link should open a page containing '" + expectedDomain + "'.");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        logout();
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        login("j2ee", "j2ee");
        // Add first product to cart
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartBtn.click();
        // Verify badge count
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 after adding product.");

        // Go to cart
        WebElement cartLink = driver.findElement(By.id("shopping_cart_container"));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart"), "Should navigate to cart page.");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one"));
        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("checkout-step-two"));
        // Finish checkout
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete"));
        WebElement completeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(completeMsg.getText().toLowerCase().contains("thank"),
                "Checkout completion message should be displayed.");
        // Reset state for cleanliness
        resetAppState();
        logout();
    }
}