package SunaQwen3.ws05.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get(BASE_URL);
        login("invalid_user", "invalid_password");
        WebElement errorButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be displayed");
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message-container h3"));
        assertEquals("Epic sadface: Username and password do not match any user in this service", errorMessage.getText(), "Error message should match expected");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        By sortingDropdownLocator = By.cssSelector(".product_sort_container");
        WebElement sortingDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(sortingDropdownLocator));

        // Test Name (A to Z)
        sortingDropdown.click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortingDropdownLocator, "Name (A to Z)"));
        assertInventoryOrder("name", true, "Items should be sorted by name A to Z");

        // Test Name (Z to A)
        sortingDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortingDropdownLocator, "Name (Z to A)"));
        assertInventoryOrder("name", false, "Items should be sorted by name Z to A");

        // Test Price (low to high)
        sortingDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortingDropdownLocator, "Price (low to high)"));
        assertInventoryOrder("price", true, "Items should be sorted by price low to high");

        // Test Price (high to low)
        sortingDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortingDropdownLocator, "Price (high to low)"));
        assertInventoryOrder("price", false, "Items should be sorted by price high to low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page after clicking All Items");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click About (external)
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
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.stalenessOf(resetLink));
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be redirected to login page after logout");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[alt='Twitter']")));
        twitterLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        switchToNewWindowAndClose(originalWindow);
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[alt='Facebook']")));
        facebookLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        switchToNewWindowAndClose(originalWindow);
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[alt='LinkedIn']")));
        linkedinLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        switchToNewWindowAndClose(originalWindow);
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAddRemoveItemsFromCart() {
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Add first item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-to-cart-sauce-labs-backpack")));
        addToCartButton.click();

        // Verify cart badge updates
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#remove-sauce-labs-backpack")));
        removeFromCartButton.click();

        // Verify cart badge is gone
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart badge should not be displayed");
    }

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

    private void assertInventoryOrder(String type, boolean ascending, String message) {
        java.util.List<WebElement> itemElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item")));
        java.util.List<String> values = new java.util.ArrayList<>();

        for (WebElement item : itemElements) {
            if (type.equals("name")) {
                values.add(item.findElement(By.className("inventory_item_name")).getText());
            } else if (type.equals("price")) {
                String priceText = item.findElement(By.className("inventory_item_price")).getText().replace("$", "");
                values.add(priceText);
            }
        }

        java.util.List<String> sortedValues = new java.util.ArrayList<>(values);
        if (type.equals("price")) {
            sortedValues.sort(java.util.Comparator.comparingDouble(Double::parseDouble));
        } else {
            sortedValues.sort(String.CASE_INSENSITIVE_ORDER);
        }

        if (!ascending) {
            java.util.Collections.reverse(sortedValues);
        }

        assertIterableEquals(sortedValues, values, message);
    }

    private void switchToNewWindowAndClose(String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                wait.until(d -> !driver.getCurrentUrl().isEmpty());
                assertTrue(driver.getCurrentUrl().matches(".*\\.(com|org|net|edu)/?.*"), "New tab should load a valid URL");
                driver.close();
                break;
            }
        }
    }
}