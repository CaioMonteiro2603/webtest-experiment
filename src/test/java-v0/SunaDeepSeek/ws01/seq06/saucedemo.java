package SunaDeepSeek.ws01.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "Should redirect to inventory page after successful login");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
            "Inventory items should be displayed");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"),
            "Should show error message for invalid credentials");
    }

    @Test
    @Order(3)
    public void testProductSorting() {
        login();
        
        // Test Name (A to Z) sorting
        WebElement sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='az']")).click();
        
        List<WebElement> items = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.className("inventory_item_name"), 0));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"),
            "First item should be 'Sauce Labs Backpack' when sorted A-Z");

        // Test Name (Z to A) sorting
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='za']")).click();
        items = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.className("inventory_item_name"), 0));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt"),
            "First item should be 'Test.allTheThings() T-Shirt' when sorted Z-A");

        // Test Price (low to high) sorting
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='lohi']")).click();
        items = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.className("inventory_item_price"), 0));
        Assertions.assertTrue(items.get(0).getText().startsWith("$7.99"),
            "First item price should be $7.99 when sorted low to high");

        // Test Price (high to low) sorting
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='hilo']")).click();
        items = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.className("inventory_item_price"), 0));
        Assertions.assertTrue(items.get(0).getText().startsWith("$49.99"),
            "First item price should be $49.99 when sorted high to low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items link
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
            "Should be on inventory page after clicking All Items");

        // Test About link (external)
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
            "About link should open saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Logout
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
            "Should return to login page after logout");
        
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
    public void testResetAppState() {
        login();
        
        // Add item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_primary.btn_inventory")));
        addToCartButton.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart should show 1 item");
        
        // Reset app state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        resetLink.click();
        
        // Verify cart is empty
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
            "Cart should be empty after reset");
    }

    private void login() {
        driver.get(BASE_URL);
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
            driver.findElement(By.id("password")).sendKeys(PASSWORD);
            driver.findElement(By.id("login-button")).click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        link.click();
        
        // Switch to new tab
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}