package SunaDeepSeek.ws03.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("textName")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Olá"), "Login was not successful");
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
            "Expected error message for invalid credentials not found");
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
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "All Items navigation failed");

        // Open menu again
        menuButton.click();

        // Test About (external link)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        // Switch to new tab and verify URL
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About page not opened");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton.click();

        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains(""));
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Logout failed");
    }

    @Test
    @Order(4)
    public void testResetAppState() {
        // First login
        driver.get(BASE_URL);
        login();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();

        // Reset app state
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();

        // Verify reset by checking if any items are selected
        List<WebElement> selectedItems = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertEquals(0, selectedItems.size(), "App state was not reset properly");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        login();

        // Test Twitter link
        testExternalLink(By.cssSelector(".social_twitter a"), "twitter.com");

        // Test Facebook link
        testExternalLink(By.cssSelector(".social_facebook a"), "facebook.com");

        // Test LinkedIn link
        testExternalLink(By.cssSelector(".social_linkedin a"), "linkedin.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "Expected " + expectedDomain + " domain not found in URL");
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login"));

        emailField.clear();
        passwordField.clear();
        
        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
    }
}