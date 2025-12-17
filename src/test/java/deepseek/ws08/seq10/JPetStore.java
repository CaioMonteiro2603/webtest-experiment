package deepseek.ws08.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {
    private static WebDriver driver;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static WebDriverWait wait;

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement mainImage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[@class='jumbotron']")));
        Assertions.assertTrue(mainImage.isDisplayed(), "Home page did not load properly");
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Fish')]")));
        fishLink.click();
        
        WebElement fishPageTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), 'Fish')]")));
        Assertions.assertTrue(fishPageTitle.isDisplayed(), "Fish category page did not load");
    }

    @Test
    @Order(3)
    public void testProductView() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'FI-SW-01')]")));
        productLink.click();
        
        WebElement productPageTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), 'Angelfish')]")));
        Assertions.assertTrue(productPageTitle.isDisplayed(), "Product page did not load");
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/items/FI-SW-01");
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Add to Cart')]")));
        addToCartBtn.click();
        
        WebElement cartTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), 'Shopping Cart')]")));
        Assertions.assertTrue(cartTitle.isDisplayed(), "Item was not added to cart");
    }

    @Test 
    @Order(5)
    public void testValidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        username.sendKeys("j2ee");
        
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("j2ee");
        
        WebElement loginBtn = driver.findElement(By.name("signon"));
        loginBtn.click();
        
        WebElement welcomeMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'Welcome')]")));
        Assertions.assertTrue(welcomeMsg.isDisplayed(), "Login failed");
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        username.sendKeys("invalid");
        
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("invalid");
        
        WebElement loginBtn = driver.findElement(By.name("signon"));
        loginBtn.click();
        
        WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'Invalid username or password')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message not shown for invalid login");
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("keyword")));
        searchBox.sendKeys("fish");
        
        WebElement searchBtn = driver.findElement(By.name("searchProducts"));
        searchBtn.click();
        
        WebElement searchResults = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), 'Fish')]")));
        Assertions.assertTrue(searchResults.isDisplayed(), "Search results not shown");
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("Facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("LinkedIn", "linkedin.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, '" + expectedDomain + "')]")));
        link.click();
        
        // Switch to new window if opened
        if (driver.getWindowHandles().size() > 1) {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " link failed - wrong domain");
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }
}