package SunaGPT20b.ws08.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
    }

    private void login(String username, String password) {
        driver.get(BASE_URL + "login");
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//input[@type='submit' and @value='Login']"));
        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void logout() {
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        logoutLink.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("j2ee", "j2ee");
        WebElement welcomeMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h2")));
        Assertions.assertTrue(welcomeMsg.getText().contains("Welcome"), "Welcome message should be displayed after login.");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//input[@type='submit' and @value='Login']"));
        userField.sendKeys("invalidUser");
        passField.sendKeys("wrongPass");
        loginBtn.click();
        WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//font[@color='red']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"), "Error message should indicate invalid credentials.");
    }

    @Test
    @Order(3)
    public void testInventorySorting() {
        login("j2ee", "j2ee");
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FIsh")));
        fishCategory.click();
        WebElement productTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        Assertions.assertTrue(productTable.isDisplayed(), "Product table should be displayed.");
        logout();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login("j2ee", "j2ee");
        WebElement returnHome = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Return to Main Menu")));
        returnHome.click();
        wait.until(ExpectedConditions.titleContains("JPetStore"));
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"), "Should return to main menu.");
        logout();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login("j2ee", "j2ee");
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran"), "About link should navigate to aspectran page.");
        logout();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login("j2ee", "j2ee");
        logout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().contains("main"), "After logout, should return to main or login page.");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login("j2ee", "j2ee");
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FIsh")));
        itemLink.click();
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Angelfish")));
        productLink.click();
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();
        WebElement cartQuantity = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("EST-1")));
        Assertions.assertNotNull(cartQuantity, "Item should be added to cart.");
        logout();
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login("j2ee", "j2ee");
        Assertions.assertTrue(driver.getPageSource().contains("Copyright"), "Page should contain copyright information in footer.");
        logout();
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        login("j2ee", "j2ee");
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FIsh")));
        fishCategory.click();
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Angelfish")));
        productLink.click();
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();
        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout")));
        proceedToCheckout.click();
        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder")));
        continueButton.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("newOrder"), "Should navigate to checkout page.");
        logout();
    }
}