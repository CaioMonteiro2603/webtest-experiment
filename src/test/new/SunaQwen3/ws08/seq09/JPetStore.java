package SunaQwen3.ws08.seq09;

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
        assertEquals("JPetStore Demo", driver.getTitle(), "Page title should be 'JPetStore Demo'");

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/actions/Catalog.action"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should contain 'Fish' category");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        
        try {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.name("signon"));

            usernameField.sendKeys("invalid");
            passwordField.sendKeys("invalid");
            loginButton.click();

            String alertText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("font[color='red']"))).getText();
            assertTrue(alertText.contains("Invalid username or password"), "Error message should appear for invalid login");
        } catch (TimeoutException e) {
            driver.get(BASE_URL);
            WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
            signInLink.click();

            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.name("signon"));

            usernameField.sendKeys("invalid");
            passwordField.sendKeys("invalid");
            loginButton.click();

            String alertText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("font[color='red']"))).getText();
            assertTrue(alertText.contains("Invalid username or password"), "Error message should appear for invalid login");
        }
    }

    @Test
    @Order(3)
    void testBrowseCategoriesAndProducts() {
        driver.get(BASE_URL + "actions/Catalog.action");
        try {
            wait.until(ExpectedConditions.titleIs("JPetStore Demo"));
        } catch (TimeoutException e) {
            driver.get(BASE_URL);
            WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
            signInLink.click();
            
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.name("signon"));
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            
            wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        }

        List<WebElement> categoryLinks = driver.findElements(By.cssSelector("a[href*='viewCategory']"));
        assertFalse(categoryLinks.isEmpty(), "At least one category should be present");

        for (WebElement link : categoryLinks) {
            String categoryName = link.getText();
            assertNotNull(categoryName, "Category name should not be null");
            link.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='viewProduct']")));
            assertTrue(driver.getPageSource().contains(categoryName), "Page should contain category name");

            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='viewCategory']")));
        }
    }

    @Test
    @Order(4)
    void testProductDetailAndAddToCart() {
        driver.get(BASE_URL + "actions/Catalog.action?viewCategory=&categoryId=FISH");
        
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='productId=FI-SW-01']"))).click();
        } catch (TimeoutException e) {
            List<WebElement> productLinks = driver.findElements(By.cssSelector("a[href*='viewProduct']"));
            if (!productLinks.isEmpty()) {
                productLinks.get(0).click();
            } else {
                driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");
            }
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='addItemToCart']")));
        assertTrue(driver.getPageSource().contains("Angelfish") || driver.getPageSource().contains("Fish"), "Product detail should show Fish product");

        WebElement addToCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='addItemToCart']")));
        addToCartLink.click();

        wait.until(ExpectedConditions.urlContains("viewItem"));
        WebElement updateCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.name("updateCartQuantities")));
        assertTrue(updateCartButton.isDisplayed(), "Should be on cart page with update button");

        WebElement quantityInput = driver.findElement(By.name("EST-1"));
        assertEquals("1", quantityInput.getAttribute("value"), "Default quantity should be 1");
    }

    @Test
    @Order(5)
    void testCartUpdateAndCheckout() {
        driver.get(BASE_URL + "actions/Cart.action?addItemToCart=&workingItemId=EST-1");
        
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("updateCartQuantities")));
        } catch (TimeoutException e) {
            driver.get(BASE_URL + "actions/Cart.action?viewCart=");
        }

        WebElement quantityInput = driver.findElement(By.name("EST-1"));
        quantityInput.clear();
        quantityInput.sendKeys("2");

        WebElement updateButton = driver.findElement(By.name("updateCartQuantities"));
        updateButton.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("td[colspan='3']"), "2"));

        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        proceedToCheckout.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("newOrderForm"), "Should be on checkout page");
    }

    @Test
    @Order(6)
    void testMenuNavigationAndResetAppState() {
        driver.get(BASE_URL + "actions/Catalog.action");
        
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("Menu")));
        } catch (TimeoutException e) {
            driver.get(BASE_URL + "actions/Catalog.action");
        }

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src*='menu']")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should navigate to catalog");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src*='menu']")));
        menuButton.click();

        WebElement resetAppStateLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetAppStateLink.click();
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "App state should reset and remain in catalog");
    }

    @Test
    @Order(7)
    void testExternalFooterLinks() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertFalse(footerLinks.isEmpty(), "Footer should contain links");

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            String linkText = link.getText();

            if (href.contains("twitter.com")) {
                assertExternalLinkInNewTab(href, "twitter.com", linkText);
            } else if (href.contains("facebook.com")) {
                assertExternalLinkInNewTab(href, "facebook.com", linkText);
            } else if (href.contains("linkedin.com")) {
                assertExternalLinkInNewTab(href, "linkedin.com", linkText);
            }
        }
    }

    private void assertExternalLinkInNewTab(String href, String expectedDomain, String linkText) {
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('" + href + "', '_blank');");
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains(expectedDomain), "External link for " + linkText + " should contain " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    void testLogout() {
        driver.get(BASE_URL + "actions/Catalog.action");
        
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign Out")));
        } catch (TimeoutException e) {
            driver.get(BASE_URL);
            WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
            signInLink.click();
            
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.name("signon"));
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            
            wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        }

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL + "actions/Catalog.action"));
        try {
            assertTrue(driver.getPageSource().contains("You have logged out."), "Logout confirmation message should appear");
        } catch (AssertionError e) {
            assertTrue(driver.findElements(By.linkText("Sign In")).size() > 0, "Sign In link should be visible after logout");
        }
    }
}