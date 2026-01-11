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
public class JPetStore{
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
        
        // Test Home link - try different locators
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[@href='/catalog/'] | //a[contains(text(),'Home')] | //a[@title='Home']")));
        homeLink.click();
        assertEquals("https://jpetstore.aspectran.com/catalog/", driver.getCurrentUrl());
        
        // Test Sign In link
        driver.get("https://jpetstore.aspectran.com/");
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        assertTrue(driver.getCurrentUrl().contains("signin"));
        
        // Navigate back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
        
        // Test Register link
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
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
        
        // Verify categories section exists - use different locator
        WebElement categoriesSection = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#MainImageContent, .MainImage, nav, div[id*='category'], div[class*='category']")));
        assertTrue(categoriesSection.isDisplayed());
        
        // Get all category links
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector("area[href*='category'], a[href*='category']"));
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
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        
        // Verify product listing exists
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Catalog")));
        
        // Wait for table to load
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table")));
        assertTrue(table.isDisplayed());
        
        // Find product rows in table
        List<WebElement> products = driver.findElements(By.cssSelector("table tr a[href*='product']"));
        assertTrue(products.size() > 0);
    }

    @Test
    @Order(5)
    public void testShoppingCart() {
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        
        // Add first product to cart
        List<WebElement> products = driver.findElements(By.cssSelector("table tr a[href*='product']"));
        if (!products.isEmpty()) {
            products.get(0).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Catalog")));
            
            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='addItemToCart']")));
            addToCartButton.click();
            
            // Verify cart updated
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".messages")));
        }
    }

    @Test
    @Order(6)
    public void testSignInFunctionality() {
        driver.get("https://jpetstore.aspectran.com/account/signonForm");
        
        // Verify form exists
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("j2ee");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("j2ee");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']"));
        submitButton.click();
        
        // Wait for redirect to home page
        wait.until(ExpectedConditions.urlContains("catalog"));
        assertTrue(driver.getCurrentUrl().contains("catalog"));
        
        // Verify logged in
        WebElement welcomeMessage = driver.findElement(By.cssSelector(".messages, #MenuContent"));
        assertTrue(welcomeMessage.isDisplayed());
    }

    @Test
    @Order(7)
    public void testInvalidSignIn() {
        driver.get("https://jpetstore.aspectran.com/account/signonForm");
        
        // Fill with invalid credentials
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invaliduser");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("invalidpass");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']"));
        submitButton.click();
        
        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("ul[class='messages'] li")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().toLowerCase().contains("invalid") || 
                   errorMessage.getText().toLowerCase().contains("failed"));
    }

    @Test
    @Order(8)
    public void testRegisterNewUser() {
        driver.get("https://jpetstore.aspectran.com/account/newAccountForm");
        
        // Verify form exists
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("testuser" + System.currentTimeMillis());
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("password123");
        WebElement repeatPasswordField = driver.findElement(By.name("repeatedPassword"));
        repeatPasswordField.sendKeys("password123");
        WebElement firstNameField = driver.findElement(By.name("firstName"));
        firstNameField.sendKeys("Test");
        WebElement lastNameField = driver.findElement(By.name("lastName"));
        lastNameField.sendKeys("User");
        WebElement emailField = driver.findElement(By.name("email"));
        emailField.sendKeys("testuser@example.com");
        WebElement phoneField = driver.findElement(By.name("phone"));
        phoneField.sendKeys("1234567890");
        WebElement address1Field = driver.findElement(By.name("address1"));
        address1Field.sendKeys("123 Test Street");
        WebElement cityField = driver.findElement(By.name("city"));
        cityField.sendKeys("Test City");
        WebElement stateField = driver.findElement(By.name("state"));
        stateField.sendKeys("TS");
        WebElement zipField = driver.findElement(By.name("zip"));
        zipField.sendKeys("12345");
        WebElement countryField = driver.findElement(By.name("country"));
        countryField.sendKeys("USA");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']"));
        submitButton.click();
        
        // Wait for confirmation
        wait.until(ExpectedConditions.urlContains("signonForm"));
        assertTrue(driver.getCurrentUrl().contains("signonForm"));
    }

    @Test
    @Order(9)
    public void testSearchFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Verify search bar exists
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[name*='search'], input[type='search'], #search")));
        assertTrue(searchInput.isDisplayed());
        
        // Search for a product
        searchInput.clear();
        searchInput.sendKeys("fish");
        searchInput.submit();
        
        // Verify search results page loaded
        wait.until(ExpectedConditions.titleContains("Search"));
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
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        
        // Click on a product to view details
        List<WebElement> products = driver.findElements(By.cssSelector("table tr a[href*='product']"));
        if (!products.isEmpty()) {
            // Click first product
            products.get(0).click();
            
            // Wait for product details page
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Catalog")));
            assertTrue(driver.getCurrentUrl().contains("product"));
            
            // Verify product details are displayed
            WebElement productDetails = driver.findElement(By.id("Catalog"));
            assertTrue(productDetails.isDisplayed());
            
            // Verify product name exists
            WebElement productName = driver.findElement(By.cssSelector("h2, h3"));
            assertTrue(productName.isDisplayed());
        }
    }

    @Test
    @Order(12)
    public void testCheckoutProcess() {
        driver.get("https://jpetstore.aspectran.com/catalog/categories/FISH");
        
        // Add a product to cart
        List<WebElement> products = driver.findElements(By.cssSelector("table tr a[href*='product']"));
        if (!products.isEmpty()) {
            products.get(0).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Catalog")));
            
            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='addItemToCart']")));
            addToCartButton.click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Catalog")));
            
            // Navigate to cart
            WebElement cartLink = driver.findElement(By.linkText("Cart"));
            cartLink.click();
            
            // Wait for cart page
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Catalog")));
            assertTrue(driver.getCurrentUrl().contains("cart"));
            
            if (driver.findElements(By.cssSelector("a[href*='checkout']")).size() > 0) {
                WebElement checkoutButton = driver.findElement(By.cssSelector("a[href*='checkout']"));
                checkoutButton.click();
                
                // Wait for checkout page
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Catalog")));
                assertTrue(driver.getCurrentUrl().contains("checkout"));
            }
        }
    }
}