package Qwen3.ws08.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class JPetStoreTest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        
        wait.until(ExpectedConditions.titleContains("JPetStore"));
        assertTrue(driver.getTitle().contains("JPetStore"));
        assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        assertEquals("https://jpetstore.aspectran.com/", driver.getCurrentUrl());
        
        // Test Sign In link
        driver.get("https://jpetstore.aspectran.com/");
        WebElement signInLink = driver.findElement(By.linkText("Sign In"));
        signInLink.click();
        assertTrue(driver.getCurrentUrl().contains("sign-in"));
        
        // Navigate back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
        
        // Test Register link
        WebElement registerLink = driver.findElement(By.linkText("Register"));
        registerLink.click();
        assertTrue(driver.getCurrentUrl().contains("register"));
        
        // Navigate back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(3)
    public void testProductCategories() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Verify categories section exists
        WebElement categoriesSection = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("categories")));
        assertTrue(categoriesSection.isDisplayed());
        
        // Get all category links
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector(".category-link"));
        assertTrue(categoryLinks.size() > 0);
        
        // Click first category link
        if (!categoryLinks.isEmpty()) {
            categoryLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("category"));
            assertTrue(driver.getCurrentUrl().contains("category"));
        }
    }

    @Test
    @Order(4)
    public void testProductListing() {
        driver.get("https://jpetstore.aspectran.com/category/FISH");
        
        // Verify product listing exists
        WebElement productList = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("product-list")));
        assertTrue(productList.isDisplayed());
        
        // Verify products are displayed
        List<WebElement> products = driver.findElements(By.cssSelector(".product-item"));
        assertTrue(products.size() > 0);
        
        // Verify first product details
        if (!products.isEmpty()) {
            WebElement firstProduct = products.get(0);
            assertTrue(firstProduct.isDisplayed());
            
            // Check product name
            WebElement productName = firstProduct.findElement(By.cssSelector(".product-name"));
            assertTrue(productName.isDisplayed());
            assertTrue(productName.getText().length() > 0);
            
            // Check product price
            WebElement productPrice = firstProduct.findElement(By.cssSelector(".product-price"));
            assertTrue(productPrice.isDisplayed());
        }
    }

    @Test
    @Order(5)
    public void testShoppingCart() {
        driver.get("https://jpetstore.aspectran.com/category/FISH");
        
        // Add first product to cart
        List<WebElement> products = driver.findElements(By.cssSelector(".product-item"));
        if (!products.isEmpty()) {
            WebElement addToCartButton = products.get(0).findElement(By.cssSelector(".add-to-cart"));
            addToCartButton.click();
            
            // Verify cart updated
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cart-count")));
            WebElement cartCount = driver.findElement(By.id("cart-count"));
            assertTrue(cartCount.isDisplayed());
        }
    }

    @Test
    @Order(6)
    public void testSignInFunctionality() {
        driver.get("https://jpetstore.aspectran.com/sign-in");
        
        // Verify sign in form exists
        WebElement signInForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sign-in-form")));
        assertTrue(signInForm.isDisplayed());
        
        // Fill sign in form
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        usernameField.sendKeys("j2ee");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("j2ee");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for redirect to home page
        wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
        assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
        
        // Verify logged in
        WebElement welcomeMessage = driver.findElement(By.cssSelector(".welcome-message"));
        assertTrue(welcomeMessage.isDisplayed());
    }

    @Test
    @Order(7)
    public void testInvalidSignIn() {
        driver.get("https://jpetstore.aspectran.com/sign-in");
        
        // Fill with invalid credentials
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        usernameField.sendKeys("invaliduser");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalidpass");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("Invalid"));
    }

    @Test
    @Order(8)
    public void testRegisterNewUser() {
        driver.get("https://jpetstore.aspectran.com/register");
        
        // Verify registration form exists
        WebElement registerForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("registration-form")));
        assertTrue(registerForm.isDisplayed());
        
        // Fill registration form
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        usernameField.sendKeys("testuser" + System.currentTimeMillis());
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("password123");
        WebElement repeatPasswordField = driver.findElement(By.id("repeat-password"));
        repeatPasswordField.sendKeys("password123");
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        firstNameField.sendKeys("Test");
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("User");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("testuser@example.com");
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("1234567890");
        WebElement address1Field = driver.findElement(By.id("address1"));
        address1Field.sendKeys("123 Test Street");
        WebElement cityField = driver.findElement(By.id("city"));
        cityField.sendKeys("Test City");
        WebElement stateField = driver.findElement(By.id("state"));
        stateField.sendKeys("TS");
        WebElement zipField = driver.findElement(By.id("zip"));
        zipField.sendKeys("12345");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for confirmation
        wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
        assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(9)
    public void testSearchFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Verify search bar exists
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("search-input")));
        assertTrue(searchInput.isDisplayed());
        
        // Search for a product
        searchInput.sendKeys("fish");
        WebElement searchButton = driver.findElement(By.id("search-button"));
        searchButton.click();
        
        // Verify search results page loaded
        wait.until(ExpectedConditions.urlContains("search"));
        assertTrue(driver.getCurrentUrl().contains("search"));
        
        // Verify results displayed
        WebElement searchResults = driver.findElement(By.id("search-results"));
        assertTrue(searchResults.isDisplayed());
    }

    @Test
    @Order(10)
    public void testFooterLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test Footer Links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0);
        
        // Test a few footer links by checking if they are clickable and accessible
        for (WebElement link : footerLinks) {
            if (link.isDisplayed() && !link.getAttribute("href").isEmpty()) {
                // Just verify the link exists and is enabled
                assertTrue(link.isEnabled());
            }
        }
    }

    @Test
    @Order(11)
    public void testProductDetails() {
        driver.get("https://jpetstore.aspectran.com/category/FISH");
        
        // Click on a product to view details
        List<WebElement> products = driver.findElements(By.cssSelector(".product-item"));
        if (!products.isEmpty()) {
            // Click first product
            WebElement productLink = products.get(0).findElement(By.cssSelector(".product-link"));
            productLink.click();
            
            // Wait for product details page
            wait.until(ExpectedConditions.urlContains("product"));
            assertTrue(driver.getCurrentUrl().contains("product"));
            
            // Verify product details are displayed
            WebElement productDetails = driver.findElement(By.id("product-details"));
            assertTrue(productDetails.isDisplayed());
            
            // Verify product name exists
            WebElement productName = driver.findElement(By.cssSelector(".product-name"));
            assertTrue(productName.isDisplayed());
        }
    }

    @Test
    @Order(12)
    public void testCheckoutProcess() {
        driver.get("https://jpetstore.aspectran.com/category/FISH");
        
        // Add a product to cart
        List<WebElement> products = driver.findElements(By.cssSelector(".product-item"));
        if (!products.isEmpty()) {
            WebElement addToCartButton = products.get(0).findElement(By.cssSelector(".add-to-cart"));
            addToCartButton.click();
            
            // Navigate to cart
            WebElement cartLink = driver.findElement(By.linkText("Cart"));
            cartLink.click();
            
            // Wait for cart page
            wait.until(ExpectedConditions.urlContains("cart"));
            assertTrue(driver.getCurrentUrl().contains("cart"));
            
            // Proceed to checkout
            WebElement checkoutButton = driver.findElement(By.cssSelector("button.checkout-button"));
            checkoutButton.click();
            
            // Wait for checkout page
            wait.until(ExpectedConditions.urlContains("checkout"));
            assertTrue(driver.getCurrentUrl().contains("checkout"));
        }
    }
}