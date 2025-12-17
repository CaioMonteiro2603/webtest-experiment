package SunaQwen3.ws01.seq04;

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
    void testValidLoginSuccess() {
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");
        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginError() {
        driver.get(BASE_URL);
        login("invalid_user", "invalid_password");
        WebElement errorButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be visible");
        String errorMessage = driver.findElement(By.cssSelector(".error-message-container h3")).getText();
        assertEquals("Epic sadface: Username and password do not match any user in this service", errorMessage, "Error message should match expected");
    }

    @Test
    @Order(3)
    void testLockedUserLogin() {
        driver.get(BASE_URL);
        login("locked_out_user", PASSWORD);
        WebElement errorButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be visible");
        String errorMessage = driver.findElement(By.cssSelector(".error-message-container h3")).getText();
        assertEquals("Epic sadface: Sorry, this user has been locked out.", errorMessage, "Error message should indicate locked user");
    }

    @Test
    @Order(4)
    void testSortByNameAtoZ() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        List<WebElement> itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        String firstItem = itemNames.get(0).getText();
        String lastItem = itemNames.get(itemNames.size() - 1).getText();
        assertTrue(firstItem.compareTo(lastItem) < 0, "Items should be sorted A to Z");
    }

    @Test
    @Order(5)
    void testSortByNameZtoA() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        List<WebElement> itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        String firstItem = itemNames.get(0).getText();
        String lastItem = itemNames.get(itemNames.size() - 1).getText();
        assertTrue(firstItem.compareTo(lastItem) > 0, "Items should be sorted Z to A");
    }

    @Test
    @Order(6)
    void testSortByPriceLowToHigh() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        List<WebElement> itemPrices = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstPrice = parsePrice(itemPrices.get(0).getText());
        double lastPrice = parsePrice(itemPrices.get(itemPrices.size() - 1).getText());
        assertTrue(firstPrice <= lastPrice, "Items should be sorted from low to high price");
    }

    @Test
    @Order(7)
    void testSortByPriceHighToLow() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        List<WebElement> itemPrices = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstPrice = parsePrice(itemPrices.get(0).getText());
        double lastPrice = parsePrice(itemPrices.get(itemPrices.size() - 1).getText());
        assertTrue(firstPrice >= lastPrice, "Items should be sorted from high to low price");
    }

    @Test
    @Order(8)
    void testAddRemoveItemFromCart() {
        navigateToInventory();
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");
        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#remove-sauce-labs-backpack")));
        removeFromCartButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart badge should not be present after removing item");
    }

    @Test
    @Order(9)
    void testMenuAllItemsNavigation() {
        navigateToInventory();
        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page when clicking All Items");
    }

    @Test
    @Order(10)
    void testMenuAboutNavigation() {
        navigateToInventory();
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");
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
        assertTrue(driver.findElement(By.cssSelector("#login-button")).isDisplayed(), "Login button should be visible on login page");
    }

    @Test
    @Order(12)
    void testMenuResetAppState() {
        navigateToInventory();
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should have 1 item before reset");
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart should be empty after reset app state");
    }

    @Test
    @Order(13)
    void testFooterTwitterLink() {
        navigateToInventory();
        WebElement twitterLink = driver.findElement(By.cssSelector("[data-test='social-twitter']"));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open Twitter domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
    void testFooterFacebookLink() {
        navigateToInventory();
        WebElement facebookLink = driver.findElement(By.cssSelector("[data-test='social-facebook']"));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open Facebook domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    void testFooterLinkedInLink() {
        navigateToInventory();
        WebElement linkedinLink = driver.findElement(By.cssSelector("[data-test='social-linkedin']"));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open LinkedIn domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    void testCheckoutProcess() {
        navigateToInventory();
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#checkout")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#first-name")));
        firstNameField.sendKeys("John");
        driver.findElement(By.cssSelector("#last-name")).sendKeys("Doe");
        driver.findElement(By.cssSelector("#postal-code")).sendKeys("12345");
        driver.findElement(By.cssSelector("#continue")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#finish")));
        finishButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
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
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        // Reset app state to ensure clean state
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu_button_container button")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replace("$", ""));
    }
}