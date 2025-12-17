package Qwen3.ws01.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class saucedemo {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testValidLogin() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("standard_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Epic sadface"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='product_sort_container']")));
        sortDropdown.click(); 

        // Test sorting by price low to high
        WebElement priceLowHighOption = driver.findElement(By.cssSelector("[value='lohi']"));
        priceLowHighOption.click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (low to high)"));
        assertTrue(driver.findElement(By.className("inventory_item_price")).getText().startsWith("$"));

        // Test sorting by price high to low
        sortDropdown.click();
        WebElement priceHighLowOption = driver.findElement(By.cssSelector("[value='hilo']"));
        priceHighLowOption.click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (high to low)"));
        assertTrue(driver.findElement(By.className("inventory_item_price")).getText().startsWith("$"));

        // Test sorting by name A-Z
        sortDropdown.click();
        WebElement nameAZOption = driver.findElement(By.cssSelector("[value='az']"));
        nameAZOption.click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Name (A to Z)"));
        assertTrue(driver.findElement(By.className("inventory_item_name")).getText().length() > 0);

        // Test sorting by name Z-A
        sortDropdown.click();
        WebElement nameZAOption = driver.findElement(By.cssSelector("[value='za']"));
        nameZAOption.click();
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Name (Z to A)"));
        assertTrue(driver.findElement(By.className("inventory_item_name")).getText().length() > 0);
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button_container")));
        menuButton.click();

        // Test All Items
        WebElement allItemsButton = driver.findElement(By.id("inventory_sidebar_link"));
        allItemsButton.click();
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());

        menuButton.click();
        // Test About
        WebElement aboutButton = driver.findElement(By.id("about_sidebar_link"));
        aboutButton.click();
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);

        menuButton.click();
        // Test Logout
        WebElement logoutButton = driver.findElement(By.id("logout_sidebar_link"));
        logoutButton.click();
        assertTrue(driver.getCurrentUrl().contains("index.html"));

        // Return to inventory page and reset app state
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        menuButton.click();
        WebElement resetAppStateButton = driver.findElement(By.id("reset_sidebar_link"));
        resetAppStateButton.click();
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("[href*='twitter.com']"));
        twitterLink.click();
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("twitter.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("[href*='facebook.com']"));
        facebookLink.click();
        originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("facebook.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("[href*='linkedin.com']"));
        linkedinLink.click();
        originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

}