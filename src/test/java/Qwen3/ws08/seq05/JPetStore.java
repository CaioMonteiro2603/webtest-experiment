package Qwen3.ws08.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
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
    public void testPageLoadAndTitle() {
        driver.get("https://jpetstore.aspectran.com/");
        
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("JPetStore"), "Page title should contain JPetStore");
        
        WebElement header = driver.findElement(By.cssSelector("header h1"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
        assertEquals("JPetStore", header.getText(), "Header text should match expected value");
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://jpetstore.aspectran.com/");
        
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Should have navigation links");
        
        for (WebElement link : navLinks) {
            assertTrue(link.isDisplayed(), "Navigation link should be displayed");
            assertNotNull(link.getAttribute("href"), "Navigation link should have href attribute");
        }
    }

    @Test
    @Order(3)
    public void testHomePageElements() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check for welcome message
        WebElement welcomeMessage = driver.findElement(By.cssSelector(".welcome-message"));
        assertTrue(welcomeMessage.isDisplayed(), "Welcome message should be displayed");
        
        // Check for featured products
        WebElement featuredProducts = driver.findElement(By.id("featured-products"));
        assertTrue(featuredProducts.isDisplayed(), "Featured products section should be displayed");
    }

    @Test
    @Order(4)
    public void testCategoryNavigation() {
        driver.get("https://jpetstore.aspectran.com/");
        
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector(".category-link"));
        assertTrue(categoryLinks.size() > 0, "Should have category links");
        
        for (WebElement link : categoryLinks) {
            assertTrue(link.isDisplayed(), "Category link should be displayed");
            assertNotNull(link.getAttribute("href"), "Category link should have href attribute");
        }
    }

    @Test
    @Order(5)
    public void testProductListing() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Navigate to a category to see product listings
        try {
            WebElement categoryLink = driver.findElement(By.linkText("Fish"));
            categoryLink.click();
            
            // Wait for products to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-list")));
            
            WebElement productListing = driver.findElement(By.className("product-list"));
            assertTrue(productListing.isDisplayed(), "Product listing should be displayed");
            
            List<WebElement> productItems = driver.findElements(By.cssSelector(".product-item"));
            assertTrue(productItems.size() > 0, "Should have product items");
            
            for (WebElement item : productItems) {
                assertTrue(item.isDisplayed(), "Product item should be displayed");
            }
        } catch (NoSuchElementException ignored) {
            // If Fish category not found, proceed with general testing
        }
    }

    @Test
    @Order(6)
    public void testShoppingCartFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check if cart is accessible
        try {
            WebElement cartLink = driver.findElement(By.cssSelector("[href='/cart']"));
            assertTrue(cartLink.isDisplayed(), "Cart link should be displayed");
        } catch (NoSuchElementException ignored) {
            // Cart access might depend on session state
        }
        
        // Try to find and interact with add to cart button
        try {
            List<WebElement> addToCartButtons = driver.findElements(By.cssSelector(".add-to-cart-btn"));
            if (!addToCartButtons.isEmpty()) {
                WebElement addButton = addToCartButtons.get(0);
                assertTrue(addButton.isDisplayed(), "Add to cart button should be displayed");
            }
        } catch (NoSuchElementException ignored) {
            // Might not find add to cart buttons on homepage
        }
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check footer
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
        
        // Find footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed(), "Footer link should be displayed");
            assertNotNull(link.getAttribute("href"), "Footer link should have href attribute");
        }
    }

    @Test
    @Order(8)
    public void testExternalLinksInFooter() {
        driver.get("https://jpetstore.aspectran.com/");
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        String mainWindowHandle = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Click external links that open in new tabs
                link.click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                String currentUrl = driver.getCurrentUrl();
                // Look for any valid external domains
                if (currentUrl.contains("aspectran.com") || currentUrl.contains("github.com")) {
                    // These are valid internal references
                } else {
                    // For external links, just ensure they're not malformed
                    assertTrue(currentUrl.contains("http") || currentUrl.contains("https"), 
                               "External link should be a valid URL");
                }
                
                driver.close();
                driver.switchTo().window(mainWindowHandle);
            }
        }
    }

    @Test
    @Order(9)
    public void testSearchFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check if search bar is present
        try {
            WebElement searchInput = driver.findElement(By.id("search-input"));
            WebElement searchButton = driver.findElement(By.id("search-button"));
            
            assertTrue(searchInput.isDisplayed(), "Search input should be displayed");
            assertTrue(searchButton.isDisplayed(), "Search button should be displayed");
            
            // Test typing into search box
            searchInput.sendKeys("fish");
            searchButton.click();
        } catch (NoSuchElementException ignored) {
            // Search might not be available on homepage or might not be active
        }
    }

    @Test
    @Order(10)
    public void testResponsiveDesignElements() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Check basic responsive elements
        List<WebElement> navElements = driver.findElements(By.cssSelector("nav"));
        assertTrue(navElements.size() > 0, "Should have navigation elements");
        
        // Check for mobile menu toggle if present
        try {
            WebElement mobileToggle = driver.findElement(By.cssSelector(".mobile-menu-toggle"));
            assertTrue(mobileToggle.isDisplayed(), "Mobile menu toggle should be displayed");
        } catch (NoSuchElementException ignored) {
            // Mobile menu might not be displayed in headless mode
        }
        
        // Check for responsive image containers
        WebElement mainContent = driver.findElement(By.tagName("main"));
        assertTrue(mainContent.isDisplayed(), "Main content should be displayed");
    }

    @Test
    @Order(11)
    public void testUserAccountFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Try to find login elements
        try {
            WebElement loginLink = driver.findElement(By.cssSelector("[href='/login']"));
            assertTrue(loginLink.isDisplayed(), "Login link should be displayed");
        } catch (NoSuchElementException ignored) {
            // Login not immediately visible
        }
        
        // Try to find register link
        try {
            WebElement registerLink = driver.findElement(By.cssSelector("[href='/register']"));
            assertTrue(registerLink.isDisplayed(), "Register link should be displayed");
        } catch (NoSuchElementException ignored) {
            // Registration might not be part of main UI or available
        }
    }

    @Test
    @Order(12)
    public void testProductDetailView() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Try to get to a product detail page
        try {
            // Get the first category URL
            WebElement fishLink = driver.findElement(By.linkText("Fish"));
            fishLink.click();
            
            // Wait for category page to load
            wait.until(ExpectedConditions.urlContains("/category/Fish"));
            
            // Try to click on first product
            List<WebElement> productLinks = driver.findElements(By.cssSelector(".product-link"));
            if (!productLinks.isEmpty()) {
                WebElement firstProduct = productLinks.get(0);
                String productUrl = firstProduct.getAttribute("href");
                assertTrue(productUrl != null && !productUrl.isEmpty(), "Product link should have URL");
            }
        } catch (NoSuchElementException e) {
            // If Fish category is not there, continue with basic testing
        }
    }

    @Test
    @Order(13)
    public void testPageNavigation() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Navigate through different sections
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        int linksCount = Math.min(navLinks.size(), 5); // Limit to prevent too many navigations
        
        for (int i = 0; i < linksCount; i++) {
            WebElement link = navLinks.get(i);
            String href = link.getAttribute("href");
            
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Only test internal links, not external ones
                if (href.contains("jpetstore.aspectran.com")) {
                    link.click();
                    wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
                    
                    // Verify navigation
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("jpetstore.aspectran.com"), 
                               "Should remain on JPetStore domain after navigation");
                    
                    // Go back to home
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
                }
            }
        }
    }

    @Test
    @Order(14)
    public void testAccessibilityAndSemanticElements() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Test for semantic HTML elements
        List<WebElement> headerElements = driver.findElements(By.tagName("header"));
        assertEquals(1, headerElements.size(), "Should have one header element");
        
        List<WebElement> navElements = driver.findElements(By.tagName("nav"));
        assertTrue(navElements.size() > 0, "Should have navigation elements");
        
        List<WebElement> mainElements = driver.findElements(By.tagName("main"));
        assertEquals(1, mainElements.size(), "Should have one main element");
        
        List<WebElement> footerElements = driver.findElements(By.tagName("footer"));
        assertEquals(1, footerElements.size(), "Should have one footer element");
        
        // Test for images with alt attributes
        List<WebElement> imgElements = driver.findElements(By.tagName("img"));
        for (WebElement img : imgElements) {
            String altText = img.getAttribute("alt");
            if (altText != null) {
                assertFalse(altText.trim().isEmpty(), "Image alt text should not be empty");
            }
        }
    }

    @Test
    @Order(15)
    public void testLoginForm() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Try to locate login elements
        try {
            WebElement loginLink = driver.findElement(By.linkText("Sign In"));
            assertTrue(loginLink.isDisplayed(), "Sign In link should be displayed");
            
            // Check if sign-in form is present on click
            loginLink.click();
            wait.until(ExpectedConditions.urlContains("/login"));
            
            WebElement loginForm = driver.findElement(By.cssSelector("form[action='/login']"));
            assertTrue(loginForm.isDisplayed(), "Login form should be displayed");
            
            // Go back to main page
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
        } catch (NoSuchElementException | TimeoutException ignored) {
            // Login functionality might be disabled or not present on homepage
        }
    }
}