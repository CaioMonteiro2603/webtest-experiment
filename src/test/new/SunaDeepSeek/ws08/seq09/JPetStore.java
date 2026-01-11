package SunaDeepSeek.ws08.seq09;

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
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"));
    }

    @Test
    @Order(2)
    public void testMainCategoriesNavigation() {
        driver.get(BASE_URL);
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("#SidebarContent a")));
        
        for (WebElement category : categories) {
            String categoryName = category.getText();
            category.click();
            
            wait.until(ExpectedConditions.urlContains("/categories/"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/categories/"));
            
            List<WebElement> products = driver.findElements(By.cssSelector("#Catalog table tr td a"));
            Assertions.assertTrue(products.size() > 0, "No products found for category: " + categoryName);
            
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
        }
    }

    @Test
    @Order(3)
    public void testProductDetailsNavigation() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        List<WebElement> productLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("#Catalog table tr td a")));
        WebElement productLink = productLinks.get(0);
        String productName = productLink.getText();
        productLink.click();
        
        wait.until(ExpectedConditions.urlContains("productId="));
        WebElement productTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Catalog h2")));
        Assertions.assertTrue(productTitle.getText().contains(productName));
    }

    @Test
    @Order(4)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "account/signonForm");
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMessage.getText().contains(USERNAME));
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Content ul li")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test footer links
        testExternalLink(By.linkText("Documentation"), "aspectran.com");
        testExternalLink(By.linkText("GitHub"), "github.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testShoppingCart() {
        // Login first
        driver.get(BASE_URL + "account/signonForm");
        driver.findElement(By.name("username")).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        
        // Navigate to a product
        driver.get(BASE_URL + "catalog/products/FI-FW-01");
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("td[colspan='2'] a")));
        addToCartButton.click();
        
        // Verify cart
        wait.until(ExpectedConditions.urlContains("viewCart"));
        WebElement cartItem = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Cart tr:nth-child(2) td:nth-child(2)")));
        Assertions.assertTrue(cartItem.getText().contains("Angelfish"));
        
        // Remove item
        WebElement removeButton = driver.findElement(By.name("remove"));
        removeButton.click();
        
        WebElement emptyCartMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Cart td")));
        Assertions.assertTrue(emptyCartMessage.getText().contains("Your cart is empty"));
    }

    @Test
    @Order(8)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        
        WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("keyword")));
        searchField.sendKeys("fish");
        WebElement searchButton = driver.findElement(By.cssSelector("input[type='submit'][value='Search']"));
        searchButton.click();
        
        wait.until(ExpectedConditions.urlContains("keyword=fish"));
        List<WebElement> products = driver.findElements(By.cssSelector("#Catalog table tr td a"));
        Assertions.assertTrue(products.size() > 0);
        
        for (WebElement product : products) {
            Assertions.assertTrue(product.getText().toLowerCase().contains("fish"));
        }
    }
}