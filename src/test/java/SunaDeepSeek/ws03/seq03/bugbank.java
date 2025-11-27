package SunaDeepSeek.ws03.seq03;

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
public class BugBankWebTest {
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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("welcome")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Olá"), "Login failed - welcome message not displayed");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"), 
            "Expected invalid credentials error message not displayed");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        // First login
        driver.get(BASE_URL);
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "All Items navigation failed");
        
        // Test About (external)
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About page not opened in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout failed");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        login();
        
        // Twitter
        testExternalLink("social_twitter", "twitter.com");
        
        // Facebook
        testExternalLink("social_facebook", "facebook.com");
        
        // LinkedIn
        testExternalLink("social_linkedin", "linkedin.com");
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        driver.get(BASE_URL);
        login();
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify reset by checking if cart is empty
        List<WebElement> cartItems = driver.findElements(By.className("cart_item"));
        Assertions.assertEquals(0, cartItems.size(), "App state was not reset properly");
    }

    private void login() {
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("welcome")));
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link " + linkId + " did not navigate to expected domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}