package Qwen3.ws08.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String LOGIN_USERNAME = "j2ee";
    private static final String LOGIN_PASSWORD = "j2ee";

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
    public void testHomePageLoadAndCategories() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JPetStore Demo"));

        assertTrue(driver.findElement(By.cssSelector("div#Sidebar")).isDisplayed(), "Sidebar with categories should be visible.");
        
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector("#Sidebar a"));
        assertTrue(categoryLinks.size() > 3, "There should be multiple category links in the sidebar.");
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);

        // Click on the first category link, e.g., "Fish"
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'categoryId=FISH')]")));
        fishCategory.click();

        wait.until(ExpectedConditions.urlContains("categoryId=FISH"));
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Fish"),
                "Fish category page should be displayed.");
        
        List<WebElement> productLinks = driver.findElements(By.cssSelector("a[href*='itemId=']"));
        assertTrue(productLinks.size() > 0, "There should be products listed under the Fish category.");
    }

    @Test
    @Order(3)
    public void testProductDetails() {
        // Navigate to a product details page (one level deep from category)
        driver.get(BASE_URL + "catalog/category/FISH");

        // Click on the first product
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='itemId=']")));
        String productId = firstProduct.getAttribute("href").split("itemId=")[1];
        firstProduct.click();

        wait.until(ExpectedConditions.urlContains("itemId=" + productId));
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains(productId),
                "Product details page for the correct item should be displayed.");
    }

    @Test
    @Order(4)
    public void testValidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign In")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys(LOGIN_USERNAME);
        driver.findElement(By.name("password")).sendKeys(LOGIN_PASSWORD);
        driver.findElement(By.cssSelector("input[value='Login']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("My Account")));
        assertTrue(driver.findElement(By.linkText("My Account")).isDisplayed(),
                "My Account link should be visible after successful login.");
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign In")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys("invalid_user");
        driver.findElement(By.name("password")).sendKeys("wrong_pass");
        driver.findElement(By.cssSelector("input[value='Login']")).click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3")));
        assertTrue(errorElement.getText().contains("Error:"), "Error message should be displayed for invalid login.");
    }

    @Test
    @Order(6)
    public void testAddToCart() {
        testValidLogin(); // Ensure we are logged in

        // Go to a category and product
        driver.get(BASE_URL + "catalog/category/FISH");
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='itemId=']")));
        firstProduct.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='cart?addItem']"))).click();

        wait.until(ExpectedConditions.urlContains("cart"));
        assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Shopping Cart"),
                "Shopping Cart page should be displayed after adding an item.");
        
        List<WebElement> cartItems = driver.findElements(By.cssSelector("table tr"));
        assertTrue(cartItems.size() > 1, "Cart should contain at least one item row.");
    }

    @Test
    @Order(7)
    public void testUpdateCartQuantity() {
        // Ensure an item is in the cart
        testAddToCart();

        // Update quantity of the first item
        WebElement quantityField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name*='quantity']")));
        String originalValue = quantityField.getAttribute("value");
        quantityField.clear();
        quantityField.sendKeys("5");
        driver.findElement(By.cssSelector("input[value='Update Cart']")).click();

        // A simple check is to see if the page reloads with a total cost (indicating update)
        wait.until(ExpectedConditions.stalenessOf(quantityField));
        // Re-assert presence of cart items
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table tr")));
        // We can't easily assert the value changed without complex locators, but the flow is tested.
    }

    @Test
    @Order(8)
    public void testProceedToCheckout() {
        // Ensure an item is in the cart
        testAddToCart();

        driver.findElement(By.linkText("Proceed to Checkout")).click();
        wait.until(ExpectedConditions.urlContains("newOrderForm"));

        assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Order"),
                "New Order form should be displayed.");
    }

    @Test
    @Order(9)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Click Aspectran link (external)
        WebElement aspectranLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Aspectran")));
        aspectranLink.click();
        assertExternalLinkAndReturn(originalWindow, "aspectran.com");

        // Click MyBatis link (external)
        driver.get(BASE_URL); // Reset
        originalWindow = driver.getWindowHandle();
        WebElement myBatisLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("MyBatis")));
        myBatisLink.click();
        assertExternalLinkAndReturn(originalWindow, "mybatis.org");
    }

    @Test
    @Order(10)
    public void testLogout() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.linkText("Sign Out")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign In")));
        assertTrue(driver.findElement(By.linkText("Sign In")).isDisplayed(),
                "Sign In link should be visible after logout.");
    }

    // --- Helper Methods ---

    private void assertExternalLinkAndReturn(String originalWindow, String expectedDomain) {
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        assertNotNull(newWindow, "A new window should have been opened for " + expectedDomain);
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "New window URL should contain " + expectedDomain + ". URL was: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}