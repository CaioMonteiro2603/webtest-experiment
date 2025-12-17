package SunaDeepSeek.ws03.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().contains("BugBank"));
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm")));
        Assertions.assertTrue(loginForm.isDisplayed());
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();

        WebElement accountInfo = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountInfo")));
        Assertions.assertTrue(accountInfo.isDisplayed());
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("credenciais inválidas"));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        // First login
        driver.get(BASE_URL);
        login();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();

        // Test All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventoryLink")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"));

        // Test About (external)
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("aboutLink")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Logout
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logoutLink")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/"));
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        login();

        // Twitter link
        testExternalLink("twitterLink", "twitter.com");
        
        // Facebook link
        testExternalLink("facebookLink", "facebook.com");
        
        // LinkedIn link
        testExternalLink("linkedinLink", "linkedin.com");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        driver.get(BASE_URL);
        login();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("resetLink")));
        resetLink.click();

        // Verify reset confirmation
        WebElement resetMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(resetMessage.getText().contains("reset"));
    }

    private void login() {
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountInfo")));
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        socialLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}