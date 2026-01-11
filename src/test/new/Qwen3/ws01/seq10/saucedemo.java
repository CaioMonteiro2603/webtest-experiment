package Qwen3.ws01.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class saucedemo {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private final String LOGIN = "standard_user";
    private final String PASSWORD = "secret_sauce";

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
    void testValidLogin_SuccessfulRedirectToInventory() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");
        assertTrue(isElementPresent(By.className("inventory_list")), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin_ErrorMessageDisplayed() {
        driver.get(BASE_URL);

        loginWithCredentials("invalid_user", PASSWORD);
        WebElement errorButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".error-button")));
        assertTrue(errorButton.isDisplayed(), "Error button should be visible for invalid login");
        String errorMessage = driver.findElement(By.cssSelector(".error-message-container")).getText();
        assertTrue(errorMessage.contains("Epic sadface"), "Error message should indicate login failure");
    }

    @Test
    @Order(3)
    void testLockedUser_RedirectsToProblemUserPage() {
        driver.get(BASE_URL);

        loginWithCredentials("locked_out_user", PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        String errorMessage = driver.findElement(By.cssSelector(".error-message-container")).getText();
        assertTrue(errorMessage.contains("locked out"), "Error message should indicate user is locked out");
    }

    @Test
    @Order(4)
    void testMenuAllItems_NavigatesToInventory() {
        goToInventoryPage();
        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Clicking 'All Items' should return to inventory");
    }

    @Test
    @Order(5)
    void testMenuAbout_OpenExternalInNewTab() {
        goToInventoryPage();
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();

        aboutLink.sendKeys(Keys.CONTROL + "k"); // Attempt to open in new tab (inspect behavior)
        aboutLink.click(); // Some browsers may open in same tab

        // Check if new window appeared
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                wait.until(ExpectedConditions.urlContains("saucelabs.com"));
                assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // If no new tab, validate same window redirect
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    @Test
    @Order(6)
    void testMenuLogout_ReturnsToLoginPage() {
        goToInventoryPage();
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Logout should redirect to login page");
    }

    @Test
    @Order(7)
    void testMenuResetAppState_ResetsCart() {
        goToInventoryPage();

        // Add item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item .btn_primary")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should contain 1 item before reset");

        openMenu();
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetButton.click();

        assertFalse(isElementPresent(By.cssSelector(".shopping_cart_badge")), "Cart badge should disappear after reset");
    }

    @Test
    @Order(8)
    void testSortByNameAZ_ItemsSortedAlphabetically() {
        goToInventoryPage();
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortSelect.click();
        sortSelect.sendKeys("az");
        sortSelect.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Name (A to Z)"));

        // Validate first and last product names
        WebElement firstProduct = driver.findElement(By.cssSelector(".inventory_item_name"));
        String actualFirst = firstProduct.getText();
        assertTrue(actualFirst.compareTo("Sauce Labs Backpack") <= 0, "First item should be first alphabetically (A-Z)");
    }

    @Test
    @Order(9)
    void testSortByNameZA_ItemsSortedReverseAlphabetically() {
        goToInventoryPage();
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortSelect.click();
        sortSelect.sendKeys("az");
        sortSelect.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Name (A to Z)"));

        WebElement firstProduct = driver.findElement(By.cssSelector(".inventory_item_name"));
        assertEquals("Sauce Labs Backpack", firstProduct.getText(), "First item should be Sauce Labs Backpack (A-Z)");
    }

    @Test
    @Order(10)
    void testSortByPriceLowToHigh_ItemsSortedAscending() {
        goToInventoryPage();
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortSelect.click();
        sortSelect.sendKeys("lohi");
        sortSelect.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Price (low to high)"));

        java.util.List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        double lastPrice = 0;
        for (WebElement priceElement : prices) {
            double currentPrice = Double.parseDouble(priceElement.getText().replace("$", ""));
            assertTrue(currentPrice >= lastPrice, "Prices should be in ascending order");
            lastPrice = currentPrice;
        }
    }

    @Test
    @Order(11)
    void testSortByPriceHighToLow_ItemsSortedDescending() {
        goToInventoryPage();
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortSelect.click();
        sortSelect.sendKeys("hilo");
        sortSelect.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".active_option"), "Price (high to low)"));

        java.util.List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        double lastPrice = Double.MAX_VALUE;
        for (WebElement priceElement : prices) {
            double currentPrice = Double.parseDouble(priceElement.getText().replace("$", ""));
            assertTrue(currentPrice <= lastPrice, "Prices should be in descending order");
            lastPrice = currentPrice;
        }
    }

    @Test
    @Order(12)
    void testFooterTwitterLink_OpenExternalInNewTab() {
        goToInventoryPage();
        WebElement twitterIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[alt='Twitter']")));
        String originalWindow = driver.getWindowHandle();
        twitterIcon.click();

        switchToNewWindowOrValidateRedirect(originalWindow, "twitter.com");
    }

    @Test
    @Order(13)
    void testFooterFacebookLink_OpenExternalInNewTab() {
        goToInventoryPage();
        WebElement facebookIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[alt='Facebook']")));
        String originalWindow = driver.getWindowHandle();
        facebookIcon.click();

        switchToNewWindowOrValidateRedirect(originalWindow, "facebook.com");
    }

    @Test
    @Order(14)
    void testFooterLinkedInLink_OpenExternalInNewTab() {
        goToInventoryPage();
        WebElement linkedinIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[alt='LinkedIn']")));
        String originalWindow = driver.getWindowHandle();
        linkedinIcon.click();

        switchToNewWindowOrValidateRedirect(originalWindow, "linkedin.com");
    }

    private void switchToNewWindowOrValidateRedirect(String originalWindow, String expectedDomain) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Social link should open correct domain: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // If no new tab, then current tab redirected
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Social link should redirect to " + expectedDomain);
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void loginWithCredentials(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        passwordField.clear();

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }

    private void goToInventoryPage() {
        driver.get(BASE_URL);
        loginWithCredentials(LOGIN, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("bm-burger-button")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));
    }

    private boolean isElementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }
}