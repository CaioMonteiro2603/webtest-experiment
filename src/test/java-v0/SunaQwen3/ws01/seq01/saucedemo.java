package SunaQwen3.ws01.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;
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

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        Assertions.assertTrue(inventoryList.isDisplayed(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);
        login("invalid_user", "invalid_password");
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message-container h3"));
        Assertions.assertEquals("Epic sadface: Username and password do not match any user in this service", errorMessage.getText(), "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testLockedUserLogin() {
        driver.get(BASE_URL);
        login("locked_out_user", PASSWORD);

        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message-container h3"));
        Assertions.assertEquals("Epic sadface: Sorry, this user has been locked out.", errorMessage.getText(), "Error message should indicate user is locked out");
    }

    @Test
    @Order(4)
    public void testSortProductsAZ() {
        performLoginIfNecessary();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();

        WebElement optionAZ = driver.findElement(By.cssSelector("option[value='az']"));
        optionAZ.click();

        List<WebElement> productNames = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(isSortedAscending(productNames, WebElement::getText), "Product names should be sorted A to Z");
    }

    @Test
    @Order(5)
    public void testSortProductsZA() {
        performLoginIfNecessary();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();

        WebElement optionZA = driver.findElement(By.cssSelector("option[value='za']"));
        optionZA.click();

        List<WebElement> productNames = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(isSortedDescending(productNames, WebElement::getText), "Product names should be sorted Z to A");
    }

        
    @Test
    @Order(6)
    public void testAddRemoveItemFromCart() {
        performLoginIfNecessary();
        resetAppState();

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name='remove-sauce-labs-backpack']")));
        removeFromCartButton.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size(), "Cart badge should not be displayed after removing item");
    }

    @Test
    @Order(7)
    public void testMenuAllItems() {
        performLoginIfNecessary();
        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate to inventory page when All Items is clicked");
    }

    @Test
    @Order(8)
    public void testMenuAboutExternalLink() {
        performLoginIfNecessary();
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

        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testMenuLogout() {
        performLoginIfNecessary();
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");
    }

    @Test
    @Order(10)
    public void testMenuResetAppState() {
        performLoginIfNecessary();
        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart should have 1 item before reset");

        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size(), "Cart should be empty after Reset App State");
    }

    @Test
    @Order(11)
    public void testFooterTwitterLink() {
        performLoginIfNecessary();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter footer link should open Twitter");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(12)
    public void testFooterFacebookLink() {
        performLoginIfNecessary();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook")));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook footer link should open Facebook");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    public void testFooterLinkedInLink() {
        performLoginIfNecessary();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn footer link should open LinkedIn");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
    public void testCompleteCheckoutProcess() {
        performLoginIfNecessary();
        resetAppState();

        // Add item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link")));
        cartLink.click();

        // Proceed to checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutButton.click();

        // Fill checkout info
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        firstNameField.sendKeys("John");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Doe");

        WebElement postalCodeField = driver.findElement(By.id("postal-code"));
        postalCodeField.sendKeys("12345");

        WebElement continueButton = driver.findElement(By.id("continue"));
        continueButton.click();

        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishButton.click();

        // Verify completion
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().toUpperCase(), "Checkout should be completed successfully");
    }
    

    private void login(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys(username);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();
    }

    private void performLoginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            login(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge")));
    }

    private boolean isSortedAscending(List<WebElement> elements, java.util.function.Function<WebElement, String> extractor) {
        String prev = "";
        for (WebElement element : elements) {
            String current = extractor.apply(element);
            if (prev.compareTo(current) > 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    private boolean isSortedDescending(List<WebElement> elements, java.util.function.Function<WebElement, String> extractor) {
        String prev = "";
        for (WebElement element : elements) {
            String current = extractor.apply(element);
            if (!prev.isEmpty() && prev.compareTo(current) < 0) {
                return false;
            }
            prev = current;
        }
        return true;
    }

    
}