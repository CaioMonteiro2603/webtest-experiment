package SunaDeepSeek.ws05.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.htmll";
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
    public void testLoginPage() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htmll"), "Should be on login page");

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");
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

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"), 
            "Should show error message for invalid credentials");
    }

    @Test
    @Order(3)
    public void testInventoryPageElements() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be on inventory page");

        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".header_secondary_container .title")));
        Assertions.assertEquals("Products", title.getText(), "Page title should be 'Products'");

        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertTrue(items.size() > 0, "Should display inventory items");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        login();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        WebElement firstItemAZ = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".inventory_item_name")));
        Assertions.assertTrue(firstItemAZ.getText().startsWith("Sauce Labs Backpack"), 
            "First item should be 'Sauce Labs Backpack' when sorted A-Z");

        // Test Name (Z to A)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        WebElement firstItemZA = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".inventory_item_name")));
        Assertions.assertTrue(firstItemZA.getText().startsWith("Test.allTheThings() T-Shirt (Red)"), 
            "First item should be 'Test.allTheThings() T-Shirt (Red)' when sorted Z-A");

        // Test Price (low to high)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        WebElement firstItemLow = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".inventory_item_price")));
        Assertions.assertEquals("$7.99", firstItemLow.getText(), 
            "First item price should be $7.99 when sorted low to high");

        // Test Price (high to low)
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        WebElement firstItemHigh = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".inventory_item_price")));
        Assertions.assertEquals("$49.99", firstItemHigh.getText(), 
            "First item price should be $49.99 when sorted high to low");
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();

        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "Should be on inventory page after clicking All Items");

        // Test About (external)
        menuButton.click();
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
            "Should be on saucelabs.com after clicking About");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htmll"), 
            "Should be back on login page after logout");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        login();
        
        // Test Twitter link
        testExternalLink(By.cssSelector(".social_twitter a"), "twitter.com");
        
        // Test Facebook link
        testExternalLink(By.cssSelector(".social_facebook a"), "facebook.com");
        
        // Test LinkedIn link
        testExternalLink(By.cssSelector(".social_linkedin a"), "linkedin.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "Should be on " + expectedDomain + " after clicking link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }
}