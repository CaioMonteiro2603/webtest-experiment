package Qwen3.ws01.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class saucedemo {
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
    public void testInvalidLogin() {
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
    public void testSorting() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();

        // Sort by Name (A-Z)
        WebElement sortByAtoZ = driver.findElement(By.xpath("//option[@value='az']"));
        sortByAtoZ.click();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertEquals("Sauce Labs Backpack", items.get(0).getText());

        // Sort by Name (Z-A)
        sortDropdown.click();
        WebElement sortByZtoA = driver.findElement(By.xpath("//option[@value='za']"));
        sortByZtoA.click();
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertEquals("Test.allTheThings() T-Shirt (Red)", items.get(0).getText());

        // Sort by Price (low to high)
        sortDropdown.click();
        WebElement sortByLowToHigh = driver.findElement(By.xpath("//option[@value='lo']"));
        sortByLowToHigh.click();
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstPrice = Double.parseDouble(prices.get(0).getText().substring(1));
        double secondPrice = Double.parseDouble(prices.get(1).getText().substring(1));
        assertTrue(firstPrice <= secondPrice);

        // Sort by Price (high to low)
        sortDropdown.click();
        WebElement sortByHighToLow = driver.findElement(By.xpath("//option[@value='hi']"));
        sortByHighToLow.click();
        prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        firstPrice = Double.parseDouble(prices.get(0).getText().substring(1));
        secondPrice = Double.parseDouble(prices.get(1).getText().substring(1));
        assertTrue(firstPrice >= secondPrice);
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        // Open the side menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));

        // Wait for menu items to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));

        // Click 'All Items'
        WebElement allItemsButton = driver.findElement(By.id("inventory_sidebar_link"));
        allItemsButton.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());

        // Open menu again
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("about_sidebar_link")));

        // Click 'About'
        WebElement aboutButton = driver.findElement(By.id("about_sidebar_link"));
        aboutButton.click();
        Set<String> handles = driver.getWindowHandles();
        String originalHandle = driver.getWindowHandle();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalHandle);

        // Open menu again
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));

        // Click 'Logout'
        WebElement logoutButton = driver.findElement(By.id("logout_sidebar_link"));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"));
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed());

        // Log in again
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("standard_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"));

        // Open menu again
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reset_sidebar_link")));

        // Click 'Reset App State'
        WebElement resetButton = driver.findElement(By.id("reset_sidebar_link"));
        resetButton.click();

        // Close menu
        WebElement closeButton = driver.findElement(By.id("react-burger-cross-btn"));
        closeButton.click();
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://www.saucedemo.com/v1/index.html");

        // Login first to access full site
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("standard_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        // Wait for inventory page
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Find social links
        WebElement footer = driver.findElement(By.className("footer"));
        List<WebElement> footerLinks = footer.findElements(By.tagName("a"));

        // Twitter
        WebElement twitterLink = null;
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("twitter")) {
                twitterLink = link;
                break;
            }
        }

        // Facebook
        WebElement facebookLink = null;
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("facebook")) {
                facebookLink = link;
                break;
            }
        }

        // LinkedIn
        WebElement linkedInLink = null;
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("linkedin")) {
                linkedInLink = link;
                break;
            }
        }

        assertEquals(3, footerLinks.size());

        String originalHandle = driver.getWindowHandle();

        // Test Twitter link
        twitterLink.click();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com") || driver.getCurrentUrl().contains("x.com"));
        driver.close();
        driver.switchTo().window(originalHandle);

        // Test Facebook link
        facebookLink.click();
        handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(originalHandle);

        // Test LinkedIn link
        linkedInLink.click();
        handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(originalHandle);
    }
}