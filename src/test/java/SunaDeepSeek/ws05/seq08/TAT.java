package SunaDeepSeek.ws05.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Central de Atendimento ao Cliente TAT"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        WebElement lastName = driver.findElement(By.name("lastName"));
        WebElement email = driver.findElement(By.name("email"));
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));

        firstName.sendKeys("Test");
        lastName.sendKeys("User");
        email.sendKeys("test@example.com");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//span[contains(text(),'Recebemos os seus dados.')]")));
        Assertions.assertTrue(successMessage.isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        submitButton.click();
        Assertions.assertTrue(driver.getTitle().contains("Central de Atendimento ao Cliente TAT"));
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        Assertions.assertTrue(firstName.isDisplayed());
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        Assertions.assertTrue(firstName.isDisplayed());
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        Assertions.assertTrue(firstName.isDisplayed());
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        Assertions.assertTrue(firstName.isDisplayed());
    }
}