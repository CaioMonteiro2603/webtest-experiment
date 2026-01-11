package SunaQwen3.ws01.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message-container h3"));
        assertEquals("Epic sadface: Username and password do not match any user in this service", errorMessage.getText(), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testLockedUserLogin() {
        driver.get(BASE_URL);
        login("locked_out_user", PASSWORD);
        WebElement errorButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be visible");
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message-container h3"));
        assertEquals("Epic sadface: Sorry, this user has been locked out.", errorMessage.getText(), "Error message should indicate user is locked out");
    }

    @Test
    @Order(4)
    void testSortByNameAtoZ() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Name (A to Z)");
        List<WebElement> itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertTrue(itemNames.size() > 0, "At least one item should be present");
        String firstItemName = itemNames.get(0).getText();
        assertEquals("Sauce Labs Backpack", firstItemName, "First item should be 'Sauce Labs Backpack' when sorted A to Z");
    }

    @Test
    @Order(5)
    void testSortByNameZtoA() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Name (Z to A)");
        List<WebElement> itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertTrue(itemNames.size() > 0, "At least one item should be present");
        String firstItemName = itemNames.get(0).getText();
        assertEquals("Test.allTheThings() T-Shirt (Red)", firstItemName, "First item should be 'Test.allTheThings() T-Shirt (Red)' when sorted Z to A");
    }

    @Test
    @Order(6)
    void testSortByPriceLowToHigh() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Price (low to high)");
        List<WebElement> itemPrices = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertTrue(itemPrices.size() > 0, "At least one item should be present");
        double firstPrice = parsePrice(itemPrices.get(0).getText());
        double lastPrice = parsePrice(itemPrices.get(itemPrices.size() - 1).getText());
        assertTrue(firstPrice <= lastPrice, "Prices should be in ascending order");
    }

    @Test
    @Order(7)
    void testSortByPriceHighToLow() {
        navigateToInventory();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        Select select = new Select(sortDropdown);
        select.selectByVisibleText("Price (high to low)");
        List<WebElement> itemPrices = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertTrue(itemPrices.size() > 0, "At least one item should be present");
        double firstPrice = parsePrice(itemPrices.get(0).getText());
        double lastPrice = parsePrice(itemPrices.get(itemPrices.size() - 1).getText());
        assertTrue(firstPrice >= lastPrice, "Prices should be in descending order");
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
    void testMenuAllItems() {
        navigateToInventory();
        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page after clicking All Items");
    }

    @Test
    @Order(10)
    void testMenuAboutExternalLink() {
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
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitterLink.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open Twitter page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
    void testFooterFacebookLink() {
        navigateToInventory();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebookLink.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open Facebook page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    void testFooterLinkedInLink() {
        navigateToInventory();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open LinkedIn page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    void testCheckoutProcess() {
        navigateToInventory();
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
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
        WebElement continueButton = driver.findElement(By.cssSelector("#continue"));
        continueButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#finish")));
        finishButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().toUpperCase(), "Order should be completed successfully");
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
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be on inventory page");
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replace("$", ""));
    }
}