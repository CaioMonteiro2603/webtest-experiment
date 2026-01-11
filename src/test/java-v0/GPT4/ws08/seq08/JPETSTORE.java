package GPT4.ws08.seq08;

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
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcome.getText().toLowerCase().contains("welcome"), "Home page should show welcome content");
    }

    @Test
    @Order(2)
    public void testSignInPageLoads() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        WebElement signInForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("signon")));
        Assertions.assertTrue(signInForm.isDisplayed(), "Sign in form should be visible");
    }

    @Test
    @Order(3)
    public void testInvalidLoginShowsError() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("invalid");
        driver.findElement(By.name("signon")).click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid username or password"), "Should show error for invalid login");
    }

    @Test
    @Order(4)
    public void testValidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.name("signon")).click();
        WebElement welcome = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("WelcomeContent")));
        Assertions.assertTrue(welcome.getText().toLowerCase().contains("welcome"), "Welcome message should appear after login");
    }

    @Test
    @Order(5)
    public void testBrowseFishCategory() {
        driver.get(BASE_URL);
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Fish']")));
        fishLink.click();
        wait.until(ExpectedConditions.urlContains("/catalog/categories/FISH"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#Catalog h2")));
        Assertions.assertTrue(heading.getText().contains("Fish"), "Should be in Fish category");
    }

    @Test
    @Order(6)
    public void testAddItemToCart() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        WebElement item = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='itemId=EST-1']")));
        item.click();
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.Button")));
        addToCartBtn.click();
        WebElement cartTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.cart")));
        List<WebElement> rows = cartTable.findElements(By.tagName("tr"));
        Assertions.assertTrue(rows.size() > 1, "Cart should contain at least one item row");
    }

    @Test
    @Order(7)
    public void testUpdateCartQuantity() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-1"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.Button"))).click();
        WebElement quantityInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("EST-1")));
        quantityInput.clear();
        quantityInput.sendKeys("2");
        driver.findElement(By.name("updateCartQuantities")).click();
        WebElement total = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[contains(text(),'Subtotal')]")));
        Assertions.assertTrue(total.getText().contains(".00"), "Subtotal should update correctly");
    }

    @Test
    @Order(8)
    public void testCheckoutPageLoads() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-1"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.Button"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/order/newOrderForm"));
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder")));
        Assertions.assertTrue(continueBtn.isDisplayed(), "Continue button on checkout page should be visible");
    }

    @Test
    @Order(9)
    public void testExternalLinkAspectranOpensCorrectly() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='aspectran.com']")));
        String originalWindow = driver.getWindowHandle();
        footerLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("aspectran.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"), "Should open aspectran.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testSignOut() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.name("signon")).click();
        WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOut.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign In")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore"), "After sign out, should return to main page");
    }
}
