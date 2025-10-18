package deepseek.ws08.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
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
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

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
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(),'Welcome to JPetStore')]")));
        assertTrue(welcomeMessage.isDisplayed(), "Welcome message should be displayed");
    }

    @Test
    @Order(2)
    public void testNavigationToCategory() {
        driver.get(BASE_URL);
        
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'FISH')]")));
        fishLink.click();
        
        WebElement categoryHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(),'Fish')]")));
        assertTrue(categoryHeader.isDisplayed(), "Fish category page should load");
    }

    @Test
    @Order(3)
    public void testProductSelection() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'FI-SW-01')]")));
        productLink.click();
        
        WebElement productHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(),'Angelfish')]")));
        assertTrue(productHeader.isDisplayed(), "Product page should load");
    }

    @Test
    @Order(4)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'addItemToCart')]")));
        addToCartButton.click();
        
        WebElement cartHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(),'Shopping Cart')]")));
        assertTrue(cartHeader.isDisplayed(), "Cart page should load after adding item");
        
        WebElement cartItem = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//td[contains(text(),'Angelfish')]")));
        assertTrue(cartItem.isDisplayed(), "Added item should appear in cart");
    }

    @Test
    @Order(5)
    public void testUserLogin() {
        driver.get(BASE_URL + "account/signonForm");
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        usernameField.sendKeys(USERNAME);
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);
        
        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(),'Welcome')]")));
        assertTrue(welcomeMessage.getText().contains(USERNAME), 
            "Welcome message should show logged in user");
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        usernameField.sendKeys("invalid");
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrong");
        
        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        
        WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("keyword")));
        searchField.sendKeys("fish");
        
        WebElement searchButton = driver.findElement(By.name("searchProducts"));
        searchButton.click();
        
        List<WebElement> results = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.xpath("//td[contains(text(),'Fish')]")));
        assertTrue(results.size() > 0, "Search should return results");
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Aspectran link
        WebElement aspectranLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'aspectran.com')]")));
        testExternalLink(aspectranLink, "aspectran.com");
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}