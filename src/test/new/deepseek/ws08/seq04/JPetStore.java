package deepseek.ws08.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@id='Logo']")));
        Assertions.assertTrue(logo.isDisplayed());
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.name("username")));
        usernameField.sendKeys(USERNAME);
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);
        
        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(text(), 'Welcome')]")));
        Assertions.assertTrue(welcomeMessage.getText().contains(USERNAME));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.name("username")));
        usernameField.sendKeys("invalid");
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("credentials");
        
        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//ul/li[contains(text(), 'Invalid')]")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"));
    }

    @Test
    @Order(4)
    public void testCategoryNavigation() {
        login();
        
        // Test Fish category
        testCategoryLink("Fish", "FI-FW-01", "Goldfish");
        // Test Dogs category
        testCategoryLink("Dogs", "K9-BD-01", "Bulldog");
        // Test Reptiles category
        testCategoryLink("Reptiles", "RP-SN-01", "Rattlesnake");
    }

    private void testCategoryLink(String categoryName, String expectedItemId, String expectedItemName) {
        driver.get(BASE_URL);
        WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@id='QuickLinks']//a[contains(text(),'" + categoryName + "')]")));
        categoryLink.click();
        
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'viewProduct=&productId=" + expectedItemId + "')]")));
        productLink.click();
        
        WebElement itemName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h2[contains(text(), '" + expectedItemName + "')]")));
        Assertions.assertTrue(itemName.isDisplayed());
        
        driver.get(BASE_URL); // Return to home
    }

    @Test
    @Order(5)
    public void testAddToCart() {
        login();
        
        // Navigate to specific product
        driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");
        
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'addItemToCart=&workingItemId=EST-6')]")));
        addToCartButton.click();
        
        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//tr[contains(@class, 'CartItem')]")));
        Assertions.assertTrue(cartItem.isDisplayed());
        
        // Remove from cart
        WebElement removeButton = driver.findElement(By.name("removeItem"));
        removeButton.click();
    }

    @Test
    @Order(6)
    public void testSearchFunctionality() {
        login();
        
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.name("keyword")));
        searchInput.sendKeys("fish");
        
        WebElement searchButton = driver.findElement(By.name("searchProducts"));
        searchButton.click();
        
        List<WebElement> products = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.xpath("//table//a[contains(@href, 'viewProduct=')]"), 0));
        Assertions.assertTrue(products.size() > 0);
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test About Us link
        testFooterLink("About Us", "about");
        // Test Contact Us link
        testFooterLink("Contact Us", "contact");
    }

    private void testFooterLink(String linkText, String expectedUrlPart) {
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@id='Footer']//a[contains(text(),'" + linkText + "')]")));
        footerLink.click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        driver.get(BASE_URL); // Return to home
    }

    @Test
    @Order(8)
    public void testLogout() {
        login();
        
        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sign Out')]")));
        signOutLink.click();
        
        WebElement signInLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//a[contains(text(), 'Sign In')]")));
        Assertions.assertTrue(signInLink.isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        if (driver.findElements(By.name("signon")).size() > 0) {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.name("username")));
            usernameField.sendKeys(USERNAME);
            
            WebElement passwordField = driver.findElement(By.name("password"));
            passwordField.sendKeys(PASSWORD);
            
            WebElement loginButton = driver.findElement(By.name("signon"));
            loginButton.click();
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(text(), 'Welcome')]")));
        }
    }
}