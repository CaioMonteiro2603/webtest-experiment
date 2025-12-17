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
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

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
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".modal-body")));
        Assertions.assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"), "Error message for invalid login should be displayed");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();

        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Should be on home page after clicking All Items");

        // Open menu again
        menuButton.click();

        // Test About (external link)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About page should open in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton.click();

        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Should be on login page after logout");
    }

    @Test
    @Order(4)
    public void testResetAppState() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();

        // Reset app state
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();

        // Verify reset by checking if any error messages are cleared
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error-message"));
        Assertions.assertEquals(0, errorMessages.size(), "Error messages should be cleared after reset");
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
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'" + linkText + "')]")));
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
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("home"));
    }
}