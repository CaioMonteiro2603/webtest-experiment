package SunaDeepSeek.ws08.seq05;

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
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

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
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JPetStore Demo"));

        // Verify main categories
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("#SidebarContent a")));
        Assertions.assertEquals(5, categories.size(), "Should have 5 main categories");
    }

    @Test
    @Order(2)
    public void testCategoryPages() {
        driver.get(BASE_URL);
        
        // Test Fish category
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#SidebarContent a[href$='FISH']")));
        fishLink.click();
        wait.until(ExpectedConditions.urlContains("FISH"));
        Assertions.assertTrue(driver.getPageSource().contains("Fish"), "Fish category page should load");

        // Test Dogs category
        driver.get(BASE_URL);
        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#SidebarContent a[href$='DOGS']")));
        dogsLink.click();
        wait.until(ExpectedConditions.urlContains("DOGS"));
        Assertions.assertTrue(driver.getPageSource().contains("Dogs"), "Dogs category page should load");
    }

    @Test
    @Order(3)
    public void testProductPages() {
        // Navigate to Fish category
        driver.get(BASE_URL + "catalog/categories/FISH");
        
        // Test product link
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#Catalog table tr:nth-child(3) a")));
        productLink.click();
        wait.until(ExpectedConditions.urlContains("products"));
        Assertions.assertTrue(driver.getPageSource().contains("List of Pets"), "Product page should load");
    }

    @Test
    @Order(4)
    public void testItemPages() {
        // Navigate to Fish product
        driver.get(BASE_URL + "catalog/products/FI-FW-01");
        
        // Test item link
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#Catalog table tr:nth-child(3) a")));
        itemLink.click();
        wait.until(ExpectedConditions.urlContains("items"));
        Assertions.assertTrue(driver.getPageSource().contains("Back to Products"), "Item page should load");
    }

    @Test
    @Order(5)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "account/signonForm");
        
        // Valid login
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        Assertions.assertTrue(driver.getPageSource().contains("Welcome"), "Successful login should show welcome message");

        // Logout
        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#MenuContent a[href$='signoff']")));
        signOutLink.click();
        wait.until(ExpectedConditions.urlContains("signonForm"));
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"), "Should show invalid login message");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("footer a[href*='twitter.com']")));
        twitterLink.click();
        
        // Switch to new tab and verify domain
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testCartFunctionality() {
        // Login first
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Navigate to an item
        driver.get(BASE_URL + "catalog/items/FI-FW-01");
        
        // Add to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#Catalog table tr:nth-child(3) a")));
        addToCartButton.click();
        
        // Verify cart
        wait.until(ExpectedConditions.urlContains("viewCart"));
        WebElement cartTable = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Cart table")));
        Assertions.assertTrue(cartTable.getText().contains("Sub Total"), "Cart should show items");
        
        // Remove from cart
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#Cart a[href*='removeItemFromCart']")));
        removeButton.click();
        
        WebElement emptyCartMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Cart")));
        Assertions.assertTrue(emptyCartMessage.getText().contains("empty"), "Cart should be empty after removal");
    }
}