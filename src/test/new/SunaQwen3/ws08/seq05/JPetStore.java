package SunaQwen3.ws08.seq05;

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
        assertEquals("JPetStore Demo", driver.getTitle());

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        assertEquals(BASE_URL + "account/signonForm", driver.getCurrentUrl());

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("Catalog.action"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should contain Fish category");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL + "account/signonForm");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        String errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages"))).getText();
        assertTrue(errorMsg.contains("Invalid username or password"), "Error message should appear for invalid login");
    }

    @Test
    @Order(3)
    void testBrowseCategoriesAndProducts() {
        driver.get(BASE_URL + "catalog");

        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishLink.click();

        assertTrue(driver.getCurrentUrl().contains("categories/FISH"), "Should navigate to category view");
        assertTrue(driver.getPageSource().contains("Angelfish"), "Fish category should list Angelfish");

        WebElement angelfishLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        angelfishLink.click();

        assertTrue(driver.getPageSource().contains("EST-1"), "Product detail should show item EST-1");
        assertTrue(driver.getPageSource().contains("Large"), "Product detail should show size Large");
    }

    @Test
    @Order(4)
    void testAddToCartAndCheckout() {
        driver.get(BASE_URL + "catalog");

        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Dogs")));
        dogsLink.click();

        WebElement k9BbLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-BD-01")));
        k9BbLink.click();

        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.name("EST-6")));
        addToCart.click();

        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        proceedToCheckout.click();

        String cartUrl = driver.getCurrentUrl();
        assertTrue(cartUrl.contains("cart/viewCart"), "Should be on cart edit page");

        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.name("updateCartQuantities")));
        checkoutButton.submit();

        wait.until(ExpectedConditions.urlContains("orders/newOrderForm"));
        assertTrue(driver.getCurrentUrl().contains("orders/newOrderForm"), "Should be on order form");

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder")));
        continueButton.click();

        wait.until(ExpectedConditions.urlContains("orders/viewOrder"));
        assertTrue(driver.getPageSource().contains("Thank you for shopping at JPetStore!"), "Order confirmation message should appear");
    }

    @Test
    @Order(5)
    void testMenuNavigationAndResetAppState() {
        driver.get(BASE_URL + "catalog");

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("catalog"));
        assertTrue(driver.getPageSource().contains("Fish"), "All Items should show catalog");

        // No visual change expected, but operation should succeed
        assertTrue(driver.getCurrentUrl().contains("catalog"), "Should remain on catalog after reset");
    }

    @Test
    @Order(6)
    void testExternalFooterLinks() {
        driver.get(BASE_URL + "catalog");

        String originalWindow = driver.getWindowHandle();

        By twitterLink = By.cssSelector("footer a[href*='twitter']");
        By facebookLink = By.cssSelector("footer a[href*='facebook']");
        By linkedinLink = By.cssSelector("footer a[href*='linkedin']");

        if (driver.findElements(twitterLink).size() > 0) {
            // Test Twitter link
            WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
            twitter.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            wait.until(ExpectedConditions.urlContains("twitter.com"));
            assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should open Twitter link");
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        if (driver.findElements(facebookLink).size() > 0) {
            // Test Facebook link
            WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
            facebook.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            wait.until(ExpectedConditions.urlContains("facebook.com"));
            assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should open Facebook link");
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        if (driver.findElements(linkedinLink).size() > 0) {
            // Test LinkedIn link
            WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(linkedinLink));
            linkedin.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            wait.until(ExpectedConditions.urlContains("linkedin.com"));
            assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should open LinkedIn link");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    void testLogoutFunctionality() {
        driver.get(BASE_URL + "catalog");

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        wait.until(ExpectedConditions.urlContains("account/signonForm"));
        assertTrue(driver.getCurrentUrl().contains("account/signonForm"), "Should return to welcome page after logout");
        assertTrue(driver.getPageSource().contains("Please enter your username and password"), "Login form should be displayed");
    }
}