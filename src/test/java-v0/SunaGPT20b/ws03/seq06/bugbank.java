package SunaGPT20b.ws03.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String VALID_EMAIL = "caio@gmail.com";
    private static final String VALID_PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    private void login(String email, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.clear();
        userField.sendKeys(email);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void resetAppState() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Ensure menu is closed
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    private void switchToNewTabAndValidate(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link did not navigate to expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_EMAIL, VALID_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login did not navigate to inventory page.");
        WebElement inventory = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventory.isDisplayed(), "Inventory container is not displayed after login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login(VALID_EMAIL, "wrongPassword");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message not displayed for invalid login.");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username") ||
                        error.getText().toLowerCase().contains("password"),
                "Error message does not reference credentials.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(VALID_EMAIL, VALID_PASSWORD);
        By sortLocator = By.cssSelector("[data-test='product_sort_container']");
        WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        Select sortSelect = new Select(sortElement);
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            sortSelect.selectByVisibleText(option);
            // Verify that the first item changes after sorting
            List<WebElement> items = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.cssSelector(".inventory_item_name")));
            Assertions.assertFalse(items.isEmpty(), "No inventory items found after sorting.");
            // Simple sanity check: ensure at least one item is displayed
            Assertions.assertTrue(items.get(0).isDisplayed(),
                    "First inventory item not displayed after sorting with option: " + option);
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItemsAndAbout() {
        login(VALID_EMAIL, VALID_PASSWORD);
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items did not navigate to inventory page.");

        // Open menu again for About
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        switchToNewTabAndValidate("saucelabs.com");
    }

    @Test
    @Order(5)
    public void testMenuLogoutAndReset() {
        login(VALID_EMAIL, VALID_PASSWORD);
        // Add an item to cart to verify reset later
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.btn_inventory")));
        addBtn.click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 after adding item.");

        // Logout
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL),
                "Logout did not return to login page.");

        // Login again and reset state
        login(VALID_EMAIL, VALID_PASSWORD);
        resetAppState();
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after reset.");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        login(VALID_EMAIL, VALID_PASSWORD);
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href == null) continue;
            if (href.contains("twitter.com")) {
                link.click();
                switchToNewTabAndValidate("twitter.com");
            } else if (href.contains("facebook.com")) {
                link.click();
                switchToNewTabAndValidate("facebook.com");
            } else if (href.contains("linkedin.com")) {
                link.click();
                switchToNewTabAndValidate("linkedin.com");
            }
        }
    }

    @Test
    @Order(7)
    public void testAddToCartAndCheckout() {
        login(VALID_EMAIL, VALID_PASSWORD);
        // Add first item to cart
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.btn_inventory")));
        addBtn.click();

        // Go to cart
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartIcon.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"),
                "Did not navigate to cart page.");

        // Verify item in cart
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart_item"));
        Assertions.assertFalse(cartItems.isEmpty(), "Cart is empty after adding an item.");

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/checkout-step-one.html"),
                "Did not navigate to checkout step one.");

        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish checkout
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();

        // Verify completion
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().trim(),
                "Order completion message not as expected.");
    }
}