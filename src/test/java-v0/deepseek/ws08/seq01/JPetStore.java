package deepseek.ws08.seq01;

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
    public void testStoreNavigation() {
        driver.get(BASE_URL);
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishLink.click();
        wait.until(ExpectedConditions.urlContains("Fish"));
        Assertions.assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Fish"), "Should navigate to Fish category");

        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Dogs")));
        dogsLink.click();
        wait.until(ExpectedConditions.urlContains("Dogs"));
        Assertions.assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Dogs"), "Should navigate to Dogs category");
    }

    @Test
    @Order(2)
    public void testProductDetails() {
        driver.get(BASE_URL + "catalog/Categories.jsp?categoryId=FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        productLink.click();
        wait.until(ExpectedConditions.urlContains("FI-SW-01"));
        Assertions.assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Koi"), "Should show product details");
    }

    @Test
    @Order(3)
    public void testCartOperations() {
        driver.get(BASE_URL + "catalog/Products.jsp?productId=FI-SW-01");
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();
        wait.until(ExpectedConditions.urlContains("Cart"));
        
        WebElement cartTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Cart")));
        Assertions.assertTrue(cartTable.isDisplayed(), "Cart should display added items");
        
        WebElement removeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Remove")));
        removeLink.click();
        Assertions.assertEquals(0, driver.findElements(By.xpath("//tr[contains(@class,'cartItem')]")).size(), "Cart should be empty after removal");
    }

    @Test
    @Order(4)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "account/Signon.jsp");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        username.sendKeys("j2ee");
        password.sendKeys("j2ee");
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"), "Should be logged in successfully");
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/Signon.jsp");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        username.sendKeys("invalid");
        password.sendKeys("credentials");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"), "Error message should be displayed");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement aspectranLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Powered by Aspectran")));
        
        String originalWindow = driver.getWindowHandle();
        aspectranLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                wait.until(ExpectedConditions.urlContains("aspectran"));
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("keyword")));
        WebElement searchButton = driver.findElement(By.name("searchProducts"));

        searchInput.sendKeys("fish");
        searchButton.click();

        List<WebElement> products = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".product")));
        Assertions.assertTrue(products.size() > 0, "Should find matching products");
    }
}