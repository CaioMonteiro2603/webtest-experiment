package SunaDeepSeek.ws08.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStoreTest {
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
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
        
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='signonForm']")));
        signInLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("signonForm"));
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        WebElement welcomeMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMsg.getText().contains(USERNAME));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();
        
        WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".messages li")));
        Assertions.assertTrue(errorMsg.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(4)
    public void testCategoryNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='signonForm']"))).click();
        login();
        
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#SidebarContent a")));
        
        for (WebElement category : categories) {
            String categoryName = category.getText();
            category.click();
            
            WebElement categoryHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Catalog h2")));
            Assertions.assertTrue(categoryHeader.getText().contains(categoryName));
            
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#SidebarContent a")));
        }
    }

    @Test
    @Order(5)
    public void testProductNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='signonForm']"))).click();
        login();
        
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")));
        fishCategory.click();
        
        List<WebElement> products = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#Catalog table a")));
        
        for (int i = 0; i < Math.min(2, products.size()); i++) {
            String productName = products.get(i).getText();
            products.get(i).click();
            
            WebElement productHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#Catalog h2")));
            Assertions.assertTrue(productHeader.getText().contains(productName));
            
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#Catalog table a")));
            products = driver.findElements(By.cssSelector("#Catalog table a"));
        }
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        
        // Test Facebook link
        testExternalLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(7)
    public void testLogout() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='signonForm']"))).click();
        login();
        
        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='signoff']")));
        signOutLink.click();
        
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='signonForm']")));
        Assertions.assertTrue(signInLink.isDisplayed());
    }

    private void login() {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#WelcomeContent")));
    }
}