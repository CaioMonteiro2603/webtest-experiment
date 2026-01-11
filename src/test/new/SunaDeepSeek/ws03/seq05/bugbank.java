package SunaDeepSeek.ws03.seq05;

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".home__text")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Olá"), "Login successful message should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".modal-text")));
        Assertions.assertTrue(errorMessage.getText().contains("Email e/ou senha inválidos"), "Error message for invalid login should be displayed");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExit")));
        menuButton.click();

        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-home")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Should be on home page after clicking All Items");

        // Open menu again
        menuButton.click();

        // Test About (external link)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-about")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank.netlify.app"), "About page should open in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton.click();

        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-logout")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Should be on login page after logout");
    }

    @Test
    @Order(4)
    public void testResetAppState() {
        login();
        
        // Add some state by creating a transaction
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExit")));
        menuButton.click();

        // Reset app state
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-reset")));
        reset.click();

        // Verify reset by checking if menu is closed
        WebElement menuButtonAfter = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExit")));
        Assertions.assertTrue(menuButtonAfter.isDisplayed(), "Menu should be closed after reset");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");

        // Test Facebook link
        testExternalLink("Facebook", "facebook.com");

        // Test LinkedIn link
        testExternalLink("LinkedIn", "linkedin.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'" + expectedDomain + "')]")));
        link.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), linkText + " page should contain " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("home"));
    }
}