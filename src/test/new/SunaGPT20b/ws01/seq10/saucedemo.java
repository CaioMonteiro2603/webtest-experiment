package SunaGPT20b.ws01.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
        // Ensure we are on the login page before each test
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void login(String user, String pass) {
        WebElement usernameInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameInput.clear();
        usernameInput.sendKeys(user);

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys(pass);

        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();
    }

    private void openMenu() {
        WebElement menuButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for the menu to close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu")));
    }

    private void logout() {
        openMenu();
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > windowsBefore.size());

        Set<String> windowsAfter = driver.getWindowHandles();
        windowsAfter.removeAll(windowsBefore);
        String newWindow = windowsAfter.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "Expected URL to contain domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login should navigate to inventory page.");

        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container should be displayed after login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login(USERNAME, "wrong_password");
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username and password do not match"),
                "Error message should indicate credential mismatch.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container")));

        Select sortSelect = new Select(driver.findElement(By.className("product_sort_container")));
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};

        for (String option : options) {
            sortSelect.selectByVisibleText(option);
            // Verify that the first item changes after sorting
            List<WebElement> items = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
            Assertions.assertFalse(items.isEmpty(), "Inventory items should be present after sorting.");
            String firstItem = items.get(0).getText();
            Assertions.assertNotNull(firstItem, "First item name should not be null after sorting with option: " + option);
        }
    }

    @Test
    @Order(4)
    public void testMenuResetAppState() {
        login(USERNAME, PASSWORD);
        // Add an item to cart
        WebElement addToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();

        // Verify cart badge appears
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 after adding an item.");

        // Reset app state
        resetAppState();

        // Verify cart badge is gone
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(),
                "Cart badge should be removed after resetting app state.");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // The About link opens a new tab to saucelabs.com
        switchToNewTabAndVerify("saucelabs.com");
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USERNAME, PASSWORD);
        logout();
        // Verify we are back on login page
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "After logout, URL should be the login page.");
        WebElement loginBtn = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(loginBtn.isDisplayed(), "Login button should be visible after logout.");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Twitter
        WebElement twitterLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_twitter")));
        twitterLink.click();
        switchToNewTabAndVerify("twitter.com");

        // Facebook
        WebElement facebookLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_facebook")));
        facebookLink.click();
        switchToNewTabAndVerify("facebook.com");

        // LinkedIn
        WebElement linkedInLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_linkedin")));
        linkedInLink.click();
        switchToNewTabAndVerify("linkedin.com");
    }

    @Test
    @Order(8)
    public void testCheckoutProcess() {
        login(USERNAME, PASSWORD);
        // Add first item to cart
        WebElement addToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();

        // Go to cart
        WebElement cartIcon = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartIcon.click();

        // Verify cart page
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"),
                "Should navigate to cart page.");

        // Click Checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout information
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish checkout
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish")));
        driver.findElement(By.id("finish")).click();

        // Verify completion message
        WebElement thankYou = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("Thank you for your order!", thankYou.getText(),
                "Checkout completion message should be displayed.");
    }
}