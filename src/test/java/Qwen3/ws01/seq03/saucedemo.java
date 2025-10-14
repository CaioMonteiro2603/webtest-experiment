package Qwen3.ws01.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("standard_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("/inventory.html"));
        assertTrue(driver.findElement(By.id("inventory_container")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".error-message-container")).getText().contains("Epic sadface"));
    }

    @Test
    @Order(3)
    public void testSorting() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test=\"product_sort_container\"]")));
        sortDropdown.click();

        // Test sorting by Name (A-Z)
        WebElement sortByName = driver.findElement(By.xpath("//option[@value='az']"));
        sortByName.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_name")));
        List<WebElement> itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        String firstItemName = itemNames.get(0).getText();
        assertEquals("Sauce Labs Backpack", firstItemName);

        // Test sorting by Name (Z-A)
        sortDropdown.click();
        WebElement sortByZ = driver.findElement(By.xpath("//option[@value='za']"));
        sortByZ.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_name")));
        itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        String lastItemName = itemNames.get(itemNames.size() - 1).getText();
        assertEquals("Test.allTheThings() T-Shirt (Red)", lastItemName);
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Click burger menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click All Items
        WebElement allItems = driver.findElement(By.id("inventory_sidebar_link"));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("/inventory.html"));

        // Re-open menu
        menuButton.click();

        // Click About
        WebElement about = driver.findElement(By.id("about_sidebar_link"));
        about.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);

        // Re-open menu
        menuButton.click();

        // Click Logout
        WebElement logout = driver.findElement(By.id("logout_sidebar_link"));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/index.html"));
        assertTrue(driver.getCurrentUrl().contains("/index.html"));

        // Re-login
        testValidLogin();
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://www.saucedemo.com/v1/index.html");

        // Click Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("[href*='twitter']"));
        twitterLink.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);

        // Click Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("[href*='facebook']"));
        facebookLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);

        // Click LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("[href*='linkedin']"));
        linkedinLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }
}