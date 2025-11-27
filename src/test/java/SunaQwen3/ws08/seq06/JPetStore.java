package SunaQwen3.ws08.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

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
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"), "Page title should contain JPetStore");

        WebElement signInLink = wait.until(elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(visibilityOfElementLocated(By.xpath("//h3[text()[contains(.,'Welcome')]]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Welcome message should be displayed after login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/"), "URL should contain /catalog/ after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        WebElement errorMessage = wait.until(visibilityOfElementLocated(By.className("messages")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"), "Error message should appear for invalid credentials");
    }

    @Test
    @Order(3)
    public void testBrowseCategoriesAndProducts() {
        driver.get(BASE_URL + "actions/Catalog.action");
        wait.until(visibilityOfElementLocated(By.className("product")));

        List<WebElement> productLinks = driver.findElements(By.cssSelector(".product a"));
        Assertions.assertTrue(productLinks.size() > 0, "At least one product should be listed");

        // Click on the first product
        String firstProductName = productLinks.get(0).getText();
        productLinks.get(0).click();

        WebElement productDetailName = wait.until(visibilityOfElementLocated(By.tagName("h2")));
        Assertions.assertEquals(firstProductName, productDetailName.getText(), "Product name should match");
    }

    @Test
    @Order(4)
    public void testAddToCartAndCheckout() {
        driver.get(BASE_URL + "actions/Catalog.action");
        wait.until(visibilityOfElementLocated(By.className("product")));

        // Click on first product
        driver.findElements(By.cssSelector(".product a")).get(0).click();
        wait.until(visibilityOfElementLocated(By.name("EST-6")));

        // Add to cart
        WebElement addToCartButton = wait.until(elementToBeClickable(By.name("EST-6")));
        addToCartButton.click();

        // Proceed to cart
        WebElement cartLink = wait.until(elementToBeClickable(By.linkText("Proceed to Checkout")));
        cartLink.click();

        // Continue as guest
        WebElement continueButton = wait.until(elementToBeClickable(By.name("newOrderForm")));
        continueButton.submit();

        // Fill billing info
        WebElement firstName = wait.until(visibilityOfElementLocated(By.name("order.billToFirstName")));
        firstName.sendKeys("John");
        driver.findElement(By.name("order.billToLastName")).sendKeys("Doe");
        driver.findElement(By.name("order.billAddress1")).sendKeys("123 Main St");
        driver.findElement(By.name("order.billAddress2")).sendKeys("Apt 4");
        driver.findElement(By.name("order.billCity")).sendKeys("New York");
        driver.findElement(By.name("order.billState")).sendKeys("NY");
        driver.findElement(By.name("order.billZip")).sendKeys("10001");
        driver.findElement(By.name("order.billCountry")).sendKeys("USA");

        // Continue to shipping
        WebElement continueToShipping = driver.findElement(By.name("newOrderForm"));
        continueToShipping.submit();

        // Continue to confirm
        WebElement continueToConfirm = wait.until(elementToBeClickable(By.name("shippingAddressRequired")));
        continueToConfirm.submit();

        // Confirm order
        WebElement confirmButton = wait.until(elementToBeClickable(By.name("confirmOrder")));
        confirmButton.click();

        // Verify order success
        WebElement successMessage = wait.until(visibilityOfElementLocated(By.className("messages")));
        Assertions.assertTrue(successMessage.getText().contains("Thank you"), "Order confirmation message should appear");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        wait.until(visibilityOfElementLocated(By.tagName("footer")));

        // Find all footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.startsWith("mailto:")) {
                continue;
            }

            String originalWindow = driver.getWindowHandle();
            link.click();

            // Wait for new window
            wait.until(numberOfWindowsToBe(2));

            // Switch to new window
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            if (href.contains("twitter.com")) {
                Assertions.assertTrue(currentUrl.contains("twitter.com"), "Twitter link should open correct domain");
            } else if (href.contains("facebook.com")) {
                Assertions.assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (href.contains("linkedin.com")) {
                Assertions.assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            }

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(elementToBeClickable(By.name("keyword")));
        searchInput.clear();
        searchInput.sendKeys("Goldfish");
        searchInput.submit();

        WebElement searchResults = wait.until(visibilityOfElementLocated(By.className("product")));
        List<WebElement> results = driver.findElements(By.className("product"));
        Assertions.assertTrue(results.size() > 0, "Search should return at least one result for 'Goldfish'");
    }

    @Test
    @Order(7)
    public void testLogout() {
        // Ensure logged in
        driver.get(BASE_URL + "actions/Catalog.action");
        wait.until(visibilityOfElementLocated(By.linkText("Sign Out")));

        WebElement logoutLink = wait.until(elementToBeClickable(By.linkText("Sign Out")));
        logoutLink.click();

        WebElement signInLink = wait.until(elementToBeClickable(By.linkText("Sign In")));
        Assertions.assertTrue(signInLink.isDisplayed(), "Sign In link should be visible after logout");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should return to catalog after logout");
    }
}