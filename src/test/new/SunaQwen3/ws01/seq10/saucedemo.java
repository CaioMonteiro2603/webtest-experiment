package SunaQwen3.ws01.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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
    public void testValidLoginSuccess() {
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);

        // Assert successful login by checking URL and presence of inventory container
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");

        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertNotNull(inventoryContainer, "Inventory container should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);
        login("invalid_user", "invalid_password");

        // Assert error message is displayed
        WebElement errorButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-button")));
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message-container h3"));

        assertTrue(errorMessage.getText().contains("Epic sadface"), "Error message should indicate login failure");
        assertTrue(errorButton.isDisplayed(), "Error close button should be visible");
    }

    @Test
    @Order(3)
    public void testLockedUserLogin() {
        driver.get(BASE_URL);
        login("locked_out_user", PASSWORD);

        // Assert locked user error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container h3")));
        assertTrue(errorMessage.getText().contains("locked out"), "Error message should indicate account is locked");
    }

    @Test
    @Order(4)
    public void testSortingLowToHigh() {
        loginIfNecessary();

        // Select low to high price sorting
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("lohi");
        sortDropdown.sendKeys(Keys.RETURN);

        // Wait for items to reload
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Price (low to high)"));

        // Get all prices and verify they are in ascending order
        List<WebElement> priceElements = driver.findElements(By.cssSelector(".inventory_item_price"));
        double previousPrice = 0.0;
        for (WebElement element : priceElements) {
            double currentPrice = Double.parseDouble(element.getText().replace("$", ""));
            assertTrue(currentPrice >= previousPrice, "Prices should be sorted from low to high");
            previousPrice = currentPrice;
        }
    }

    @Test
    @Order(5)
    public void testSortingHighToLow() {
        loginIfNecessary();

        // Select high to low price sorting
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("hilo");
        sortDropdown.sendKeys(Keys.RETURN);

        // Wait for items to reload
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Price (high to low)"));

        // Get all prices and verify they are in descending order
        List<WebElement> priceElements = driver.findElements(By.cssSelector(".inventory_item_price"));
        double previousPrice = Double.MAX_VALUE;
        for (WebElement element : priceElements) {
            double currentPrice = Double.parseDouble(element.getText().replace("$", ""));
            assertTrue(currentPrice <= previousPrice, "Prices should be sorted from high to low");
            previousPrice = currentPrice;
        }
    }

    @Test
    @Order(6)
    public void testSortingAtoZ() {
        loginIfNecessary();

        // Select A to Z sorting
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("az");
        sortDropdown.sendKeys(Keys.RETURN);

        // Wait for items to reload
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Name (A to Z)"));

        // Get all names and verify alphabetical order
        List<WebElement> nameElements = driver.findElements(By.cssSelector(".inventory_item_name"));
        String previousName = "";
        for (WebElement element : nameElements) {
            String currentName = element.getText();
            assertTrue(currentName.compareToIgnoreCase(previousName) >= 0, "Item names should be sorted A to Z");
            previousName = currentName;
        }
    }

    @Test
    @Order(7)
    public void testSortingZtoA() {
        loginIfNecessary();

        // Select Z to A sorting
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("za");
        sortDropdown.sendKeys(Keys.RETURN);

        // Wait for items to reload
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Name (Z to A)"));

        // Get all names and verify reverse alphabetical order
        List<WebElement> nameElements = driver.findElements(By.cssSelector(".inventory_item_name"));
        String previousName = "ZZZZZ";
        for (WebElement element : nameElements) {
            String currentName = element.getText();
            assertTrue(currentName.compareToIgnoreCase(previousName) <= 0, "Item names should be sorted Z to A");
            previousName = currentName;
        }
    }

    @Test
    @Order(8)
    public void testAddRemoveItemFromCart() {
        loginIfNecessary();

        // Add first item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_primary")));
        addToCartButton.click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_secondary")));
        removeFromCartButton.click();

        // Verify cart badge is gone
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart badge should not be present after removing item");
    }

    @Test
    @Order(9)
    public void testMultipleItemsInCart() {
        loginIfNecessary();

        // Add two items to cart
        List<WebElement> addToCartButtons = driver.findElements(By.cssSelector(".btn_primary"));
        addToCartButtons.get(0).click();
        addToCartButtons.get(1).click();

        // Verify cart badge shows 2
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("2", cartBadge.getText(), "Cart badge should show 2 items");

        // Navigate to cart
        driver.findElement(By.cssSelector(".shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Verify two items are in cart
        List<WebElement> cartItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".cart_item")));
        assertEquals(2, cartItems.size(), "Cart should contain 2 items");
    }

    @Test
    @Order(10)
    public void testCheckoutProcessSuccess() {
        loginIfNecessary();

        // Add one item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_primary")));
        addToCartButton.click();

        // Go to cart
        driver.findElement(By.cssSelector(".shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Proceed to checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#checkout")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill in user information
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        firstNameField.sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.cssSelector(".btn_primary")).click();

        // Wait for step two
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));

        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#finish")));
        finishButton.click();

        // Verify success message
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        assertTrue(completeHeader.getText().contains("Thank you"), "Checkout should be successful with thank you message");
    }

    @Test
    @Order(11)
    public void testMenuAllItemsNavigation() {
        loginIfNecessary();
        openMenuIfClosed();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        // Wait for inventory page to load
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate back to inventory page");
    }

    @Test
    @Order(12)
    public void testMenuAboutExternalLink() {
        loginIfNecessary();
        openMenuIfClosed();

        // Click About link
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("saucelabs.com"), "About link should open Sauce Labs website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    public void testMenuLogout() {
        loginIfNecessary();
        openMenuIfClosed();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        // Assert back to login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should return to login page after logout");
    }

    @Test
    @Order(14)
    public void testMenuResetAppState() {
        loginIfNecessary();

        // Add item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_primary")));
        addToCartButton.click();

        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should initially have 1 item");

        // Open menu and reset app state
        openMenuIfClosed();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu and verify cart is empty
        driver.findElement(By.cssSelector(".bm-cross-button")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart should be empty after reset");
    }

    @Test
    @Order(15)
    public void testFooterTwitterLink() {
        loginIfNecessary();
        closeMenuIfOpen();

        // Find Twitter link in footer
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter")));
        twitterLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("twitter.com") || currentUrl.contains("x.com"), "Twitter link should open Twitter/X website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    public void testFooterFacebookLink() {
        loginIfNecessary();
        closeMenuIfOpen();

        // Find Facebook link in footer
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook")));
        facebookLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open Facebook website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(17)
    public void testFooterLinkedInLink() {
        loginIfNecessary();
        closeMenuIfOpen();

        // Find LinkedIn link in footer
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin")));
        linkedinLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open LinkedIn website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    // Helper methods
    private void login(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
    }

    private void loginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            login(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu_button_container .bm-burger-button")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu_button_container")));
    }

    private void openMenuIfClosed() {
        try {
            // Check if menu is closed by checking if menu button is visible
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu_button_container .bm-burger-button")));
            if (menuButton.isDisplayed()) {
                menuButton.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));
            }
        } catch (Exception e) {
            // Menu might already be open or in different state
        }
    }

    private void closeMenuIfOpen() {
        try {
            // Check if menu is open by looking for the close button
            WebElement closeButton = driver.findElement(By.cssSelector(".bm-cross-button"));
            if (closeButton.isDisplayed()) {
                closeButton.click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu")));
            }
        } catch (Exception e) {
            // Menu might already be closed or in different state
        }
    }

    private void resetAppState() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get("https://www.saucedemo.com/v1/inventory.html");
        }
        openMenuIfClosed();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        driver.findElement(By.cssSelector(".bm-cross-button")).click();
    }
}