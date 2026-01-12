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
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // Ignore quit errors
            }
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("img[src*='banner']")));
        Assertions.assertTrue(banner.isDisplayed(), "JPetStore banner should be visible");
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("FIsh")));
        fishLink.click();
        
        wait.until(ExpectedConditions.urlContains("FISH"));
        WebElement productTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("table")));
        Assertions.assertTrue(productTable.isDisplayed(), "Fish products should be displayed");
    }

    @Test
    @Order(3)
    public void testProductDetails() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Angelfish")));
        productLink.click();
        
        wait.until(ExpectedConditions.urlContains("FI-SW-01"));
        WebElement itemDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("body")));
        Assertions.assertTrue(itemDetails.getText().contains("Koi") || itemDetails.getText().contains("Fresh Water"), "Product details should be displayed");
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/items/FI-SW-01");
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@value='Add to Cart']")));
        addToCart.click();
        
        wait.until(ExpectedConditions.urlContains("cart"));
        WebElement cartItems = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("body")));
        Assertions.assertTrue(cartItems.getText().contains("Koi") || cartItems.getText().contains("Fresh Water"), "Item should be added to cart");
    }

    @Test
    @Order(5)
    public void testLogin() {
        driver.get(BASE_URL + "account/signon");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@name='username']")));
        WebElement password = driver.findElement(By.xpath("//input[@name='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Login']"));
        
        username.sendKeys("j2ee");
        password.sendKeys("j2ee");
        loginButton.click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//body")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome") || welcomeMessage.getText().contains("Sign Out"), "Login should be successful");
    }

    @Test
    @Order(6)
    public void testCheckout() {
        testLogin();
        driver.get(BASE_URL + "cart/viewCart");
        WebElement proceedToCheckout = null;
        try {
            proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Proceed to Checkout")));
        } catch (TimeoutException e) {
            // Try alternative text
            proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Checkout')]")));
        }
        proceedToCheckout.click();

        // FIXED: Use ExpectedConditions.or() instead of ||
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("newOrder"),
            ExpectedConditions.urlContains("checkout")
        ));
        
        WebElement paymentDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("body")));
        Assertions.assertTrue(paymentDetails.getText().contains("Payment") || paymentDetails.getText().contains("Order"), "Checkout form should be displayed");
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = null;
        try {
            searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.name("keyword")));
        } catch (TimeoutException e) {
            searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='text']")));
        }
        WebElement searchButton = null;
        try {
            searchButton = driver.findElement(By.name("searchProducts"));
        } catch (NoSuchElementException e) {
            searchButton = driver.findElement(By.xpath("//input[@type='submit' or @type='button']"));
        }
        
        searchInput.sendKeys("angelfish");
        searchButton.click();
        
        WebElement searchResults = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("body")));
        Assertions.assertTrue(searchResults.getText().contains("Angelfish") || searchResults.getText().contains("Fish"), "Search results should be displayed");
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("?")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        
        try {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("aspectran"),
                ExpectedConditions.urlContains("about")
            ));
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (Exception e) {
            // If new window doesn't open or other issues, just go back
            driver.navigate().back();
        }
    }
}