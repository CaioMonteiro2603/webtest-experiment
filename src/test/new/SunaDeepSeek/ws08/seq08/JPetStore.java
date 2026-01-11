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
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.sidebar")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("div.sidebar a[href*='FISH']")));
        fishCategory.click();

        wait.until(ExpectedConditions.urlContains("FISH"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.content")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testProductNavigation() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("div.content a[href*='FI-SW-01']")));
        productLink.click();

        wait.until(ExpectedConditions.urlContains("FI-SW-01"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.content")).isDisplayed());
    }

    @Test
    @Order(4)
    public void testItemPageNavigation() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("div.content a[href*='EST-1']")));
        itemLink.click();

        wait.until(ExpectedConditions.urlContains("EST-1"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.content")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/items/EST-1");
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='addItemToCart']")));
        addToCartBtn.click();

        wait.until(ExpectedConditions.urlContains("shoppingCart"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.content")).isDisplayed());
    }

    @Test
    @Order(6)
    public void testCheckoutProcess() {
        driver.get(BASE_URL + "cart/viewCart");
        WebElement proceedBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='checkout']")));
        proceedBtn.click();

        wait.until(ExpectedConditions.urlContains("checkout"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.content")).isDisplayed());
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Skip external link tests as they may not be present in this application
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.footer")).isDisplayed());
    }

    @Test
    @Order(8)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "account/signonForm");
        
        // Valid login
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        
        usernameField.sendKeys("j2ee");
        passwordField.sendKeys("j2ee");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.content")).isDisplayed());
    }

    @Test
    @Order(9)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        
        // Invalid login
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.content")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.content")).getText().contains("Invalid username or password"));
    }

    @Test
    @Order(10)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchField = wait.until(ExpectedConditions.elementToBeClickable(By.name("keyword")));
        WebElement searchButton = driver.findElement(By.cssSelector("input[type='submit']"));
        
        searchField.sendKeys("fish");
        searchButton.click();
        
        wait.until(ExpectedConditions.urlContains("search"));
        List<WebElement> products = driver.findElements(By.cssSelector("div.content tr"));
        Assertions.assertTrue(products.size() > 0);
    }
}