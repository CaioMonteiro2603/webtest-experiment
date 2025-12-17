package geminiPro.ws08.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for the JPetStore demo application.
 * This suite covers navigation, search, login, and a full end-to-end checkout flow.
 * It uses Selenium WebDriver with headless Firefox.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/catalog/";
    private static final String VALID_USERNAME = "j2ee";
    private static final String VALID_PASSWORD = "j2ee";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private final By signInLink = By.linkText("Sign In");
    private final By signOutLink = By.linkText("Sign Out");
    private final By usernameInput = By.name("username");
    private final By passwordInput = By.name("password");
    private final By loginButton = By.name("signon");
    private final By welcomeContent = By.id("WelcomeContent");
    private final By searchInput = By.name("keyword");
    private final By searchButton = By.name("searchProducts");
    private final By myBatisLink = By.cssSelector("#MainImage a");
    private final By errorMessage = By.cssSelector(".messages li");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
    }
    
    @AfterEach
    void signOutIfLoggedIn() {
        try {
            if (driver.findElements(signOutLink).size() > 0) {
                driver.findElement(signOutLink).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(signInLink));
            }
        } catch (NoSuchElementException e) {
            // User is not logged in, which is fine.
        }
    }

    @Test
    @Order(1)
    void testCategoryNavigation() {
        // Test navigation via side bar links
        navigateToCategoryAndAssertTitle("Fish", "Fish");
        navigateToCategoryAndAssertTitle("Dogs", "Dogs");
        navigateToCategoryAndAssertTitle("Cats", "Cats");
        navigateToCategoryAndAssertTitle("Reptiles", "Reptiles");
        navigateToCategoryAndAssertTitle("Birds", "Birds");
    }

    @Test
    @Order(2)
    void testSearchForProduct() {
        driver.findElement(searchInput).sendKeys("Bulldog");
        driver.findElement(searchButton).click();
        
        wait.until(ExpectedConditions.titleIs("JPetStore Demo"));
        WebElement productLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Bulldog")));
        assertTrue(productLink.isDisplayed(), "Search result for 'Bulldog' should be displayed.");
    }
    
    @Test
    @Order(3)
    void testInvalidLogin() {
        driver.findElement(signInLink).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
        
        driver.findElement(usernameInput).sendKeys("invalidUser");
        driver.findElement(passwordInput).sendKeys("wrongPassword");
        driver.findElement(loginButton).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        assertTrue(error.getText().contains("Invalid username or password."), "Error message for invalid login is incorrect.");
    }

    @Test
    @Order(4)
    void testFullCheckoutFlow() {
        // 1. Login
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        
        // 2. Navigate and add item to cart
        navigateToCategoryAndAssertTitle("Dogs", "Dogs");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-BD-01"))).click(); // Bulldog
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-6"))).click(); // Male Adult Bulldog
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        
        // 3. Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();

        // 4. Continue through payment and confirmation
        wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Confirm"))).click();
        
        // 5. Assert order completion
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        assertTrue(successMessage.getText().contains("Thank you, your order has been submitted."), "Order confirmation message not found.");
    }

    @Test
    @Order(5)
    void testUpdateCartQuantity() {
        // 1. Add an item to the cart
        navigateToCategoryAndAssertTitle("Reptiles", "Reptiles");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("RP-LI-02"))).click(); // Iguana
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-11"))).click(); // Male Iguana
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        
        // 2. Go to cart and update quantity
        WebElement quantityInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("EST-11")));
        quantityInput.clear();
        quantityInput.sendKeys("3");
        driver.findElement(By.name("updateCartQuantities")).click();

        // 3. Assert total cost is updated
        WebElement subTotal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(), 'Sub Total:')]")));
        // Price of Iguana is $18.50. 18.50 * 3 = 55.50
        assertTrue(subTotal.getText().contains("$55.50"), "Subtotal did not update correctly after changing quantity.");
    }
    
    @Test
    @Order(6)
    void testMyBatisExternalLink() {
        handleExternalLink(driver.findElement(myBatisLink), "mybatis.org");
    }

    // --- Helper Methods ---
    
    private void performLogin(String username, String password) {
        driver.findElement(signInLink).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
        driver.findElement(usernameInput).sendKeys(username);
        // The password field name is 'password' but it's inside a form with name='signon'
        driver.findElement(By.cssSelector("form[name='signon'] input[name='password']")).clear();
        driver.findElement(By.cssSelector("form[name='signon'] input[name='password']")).sendKeys(password);
        driver.findElement(loginButton).click();
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeContent));
        assertTrue(welcome.getText().contains("Welcome"), "Login was not successful.");
    }
    
    private void navigateToCategoryAndAssertTitle(String categoryName, String expectedTitle) {
        // XPath to find the image link for a category in the side menu
        String categoryXPath = String.format("//div[@id='Content']//a[img[contains(@src, '%s_icon.gif')]]", categoryName);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(categoryXPath))).click();
        wait.until(ExpectedConditions.titleIs("JPetStore Demo"));
        WebElement header = driver.findElement(By.tagName("h2"));
        assertEquals(expectedTitle, header.getText(), "Header for category " + categoryName + " is incorrect.");
    }
    
    private void handleExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(linkElement)).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        
        if (newWindow == null) {
            fail("New window did not open for link with expected domain: " + expectedDomain);
        }
        
        driver.switchTo().window(newWindow);
        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);
        driver.close();
        
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        assertEquals("JPetStore Demo", driver.getTitle(), "Should have returned to the JPetStore page.");
    }
}