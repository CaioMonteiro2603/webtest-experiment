package deepseek.ws01.seq08;

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
public class SaucedemoTest {
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";
    private static WebDriver driver;
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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        username.sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
                "Inventory items should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        username.sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().contains("Epic sadface"), "Should display error for invalid login");
    }

    @Test
    @Order(3)
    public void testInventorySorting() {
        loginIfNeeded();
        
        Select sortDropdown = new Select(driver.findElement(By.cssSelector(".product_sort_container")));
        
        // Test name A-Z
        sortDropdown.selectByValue("az");
        List<WebElement> itemsAz = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(itemsAz.get(0).getText().startsWith("Sauce Labs Backpack"),
                "First item should be 'Sauce Labs Backpack' when sorted A-Z");

        // Test name Z-A
        sortDropdown.selectByValue("za");
        List<WebElement> itemsZa = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(itemsZa.get(0).getText().startsWith("Test.allTheThings()"),
                "First item should be 'Test.allTheThings()' when sorted Z-A");

        // Test price low-high
        sortDropdown.selectByValue("lohi");
        List<WebElement> pricesLoHi = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(pricesLoHi.get(0).getText().startsWith("$7.99"),
                "First item price should be $7.99 when sorted low-high");

        // Test price high-low
        sortDropdown.selectByValue("hilo");
        List<WebElement> pricesHiLo = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(pricesHiLo.get(0).getText().startsWith("$49.99"),
                "First item price should be $49.99 when sorted high-low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        loginIfNeeded();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "All Items should navigate to inventory page");
        
        // Open menu again
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open saucelabs domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test reset app state and logout
        testResetAppState();
        testLogout();
    }

    private void testResetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        wait.until(ExpectedConditions.invisibilityOf(reset));
        driver.findElement(By.id("react-burger-cross-btn")).click();
    }

    private void testLogout() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        loginIfNeeded();
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter
        testExternalLink("social_twitter", "twitter.com", originalWindow);
        
        // Test Facebook
        testExternalLink("social_facebook", "facebook.com", originalWindow);
        
        // Test LinkedIn
        testExternalLink("social_linkedin", "linkedin.com", originalWindow);
    }

    private void testExternalLink(String linkId, String expectedDomain, String originalWindow) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        link.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "Link should open " + expectedDomain + " domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
            username.sendKeys(USERNAME);
            driver.findElement(By.id("password")).sendKeys(PASSWORD);
            driver.findElement(By.id("login-button")).click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }
}