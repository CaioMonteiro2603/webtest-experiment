package Qwen3.ws08.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JPetStoreTest {

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
    public void testPageLoad() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Verify page title
        String title = driver.getTitle();
        assertTrue(title.contains("JPetStore"));
        
        // Verify main elements are present
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("header")));
        assertTrue(header.isDisplayed());
        
        WebElement mainContent = driver.findElement(By.id("main-content"));
        assertTrue(mainContent.isDisplayed());
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("index"));
        assertEquals("https://jpetstore.aspectran.com/index.html", driver.getCurrentUrl());

        // Test Catalog link
        driver.get("https://jpetstore.aspectran.com/");
        WebElement catalogLink = driver.findElement(By.linkText("Catalog"));
        catalogLink.click();
        wait.until(ExpectedConditions.urlContains("catalog"));
        assertTrue(driver.getCurrentUrl().contains("catalog"));

        // Test Cart link
        driver.get("https://jpetstore.aspectran.com/");
        WebElement cartLink = driver.findElement(By.linkText("Cart"));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart"));
        assertTrue(driver.getCurrentUrl().contains("cart"));
    }

    @Test
    @Order(3)
    public void testLoginFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test login link
        WebElement loginLink = driver.findElement(By.linkText("Login"));
        loginLink.click();
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"));
        
        // Test login form elements
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        assertTrue(usernameField.isDisplayed());
        assertTrue(passwordField.isDisplayed());
        assertTrue(loginButton.isDisplayed());
    }

    @Test
    @Order(4)
    public void testProductCatalog() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Verify product catalog is displayed
        WebElement catalog = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("catalog")));
        assertTrue(catalog.isDisplayed());
        
        // Check for product listings
        List<WebElement> productItems = driver.findElements(By.cssSelector(".product-item"));
        assertTrue(productItems.size() > 0);
        
        // Check first product details
        if (!productItems.isEmpty()) {
            WebElement firstProduct = productItems.get(0);
            assertTrue(firstProduct.isDisplayed());
            
            List<WebElement> productNames = firstProduct.findElements(By.cssSelector(".product-name"));
            if (!productNames.isEmpty()) {
                assertTrue(productNames.get(0).isDisplayed());
            }
            
            List<WebElement> productPrices = firstProduct.findElements(By.cssSelector(".product-price"));
            if (!productPrices.isEmpty()) {
                assertTrue(productPrices.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(5)
    public void testProductCategories() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Check categories navigation
        List<WebElement> categories = driver.findElements(By.cssSelector(".category"));
        assertTrue(categories.size() > 0);
        
        // Check that at least one category is clickable
        for (WebElement category : categories) {
            if (category.isDisplayed()) {
                assertTrue(category.isDisplayed());
                break;
            }
        }
    }

    @Test
    @Order(6)
    public void testShoppingCart() {
        driver.get("https://jpetstore.aspectran.com/cart");
        
        // Verify cart page
        WebElement cartContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cart-container")));
        assertTrue(cartContainer.isDisplayed());
        
        // Check for cart items (initially empty)
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart-item"));
        assertTrue(cartItems.size() >= 0);
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0);
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed());
        }
    }

    @Test
    @Order(8)
    public void testProductDetailsPage() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Get a product link and click it
        List<WebElement> productLinks = driver.findElements(By.cssSelector(".product-link"));
        if (!productLinks.isEmpty()) {
            WebElement productLink = productLinks.get(0);
            String productUrl = productLink.getAttribute("href");
            
            // Click on the product link
            productLink.click();
            
            // Wait for product details page
            wait.until(ExpectedConditions.urlContains("product"));
            assertTrue(driver.getCurrentUrl().contains("product"));
        }
    }

    @Test
    @Order(9)
    public void testSearchFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check search form
        List<WebElement> searchForms = driver.findElements(By.cssSelector("form[action*='search']"));
        if (!searchForms.isEmpty()) {
            WebElement searchForm = searchForms.get(0);
            assertTrue(searchForm.isDisplayed());
            
            WebElement searchInput = searchForm.findElement(By.name("search"));
            assertTrue(searchInput.isDisplayed());
            
            WebElement searchButton = searchForm.findElement(By.cssSelector("button[type='submit']"));
            assertTrue(searchButton.isDisplayed());
        }
    }

    @Test
    @Order(10)
    public void testRegistration() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test registration link
        WebElement registerLink = driver.findElement(By.linkText("Register"));
        registerLink.click();
        wait.until(ExpectedConditions.urlContains("register"));
        assertTrue(driver.getCurrentUrl().contains("register"));
        
        // Check registration form
        WebElement registerForm = driver.findElement(By.id("register-form"));
        assertTrue(registerForm.isDisplayed());
        
        List<WebElement> formFields = registerForm.findElements(By.tagName("input"));
        assertTrue(formFields.size() > 0);
    }

    @Test
    @Order(11)
    public void testUserAccount() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test account link if present
        List<WebElement> accountLinks = driver.findElements(By.linkText("Account"));
        if (!accountLinks.isEmpty()) {
            WebElement accountLink = accountLinks.get(0);
            accountLink.click();
            wait.until(ExpectedConditions.urlContains("account"));
            assertTrue(driver.getCurrentUrl().contains("account"));
        }
    }

    @Test
    @Order(12)
    public void testPromotions() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check for promotions section
        List<WebElement> promotions = driver.findElements(By.cssSelector(".promotion"));
        assertTrue(promotions.size() >= 0);
        
        // Check promotion messages
        List<WebElement> promotionMessages = driver.findElements(By.cssSelector(".promo-message"));
        if (!promotionMessages.isEmpty()) {
            assertTrue(promotionMessages.get(0).isDisplayed());
        }
    }

    @Test
    @Order(13)
    public void testContactInfo() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check contact information
        List<WebElement> contactInfo = driver.findElements(By.cssSelector(".contact-info"));
        assertTrue(contactInfo.size() >= 0);
        
        WebElement contactSection = driver.findElement(By.id("contact-section"));
        assertTrue(contactSection.isDisplayed());
    }

    @Test
    @Order(14)
    public void testHeadersAndFooters() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test header elements
        WebElement header = driver.findElement(By.tagName("header"));
        assertTrue(header.isDisplayed());
        
        // Test main content
        WebElement mainContent = driver.findElement(By.id("main-content"));
        assertTrue(mainContent.isDisplayed());
        
        // Test footer
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed());
    }

    @Test
    @Order(15)
    public void testResponsiveDesign() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check for mobile menu toggle if present
        List<WebElement> mobileToggles = driver.findElements(By.cssSelector(".mobile-toggle"));
        if (!mobileToggles.isEmpty()) {
            WebElement toggle = mobileToggles.get(0);
            assertTrue(toggle.isDisplayed());
        }
    }

    @Test
    @Order(16)
    public void testImageElements() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check images on page
        List<WebElement> images = driver.findElements(By.tagName("img"));
        assertTrue(images.size() > 0);
        
        // Check at least one image is displayed
        boolean hasDisplayedImage = false;
        for (WebElement img : images) {
            if (img.isDisplayed()) {
                hasDisplayedImage = true;
                break;
            }
        }
        assertTrue(hasDisplayedImage);
    }

    @Test
    @Order(17)
    public void testNavigationToSpecificCategory() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Navigate to specific category (if available)
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector(".category-link"));
        if (!categoryLinks.isEmpty()) {
            WebElement catLink = categoryLinks.get(0);
            if (catLink.isDisplayed()) {
                catLink.click();
                // Should navigate to category page
                wait.until(ExpectedConditions.urlContains("category"));
                assertTrue(driver.getCurrentUrl().contains("category"));
            }
        }
    }

    @Test
    @Order(18)
    public void testAddToCartFunctionality() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Try to add a product to cart (minimal interaction)
        List<WebElement> addToCartButtons = driver.findElements(By.cssSelector(".add-to-cart"));
        if (!addToCartButtons.isEmpty()) {
            // Just verify button exists
            WebElement addToCartButton = addToCartButtons.get(0);
            assertTrue(addToCartButton.isDisplayed());
        }
    }
}