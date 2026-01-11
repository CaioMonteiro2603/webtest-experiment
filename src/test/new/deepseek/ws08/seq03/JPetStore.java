package deepseek.ws08.seq03;

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
public class JPetStore {
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("header")));
        Assertions.assertTrue(banner.isDisplayed());
        
        // Test category navigation
        driver.findElement(By.cssSelector("a[href='/categories/DOGS']")).click();
        wait.until(ExpectedConditions.urlContains("/categories/DOGS"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Dogs"));
    }

    @Test
    @Order(2)
    public void testProductSelection() {
        driver.get(BASE_URL + "categories/DOGS");
        
        // Select first product
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product-item a")));
        productLink.click();
        
        wait.until(ExpectedConditions.urlContains("/products/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Dog"));
    }

    @Test
    @Order(3)
    public void testItemDetails() {
        driver.get(BASE_URL + "products/AD-01");
        
        // View first item
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".item-item a")));
        itemLink.click();
        
        wait.until(ExpectedConditions.urlContains("/items/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Adult"));
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get(BASE_URL + "items/EST-6");
        
        // Add to cart
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-primary")));
        addButton.click();
        
        wait.until(ExpectedConditions.urlContains("/cart"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Shopping Cart"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".cart-item")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testLogin() {
        driver.get(BASE_URL + "account/signon");
        
        // Valid login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")))
            .sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        
        wait.until(ExpectedConditions.urlContains("/account"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".welcome")).getText().contains("Welcome"));
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signon");
        
        // Invalid login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")))
            .sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("wrong");
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        
        // Search for dogs
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.name("keyword")));
        searchBox.sendKeys("dog");
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        
        wait.until(ExpectedConditions.urlContains("/search"));
        List<WebElement> results = driver.findElements(By.cssSelector(".product-item a"));
        Assertions.assertTrue(results.size() > 0);
    }

    @Test
    @Order(8)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test About Us link
        driver.findElement(By.linkText("About")).click();
        wait.until(ExpectedConditions.urlContains("/about"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("About"));
        
        // Test Contact link (external)
        String originalWindow = driver.getWindowHandle();
        driver.findElement(By.linkText("Contact")).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}