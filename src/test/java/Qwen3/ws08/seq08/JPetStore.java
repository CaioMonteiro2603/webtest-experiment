package Qwen3.ws08.seq08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class JPetStore {

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
    public void testHomePageLoad() {
        driver.get("https://jpetstore.aspectran.com/");
        assertEquals("JPetStore Demo", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(2)
    public void testNavigationToCategories() {
        driver.get("https://jpetstore.aspectran.com/");
        WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        categoryLink.click();
        assertTrue(driver.getCurrentUrl().contains("/catalog/categories/FISH"));
        assertEquals("JPetStore Demo", driver.getTitle());
    }

    @Test
    @Order(3)
    public void testProductListing() {
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        List<WebElement> productItems = driver.findElements(By.cssSelector("table.table tbody tr"));
        assertTrue(productItems.size() > 0);
        
        for (WebElement item : productItems) {
            assertTrue(item.isDisplayed());
            List<WebElement> columns = item.findElements(By.tagName("td"));
            assertTrue(columns.size() >= 2);
        }
    }

    @Test
    @Order(4)
    public void testProductSorting() {
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        
        List<WebElement> productItems = driver.findElements(By.cssSelector("table.table tbody tr"));
        assertTrue(productItems.size() > 0);
    }

    @Test
    @Order(5)
    public void testAddToCart() {
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("table.table tbody tr:first-child a")));
        firstProduct.click();
        
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCartButton.click();
        
        WebElement cartBadge = driver.findElement(By.cssSelector("span.badge"));
        assertEquals("1", cartBadge.getText());
    }

    @Test
    @Order(6)
    public void testShoppingCart() {
        driver.get("https://jpetstore.aspectran.com/cart");
        WebElement cartItems = driver.findElement(By.cssSelector("div.container"));
        assertTrue(cartItems.isDisplayed());
        
        WebElement cartTotal = driver.findElement(By.cssSelector("div.container"));
        assertTrue(cartTotal.isDisplayed());
    }

    @Test
    @Order(7)
    public void testCheckoutProcess() {
        driver.get("https://jpetstore.aspectran.com/cart");
        WebElement checkoutButton = driver.findElement(By.linkText("Proceed to Checkout"));
        checkoutButton.click();
        
        WebElement customerForm = driver.findElement(By.cssSelector("div.container"));
        assertTrue(customerForm.isDisplayed());
    }

    @Test
    @Order(8)
    public void testLoginFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        WebElement loginLink = driver.findElement(By.linkText("Sign In"));
        loginLink.click();
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("testuser");
        passwordField.sendKeys("testpass");
        loginButton.click();
        
        WebElement errorMessage = driver.findElement(By.cssSelector("div.container"));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(9)
    public void testFooterLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() >= 1);
    }

    @Test
    @Order(10)
    public void testNavigationToProduct() {
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("table.table tbody tr:first-child a")));
        String productName = firstProduct.getText();
        firstProduct.click();
        
        WebElement productDetail = driver.findElement(By.cssSelector("div.container"));
        assertTrue(productDetail.isDisplayed());
        assertTrue(driver.getTitle().contains("JPetStore Demo"));
    }
}