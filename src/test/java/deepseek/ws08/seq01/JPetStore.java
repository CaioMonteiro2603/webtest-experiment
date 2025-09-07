package deepseek.ws08.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStoreWebTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("img[src*='logo-jpetstore']")));
        Assertions.assertTrue(logo.isDisplayed(), "JPetStore logo should be displayed");
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), 
            "Current URL should match base URL");
    }

    @Test
    @Order(2)
    public void testMainCategoriesNavigation() {
        driver.get(BASE_URL);
        
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#Category a")));
        
        Assertions.assertTrue(categories.size() > 0, "Should have at least one category");
        
        for (WebElement category : categories) {
            String categoryName = category.getText();
            category.click();
            
            wait.until(ExpectedConditions.urlContains("/catalog/categories/"));
            WebElement heading = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("main h2")));
            
            Assertions.assertTrue(heading.getText().contains(categoryName), 
                "Category page should display correct heading");
            driver.navigate().back();
        }
    }

    @Test
    @Order(3)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "account/signonForm");
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        // Invalid credentials test
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("wrong");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"), 
            "Error message for invalid login should be displayed");
        
        // Successful login test
        driver.navigate().refresh();
        usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        passwordField = driver.findElement(By.name("password"));
        loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys("j2ee");
        passwordField.sendKeys("j2ee");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/account/"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"), 
            "Welcome message should be displayed after login");
    }

    @Test
    @Order(4)
    public void testProductSearch() {
        driver.get(BASE_URL);
        WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("keyword")));
        WebElement searchButton = driver.findElement(By.cssSelector("input[value='Search']"));
        
        searchField.sendKeys("fish");
        searchButton.click();
        
        wait.until(ExpectedConditions.urlContains("/search"));
        List<WebElement> products = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".products a")));
        
        Assertions.assertTrue(products.size() > 0, "Search should return at least one product");
        Assertions.assertTrue(products.get(0).getText().toLowerCase().contains("fish"),
            "First product should contain search term 'fish'");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("?")));
        
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com/about"), 
            "About page should open in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/products/FI-FW-01");
        
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Add to Cart")));
        addToCartButton.click();
        
        WebElement cartItem = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("td[headers='Item']")));
        Assertions.assertTrue(cartItem.getText().contains("Fish"), 
            "Item should be added to cart");
        
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Remove")));
        removeButton.click();
        
        wait.until(ExpectedConditions.invisibilityOf(cartItem));
        List<WebElement> emptyCartMessage = driver.findElements(By.xpath("//td[contains(text(),'Your cart is empty')]"));
        Assertions.assertTrue(emptyCartMessage.size() > 0, 
            "Cart should be empty after removing item");
    }
}