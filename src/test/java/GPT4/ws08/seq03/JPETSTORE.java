package GPT4.ws08.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @BeforeEach
    public void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("LogoContent")));
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        String title = driver.getTitle();
        Assertions.assertTrue(title.contains("JPetStore"), "Home page title should contain 'JPetStore'");
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("LogoContent")));
        Assertions.assertTrue(logo.isDisplayed(), "Logo not visible on home page");
    }

    @Test
    @Order(2)
    public void testSignInWithValidCredentials() {
        driver.findElement(By.linkText("Sign In")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.name("signon")).click();
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#WelcomeContent")));
        Assertions.assertTrue(welcome.getText().contains("Welcome"), "Login failed for valid user");
    }

    @Test
    @Order(3)
    public void testSignOut() {
        testSignInWithValidCredentials();
        WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOut.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign In")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"), "User not redirected after sign out");
    }

    @Test
    @Order(4)
    public void testInvalidLoginShowsError() {
        driver.findElement(By.linkText("Sign In")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("wrong");
        driver.findElement(By.name("signon")).click();
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(message.getText().toLowerCase().contains("invalid"), "Expected error for invalid credentials");
    }

    @Test
    @Order(5)
    public void testCategoryLinks() {
        String[] categories = {"FISH", "DOGS", "REPTILES", "CATS", "BIRDS"};
        for (String cat : categories) {
            driver.findElement(By.cssSelector("area[alt='" + cat + "']")).click();
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#Catalog h2")));
            Assertions.assertTrue(heading.getText().toUpperCase().contains(cat), "Category page did not load for " + cat);
            driver.navigate().back();
        }
    }

    @Test
    @Order(6)
    public void testHelpExternalLink() {
        WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("?</a>")));
        String originalWindow = driver.getWindowHandle();
        helpLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("aspectran.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"), "External help link did not open expected domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testViewProductDetails() {
        driver.findElement(By.cssSelector("area[alt='FISH']")).click();
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        productLink.click();
        WebElement details = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#Catalog h2")));
        Assertions.assertTrue(details.getText().contains("Angelfish"), "Product details page did not load correctly");
    }

    @Test
    @Order(8)
    public void testAddItemToCart() {
        driver.findElement(By.cssSelector("area[alt='DOGS']")).click();
        WebElement item = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-BD-01")));
        item.click();
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();
        WebElement cart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Cart")));
        Assertions.assertTrue(cart.getText().contains("Bulldog"), "Item was not added to cart");
    }

    @Test
    @Order(9)
    public void testCheckoutCartFlow() {
        testSignInWithValidCredentials();
        testAddItemToCart();
        driver.findElement(By.linkText("Proceed to Checkout")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("newOrder")));
        driver.findElement(By.name("newOrder")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("order.confirmed")));
        driver.findElement(By.name("order.confirmed")).click();
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#Content h2")));
        Assertions.assertTrue(message.getText().contains("Thank you"), "Checkout did not complete successfully");
    }
}
