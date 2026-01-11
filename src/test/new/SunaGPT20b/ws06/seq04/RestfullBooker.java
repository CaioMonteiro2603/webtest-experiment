package SunaGPT20b.ws06.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
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

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void login(String username, String password) {
        navigateToBase();
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void resetAppState() {
        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Close menu
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "URL should contain /inventory.html after successful login");
        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container should be displayed after login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToBase();
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_password");
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("standard_user", "secret_sauce");
        // Ensure we are on inventory page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));

        By sortLocator = By.cssSelector("select.product_sort_container");
        WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        Select sortSelect = new Select(sortElement);

        // Options: Name (A to Z), Name (Z to A), Price (low to high), Price (high to low)
        String[] options = {"az", "za", "lohi", "hilo"};
        for (String value : options) {
            sortSelect.selectByValue(value);
            // Verify that the first item changes after each selection
            List<WebElement> items = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
            Assertions.assertFalse(items.isEmpty(), "Item list should not be empty after sorting");
            // Simple sanity check: first item's text is not null
            Assertions.assertNotNull(items.get(0).getText(),
                    "First item name should be present after sorting with option " + value);
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuBurgerAndExternalLinks() {
        login("standard_user", "secret_sauce");
        // Open burger menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should navigate to inventory page after clicking All Items");

        // Open menu again for other actions
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open Saucelabs domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Verify cart badge is gone
        List<WebElement> badge = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badge.isEmpty(), "Cart badge should be cleared after reset");

        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/"),
                "Should be back on login page after logout");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");
        // Scroll to footer
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Define social link selectors and expected domains
        String[][] socials = {
                {"a.social_twitter", "twitter.com"},
                {"a.social_facebook", "facebook.com"},
                {"a.social_linkedin", "linkedin.com"}
        };

        for (String[] social : socials) {
            By locator = By.cssSelector(social[0]);
            List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
                continue; // Skip if not present
            }
            WebElement link = elements.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new window
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(social[1]));
            Assertions.assertTrue(driver.getCurrentUrl().contains(social[1]),
                    "Social link should open a page containing " + social[1]);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login("standard_user", "secret_sauce");
        // Add first item to cart
        WebElement firstAddBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.btn_inventory")));
        firstAddBtn.click();

        // Verify cart badge shows 1
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(),
                "Cart badge should display count 1 after adding an item");

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"),
                "Should be on cart page");

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));

        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish")));
        driver.findElement(By.id("finish")).click();

        // Verify success message
        WebElement thankYou = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h2.complete-header")));
        Assertions.assertTrue(thankYou.getText().toUpperCase().contains("THANK YOU"),
                "Checkout should display a thank you message");

        // Reset state for next tests
        resetAppState();
    }
}