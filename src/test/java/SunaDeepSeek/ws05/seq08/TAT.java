package SunaDeepSeek.ws05.seq08;

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
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Swag Labs"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0);
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("invalid_user");
        password.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        testSuccessfulLogin(); // Reuse login to get to inventory page
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.sendKeys("Name (A to Z)");
        List<WebElement> itemsAZ = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.className("inventory_item_name")));
        Assertions.assertTrue(itemsAZ.get(0).getText().startsWith("Sauce Labs Backpack"));

        // Test Name (Z to A)
        sortDropdown.sendKeys("Name (Z to A)");
        List<WebElement> itemsZA = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.className("inventory_item_name")));
        Assertions.assertTrue(itemsZA.get(0).getText().startsWith("Test.allTheThings()"));

        // Test Price (low to high)
        sortDropdown.sendKeys("Price (low to high)");
        List<WebElement> pricesLowHigh = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.className("inventory_item_price")));
        Assertions.assertTrue(pricesLowHigh.get(0).getText().startsWith("$7.99"));

        // Test Price (high to low)
        sortDropdown.sendKeys("Price (high to low)");
        List<WebElement> pricesHighLow = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.className("inventory_item_price")));
        Assertions.assertTrue(pricesHighLow.get(0).getText().startsWith("$49.99"));
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        testSuccessfulLogin(); // Reuse login to get to inventory page
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        testSuccessfulLogin(); // Reuse login to get to inventory page
        
        // Test Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Twitter")));
        twitter.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Facebook")));
        facebook.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("LinkedIn")));
        linkedin.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        testSuccessfulLogin(); // Reuse login to get to inventory page
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();
        
        // Verify cart has item
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText());
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty());
    }
}