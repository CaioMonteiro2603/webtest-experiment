package Qwen3.ws08.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomepageLoad() {
        driver.get("https://jpetstore.aspectran.com/");
        
        String title = driver.getTitle();
        assertTrue(title.contains("JPetStore"), "Page title should contain 'JPetStore'");
        
        // Verify main elements are present
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".logo")));
        assertTrue(logo.isDisplayed(), "Logo should be displayed");
        
        WebElement searchBox = driver.findElement(By.id("searchBox"));
        assertTrue(searchBox.isDisplayed(), "Search box should be displayed");
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test main category navigation
        List<WebElement> categories = driver.findElements(By.cssSelector("#navigation li a"));
        assertTrue(categories.size() > 0, "Categories should be present");
        
        // Click on first category (assuming it's Fish)
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishCategory.click();
        
        // Verify navigation to category page
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("category"), "Should navigate to category page");
        
        // Go back to homepage
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(3)
    public void testProductListingAndSorting() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Navigate to Fish category
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishCategory.click();
        
        // Verify we're in the correct category
        WebElement categoryName = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".category-name")));
        assertTrue(categoryName.getText().contains("Fish"), "Should be viewing Fish category");
        
        // Test product sorting
        WebElement sortDropdown = driver.findElement(By.id("sortSelect"));
        if (sortDropdown != null) {
            Select sortSelect = new Select(sortDropdown);
            sortSelect.selectByVisibleText("Name (A to Z)");
            
            // Verify sorting worked by checking first item
            WebElement firstItem = driver.findElement(By.cssSelector(".product-item:first-child .product-name"));
            String firstProductName = firstItem.getText();
            assertTrue(firstProductName.length() > 0, "First product name should be displayed");
        }
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Navigate to Fish category
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishCategory.click();
        
        // Get first product and add to cart
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product-item:first-child .product-link")));
        firstProduct.click();
        
        // Wait for product detail page
        wait.until(ExpectedConditions.urlContains("product"));
        
        // Add to cart
        WebElement addToCartButton = driver.findElement(By.id("addToCartButton"));
        addToCartButton.click();
        
        // Verify item added to cart by checking cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".cart-badge")));
        assertTrue(cartBadge.isDisplayed(), "Cart badge should be displayed after adding item");
    }

    @Test
    @Order(5)
    public void testShoppingCart() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Navigate to Fish category and add product to cart
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishCategory.click();
        
        // Add first product to cart
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product-item:first-child .product-link")));
        firstProduct.click();
        WebElement addToCartButton = driver.findElement(By.id("addToCartButton"));
        addToCartButton.click();
        
        // Go to shopping cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Cart")));
        cartLink.click();
        
        // Verify cart page
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("cart"), "Should navigate to cart page");
        
        // Confirm cart is not empty
        WebElement cartItems = driver.findElement(By.cssSelector(".cart-items"));
        assertTrue(cartItems.isDisplayed(), "Cart items should be displayed");
    }

    @Test
    @Order(6)
    public void testCheckoutProcess() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Navigate to Fish category
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishCategory.click();
        
        // Add product to cart
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product-item:first-child .product-link")));
        firstProduct.click();
        WebElement addToCartButton = driver.findElement(By.id("addToCartButton"));
        addToCartButton.click();
        
        // Proceed to checkout
        WebElement checkoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Checkout")));
        checkoutLink.click();
        
        // Verify we're on checkout page
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("checkout"), "Should navigate to checkout page");
        
        // Fill in basic checkout information
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement addressField = driver.findElement(By.id("address"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        addressField.sendKeys("123 Main St");
        
        // Submit order
        WebElement submitButton = driver.findElement(By.id("submitOrder"));
        submitButton.click();
        
        // Verify order confirmation
        WebElement confirmationMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".confirmation-message")));
        assertTrue(confirmationMessage.isDisplayed(), "Confirmation message should appear after checkout");
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        
        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }
        
        // Test external footer links if they exist
        try {
            WebElement aboutLink = driver.findElement(By.linkText("About"));
            aboutLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            // Should remain within site structure or navigate appropriately
            assertTrue(currentUrl.contains("jpetstore") || currentUrl.contains("about"), 
                      "Should navigate to about page or related content");
            
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if About link not found
        }
        
        try {
            WebElement helpLink = driver.findElement(By.linkText("Help"));
            helpLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("jpetstore") || currentUrl.contains("help"), 
                      "Should navigate to help page or related content");
            
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if Help link not found
        }
        
        try {
            WebElement termsLink = driver.findElement(By.linkText("Terms"));
            termsLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("jpetstore") || currentUrl.contains("terms"), 
                      "Should navigate to terms page or related content");
            
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if Terms link not found
        }
    }

    @Test
    @Order(8)
    public void testResponsiveDesign() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check mobile menu functionality
        WebElement mobileMenuButton = driver.findElement(By.cssSelector(".mobile-menu-button"));
        assertTrue(mobileMenuButton.isDisplayed(), "Mobile menu button should be displayed");
        
        // Check that core sections are present
        WebElement header = driver.findElement(By.tagName("header"));
        WebElement footer = driver.findElement(By.tagName("footer"));
        WebElement mainContent = driver.findElement(By.cssSelector("main"));
        
        assertTrue(header.isDisplayed(), "Header should be displayed");
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
        assertTrue(mainContent.isDisplayed(), "Main content should be displayed");
    }
}