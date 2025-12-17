package GPT4.ws08.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
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
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#WelcomeContent")));
        Assertions.assertTrue(welcome.isDisplayed(), "Welcome content should be visible");
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"), "Title should contain 'JPetStore'");
    }

    @Test
    @Order(2)
    public void testEnterStoreLink() {
        driver.get(BASE_URL);
        WebElement enterStore = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='catalog']")));
        enterStore.click();
        wait.until(ExpectedConditions.urlContains("/catalog/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/"), "Should navigate to catalog page");
        WebElement quickLinks = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("QuickLinks")));
        Assertions.assertTrue(quickLinks.isDisplayed(), "QuickLinks should be visible");
    }

    @Test
    @Order(3)
    public void testSignInInvalidCredentials() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement login = driver.findElement(By.name("signon"));

        username.sendKeys("invalid");
        password.sendKeys("wrong");
        login.click();

        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(message.getText().toLowerCase().contains("invalid"), "Should show invalid login message");
    }

    @Test
    @Order(4)
    public void testSignInValidCredentials() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement login = driver.findElement(By.name("signon"));

        username.clear();
        password.clear();
        username.sendKeys("j2ee");
        password.sendKeys("j2ee");
        login.click();

        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));
        Assertions.assertTrue(greeting.getText().contains("Welcome"), "Should show welcome message after login");
    }

    @Test
    @Order(5)
    public void testCategoryNavigation() {
        driver.get(BASE_URL + "catalog/");
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Fish']")));
        fishLink.click();

        wait.until(ExpectedConditions.urlContains("/catalog/categories/FISH"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        Assertions.assertTrue(heading.getText().contains("Fish"), "Category heading should say Fish");
    }

    @Test
    @Order(6)
    public void testAddToCart() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='catalog/items/FI-SW-01']")));
        itemLink.click();

        wait.until(ExpectedConditions.urlContains("items/FI-SW-01"));
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();

        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.cart")));
        Assertions.assertTrue(cartTable.getText().contains("FI-SW-01"), "Cart should contain the added item");
    }

    @Test
    @Order(7)
    public void testExternalAspectranLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='aspectran.com']")));
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("aspectran.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"), "Should navigate to external aspectran.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testLogout() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement login = driver.findElement(By.name("signon"));

        username.clear();
        password.clear();
        username.sendKeys("j2ee");
        password.sendKeys("j2ee");
        login.click();

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        WebElement enterStore = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store")));
        Assertions.assertTrue(enterStore.isDisplayed(), "Should return to home page after logout");
    }
}
