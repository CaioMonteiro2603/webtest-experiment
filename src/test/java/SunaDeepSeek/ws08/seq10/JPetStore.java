package SunaDeepSeek.ws08.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JPetStore Demo"));
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be on home page");
    }

    @Test
    @Order(2)
    public void testNavigationToCategories() {
        driver.get(BASE_URL);
        List<WebElement> categoryLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#SidebarContent a")));
        
        for (WebElement category : categoryLinks) {
            String categoryName = category.getText();
            category.click();
            
            wait.until(ExpectedConditions.urlContains("categoryId="));
            Assertions.assertTrue(driver.getCurrentUrl().contains("categoryId="), 
                "Should be on category page after clicking " + categoryName);
            
            // Verify products are displayed
            List<WebElement> products = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("#Catalog table tr")));
            Assertions.assertTrue(products.size() > 1, "Should display products for " + categoryName);
            
            driver.navigate().back();
        }
    }

    @Test
    @Order(3)
    public void testProductDetailsPage() {
        driver.get(BASE_URL);
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")));
        fishCategory.click();
        
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#Catalog table tr:nth-child(2) a")));
        String productName = firstProduct.getText();
        firstProduct.click();
        
        wait.until(ExpectedConditions.urlContains("productId="));
        WebElement productTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Catalog h2")));
        Assertions.assertTrue(productTitle.getText().contains(productName), 
            "Product details page should show correct product");
    }

    @Test
    @Order(4)
    public void testLogin() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")));
        signInLink.click();
        
        wait.until(ExpectedConditions.urlContains("signonForm"));
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        WebElement welcomeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMsg.getText().contains(USERNAME), 
            "Should show welcome message after login");
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Content ul li")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"), 
            "Should show error message for invalid login");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test footer links
        String[] externalLinks = {
            "a[href*='facebook.com']",
            "a[href*='twitter.com']"
        };
        
        for (String linkSelector : externalLinks) {
            WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(linkSelector)));
            externalLink.click();
            
            // Switch to new tab
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            // Verify domain and close tab
            Assertions.assertNotEquals(driver.getCurrentUrl(), BASE_URL, 
                "Should be on external site");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    public void testAddToCart() {
        // First login
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Navigate to a product
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#Catalog table tr:nth-child(2) a")));
        firstProduct.click();
        
        // Add to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#Catalog table tr:nth-child(7) a")));
        addToCartButton.click();
        
        // Verify cart
        wait.until(ExpectedConditions.urlContains("viewCart"));
        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Cart tr:nth-child(2)")));
        Assertions.assertTrue(cartItem.getText().contains("Fish"), "Item should be in cart");
    }

    @Test
    @Order(8)
    public void testLogout() {
        // First login
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Logout
        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign Out")));
        signOutLink.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        WebElement signInLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.linkText("Sign In")));
        Assertions.assertTrue(signInLink.isDisplayed(), "Should show sign in link after logout");
    }
}