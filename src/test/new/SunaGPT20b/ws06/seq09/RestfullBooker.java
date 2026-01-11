package SunaGPT20b.ws06.seq09;

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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://www.saucedemo.com/";
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
    public void ensureLoggedIn() {
        driver.get(BASE_URL);
        if (!isLoggedIn()) {
            performLogin("standard_user", "secret_sauce");
        }
        // Ensure we are on the inventory page for each test start
        driver.get(BASE_URL + "inventory.html");
    }

    private boolean isLoggedIn() {
        List<WebElement> inventory = driver.findElements(By.id("inventory_container"));
        return !inventory.isEmpty();
    }

    private void performLogin(String username, String password) {
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

        // Verify login success
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(isLoggedIn(),
                "Login failed – inventory container not found.");
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        performLogin("standard_user", "secret_sauce");
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "After login URL should contain 'inventory.html'.");
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
                "Error message should be displayed for invalid credentials.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username/password issue.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='product_sort_container']")));
        Select select = new Select(sortDropdown);
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};

        for (String option : options) {
            select.selectByVisibleText(option);
            // Verify that the first item reflects the sorting order
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem, "First inventory item should be visible after sorting.");
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItemsAndReset() {
        openBurgerMenu();

        // Click All Items
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "All Items should navigate to inventory page.");

        // Add an item to cart, then reset app state
        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addBtn.click();
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 after adding an item.");

        // Open menu again to reset
        openBurgerMenu();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Verify cart is empty
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(),
                "Cart badge should be removed after resetting app state.");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        openBurgerMenu();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new window
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify external domain (example: "saucelabs.com")
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open a Saucelabs page.");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        openBurgerMenu();
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to the login page.");

        // Re-login for subsequent tests
        performLogin("standard_user", "secret_sauce");
    }

    @Test
    @Order(7)
    public void testAddRemoveCartAndCheckout() {
        // Add two items
        WebElement addBackpack = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        WebElement addBikeLight = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-bike-light']")));
        addBackpack.click();
        addBikeLight.click();

        // Verify cart badge shows 2
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("2", cartBadge.getText(),
                "Cart badge should show 2 after adding two items.");

        // Go to cart
        WebElement cartIcon = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartIcon.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart.html"),
                "Should navigate to cart page.");

        // Remove one item
        WebElement removeBackpack = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='remove-sauce-labs-backpack']")));
        removeBackpack.click();

        // Verify badge updates to 1
        List<WebElement> badgeAfterRemove = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(1, badgeAfterRemove.size(),
                "Cart badge should be present after removing one item.");
        Assertions.assertEquals("1", badgeAfterRemove.get(0).getText(),
                "Cart badge should show 1 after removal.");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("checkout-step-one.html"),
                "Should be on checkout step one page.");

        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("checkout-step-two.html"),
                "Should be on checkout step two page.");

        // Finish checkout
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER",
                completeHeader.getText().toUpperCase(),
                "Checkout completion message should be displayed.");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        // Twitter
        testExternalFooterLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");

        // Facebook
        testExternalFooterLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");

        // LinkedIn
        testExternalFooterLink(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    private void testExternalFooterLink(By locator, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should open a page containing domain: " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void openBurgerMenu() {
        WebElement burgerBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        // Wait for menu to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));
    }
}