package SunaGPT20b.ws06.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online/";
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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();

        // Verify successful navigation to inventory page
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login should navigate to inventory page");
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-reset-sidebar")));
        resetLink.click();
        // The menu closes automatically after reset
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-menu")));
    }

    private void switchToNewWindowAndValidate(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > windowsBefore.size());

        Set<String> windowsAfter = driver.getWindowHandles();
        windowsAfter.removeAll(windowsBefore);
        String newWindow = windowsAfter.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should contain domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        // Verify inventory items are displayed
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory should contain items after login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_password");
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("standard_user", "secret_sauce");
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='product_sort_container']")));

        // Options: Name (A to Z), Name (Z to A), Price (low to high), Price (high to low)
        String[] options = {"az", "za", "lohi", "hilo"};
        String[] expectedFirstItem = {"Sauce Labs Backpack", "Test.allTheThings() T-Shirt (Red)", "Sauce Labs Onesie", "Sauce Labs Bike Light"};

        for (int i = 0; i < options.length; i++) {
            sortDropdown.click();
            WebElement option = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='" + options[i] + "']")));
            option.click();

            // Verify first item text matches expectation
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
            Assertions.assertEquals(expectedFirstItem[i], firstItem.getText(),
                    "First item after sorting should match expected order");
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login("standard_user", "secret_sauce");
        openMenu();

        // All Items (should stay on inventory page)
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-all-items")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate to inventory page");

        // About (external link)
        openMenu();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-about")));
        aboutLink.click();
        switchToNewWindowAndValidate("saucelabs.com");

        // Logout
        openMenu();
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-logout")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to base URL");

        // Login again for reset verification
        login("standard_user", "secret_sauce");
        // Add an item to cart to verify reset clears it
        WebElement addToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 after adding item");

        // Reset App State
        resetAppState();
        List<WebElement> badgesAfterReset = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badgesAfterReset.isEmpty(),
                "Cart badge should be cleared after resetting app state");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");
        // Twitter
        WebElement twitterLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        switchToNewWindowAndValidate("twitter.com");

        // Facebook
        WebElement facebookLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        switchToNewWindowAndValidate("facebook.com");

        // LinkedIn
        WebElement linkedInLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedInLink.click();
        switchToNewWindowAndValidate("linkedin.com");
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login("standard_user", "secret_sauce");
        // Add two items
        WebElement addBackpack = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addBackpack.click();
        WebElement addBikeLight = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-bike-light']")));
        addBikeLight.click();

        // Verify cart badge shows 2
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("2", cartBadge.getText(),
                "Cart badge should display 2 after adding two items");

        // Go to cart
        WebElement cartIcon = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartIcon.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"),
                "Should navigate to cart page");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/checkout-step-one.html"),
                "Should be on checkout step one page");

        // Fill checkout info
        WebElement firstName = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("last-name")));
        WebElement postalCode = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("postal-code")));
        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        postalCode.sendKeys("12345");
        WebElement continueBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("continue")));
        continueBtn.click();

        // Verify overview page
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/checkout-step-two.html"),
                "Should be on checkout overview page");

        // Finish checkout
        WebElement finishBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(),
                "Checkout completion message should be displayed");

        // Reset state for next tests
        resetAppState();
    }
}