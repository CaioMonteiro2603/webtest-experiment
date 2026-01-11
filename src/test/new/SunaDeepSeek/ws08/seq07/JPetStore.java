package SunaDeepSeek.ws08.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

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
        Assertions.assertEquals("JPetStore Demo", driver.getTitle());
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(2)
    public void testNavigationToCategories() {
        driver.get(BASE_URL);
        List<WebElement> categoryLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#SidebarContent a")
        ));
        
        for (WebElement categoryLink : categoryLinks) {
            categoryLink.click();
            
            wait.until(ExpectedConditions.urlContains("categories"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("categories"));
            
            WebElement productTable = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Catalog table")
            ));
            Assertions.assertTrue(productTable.isDisplayed());
            
            driver.navigate().back();
        }
    }

    @Test
    @Order(3)
    public void testProductDetailsNavigation() {
        driver.get(BASE_URL);
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")
        ));
        fishCategory.click();
        
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("table tr:nth-child(2) a")
        ));
        firstProduct.click();
        
        wait.until(ExpectedConditions.urlContains("productId="));
        WebElement itemTable = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#Catalog table")
        ));
        Assertions.assertTrue(itemTable.isDisplayed());
    }

    @Test
    @Order(4)
    public void testLoginFunctionality() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")
        ));
        signInLink.click();
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")
        ));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("password")
        ));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='submit'][value*='Login']")
        ));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("main"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#WelcomeContent")
        ));
        Assertions.assertTrue(welcomeMessage.getText().contains(USERNAME));
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")
        ));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("password")
        ));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='submit'][value*='Login']")
        ));
        
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#Content ul li")
        ));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String mainWindow = driver.getWindowHandle();
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")
        ));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(mainWindow);
        
        // Test Aspectran link
        WebElement aspectranLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='aspectran.com']")
        ));
        aspectranLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(7)
    public void testAddToCart() {
        // Login first
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")
        ));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("password")
        ));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='submit'][value*='Login']")
        ));
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Navigate to a product
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")
        ));
        fishCategory.click();
        
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("table tr:nth-child(2) a")
        ));
        firstProduct.click();
        
        // Add to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Add to Cart']")
        ));
        addToCartButton.click();
        
        // Verify cart
        wait.until(ExpectedConditions.urlContains("viewCart"));
        WebElement cartTable = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#Cart table")
        ));
        Assertions.assertTrue(cartTable.isDisplayed());
    }

    @Test
    @Order(8)
    public void testCheckoutProcess() {
        // Login first
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")
        ));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("password")
        ));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='submit'][value*='Login']")
        ));
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Add item to cart
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")
        ));
        fishCategory.click();
        
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("table tr:nth-child(2) a")
        ));
        firstProduct.click();
        
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Add to Cart']")
        ));
        addToCartButton.click();
        
        // Proceed to checkout
        WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Proceed to Checkout']")
        ));
        proceedToCheckout.click();
        
        // Complete order
        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Continue']")
        ));
        continueButton.click();
        
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Confirm']")
        ));
        confirmButton.click();
        
        // Verify order confirmation
        WebElement confirmationMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#Content ul li")
        ));
        Assertions.assertTrue(confirmationMessage.getText().contains("Thank you"));
    }
}