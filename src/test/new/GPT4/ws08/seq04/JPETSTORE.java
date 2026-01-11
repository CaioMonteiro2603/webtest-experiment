package GPT4.ws08.seq04;

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
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Welcome")));
        Assertions.assertTrue(welcome.getText().contains("Welcome"), "Welcome message not found");
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"), "Page title incorrect");
    }

    @Test
    @Order(2)
    public void testEnterStoreLink() {
        driver.get(BASE_URL);
        WebElement enterStoreLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store")));
        enterStoreLink.click();
        wait.until(ExpectedConditions.urlContains("/catalog"));
        WebElement quickLinks = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("QuickLinks")));
        Assertions.assertTrue(quickLinks.isDisplayed(), "QuickLinks not visible");
    }

    @Test
    @Order(3)
    public void testSignInValidCredentials() {
        driver.get(BASE_URL + "catalog");
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        username.sendKeys("j2ee");
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("j2ee");
        WebElement loginBtn = driver.findElement(By.name("signon"));
        loginBtn.click();
        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Welcome")));
        Assertions.assertTrue(greeting.getText().contains("Welcome"), "Login failed or greeting not shown");
    }

    @Test
    @Order(4)
    public void testSignInInvalidCredentials() {
        driver.get(BASE_URL + "catalog");
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        username.sendKeys("invalidUser");
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("wrongPass");
        WebElement loginBtn = driver.findElement(By.name("signon"));
        loginBtn.click();
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(message.getText().contains("invalid"), "Expected invalid login message");
    }

    @Test
    @Order(5)
    public void testCategoryLinks() {
        driver.get(BASE_URL + "catalog");
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#SidebarContent a")));
        Assertions.assertTrue(categories.size() > 0, "No categories found");
        WebElement firstCategory = categories.get(0);
        String categoryName = firstCategory.getText();
        firstCategory.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        WebElement heading = driver.findElement(By.cssSelector("h2"));
        Assertions.assertTrue(heading.getText().contains(categoryName), "Category page did not load correctly");
    }

    @Test
    @Order(6)
    public void testExternalLinkOpensCorrectly() {
        driver.get(BASE_URL);
        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='aspectran']")));
        String originalWindow = driver.getWindowHandle();
        externalLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("aspectran"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran"), "External link did not open correctly");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL + "catalog");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("keyword")));
        searchInput.clear();
        searchInput.sendKeys("fish");
        WebElement searchBtn = driver.findElement(By.name("searchProducts"));
        searchBtn.click();
        wait.until(ExpectedConditions.urlContains("search"));
        WebElement resultTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Catalog table")));
        Assertions.assertTrue(resultTable.isDisplayed(), "Search results not visible");
    }

    @Test
    @Order(8)
    public void testAddItemToCart() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='FI-SW-01']")));
        itemLink.click();
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Cart table")));
        List<WebElement> rows = cartTable.findElements(By.tagName("tr"));
        Assertions.assertTrue(rows.size() > 1, "Item was not added to cart");
    }

    @Test
    @Order(9)
    public void testRemoveItemFromCart() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='FI-SW-01']")));
        itemLink.click();
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();
        WebElement qtyInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("quantity")));
        qtyInput.clear();
        qtyInput.sendKeys("0");
        WebElement updateBtn = driver.findElement(By.name("updateCartQuantities"));
        updateBtn.click();
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Cart")));
        Assertions.assertTrue(message.getText().contains("empty"), "Cart was not emptied after removing item");
    }

    @Test
    @Order(10)
    public void testSignOut() {
        driver.get(BASE_URL + "catalog");
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("/account/signonForm"));
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        username.sendKeys("j2ee");
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("j2ee");
        WebElement loginBtn = driver.findElement(By.name("signon"));
        loginBtn.click();
        WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOut.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign In")));
        Assertions.assertTrue(driver.getPageSource().contains("Sign In"), "Sign out failed");
    }
}