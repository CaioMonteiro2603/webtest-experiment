package Qwen3.ws08.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("JPetStore", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(2)
    public void testNavigationToCategories() {
        driver.get("https://jpetstore.aspectran.com/");
        WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        categoryLink.click();
        assertTrue(driver.getCurrentUrl().contains("/catalog/category"));
        assertEquals("JPetStore - Fish", driver.getTitle());
    }

    @Test
    @Order(3)
    public void testProductListing() {
        driver.get("https://jpetstore.aspectran.com/catalog/category?categoryId=FISH");
        List<WebElement> productItems = driver.findElements(By.cssSelector(".product-item"));
        assertTrue(productItems.size() > 0);
        
        for (WebElement item : productItems) {
            assertTrue(item.isDisplayed());
            WebElement productName = item.findElement(By.cssSelector(".product-name"));
            WebElement productPrice = item.findElement(By.cssSelector(".product-price"));
            assertTrue(productName.isDisplayed());
            assertTrue(productPrice.isDisplayed());
        }
    }

    @Test
    @Order(4)
    public void testProductSorting() {
        driver.get("https://jpetstore.aspectran.com/catalog/category?categoryId=FISH");
        
        WebElement sortDropdown = driver.findElement(By.id("sortOptions"));
        Select select = new Select(sortDropdown);
        
        select.selectByValue("name");
        List<WebElement> productNames = driver.findElements(By.cssSelector(".product-name"));
        assertTrue(productNames.size() > 0);
        
        select.selectByValue("price");
        List<WebElement> productPrices = driver.findElements(By.cssSelector(".product-price"));
        assertTrue(productPrices.size() > 0);
    }

    @Test
    @Order(5)
    public void testAddToCart() {
        driver.get("https://jpetstore.aspectran.com/catalog/category?categoryId=FISH");
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product-item:first-child .product-name")));
        firstProduct.click();
        
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart")));
        addToCartButton.click();
        
        WebElement cartBadge = driver.findElement(By.id("cart-badge"));
        assertEquals("1", cartBadge.getText());
    }

    @Test
    @Order(6)
    public void testShoppingCart() {
        driver.get("https://jpetstore.aspectran.com/cart");
        WebElement cartItems = driver.findElement(By.id("cart-items"));
        assertTrue(cartItems.isDisplayed());
        
        WebElement cartTotal = driver.findElement(By.id("cart-total"));
        assertTrue(cartTotal.isDisplayed());
    }

    @Test
    @Order(7)
    public void testCheckoutProcess() {
        driver.get("https://jpetstore.aspectran.com/cart");
        WebElement checkoutButton = driver.findElement(By.id("checkout-button"));
        checkoutButton.click();
        
        WebElement customerForm = driver.findElement(By.id("customer-form"));
        assertTrue(customerForm.isDisplayed());
    }

    @Test
    @Order(8)
    public void testLoginFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        WebElement loginLink = driver.findElement(By.linkText("Sign In"));
        loginLink.click();
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("testuser");
        passwordField.sendKeys("testpass");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(9)
    public void testFooterLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertTrue(footerLinks.size() >= 3);
        
        String originalWindow = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                link.click();
                
                Set<String> windowHandles = driver.getWindowHandles();
                String newWindow = windowHandles.stream()
                        .filter(w -> !w.equals(originalWindow))
                        .findFirst()
                        .orElse(null);
                
                if (newWindow != null) {
                    driver.switchTo().window(newWindow);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("aspectran.com") || 
                               currentUrl.contains("github.com") || 
                               currentUrl.contains("linkedin.com") ||
                               currentUrl.contains("twitter.com"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(10)
    public void testNavigationToProduct() {
        driver.get("https://jpetstore.aspectran.com/catalog/category?categoryId=FISH");
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product-item:first-child .product-name")));
        String productName = firstProduct.getText();
        firstProduct.click();
        
        WebElement productDetail = driver.findElement(By.id("product-detail"));
        assertTrue(productDetail.isDisplayed());
        assertTrue(driver.getTitle().contains(productName));
    }
}