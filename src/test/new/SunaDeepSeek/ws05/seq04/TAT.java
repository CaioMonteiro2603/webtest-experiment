package SunaDeepSeek.ws05.seq04;

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
    private static final String BASE_URL = "https://www.saucedemo.com";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLogin() {
        driver.get(BASE_URL);
        
        // Test invalid login
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();
        
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"),
            "Error message for invalid login should be displayed");
        
        // Test valid login
        usernameField.clear();
        passwordField.clear();
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
            "Should be redirected to inventory page after successful login");
    }

    @Test
    @Order(2)
    public void testSortingDropdown() {
        driver.get(BASE_URL + "/inventory.html");
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select.product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.sendKeys("az");
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"),
            "First item should be 'Sauce Labs Backpack' when sorted A-Z");
        
        // Test Name (Z to A)
        sortDropdown.sendKeys("za");
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt"),
            "First item should be 'Test.allTheThings() T-Shirt' when sorted Z-A");
        
        // Test Price (low to high)
        sortDropdown.sendKeys("lohi");
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(prices.get(0).getText().startsWith("$7.99"),
            "First item price should be $7.99 when sorted low to high");
        
        // Test Price (high to low)
        sortDropdown.sendKeys("hilo");
        prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(prices.get(0).getText().startsWith("$49.99"),
            "First item price should be $49.99 when sorted high to low");
    }

    @Test
    @Order(3)
    public void testMenuOptions() {
        driver.get(BASE_URL + "/inventory.html");
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
            "Should remain on inventory page when clicking All Items");
        
        // Reopen menu
        menuButton.click();
        
        // Test About (external link)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
            "About link should open saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Reopen menu
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
            "Should be redirected to login page after logout");
        
        // Login again for subsequent tests
        driver.findElement(By.id("user-name")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get(BASE_URL + "/inventory.html");
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[data-test='social-twitter']")));
        twitterLink.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
            "Twitter link should open twitter.com");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[data-test='social-facebook']")));
        facebookLink.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"),
            "Facebook link should open facebook.com");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[data-test='social-linkedin']")));
        linkedinLink.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"),
            "LinkedIn link should open linkedin.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        driver.get(BASE_URL + "/inventory.html");
        
        // Add an item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
            "Cart should show 1 item after adding");
        
        // Open menu and reset app state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size(),
            "Cart should be empty after reset");
    }

    private void switchToNewWindow(String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}