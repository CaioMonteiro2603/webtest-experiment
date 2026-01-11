package SunaDeepSeek.ws08.seq06;

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
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JPetStore Demo"));

        // Verify main categories
        List<WebElement> categories = wait.until(
            ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div#MainImageContent area")));
        Assertions.assertEquals(6, categories.size(), "Should have 6 main categories");
    }

    @Test
    @Order(2)
    public void testFishCategoryNavigation() {
        driver.get(BASE_URL);
        WebElement fishCategory = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#MainImageContent area[alt='Fish']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fishCategory);

        wait.until(ExpectedConditions.urlContains("categoryId=FISH"));
        Assertions.assertTrue(driver.getPageSource().contains("Fish"), "Fish category page should load");
    }

    @Test
    @Order(3)
    public void testProductDetailsPage() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.linkText("FI-SW-01")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", productLink);

        wait.until(ExpectedConditions.urlContains("productId=FI-SW-01"));
        Assertions.assertTrue(driver.getPageSource().contains("Koi"), "Product details should show Koi");
    }

    @Test
    @Order(4)
    public void testItemPageAndAddToCart() {
        driver.get(BASE_URL + "catalog/products/FI-SW-01");
        WebElement itemLink = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.linkText("EST-1")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", itemLink);

        wait.until(ExpectedConditions.urlContains("itemId=EST-1"));
        WebElement addToCartBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.name("addToCart")));
        addToCartBtn.click();

        WebElement cartMessage = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.Message")));
        Assertions.assertTrue(cartMessage.getText().contains("added to your shopping cart"), 
            "Item should be added to cart");
    }

    @Test
    @Order(5)
    public void testCartOperations() {
        driver.get(BASE_URL + "cart/viewCart");
        WebElement removeBtn = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.name("remove")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", removeBtn);

        WebElement emptyCartMsg = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.Message")));
        Assertions.assertTrue(emptyCartMsg.getText().contains("Your cart is empty"), 
            "Cart should be empty after removal");
    }

    @Test
    @Order(6)
    public void testSignInAndOut() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        username.sendKeys("j2ee");

        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("j2ee");

        WebElement signInBtn = driver.findElement(By.cssSelector("input[type='submit']"));
        signInBtn.click();

        wait.until(ExpectedConditions.urlContains("main"));
        WebElement welcomeMsg = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#WelcomeContent")));
        Assertions.assertTrue(welcomeMsg.getText().contains("Welcome"), "Should show welcome message");

        WebElement signOutLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        wait.until(ExpectedConditions.urlContains("signonForm"));
        Assertions.assertTrue(driver.getPageSource().contains("Please enter your username and password"), 
            "Should return to sign in page");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='twitter.com']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", twitterLink);

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
            "Should open Twitter in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='facebook.com']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", facebookLink);

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), 
            "Should open Facebook in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.sendKeys("fish");

        WebElement searchBtn = driver.findElement(By.cssSelector("input[type='submit']"));
        searchBtn.click();

        wait.until(ExpectedConditions.urlContains("keyword=fish"));
        List<WebElement> products = wait.until(
            ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table[width='80%'] tr")));
        Assertions.assertTrue(products.size() > 1, "Should find multiple fish products");
    }
}