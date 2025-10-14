package deepseek.ws01.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
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
public class SauceDemoTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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
    public void testLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("index.html"));

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
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

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        Select sortDropdown = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container"))));
        
        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> itemsAZ = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item_name")));
        Assertions.assertTrue(itemsAZ.get(0).getText().startsWith("Sauce Labs Backpack"));

        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        List<WebElement> itemsZA = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item_name")));
        Assertions.assertTrue(itemsZA.get(0).getText().startsWith("Test.allTheThings() T-Shirt"));

        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        List<WebElement> pricesLOHI = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item_price")));
        Assertions.assertTrue(pricesLOHI.get(0).getText().startsWith("$7.99"));

        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        List<WebElement> pricesHILO = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item_price")));
        Assertions.assertTrue(pricesHILO.get(0).getText().startsWith("$49.99"));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));

        // Test About (external)
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
        
        // Log back in for subsequent tests
        testLoginPage();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        String originalWindow = driver.getWindowHandle();

        // Test Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitterLink.click();
        switchToNewWindowAndAssertDomain("twitter.com", originalWindow);

        // Test Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebookLink.click();
        switchToNewWindowAndAssertDomain("facebook.com", originalWindow);

        // Test LinkedIn
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        switchToNewWindowAndAssertDomain("linkedin.com", originalWindow);
    }

    private void switchToNewWindowAndAssertDomain(String domain, String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(domain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        
        // Add an item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_inventory")));
        addToCart.click();
        Assertions.assertTrue(driver.findElement(By.cssSelector(".shopping_cart_badge")).isDisplayed());

        // Reset app state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size());
    }
}