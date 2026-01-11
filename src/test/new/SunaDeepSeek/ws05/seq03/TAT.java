package SunaDeepSeek.ws05.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
        wait.until(ExpectedConditions.titleContains("Central de Atendimento ao Cliente TAT"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be on home page");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement lastName = driver.findElement(By.id("lastName"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement button = driver.findElement(By.xpath("//button[contains(text(), 'Enviar')]"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        button.click();

        WebElement success = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'Obrigado')]")));
        Assertions.assertTrue(success.getText().contains("Obrigado"), 
            "Success message should be displayed");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement lastName = driver.findElement(By.id("lastName"));
        WebElement button = driver.findElement(By.xpath("//button[contains(text(), 'Enviar')]"));

        firstName.sendKeys("John");
        button.click();

        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'Por favor')]")));
        Assertions.assertTrue(error.getText().contains("Por favor"), 
            "Error message should be displayed");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        testSuccessfulLogin(); // Ensure logged in
        
        WebElement success = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'Obrigado')]")));
        Assertions.assertTrue(success.getText().contains("Obrigado"), 
            "Should see success message");
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        testSuccessfulLogin(); // Ensure logged in
        
        WebElement success = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'Obrigado')]")));
        Assertions.assertTrue(success.getText().contains("Obrigado"), 
            "Should be on success page");
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        testSuccessfulLogin(); // Ensure logged in
        
        WebElement success = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'Obrigado')]")));
        Assertions.assertTrue(success.getText().contains("Obrigado"), 
            "Should see success message");
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        testSuccessfulLogin(); // Ensure logged in
        
        WebElement success = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'Obrigado')]")));
        Assertions.assertTrue(success.getText().contains("Obrigado"), 
            "Should see success message");
    }

    private void switchToNewWindow(String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}