package SunaDeepSeek.ws08.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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
        wait.until(ExpectedConditions.titleContains("JPetStore Demo"));

        // Verify main sections
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.container")).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.category")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("div.category a[href*='categoryId=FISH']")));
        fishCategory.click();

        wait.until(ExpectedConditions.urlContains("categoryId=FISH"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.product")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testProductNavigation() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("div.product a[href*='productId=FI-SW-01']")));
        productLink.click();

        wait.until(ExpectedConditions.urlContains("productId=FI-SW-01"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.item")).isDisplayed());
    }

    @Test
    @Order(4)
    public void testItemPageNavigation() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("div.item a[href*='itemId=EST-1']")));
        itemLink.click();

        wait.until(ExpectedConditions.urlContains("itemId=EST-1"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.detail")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/items/EST-1");
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[onclick*='addItemToCart']")));
        addToCartBtn.click();

        wait.until(ExpectedConditions.urlContains("viewCart"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.cart")).isDisplayed());
    }

    @Test
    @Order(6)
    public void testCheckoutProcess() {
        driver.get(BASE_URL + "cart/viewCart");
        WebElement proceedBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='newOrderForm']")));
        proceedBtn.click();

        wait.until(ExpectedConditions.urlContains("newOrderForm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("form[action*='newOrder']")).isDisplayed());
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        
        // Test Facebook link
        testExternalLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
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
    @Order(8)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "account/signonForm");
        
        // Valid login
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys("j2ee");
        passwordField.sendKeys("j2ee");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.welcome")).isDisplayed());
    }

    @Test
    @Order(9)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        
        // Invalid login
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.error")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.error")).isDisplayed());
    }

    @Test
    @Order(10)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchField = wait.until(ExpectedConditions.elementToBeClickable(By.name("keyword")));
        WebElement searchButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        searchField.sendKeys("fish");
        searchButton.click();
        
        wait.until(ExpectedConditions.urlContains("searchProducts"));
        List<WebElement> products = driver.findElements(By.cssSelector("div.product"));
        Assertions.assertTrue(products.size() > 0);
    }
}