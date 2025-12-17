package deepseek.ws08.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
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
    public void testHomePageLoading() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".navbar-brand")));
        Assertions.assertTrue(header.getText().contains("JPetStore"),
            "JPetStore header should be displayed");
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        
        // Navigate to Dogs category
        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Dogs")));
        dogsLink.click();
        
        WebElement dogsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".page-header")));
        Assertions.assertTrue(dogsHeader.getText().contains("Dogs"), 
            "Dogs category page should load");
    }

    @Test
    @Order(3)
    public void testProductDetails() {
        driver.get(BASE_URL);
        
        // Navigate to Dogs category
        driver.findElement(By.linkText("Dogs")).click();
        
        // Click on first product
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product-item a")));
        firstProduct.click();
        
        WebElement productDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".product-details")));
        Assertions.assertTrue(productDetails.isDisplayed(), 
            "Product details page should load");
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get(BASE_URL);
        
        // Navigate to Dogs category
        driver.findElement(By.linkText("Dogs")).click();
        
        // Click on first product
        driver.findElement(By.cssSelector(".product-item a")).click();
        
        // Add to cart
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-add-to-cart")));
        addToCartBtn.click();
        
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".badge")));
        Assertions.assertTrue(cartBadge.getText().contains("1"), 
            "Cart badge should show 1 item added");
    }

    @Test
    @Order(5)
    public void testViewCart() {
        driver.get(BASE_URL);
        
        // Navigate to Dogs category and add to cart
        driver.findElement(By.linkText("Dogs")).click();
        driver.findElement(By.cssSelector(".product-item a")).click();
        driver.findElement(By.cssSelector(".btn-add-to-cart")).click();
        
        // View cart
        WebElement viewCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-view-cart")));
        viewCartBtn.click();
        
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".cart-table")));
        Assertions.assertTrue(cartTable.isDisplayed(), 
            "Cart page should display items added");
    }

    @Test
    @Order(6)
    public void testRemoveFromCart() {
        driver.get(BASE_URL);
        
        // Navigate to Dogs category and add to cart
        driver.findElement(By.linkText("Dogs")).click();
        driver.findElement(By.cssSelector(".product-item a")).click();
        driver.findElement(By.cssSelector(".btn-add-to-cart")).click();
        
        // View cart and remove item
        driver.findElement(By.cssSelector(".btn-view-cart")).click();
        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-remove")));
        removeBtn.click();
        
        WebElement emptyCart = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".empty-cart")));
        Assertions.assertTrue(emptyCart.isDisplayed(), 
            "Cart should be empty after removing item");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github']")));
        githubLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"),
            "GitHub link should open in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}