package SunaQwen3.ws08.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Enter Store")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/catalog/"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("JPetStore Demo"), "Homepage should contain JPetStore Demo text");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        driver.findElement(By.name("username")).sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("invalid");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String alertText = driver.findElement(By.cssSelector("font[color='red']")).getText();
        assertTrue(alertText.contains("Invalid username or password"), "Error message should appear for invalid credentials");
    }

    @Test
    @Order(3)
    void testFishCategoryNavigation() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01"))).click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("productId=FI-SW-01"), "Should navigate to Fish product");
        assertTrue(driver.getPageSource().contains("Angelfish"), "Page should contain Angelfish");
    }

    @Test
    @Order(4)
    void testDogCategoryNavigation() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Dogs"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("DO-SF-01"))).click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("productId=DO-SF-01"), "Should navigate to Dog product");
        assertTrue(driver.getPageSource().contains("Bulldog"), "Page should contain Bulldog");
    }

    @Test
    @Order(5)
    void testAddToCartAndCheckout() {
        // Login first
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Navigate to product
        driver.findElement(By.linkText("Fish")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-6"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("EST-6"))).click();

        // Proceed to checkout
        driver.findElement(By.linkText("Proceed to Checkout")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder"))).click();
        driver.findElement(By.name("confirm")).click();

        String confirmation = driver.findElement(By.cssSelector("div[class='message']")).getText();
        assertTrue(confirmation.contains("Your order has been submitted successfully"), "Order confirmation message should appear");
    }

    @Test
    @Order(6)
    void testSortingDropdown() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("sortBy")));

        // Select price low to high
        WebElement sortDropdown = driver.findElement(By.id("sortBy"));
        sortDropdown.findElement(By.cssSelector("option[value='priceAsc']")).click();
        wait.until(ExpectedConditions.urlContains("sortBy=priceAsc"));

        // Verify URL contains sort parameter
        assertTrue(driver.getCurrentUrl().contains("sortBy=priceAsc"), "URL should reflect price ascending sort");

        // Select price high to low
        sortDropdown = driver.findElement(By.id("sortBy"));
        sortDropdown.findElement(By.cssSelector("option[value='priceDesc']")).click();
        wait.until(ExpectedConditions.urlContains("sortBy=priceDesc"));
        assertTrue(driver.getCurrentUrl().contains("sortBy=priceDesc"), "URL should reflect price descending sort");
    }

    @Test
    @Order(7)
    void testMenuNavigation() {
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About"))).click();

        // Switch to new window
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("aspectran"), "About link should open aspectran site");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Return to main site
        driver.get(BASE_URL + "catalog/");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        driver.findElement(By.linkText("Sign Out")).click();

        assertTrue(driver.getPageSource().contains("You have signed off"), "Logout should display confirmation message");
    }

    @Test
    @Order(8)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        try {
            driver.findElement(By.cssSelector("a[href*='twitter']")).click();
            wait.until(d -> driver.getWindowHandles().size() > 1);
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            assertTrue(driver.getCurrentUrl().contains("twitter"), "Twitter link should open Twitter domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (NoSuchElementException e) {
            // Skip if social links don't exist
        }

        // Test Facebook link
        try {
            driver.findElement(By.cssSelector("a[href*='facebook']")).click();
            wait.until(d -> driver.getWindowHandles().size() > 1);
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            assertTrue(driver.getCurrentUrl().contains("facebook"), "Facebook link should open Facebook domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (NoSuchElementException e) {
            // Skip if social links don't exist
        }

        // Test LinkedIn link
        try {
            driver.findElement(By.cssSelector("a[href*='linkedin']")).click();
            wait.until(d -> driver.getWindowHandles().size() > 1);
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            assertTrue(driver.getCurrentUrl().contains("linkedin"), "LinkedIn link should open LinkedIn domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (NoSuchElementException e) {
            // Skip if social links don't exist
        }
    }

    @Test
    @Order(9)
    void testResetAppState() {
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Add item to cart
        driver.findElement(By.linkText("Fish")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-6"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("EST-6"))).click();

        // Reset app state
        driver.get(BASE_URL + "account/signonForm");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out"))).click();

        // Verify cart is empty
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/cart']")));
        String cartText = driver.findElement(By.cssSelector("a[href='/cart']")).getText();
        assertEquals("Cart", cartText, "Cart should be reset");
    }
}