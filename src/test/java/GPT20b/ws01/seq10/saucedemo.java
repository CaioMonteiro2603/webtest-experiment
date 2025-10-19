package GPT20b.ws01.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.Set;
import java.util.Iterator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --------------------------------------------------------------
    // Helper Methods
    // --------------------------------------------------------------
    private static void ensureLoggedIn() {
        if (!isLoggedIn()) {
            login(USERNAME, PASSWORD);
        }
    }

    private static boolean isLoggedIn() {
        return driver.findElements(By.cssSelector(".inventory_item")).size() > 0;
    }

    private static void login(String username, String password) {
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/v1/inventory.html"));
        Assertions.assertEquals(
                "https://www.saucedemo.com/v1/inventory.html",
                driver.getCurrentUrl(),
                "Login did not navigate to inventory page");
    }

    private static void resetAppState() {
        ensureLoggedIn();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reset_sidebar_link")));
        driver.findElement(By.id("reset_sidebar_link")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-menu-btn")));
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        driver.findElement(locator).click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream().filter(h -> !h.equals(originalHandle)).findFirst().get();
        driver.switchTo().window(newHandle);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    // --------------------------------------------------------------
    // Tests
    // --------------------------------------------------------------
    @Test
    @Order(1)
    public void testLoginValid() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(
                isLoggedIn(),
                "Inventory items should be visible after successful login");
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_password");
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        Assertions.assertTrue(
                errorMsg.getText().toLowerCase().contains("username and password do not match"),
                "Expected error message for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        ensureLoggedIn();
        By sortDropdown = By.id("inventory_filter");
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));

        String[] options = {"Name (A-Z)", "Name (Z-A)", "Price (lo-hi)", "Price (hi-lo)"};
        String[][] valueMappings = {
                {"name-za", "name-za"},
                {"name-a-z", "name-a-z"},
                {"price-accordion", "price-accordion"},
                {"price-reverse-accordion", "price-reverse-accordion"}
        };

        for (String option : options) {
            List<WebElement> list = driver.findElements(By.cssSelector(".inventory_item"));
            Assertions.assertFalse(list.isEmpty(), "No inventory items found before sorting");
            String firstItemBefore = list.get(0).findElement(By.cssSelector(".inventory_item_name")).getText();

            dropdown.click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[.='" + option + "']"))).click();

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loader"))); // wait for re-render

            List<WebElement> listAfter = driver.findElements(By.cssSelector(".inventory_item"));
            String firstItemAfter = listAfter.get(0).findElement(By.cssSelector(".inventory_item_name")).getText();

            Assertions.assertFalse(firstItemAfter.equals(firstItemBefore),
                    "Sorting option '" + option + "' did not change item order");
        }

        // Reset to original after test
        dropdown.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[.='Name (A‑Z)']"))).click();
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        ensureLoggedIn();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        driver.findElement(By.id("inventory_sidebar_link")).click();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/v1/inventory.html"),
                "Clicking All Items did not navigate to inventory page");

        // About (external)
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        openExternalLink(By.id("about_sidebar_link"), "saucelabs.com");

        // Logout
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        driver.findElement(By.id("logout_sidebar_link")).click();
        Assertions.assertEquals(
                "https://www.saucedemo.com/v1/index.html",
                driver.getCurrentUrl(),
                "Logout did not return to login page");

        // Re-login for subsequent tests
        login(USERNAME, PASSWORD);

        // Reset App State
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        driver.findElement(By.id("reset_sidebar_link")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-menu-btn")));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/v1/inventory.html"),
                "Reset App State should keep user on inventory page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        ensureLoggedIn();
        // Navigate to login page just to get fresh context for footer
        driver.get(BASE_URL);
        // Footer is present on all pages
        // Twitter
        openExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        // Facebook
        openExternalLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        // LinkedIn
        openExternalLink(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    @Test
    @Order(6)
    public void testAddRemoveItemsCart() {
        ensureLoggedIn();
        WebElement addFirst = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
        addFirst.click();
        WebElement addSecond = driver.findElement(By.id("add-to-cart-sauce-labs-bike-light"));
        addSecond.click();

        WebElement cartBadge = driver.findElement(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(
                "2",
                cartBadge.getText(),
                "Cart badge should show 2 items");

        // Remove one
        WebElement removeFirst = driver.findElement(By.id("remove-sauce-labs-backpack"));
        removeFirst.click();
        cartBadge = driver.findElement(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(
                "1",
                cartBadge.getText(),
                "Cart badge should show 1 item after removal");

        // Remove last
        WebElement removeSecond = driver.findElement(By.id("remove-sauce-labs-bike-light"));
        removeSecond.click();
        Assertions.assertTrue(
                driver.findElements(By.cssSelector(".shopping_cart_badge")).size() == 0,
                "Cart badge should be removed when cart is empty");
    }

    @Test
    @Order(7)
    public void testCheckoutSuccess() {
        ensureLoggedIn();
        // Add one item
        WebElement addItem = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
        addItem.click();

        // Go to cart
        driver.findElement(By.id("shopping_cart_container")).click();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/v1/cart.html"),
                "Did not navigate to cart page");

        // Proceed to checkout
        driver.findElement(By.id("checkout")).click();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/v1/checkout-step-one.html"),
                "Did not navigate to checkout step one");

        // Enter checkout info
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))).sendKeys("Test");
        driver.findElement(By.id("last-name")).sendKeys("User");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish"))).click();

        // Verify success
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals(
                "THANK YOU FOR YOUR ORDER",
                completeHeader.getText(),
                "Checkout completion message mismatch");
    }
}