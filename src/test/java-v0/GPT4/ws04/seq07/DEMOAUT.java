package GPT4.ws04.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class DEMOAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
    public void testPageLoads() {
        driver.get(BASE_URL);
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Input Form']")));
        Assertions.assertTrue(title.isDisplayed(), "Page title 'Input Form' should be visible");
        Assertions.assertTrue(driver.getTitle().contains("Input Form"), "Browser title should contain 'Input Form'");
    }

    @Test
    @Order(2)
    public void testFillAndSubmitFormWithValidData() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("first_name"))).sendKeys("John");
        driver.findElement(By.name("last_name")).sendKeys("Doe");
        driver.findElement(By.name("gender")).click();
        driver.findElement(By.name("dob")).sendKeys("01/01/1990");
        driver.findElement(By.name("address")).sendKeys("123 Main Street");
        driver.findElement(By.name("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.name("password")).sendKeys("Password123");
        driver.findElement(By.name("company")).sendKeys("Test Inc.");
        driver.findElement(By.name("comment")).sendKeys("This is a test comment.");
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();

        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(alert.getText().toLowerCase().contains("success"), "Submit message should indicate success");
    }

    @Test
    @Order(3)
    public void testFormValidationError() {
        driver.get(BASE_URL);
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();

        WebElement validationError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input:invalid")));
        Assertions.assertNotNull(validationError, "Form should show validation error for required fields");
    }

    @Test
    @Order(4)
    public void testExternalKatalonLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Katalon")));
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("katalon.com"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("katalon.com"), "External Katalon link should lead to katalon.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
