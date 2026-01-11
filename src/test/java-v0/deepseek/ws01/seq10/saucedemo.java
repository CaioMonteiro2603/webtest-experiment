package deepseek.ws01.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {
    private static WebDriver driver;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
    private static final String PASSWORD = "secret_sauce";
    private static WebDriverWait wait;

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
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys(LOGIN);
        password.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Login failed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("invalid_user");
        password.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"), "Error message not displayed");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginAndNavigateToInventory();
        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));

        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"), "Sort A-Z failed");

        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt (Red)"), "Sort Z-A failed");

        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$7.99"), "Sort low to high failed");

        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        items = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$49.99"), "Sort high to low failed");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        loginAndNavigateToInventory();
        
        // Open menu
        WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "All Items navigation failed");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About navigation failed");
        driver.navigate().back();
        
        // Reset app state
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Close menu
        WebElement closeButton = driver.findElement(By.id("react-burger-cross-btn"));
        closeButton.click();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        loginAndNavigateToInventory();
        
        // Test Twitter
        testExternalLink("social_twitter", "twitter.com");
        
        // Test Facebook
        testExternalLink("social_facebook", "facebook.com");
        
        // Test LinkedIn
        testExternalLink("social_linkedin", "linkedin.com");
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        link.click();
        
        // Switch to new window
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Link " + linkId + " failed");
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(6)
    public void testLogout() {
        loginAndNavigateToInventory();
        
        WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
        menuButton.click();
        
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout failed");
    }

    private void loginAndNavigateToInventory() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys(LOGIN);
        password.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }
}