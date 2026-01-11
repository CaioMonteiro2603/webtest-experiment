package SunaQwen3.ws06.seq10;

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
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String LOGIN_PAGE_URL = BASE_URL + "#/login";
    private static final String INVENTORY_PAGE_URL = BASE_URL + "#/inventory";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

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
    public void testValidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("loginButton"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "Should be redirected to inventory page after login");

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventoryContainer")));
        Assertions.assertTrue(inventoryList.isDisplayed(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("loginButton"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        navigateToInventory();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container")));
        
        // Option: Name (A to Z)
        sortDropdown.click();
        WebElement optionAZ = driver.findElement(By.cssSelector("option[value='az']"));
        optionAZ.click();
        verifyFirstItemName("Apple");

        // Option: Name (Z to A)
        sortDropdown.click();
        WebElement optionZA = driver.findElement(By.cssSelector("option[value='za']"));
        optionZA.click();
        verifyFirstItemName("Zebra");

        // Option: Price (low to high)
        sortDropdown.click();
        WebElement optionLoHi = driver.findElement(By.cssSelector("option[value='lohi']"));
        optionLoHi.click();
        verifyFirstItemPrice("$10");

        // Option: Price (high to low)
        sortDropdown.click();
        WebElement optionHiLo = driver.findElement(By.cssSelector("option[value='hilo']"));
        optionHiLo.click();
        verifyFirstItemPrice("$99");
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        navigateToInventory();

        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventoryContainer")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be displayed after clicking All Items");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        navigateToInventory();

        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open saucelabs.com");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        // Verify we're back on the inventory page
        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        navigateToInventory();

        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        navigateToInventory();

        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart should have 1 item before reset");

        // Open menu and reset app state
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu
        WebElement closeMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeMenuButton.click();

        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(cartBadges.isEmpty() || !cartBadges.get(0).isDisplayed(), "Cart should be empty after reset app state");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        navigateToInventory();

        // Twitter link
        testExternalLink(By.id("twitter-link"), "twitter.com");

        // Facebook link
        testExternalLink(By.id("facebook-link"), "facebook.com");

        // LinkedIn link
        testExternalLink(By.id("linkedin-link"), "linkedin.com");
    }

    private void navigateToInventory() {
        if (!driver.getCurrentUrl().contains("inventory")) {
            driver.get(INVENTORY_PAGE_URL);
            wait.until(ExpectedConditions.urlContains("inventory"));
        }
        // Ensure app state is clean
        resetAppState();
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));
    }

    private void resetAppState() {
        if (driver.getCurrentUrl().contains("inventory")) {
            try {
                openMenu();
                WebElement resetLink = driver.findElement(By.id("reset_sidebar_link"));
                resetLink.click();
                WebElement closeMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
                closeMenu.click();
            } catch (Exception e) {
                // Menu might not be open or reset option not available
            }
        }
    }

    private void verifyFirstItemName(String expectedName) {
        List<WebElement> itemNames = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        Assertions.assertFalse(itemNames.isEmpty(), "There should be at least one item in the inventory");
        Assertions.assertEquals(expectedName, itemNames.get(0).getText(), "First item name should match the expected name after sorting");
    }

    private void verifyFirstItemPrice(String expectedPrice) {
        List<WebElement> itemPrices = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        Assertions.assertFalse(itemPrices.isEmpty(), "There should be at least one item with a price");
        Assertions.assertEquals(expectedPrice, itemPrices.get(0).getText(), "First item price should match the expected price after sorting");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        String originalWindow = driver.getWindowHandle();

        link.click();

        // Wait for new window and switch
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert domain
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should open the expected domain");

        // Close and return
        driver.close();
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.urlContains("inventory"));
    }
}