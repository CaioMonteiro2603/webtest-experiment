package SunaQwen3.ws08.seq02;

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
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("JPetStore Demo", driver.getTitle());

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store")));
        signInLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));

        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Account.action?event=signonForm"));

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should display categories like Fish");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?event=signonForm");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        String alertText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-danger"))).getText();
        assertTrue(alertText.contains("Invalid username or password"), "Error message should appear for invalid login");
    }

    @Test
    @Order(3)
    public void testCategoryNavigation() {
        driver.get(BASE_URL + "actions/Catalog.action");

        List<WebElement> categoryLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.category a")));
        assertFalse(categoryLinks.isEmpty(), "At least one category should be present");

        for (WebElement link : categoryLinks) {
            String categoryName = link.getText();
            String href = link.getAttribute("href");

            link.click();
            wait.until(ExpectedConditions.urlContains(href));

            assertTrue(driver.getPageSource().contains(categoryName), "Page should contain category name: " + categoryName);

            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        }
    }

    @Test
    @Order(4)
    public void testProductDetailAndAddToCart() {
        driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.product-detail")));

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCartButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Cart.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Cart.action"), "Should be redirected to cart");

        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td.product-name")));
        assertEquals("FI-SW-01", cartItem.getText(), "Cart should contain the added product ID");
    }

    @Test
    @Order(5)
    public void testCartUpdateAndCheckout() {
        driver.get(BASE_URL + "actions/Cart.action");

        if (driver.findElements(By.cssSelector("td.product-name")).size() == 0) {
            // Ensure cart has an item
            driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");
            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
            addToCartButton.click();
            wait.until(ExpectedConditions.urlContains("/actions/Cart.action"));
        }

        WebElement quantityInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("EST-1.quantity")));
        quantityInput.clear();
        quantityInput.sendKeys("2");

        WebElement updateButton = driver.findElement(By.name("updateCartQuantities"));
        updateButton.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("td.total-price"), "16.50"));

        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        proceedToCheckout.click();

        wait.until(ExpectedConditions.urlContains("/actions/Order.action?newOrderForm="));
        assertTrue(driver.getCurrentUrl().contains("/actions/Order.action?newOrderForm="), "Should be on checkout page");
    }

    @Test
    @Order(6)
    public void testMenuNavigationAndResetAppState() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should reload after reset");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);

        List<WebElement> socialLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("footer a")));
        assertFalse(socialLinks.isEmpty(), "Footer should contain social links");

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String target = link.getAttribute("target");

            if (href != null && (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com"))) {
                link.click();

                wait.until(d -> d.getWindowHandles().size() > 1);
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }

                if (href.contains("twitter.com")) {
                    assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");
                } else if (href.contains("facebook.com")) {
                    assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
                } else if (href.contains("linkedin.com")) {
                    assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
                }

                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(8)
    public void testLogout() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should return to catalog after logout");
        List<WebElement> signInElements = driver.findElements(By.linkText("Sign In"));
        assertEquals(1, signInElements.size(), "Sign In link should be visible after logout");
    }
}