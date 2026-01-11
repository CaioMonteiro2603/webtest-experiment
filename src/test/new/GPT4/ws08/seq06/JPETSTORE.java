package GPT4.ws08.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JPETSTORE {

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
        wait.until(ExpectedConditions.titleContains("JPetStore"));
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"), "Home page title should contain 'JPetStore'");
    }

    @Test
    @Order(2)
    public void testSignInPageLoads() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='signonForm']")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/account/signonForm"), "Sign in page should be loaded");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Welcome")));
        Assertions.assertTrue(greeting.getText().contains("Welcome"), "Greeting message should contain 'Welcome'");
    }

    @Test
    @Order(4)
    public void testLogout() {
        testValidLogin();
        WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='signoff']")));
        signOut.click();
        wait.until(ExpectedConditions.urlContains("index"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index"), "Should be redirected to home page after logout");
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("invalid");
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(errorMsg.getText().contains("Invalid"), "Error message should mention invalid credentials");
    }

    @Test
    @Order(6)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        WebElement fishLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("area[alt='Fish']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fishLink);
        wait.until(ExpectedConditions.urlContains("catalog/categories/FISH"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("FISH"), "Should navigate to FISH category page");
    }

    @Test
    @Order(7)
    public void testItemListingFromCategory() {
        testCategoryNavigation();
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='catalog/products/FI-SW-01']")));
        itemLink.click();
        wait.until(ExpectedConditions.urlContains("products/FI-SW-01"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("FI-SW-01"), "Should navigate to item listing page");
    }

    @Test
    @Order(8)
    public void testAddItemToCart() {
        testItemListingFromCategory();
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='cart/addItemToCart']")));
        addToCartBtn.click();
        wait.until(ExpectedConditions.urlContains("cart/viewCart"));
        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td.item")));
        Assertions.assertTrue(cartItem.getText().contains("Angelfish"), "Cart should contain added item");
    }

    @Test
    @Order(9)
    public void testExternalAspectranLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='aspectran.com']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", footerLink);
        WebElement clickableFooterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='aspectran.com']")));
        String originalWindow = driver.getWindowHandle();
        clickableFooterLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("aspectran.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"), "External link should go to aspectran.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("keyword")));
        searchInput.sendKeys("fish");
        driver.findElement(By.cssSelector("input[type='submit'][value='Search']")).click();

        wait.until(ExpectedConditions.urlContains("search"));
        List<WebElement> results = driver.findElements(By.cssSelector("table[class='list'] td.item"));
        Assertions.assertFalse(results.isEmpty(), "Search results should not be empty");
    }
}