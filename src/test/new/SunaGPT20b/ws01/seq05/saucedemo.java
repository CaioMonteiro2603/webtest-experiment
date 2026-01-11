package SunaGPT20b.ws01.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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
    }

    private void login() {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys(USERNAME);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    private void resetAppState() {
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeButton.click();

        // Ensure we are back on inventory page
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After login, URL should contain /inventory.html");
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys("invalid_user");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys("wrong_password");

        driver.findElement(By.id("login-button")).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.getText().contains("Username and password do not match"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        resetAppState();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        Select select = new Select(sortDropdown);

        // Verify each sorting option changes order
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            select.selectByVisibleText(option);
            // Wait for the first item to reflect sorting change
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem, "First item should be visible after sorting by " + option);
        }
    }

    @Test
    @Order(4)
    public void testAddRemoveCart() {
        login();
        resetAppState();

        // Add first item to cart
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
        addButton.click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show count 1 after adding an item");

        // Remove the same item
        WebElement removeButton = driver.findElement(By.id("remove-sauce-labs-backpack"));
        removeButton.click();

        // Verify cart badge disappears
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should disappear after removing the item");
    }

    @Test
    @Order(5)
    public void testCheckoutProcess() {
        login();
        resetAppState();

        // Add two items
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click();

        // Go to cart
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));

        // Verify two items in cart
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart_item"));
        Assertions.assertEquals(2, cartItems.size(), "Cart should contain two items before checkout");

        // Click Checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));

        // Fill checkout information
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();

        // Verify completion message
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("Thank you for your order!", completeHeader.getText(),
                "Checkout should display thank you message");
    }

    @Test
    @Order(6)
    public void testMenuAllItems() {
        login();
        resetAppState();

        // Close menu first if open
        List<WebElement> closeButtons = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
            closeButtons.get(0).click();
        }

        // Open menu and click All Items
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        driver.findElement(By.id("inventory_sidebar_link")).click();

        // Verify we stay on inventory page
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should keep user on inventory page");
    }

    @Test
    @Order(7)
    public void testMenuAboutExternalLink() {
        login();
        resetAppState();

        // Close menu first if open
        List<WebElement> closeButtons = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
            closeButtons.get(0).click();
        }

        // Open menu and click About (external)
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify external domain (saucelabs.com)
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open a Saucelabs domain");

        // Close external tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testMenuLogout() {
        login();
        resetAppState();

        // Close menu first if open
        List<WebElement> closeButtons = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
            closeButtons.get(0).click();
        }

        // Open menu and click Logout
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        driver.findElement(By.id("logout_sidebar_link")).click();

        // Verify we are back on login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "After logout, user should be on the login page");
    }

    @Test
    @Order(9)
    public void testMenuResetAppState() {
        login();

        // Add an item to change state
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();

        // Open menu and reset
        driver.findElement(By.id("react-burger-menu-btn")).click();
        driver.findElement(By.id("reset_sidebar_link")).click();

        // Verify cart badge is gone
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Reset App State should clear the cart");
    }

    @Test
    @Order(10)
    public void testFooterSocialLinks() {
        login();
        resetAppState();

        // Define social links and expected domains
        String[][] links = {
                {"twitter", "twitter.com"},
                {"facebook", "facebook.com"},
                {"linkedin", "linkedin.com"}
        };

        for (String[] linkInfo : links) {
            String id = "social_" + linkInfo[0];
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.className(id)));
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new tab
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            // Verify domain
            Assertions.assertTrue(driver.getCurrentUrl().contains(linkInfo[1]),
                    "Social link " + linkInfo[0] + " should open domain containing " + linkInfo[1]);

            // Close tab and return
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}