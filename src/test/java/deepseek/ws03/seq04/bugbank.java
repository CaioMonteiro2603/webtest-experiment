package deepseek.ws03.seq04;

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
public class BugBankTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Welcome')]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid credentials"));
    }

    @Test
    @Order(3)
    public void testAccountNavigation() {
        login();
        
        // Test Dashboard
        WebElement dashboardLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Dashboard')]")));
        dashboardLink.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Dashboard"));
        
        // Test Transactions
        WebElement transactionsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Transactions')]")));
        transactionsLink.click();
        wait.until(ExpectedConditions.urlContains("transactions"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Transactions"));
        
        // Test Transfers
        WebElement transfersLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Transfers')]")));
        transfersLink.click();
        wait.until(ExpectedConditions.urlContains("transfers"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Transfers"));
        
        // Test Profile
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Profile')]")));
        profileLink.click();
        wait.until(ExpectedConditions.urlContains("profile"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Profile"));
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        login();
        
        // Test GitHub link
        testExternalLink("GitHub", "github.com");
        
        // Test LinkedIn link
        testExternalLink("LinkedIn", "linkedin.com");
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'" + linkText + "')]")));
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testLogout() {
        login();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Logout')]")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("input[type='email']")).isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Welcome')]")));
    }
}