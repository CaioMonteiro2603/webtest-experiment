package SunaDeepSeek.ws05.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed(), 
            "Inventory list should be displayed after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();
        
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"),
            "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        login();
        
        WebElement sortDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.sendKeys("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"),
            "First item should be 'Sauce Labs Backpack' when sorted A-Z");
        
        // Test Name (Z to A)
        sortDropdown.sendKeys("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt"),
            "First item should be 'Test.allTheThings() T-Shirt' when sorted Z-A");
        
        // Test Price (low to high)
        sortDropdown.sendKeys("lohi");
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(prices.get(0).getText().equals("$7.99"),
            "First item price should be $7.99 when sorted low to high");
        
        // Test Price (high to low)
        sortDropdown.sendKeys("hilo");
        prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(prices.get(0).getText().equals("$49.99"),
            "First item price should be $49.99 when sorted high to low");
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
            "Should be on inventory page after clicking All Items");
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test About (external link)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
            "About link should navigate to saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.findElement(By.id("login-button")).isDisplayed(),
            "Should be back on login page after logout");
        
        // Login again for remaining tests
        login();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        login();
        
        // Test Twitter link
        testExternalLink("social_twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("social_facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("social_linkedin", "linkedin.com");
    }

    @Test
    @Order(6)
    public void testAddRemoveItems() {
        login();
        resetAppState();
        
        // Add first item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_inventory")));
        addToCartButton.click();
        
        // Verify cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
            "Cart badge should show 1 after adding an item");
        
        // Remove item from cart
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_inventory")));
        removeButton.click();
        
        // Verify cart badge is gone
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.className("shopping_cart_badge")));
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
            "Cart badge should be removed after removing all items");
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        login();
        
        // Add an item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_inventory")));
        addToCartButton.click();
        
        // Open menu and reset app state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        resetLink.click();
        
        // Verify cart is empty
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.className("shopping_cart_badge")));
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
            "Cart should be empty after resetting app state");
    }

    private void login() {
        driver.get(BASE_URL);
        if (driver.getCurrentUrl().contains("index.html")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));
            
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void resetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        resetLink.click();
        
        // Close menu
        WebElement closeMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-cross-btn")));
        closeMenu.click();
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Link should navigate to " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}