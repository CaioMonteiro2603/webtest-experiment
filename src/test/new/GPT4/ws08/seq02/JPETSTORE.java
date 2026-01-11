package GPT4.ws08.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JPETSTORE {

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JPetStore"));
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"), "Homepage title does not contain JPetStore");
    }

    @Test
    @Order(2)
    public void testSignInLinkOpensLoginPage() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/account/signonForm"), "Did not navigate to login page");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign Out")));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign Out")).isDisplayed(), "Sign Out link not displayed after login");
    }

    @Test
    @Order(4)
    public void testInvalidLoginShowsError() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("invalid");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("message-error")));
        Assertions.assertTrue(error.getText().contains("invalid username or password"), "Error message not shown for invalid login");
    }

    @Test
    @Order(5)
    public void testNavigationToFishCategory() {
        driver.get(BASE_URL);
        WebElement fishLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("area[alt='Fish']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fishLink);
        wait.until(ExpectedConditions.urlContains("/catalog/categories/FISH"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/categories/FISH"), "Failed to navigate to Fish category");
    }

    @Test
    @Order(6)
    public void testProductListInFishCategory() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table tr td a")));
        Assertions.assertFalse(items.isEmpty(), "No products found in Fish category");
    }

    @Test
    @Order(7)
    public void testAddToCartAndVerifyQuantity() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        productLink.click();
        wait.until(ExpectedConditions.urlContains("/catalog/products/FI-SW-01"));
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        addButton.click();
        wait.until(ExpectedConditions.urlContains("/cart/view"));
        WebElement quantity = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("quantity")));
        Assertions.assertEquals("1", quantity.getAttribute("value"), "Quantity in cart is not 1 after add");
    }

    @Test
    @Order(8)
    public void testProceedToCheckoutDisplaysForm() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        productLink.click();
        wait.until(ExpectedConditions.urlContains("/catalog/products/FI-SW-01"));
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        addButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/order/new"));
        WebElement paymentSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("cardNumber")));
        Assertions.assertTrue(paymentSection.isDisplayed(), "Payment form not displayed on checkout");
    }

    @Test
    @Order(9)
    public void testExternalAspectranLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Aspectran")));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", footerLink);
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("aspectran.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"), "Did not navigate to Aspectran external link");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testSignOutReturnsToHome() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign In")).isDisplayed(), "Did not return to home after sign out");
    }
}