package deepseek.ws08.seq05;

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
public class JPetStoreTest {
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
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("jpetstore"));
        
        WebElement enterStoreLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='actions/Catalog.action']")));
        enterStoreLink.click();
        
        wait.until(ExpectedConditions.urlContains("Catalog.action"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("#WelcomeContent")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL + "actions/Catalog.action");
        
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='FISH']")));
        fishCategory.click();
        
        wait.until(ExpectedConditions.urlContains("FISH"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Fish"));
    }

    @Test
    @Order(3)
    public void testProductNavigation() {
        driver.get(BASE_URL + "actions/Catalog.action?viewCategory=&categoryId=FISH");
        
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='FI-SW-01']")));
        productLink.click();
        
        wait.until(ExpectedConditions.urlContains("FI-SW-01"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Angelfish"));
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");
        
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='addItemToCart']")));
        addToCartButton.click();
        
        wait.until(ExpectedConditions.urlContains("Cart.action"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("td[colspan='2']")).getText().contains("Sub Total"));
    }

    @Test
    @Order(5)
    public void testLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("j2ee");
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("j2ee");
        
        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("#WelcomeContent")).getText().contains("Welcome"));
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid");
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("invalid");
        
        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}