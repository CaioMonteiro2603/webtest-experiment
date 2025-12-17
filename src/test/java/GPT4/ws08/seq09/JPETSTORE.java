package GPT4.ws08.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

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
        if (driver != null) driver.quit();
    }

    private void openHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        openHomePage();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jpetstore"),
                "Homepage title should contain 'JPetStore'");
    }

    @Test
    @Order(2)
    public void testCatalogNavigation() {
        openHomePage();
        WebElement enterStore = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='catalog']")));
        enterStore.click();
        wait.until(ExpectedConditions.urlContains("/catalog/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/"),
                "Should navigate to catalog page");
    }

    @Test
    @Order(3)
    public void testCategoryLinks() {
        openHomePage();
        WebElement enterStore = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='catalog']")));
        enterStore.click();
        wait.until(ExpectedConditions.urlContains("/catalog/"));
        List<WebElement> categories = driver.findElements(By.cssSelector("div#Content a"));
        Assertions.assertFalse(categories.isEmpty(), "Category links should be visible");
        categories.get(0).click();
        wait.until(ExpectedConditions.urlContains("/catalog/categories/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/categories/"),
                "Should navigate to a category page");
    }

    @Test
    @Order(4)
    public void testSearchFunctionality() {
        openHomePage();
        WebElement enterStore = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='catalog']")));
        enterStore.click();
        wait.until(ExpectedConditions.urlContains("/catalog/"));
        WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("keyword")));
        searchBox.clear();
        searchBox.sendKeys("fish");
        searchBox.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.urlContains("/catalog/products/"));
        Assertions.assertTrue(driver.getPageSource().toLowerCase().contains("fish"),
                "Search results should contain 'fish'");
    }

    @Test
    @Order(5)
    public void testLoginWithInvalidCredentials() {
        openHomePage();
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        username.sendKeys("invalidUser");
        password.sendKeys("wrongPass");
        WebElement loginBtn = driver.findElement(By.name("signon"));
        loginBtn.click();
        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".messages li")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid login");
    }

    @Test
    @Order(6)
    public void testRegisterLinkNavigation() {
        openHomePage();
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        WebElement registerNow = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register Now!")));
        registerNow.click();
        wait.until(ExpectedConditions.urlContains("/account/newAccountForm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("newAccountForm"),
                "Should navigate to account registration page");
    }

    @Test
    @Order(7)
    public void testExternalAspectranLink() {
        openHomePage();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='aspectran.com']")));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        link.click();
        wait.until(d -> d.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        driver.switchTo().window(newWindows.iterator().next());
        wait.until(ExpectedConditions.urlContains("aspectran.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"),
                "Aspectran external link should open");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testCartPageAccessible() {
        openHomePage();
        WebElement enterStore = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='catalog']")));
        enterStore.click();
        wait.until(ExpectedConditions.urlContains("/catalog/"));
        WebElement cart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Cart")));
        cart.click();
        wait.until(ExpectedConditions.urlContains("/cart"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart"),
                "Cart page should be accessible");
    }
}
