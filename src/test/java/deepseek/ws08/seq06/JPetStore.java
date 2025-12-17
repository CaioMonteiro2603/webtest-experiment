package deepseek.ws08.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Header img")));
        Assertions.assertTrue(banner.isDisplayed(), "JPetStore banner should be visible");
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")));
        fishLink.click();
        
        wait.until(ExpectedConditions.urlContains("FISH"));
        WebElement productTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Catalog table")));
        Assertions.assertTrue(productTable.isDisplayed(), "Fish products should be displayed");
    }

    @Test
    @Order(3)
    public void testProductDetails() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("FI-SW-01")));
        productLink.click();
        
        wait.until(ExpectedConditions.urlContains("FI-SW-01"));
        WebElement itemDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Catalog")));
        Assertions.assertTrue(itemDetails.getText().contains("Koi"), "Product details should be displayed");
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/items/FI-SW-01");
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Add to Cart']")));
        addToCart.click();
        
        wait.until(ExpectedConditions.urlContains("cart"));
        WebElement cartItems = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Cart table")));
        Assertions.assertTrue(cartItems.getText().contains("Koi"), "Item should be added to cart");
    }

    @Test
    @Order(5)
    public void testLogin() {
        driver.get(BASE_URL + "account/signon");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        username.sendKeys("j2ee");
        password.sendKeys("j2ee");
        loginButton.click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"), "Login should be successful");
    }

    @Test
    @Order(6)
    public void testCheckout() {
        testLogin();
        driver.get(BASE_URL + "cart/viewCart");
        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Proceed to Checkout")));
        proceedToCheckout.click();
        
        wait.until(ExpectedConditions.urlContains("newOrderForm"));
        WebElement paymentDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Catalog")));
        Assertions.assertTrue(paymentDetails.getText().contains("Payment"), "Checkout form should be displayed");
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("keyword")));
        WebElement searchButton = driver.findElement(By.name("searchProducts"));
        
        searchInput.sendKeys("angelfish");
        searchButton.click();
        
        WebElement searchResults = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#Catalog table")));
        Assertions.assertTrue(searchResults.getText().contains("Angelfish"), "Search results should be displayed");
    }

    @Test
    @Order(8)
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
        wait.until(ExpectedConditions.urlContains("aspectran"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}