package SunaDeepSeek.ws03.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

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
    public void testLogin() {
        driver.get(BASE_URL);
        
        // Wait for page to load and find elements by placeholder text
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        
        // Verify successful login
        WebElement accountNumber = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".home__ContainerText-sc-1au4j7t-9")));
        Assertions.assertTrue(accountNumber.isDisplayed(), "Account number should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        // Wait for page to load and find elements by placeholder text
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        // Verify modal error message
        WebElement modalButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[class*='btn__secondary']")));
        Assertions.assertTrue(modalButton.isDisplayed(), "Error modal should be displayed for invalid login");
        modalButton.click();
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("toggleMenu")));
        menuButton.click();
        
        // Test Transfer
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='transfer']")));
        transferLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("transfer"), "Should be on transfer page");
        
        // Go back to home
        driver.get(BASE_URL + "home");
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("toggleMenu")));
        menuButton.click();
        
        // Test Statements
        WebElement statementsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='statement']")));
        statementsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("statement"), "Should be on statements page");
    }

    @Test
    @Order(4)
    public void testSocialLinks() {
        login();
        
        // Find and click WhatsApp link
        WebElement whatsappLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='wa.me']")));
        String originalWindow = driver.getWindowHandle();
        whatsappLink.click();
        
        // Wait for new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("wa.me") || driver.getCurrentUrl().contains("whatsapp"), 
            "Should be on WhatsApp domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        login();
        
        // Navigate to statement page to create state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("toggleMenu")));
        menuButton.click();
        WebElement statementsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='statement']")));
        statementsLink.click();
        
        // Verify we are on statements page
        Assertions.assertTrue(driver.getCurrentUrl().contains("statement"), "Should be on statements page");
        
        // Reset by going back to home
        driver.get(BASE_URL + "home");
        
        // Verify we are back on home page
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Should be back on home page");
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".home__ContainerText-sc-1au4j7t-9")));
    }
}