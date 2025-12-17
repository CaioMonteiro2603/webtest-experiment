package SunaDeepSeek.ws01.seq08;

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
    public void testLoginPage() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"));

        usernameField.clear();
        passwordField.clear();
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    @Test
    @Order(2)
    public void testInventoryPage() {
        driver.get(BASE_URL + "inventory.html");
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));

        List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));
        Assertions.assertEquals(6, items.size());
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get(BASE_URL + "inventory.html");
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        
        // Test Name (A to Z)
        sortDropdown.sendKeys("Name (A to Z)");
        WebElement firstItemAZ = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_name")));
        Assertions.assertTrue(firstItemAZ.getText().startsWith("Sauce Labs Backpack"));

        // Test Name (Z to A)
        sortDropdown.sendKeys("Name (Z to A)");
        WebElement firstItemZA = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_name")));
        Assertions.assertTrue(firstItemZA.getText().startsWith("Test.allTheThings() T-Shirt"));

        // Test Price (low to high)
        sortDropdown.sendKeys("Price (low to high)");
        WebElement firstItemLow = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_price")));
        Assertions.assertTrue(firstItemLow.getText().contains("7.99"));

        // Test Price (high to low)
        sortDropdown.sendKeys("Price (high to low)");
        WebElement firstItemHigh = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_price")));
        Assertions.assertTrue(firstItemHigh.getText().contains("49.99"));
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        driver.get(BASE_URL + "inventory.html");
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        
        // Reopen menu
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
        
        // Reopen menu
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test Reset App State
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Close menu
        WebElement closeMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeMenu.click();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        driver.get(BASE_URL + "inventory.html");
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitterLink.click();
        
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
        
        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebookLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        
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
    @Order(6)
    public void testLogout() {
        driver.get(BASE_URL + "inventory.html");
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
    }
}