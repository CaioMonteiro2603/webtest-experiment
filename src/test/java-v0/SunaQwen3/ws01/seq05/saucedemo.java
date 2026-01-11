package SunaQwen3.ws01.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");
        assertTrue(isInventoryListDisplayed(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        login("invalid_user", "invalid_password");
        WebElement errorButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be displayed for invalid login");
        String errorMessage = driver.findElement(By.cssSelector(".error-message-container h3")).getText();
        assertTrue(errorMessage.contains("Epic sadface"), "Error message should indicate login failure");
    }

    @Test
    @Order(3)
    void testLockedUserLogin() {
        driver.get(BASE_URL);
        login("locked_out_user", PASSWORD);
        WebElement errorButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be displayed for locked user");
        String errorMessage = driver.findElement(By.cssSelector(".error-message-container h3")).getText();
        assertTrue(errorMessage.contains("locked out"), "Error message should indicate user is locked out");
    }

    @Test
    @Order(4)
    void testSortByNameAtoZ() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        List<String> itemNames = getItemNames();
        List<String> sortedNames = new java.util.ArrayList<>(itemNames);
        sortedNames.sort(String::compareTo);
        assertIterableEquals(sortedNames, itemNames, "Items should be sorted from A to Z");
    }

    @Test
    @Order(5)
    void testSortByNameZtoA() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        List<String> itemNames = getItemNames();
        List<String> sortedNames = new java.util.ArrayList<>(itemNames);
        sortedNames.sort(String::compareTo);
        java.util.Collections.reverse(sortedNames);
        assertIterableEquals(sortedNames, itemNames, "Items should be sorted from Z to A");
    }

    @Test
    @Order(6)
    void testSortByPriceLowToHigh() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        List<Double> prices = getItemPrices();
        List<Double> sortedPrices = new java.util.ArrayList<>(prices);
        sortedPrices.sort(Double::compareTo);
        assertIterableEquals(sortedPrices, prices, "Items should be sorted by price low to high");
    }

    @Test
    @Order(7)
    void testSortByPriceHighToLow() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        List<Double> prices = getItemPrices();
        List<Double> sortedPrices = new java.util.ArrayList<>(prices);
        sortedPrices.sort(Double::compareTo);
        java.util.Collections.reverse(sortedPrices);
        assertIterableEquals(sortedPrices, prices, "Items should be sorted by price high to low");
    }

    @Test
    @Order(8)
    void testAddRemoveItemFromCart() {
        navigateToInventory();
        resetAppState();
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");
        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#remove-sauce-labs-backpack")));
        removeFromCartButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertFalse(isElementPresent(By.cssSelector(".shopping_cart_badge")), "Cart badge should disappear when cart is empty");
    }

    @Test
    @Order(9)
    void testMenuAllItems() {
        navigateToInventory();
        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page when clicking All Items");
        assertTrue(isInventoryListDisplayed(), "Inventory list should be displayed");
    }

    @Test
    @Order(10)
    void testMenuAboutExternalLink() {
        navigateToInventory();
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(11)
    void testMenuLogout() {
        navigateToInventory();
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be redirected to login page after logout");
        assertTrue(isElementPresent(By.id("login-button")), "Login button should be present on login page");
    }

    @Test
    @Order(12)
    void testMenuResetAppState() {
        navigateToInventory();
        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should have 1 item before reset");
        // Reset app state
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertFalse(isElementPresent(By.cssSelector(".shopping_cart_badge")), "Cart should be empty after reset app state");
    }

    @Test
    @Order(13)
    void testFooterTwitterLink() {
        navigateToInventory();
        WebElement twitterLink = driver.findElement(By.cssSelector(".social_twitter"));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should redirect to twitter.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
    void testFooterFacebookLink() {
        navigateToInventory();
        WebElement facebookLink = driver.findElement(By.cssSelector(".social_facebook"));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should redirect to facebook.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    void testFooterLinkedInLink() {
        navigateToInventory();
        WebElement linkedinLink = driver.findElement(By.cssSelector(".social_linkedin"));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should redirect to linkedin.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    void testCompleteCheckoutProcess() {
        navigateToInventory();
        resetAppState();
        // Add item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();
        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        // Checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#checkout")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        // Fill in user info
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#first-name")));
        firstNameField.sendKeys("John");
        driver.findElement(By.cssSelector("#last-name")).sendKeys("Doe");
        driver.findElement(By.cssSelector("#postal-code")).sendKeys("12345");
        WebElement continueButton = driver.findElement(By.cssSelector("#continue"));
        continueButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#finish")));
        finishButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        assertTrue(completeHeader.isDisplayed(), "Checkout complete header should be displayed");
        assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().toUpperCase(), "Should display thank you message");
    }

    private void login(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#user-name")));
        usernameField.clear();
        usernameField.sendKeys(username);
        WebElement passwordField = driver.findElement(By.cssSelector("#password"));
        passwordField.clear();
        passwordField.sendKeys(password);
        WebElement loginButton = driver.findElement(By.cssSelector("#login-button"));
        loginButton.click();
    }

    private void navigateToInventory() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            login(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
        resetAppState();
    }

    private void resetAppState() {
        if (isElementPresent(By.id("react-burger-menu-btn"))) {
            openMenu();
            if (isElementPresent(By.id("reset_sidebar_link"))) {
                WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
                resetLink.click();
            }
            closeMenu();
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
    }

    private void closeMenu() {
        WebElement closeMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#react-burger-cross-btn")));
        closeMenuButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("logout_sidebar_link")));
    }

    private boolean isInventoryListDisplayed() {
        return isElementPresent(By.cssSelector(".inventory_list"));
    }

    private boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private List<String> getItemNames() {
        List<WebElement> itemElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        List<String> names = new java.util.ArrayList<>();
        for (WebElement element : itemElements) {
            names.add(element.getText());
        }
        return names;
    }

    private List<Double> getItemPrices() {
        List<WebElement> priceElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        List<Double> prices = new java.util.ArrayList<>();
        for (WebElement element : priceElements) {
            String priceText = element.getText().replace("$", "");
            prices.add(Double.parseDouble(priceText));
        }
        return prices;
    }
}