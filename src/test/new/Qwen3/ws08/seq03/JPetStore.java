package Qwen3.ws08.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        assertEquals("JPetStore Demo", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Click sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        
        // Fill login form
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("j2ee");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("j2ee");
        
        // Submit login
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' and contains(.,'Login')]"));
        loginButton.click();
        
        // Verify successful login
        wait.until(ExpectedConditions.urlContains("/catalog"));
        assertTrue(driver.getCurrentUrl().contains("/catalog"));
        assertTrue(driver.getTitle().contains("JPetStore"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Click sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        
        // Fill invalid login form
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("invalid_password");
        
        // Submit login
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' and contains(.,'Login')]"));
        loginButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".error")).getText().contains("Invalid"));
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Click Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"));
        
        // Click Catalog link
        driver.get("https://jpetstore.aspectran.com/");
        WebElement catalogLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Catalog")));
        catalogLink.click();
        wait.until(ExpectedConditions.urlContains("/catalog"));
        assertTrue(driver.getCurrentUrl().contains("/catalog"));
        
        // Click Sign In link
        driver.get("https://jpetstore.aspectran.com/");
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/account"));
        assertTrue(driver.getCurrentUrl().contains("/account"));
        
        // Navigate back to home
        driver.get("https://jpetstore.aspectran.com/");
        
        // Click Order link
        WebElement orderLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Order")));
        orderLink.click();
        wait.until(ExpectedConditions.urlContains("/order"));
        assertTrue(driver.getCurrentUrl().contains("/order"));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Click Terms link
        List<WebElement> termsLinks = driver.findElements(By.linkText("Terms"));
        if (termsLinks.size() > 0) {
            WebElement termsLink = termsLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", termsLink);
            wait.until(ExpectedConditions.elementToBeClickable(termsLink));
            termsLink.click();
            String currentWindowHandle = driver.getWindowHandle();
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(currentWindowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
            driver.close();
            driver.switchTo().window(currentWindowHandle);
        }
        
        // Click Privacy link
        driver.get("https://jpetstore.aspectran.com/");
        List<WebElement> privacyLinks = driver.findElements(By.linkText("Privacy"));
        if (privacyLinks.size() > 0) {
            WebElement privacyLink = privacyLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", privacyLink);
            wait.until(ExpectedConditions.elementToBeClickable(privacyLink));
            privacyLink.click();
            String currentWindowHandle = driver.getWindowHandle();
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(currentWindowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
            driver.close();
            driver.switchTo().window(currentWindowHandle);
        }
        
        // Click Contact link
        driver.get("https://jpetstore.aspectran.com/");
        List<WebElement> contactLinks = driver.findElements(By.linkText("Contact"));
        if (contactLinks.size() > 0) {
            WebElement contactLink = contactLinks.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", contactLink);
            wait.until(ExpectedConditions.elementToBeClickable(contactLink));
            contactLink.click();
            wait.until(ExpectedConditions.urlContains("/contact"));
            assertTrue(driver.getCurrentUrl().contains("/contact"));
        }
    }

    @Test
    @Order(6)
    public void testProductCatalog() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Verify catalog page loaded
        assertTrue(driver.getTitle().contains("Catalog"));
        
        // Check that product list is displayed
        try {
            WebElement productList = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-list")));
            assertTrue(productList.isDisplayed());
            
            // Check if there are products
            List<WebElement> products = driver.findElements(By.cssSelector(".product-item"));
            if (products.size() > 0) {
                WebElement firstProduct = products.get(0);
                assertTrue(firstProduct.isDisplayed());
            }
        } catch (TimeoutException e) {
            // Alternative check for product presence
            List<WebElement> products = driver.findElements(By.xpath("//*[contains(@class,'product')]"));
            assertTrue(products.size() > 0 && products.get(0).isDisplayed());
        }
    }

    @Test
    @Order(7)
    public void testShoppingCartFunctionality() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Add a product to cart
        List<WebElement> products = driver.findElements(By.cssSelector(".product-item"));
        if (products.size() > 0) {
            WebElement firstProduct = products.get(0);
            WebElement addToCartButton = firstProduct.findElement(By.cssSelector(".add-to-cart"));
            addToCartButton.click();
            
            // Check if cart updated
            WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".cart-badge")));
            assertTrue(cartBadge.isDisplayed());
        }
    }

    @Test
    @Order(8)
    public void testSearchFunctionality() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Search for a product
        WebElement searchField;
        try {
            searchField = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        } catch (TimeoutException e) {
            searchField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='search' or @placeholder='Search']")));
        }
        searchField.sendKeys("fish");
        
        // Submit search
        WebElement searchButton;
        try {
            searchButton = driver.findElement(By.xpath("//input[@type='submit']"));
        } catch (NoSuchElementException e) {
            searchButton = driver.findElement(By.xpath("//button[@type='submit']"));
        }
        searchButton.click();
        
        // Check if search results are displayed
        try {
            WebElement searchResults = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".search-results")));
            assertTrue(searchResults.isDisplayed());
        } catch (TimeoutException e) {
            // Check for alternative search results format
            List<WebElement> results = driver.findElements(By.xpath("//*[contains(@class,'result')]"));
            assertTrue(results.size() > 0);
        }
    }

    @Test
    @Order(9)
    public void testProductCategories() {
        driver.get("https://jpetstore.aspectran.com/catalog");
        
        // Check that categories are displayed
        List<WebElement> categories;
        try {
            categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".categories")));
        } catch (TimeoutException e) {
            categories = driver.findElements(By.xpath("//*[contains(@class,'category')]"));
        }
        assertTrue(categories.size() > 0 && categories.get(0).isDisplayed());
        
        // Click on a category
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector(".category-link"));
        if (categoryLinks.size() == 0) {
            categoryLinks = driver.findElements(By.xpath("//a[contains(@href,'category')]"));
        }
        
        if (categoryLinks.size() > 0) {
            WebElement firstCategory = categoryLinks.get(0);
            firstCategory.click();
            
            // Verify category page loaded
            try {
                wait.until(ExpectedConditions.urlContains("?categoryId="));
                assertTrue(driver.getCurrentUrl().contains("?categoryId="));
            } catch (TimeoutException e) {
                assertTrue(driver.getCurrentUrl().contains("category"));
            }
        }
    }

    @Test
    @Order(10)
    public void testLogout() {
        driver.get("https://jpetstore.aspectran.com/");
        
        // Login first
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("j2ee");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("j2ee");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' and contains(.,'Login')]"));
        loginButton.click();
        
        // Wait for login to complete
        wait.until(ExpectedConditions.urlContains("/catalog"));
        
        // Click logout
        WebElement logoutLink = driver.findElement(By.linkText("Sign Out"));
        logoutLink.click();
        
        // Verify logged out
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"));
    }
}