package SunaQwen3.ws08.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
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
        assertEquals("JPetStore Demo", driver.getTitle());

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should contain Fish category");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        String alertText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("font[color='red']"))).getText();
        assertTrue(alertText.contains("Invalid username or password"), "Error message should appear for invalid login");
    }

    @Test
    @Order(3)
    void testNavigationToFishCategory() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        fishLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("EST-1")));
        assertTrue(driver.getCurrentUrl().contains("productId=FI-SW-01"), "Should navigate to Fish product details");
        assertTrue(driver.getPageSource().contains("Angelfish"), "Product details should include Angelfish");
    }

    @Test
    @Order(4)
    void testAddItemToCart() {
        driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");

        WebElement addToCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCartLink.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.name("EST-1")));
        WebElement quantityField = driver.findElement(By.name("EST-1"));
        quantityField.clear();
        quantityField.sendKeys("2");

        WebElement updateCartButton = driver.findElement(By.name("updateCartQuantities"));
        updateCartButton.click();

        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td > a[href*='EST-1']")));
        assertEquals("EST-1", cartItem.getText(), "Cart should contain item EST-1");
        assertEquals("2", driver.findElement(By.name("EST-1")).getAttribute("value"), "Quantity should be updated to 2");
    }

    @Test
    @Order(5)
    void testProceedToCheckout() {
        driver.get(BASE_URL + "actions/Order.action?viewCart=");
        
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='newOrderForm']")));
        checkoutButton.click();

        wait.until(ExpectedConditions.urlContains("newOrderForm"));
        assertTrue(driver.getCurrentUrl().contains("newOrderForm"), "Should be on checkout page");

        // Fill in shipping info (minimal required)
        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit']")));
        continueButton.click();

        // Confirm order
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Confirm']")));
        confirmButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        String confirmationHeader = driver.findElement(By.cssSelector("h2")).getText();
        assertEquals("Thank you for shopping at JPetStore!", confirmationHeader, "Order confirmation message should appear");
    }

    @Test
    @Order(6)
    void testBrowseDogsCategory() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("DO-LB-02")));
        dogsLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("EST-6")));
        assertTrue(driver.getCurrentUrl().contains("productId=DO-LB-02"), "Should navigate to Dogs product details");
        assertTrue(driver.getPageSource().contains("Bulldog"), "Product details should include Bulldog");
    }

    @Test
    @Order(7)
    void testExternalAboutLink() {
        driver.get(BASE_URL + "actions/Catalog.action");

        String originalWindow = driver.getWindowHandle();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("aspectran.com"), "About link should open aspectran.com domain");

        // Close external tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.footer")));

        // Test Twitter link
        testExternalLinkInNewTab(By.cssSelector("a[href*='twitter.com']"), "twitter.com");

        // Test Facebook link
        testExternalLinkInNewTab(By.cssSelector("a[href*='facebook.com']"), "facebook.com");

        // Test LinkedIn link
        testExternalLinkInNewTab(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    private void testExternalLinkInNewTab(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String newWindow = driver.getWindowHandles().stream()
                .filter(handle -> !handle.equals(originalWindow))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new window opened"));

        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), expectedDomain + " link should open correct domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    void testLogoutFunctionality() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should return to catalog after logout");
        List<WebElement> signInLinks = driver.findElements(By.linkText("Sign In"));
        assertTrue(signInLinks.size() > 0, "Sign In link should appear after logout");
    }

    @Test
    @Order(10)
    void testSearchFunctionality() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement searchField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchField.sendKeys("Bulldog");
        searchField.submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Bulldog")));
        assertTrue(driver.getPageSource().contains("Bulldog"), "Search results should contain Bulldog");
        assertTrue(driver.getCurrentUrl().contains("keyword=Bulldog"), "URL should reflect search keyword");
    }
}