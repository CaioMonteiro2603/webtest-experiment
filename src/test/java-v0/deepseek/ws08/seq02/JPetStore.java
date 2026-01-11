package deepseek.ws08.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")));
        signIn.click();

        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        username.clear();
        username.sendKeys(USERNAME);
        password.clear();
        password.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("WelcomeContent")));
        Assertions.assertTrue(welcomeMsg.getText().contains("Welcome"),
            "Expected welcome message after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")));
        signIn.click();

        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        username.sendKeys("invalid");
        password.sendKeys("invalid");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".messages li")));
        Assertions.assertTrue(errorMsg.getText().contains("Invalid"),
            "Expected invalid login error message");
    }

    @Test
    @Order(3)
    public void testCategoryNavigation() {
        login();
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")));
        fishCategory.click();

        WebElement productHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h2")));
        Assertions.assertEquals("Fish", productHeader.getText(),
            "Expected to navigate to Fish category");
    }

    @Test
    @Order(4)
    public void testProductSelection() {
        login();
        driver.get(BASE_URL + "actions/Catalog.action?viewCategory=&categoryId=FISH");
        
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("FI-SW-01")));
        productLink.click();

        WebElement itemId = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h2")));
        Assertions.assertTrue(itemId.getText().contains("FI-SW-01"),
            "Expected product details page");
    }

    @Test
    @Order(5)
    public void testAddToCart() {
        login();
        driver.get(BASE_URL + "actions/Catalog.action?viewItem=&itemId=EST-1");
        
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Add to Cart")));
        addToCart.click();

        WebElement cartItem = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//td[contains(text(),'EST-1')]")));
        Assertions.assertTrue(cartItem.isDisplayed(),
            "Expected item in shopping cart");
    }

    @Test
    @Order(6)
    public void testCheckoutProcess() {
        login();
        driver.get(BASE_URL + "actions/Catalog.action?viewItem=&itemId=EST-1");
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Add to Cart")));
        addToCart.click();

        WebElement proceedCheckout = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Proceed to Checkout")));
        proceedCheckout.click();

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("newOrder")));
        continueButton.click();

        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Confirm")));
        confirmButton.click();

        WebElement thankYouMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".messages li")));
        Assertions.assertTrue(thankYouMsg.getText().contains("Thank you"),
            "Expected order confirmation message");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        login();
        String originalWindow = driver.getWindowHandle();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("?")));
        aboutLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran"),
            "Expected to be on Aspectran website");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        if (driver.findElements(By.id("WelcomeContent")).size() == 0) {
            driver.get(BASE_URL + "actions/Account.action?signonForm=");
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
                By.name("username")));
            username.sendKeys(USERNAME);
            driver.findElement(By.name("password")).sendKeys(PASSWORD);
            driver.findElement(By.name("signon")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("WelcomeContent")));
        }
    }
}