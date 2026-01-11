package SunaDeepSeek.ws01.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
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
    public void testLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Login failed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"), 
            "Error message not displayed");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get(BASE_URL + "/inventory.html");
        loginIfNeeded();
        
        WebElement sortDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("select.product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.findElement(By.cssSelector("option[value='az']")).click();
        List<WebElement> itemsAZ = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".inventory_item_name")));
        Assertions.assertTrue(itemsAZ.get(0).getText().startsWith("Sauce Labs Backpack"), 
            "Sorting A-Z failed");
        
        // Test Name (Z to A)
        sortDropdown.findElement(By.cssSelector("option[value='za']")).click();
        List<WebElement> itemsZA = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(itemsZA.get(0).getText().startsWith("Test.allTheThings() T-Shirt (Red)"), 
            "Sorting Z-A failed");
        
        // Test Price (low to high)
        sortDropdown.findElement(By.cssSelector("option[value='lohi']")).click();
        List<WebElement> pricesLoHi = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(pricesLoHi.get(0).getText().equals("$7.99"), 
            "Sorting price low-high failed");
        
        // Test Price (high to low)
        sortDropdown.findElement(By.cssSelector("option[value='hilo']")).click();
        List<WebElement> pricesHiLo = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(pricesHiLo.get(0).getText().equals("$49.99"), 
            "Sorting price high-low failed");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        driver.get(BASE_URL + "/inventory.html");
        loginIfNeeded();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.className("bm-burger-button")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "All Items navigation failed");
        
        // Open menu again
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), 
            "About page not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), 
            "Logout failed");
        
        // Log back in for remaining tests
        loginIfNeeded();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        driver.get(BASE_URL + "/inventory.html");
        loginIfNeeded();
        
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".social_twitter a")));
        twitterLink.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
            "Twitter link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".social_facebook a")));
        facebookLink.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), 
            "Facebook link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), 
            "LinkedIn link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        driver.get(BASE_URL + "/inventory.html");
        loginIfNeeded();
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn_primary.btn_inventory")));
        addToCart.click();
        
        // Verify cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Item not added to cart");
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.className("bm-burger-button")));
        menuButton.click();
        
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size(), "Cart not reset");
    }

    private void loginIfNeeded() {
        if (driver.getCurrentUrl().contains("index.html")) {
            WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));
            
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void switchToNewWindow(String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}