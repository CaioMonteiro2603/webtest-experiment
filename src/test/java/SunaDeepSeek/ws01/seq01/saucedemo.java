package SunaDeepSeek.ws01.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTest {

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
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
                "Inventory items should be displayed after successful login");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
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
    public void testProductSorting() {
        login();
        
        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"),
                "First item should be 'Sauce Labs Backpack' when sorted A-Z");

        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt"),
                "First item should be 'Test.allTheThings() T-Shirt' when sorted Z-A");

        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().equals("$7.99"),
                "First item price should be $7.99 when sorted low to high");

        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().equals("$49.99"),
                "First item price should be $49.99 when sorted high to low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "Should be on inventory page after clicking All Items");

        // Open menu again
        menuButton.click();
        
        // Test About (external link)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should redirect to saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Should be back on login page after logout");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        
        // Test Twitter link
        testExternalLink("social_twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("social_facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("social_linkedin", "linkedin.com");
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "Link should redirect to " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        login();
        
        // Add item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[text()='Sauce Labs Backpack']/ancestor::div[@class='inventory_item']//button")));
        addToCartButton.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart should show 1 item after adding");

        // Open menu and reset
        WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
        menuButton.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        
        // Verify cart is empty
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
                "Cart should be empty after reset");
    }

    private void login() {
        driver.get(BASE_URL);
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }
}