package GPT4.ws04.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @BeforeEach
    public void goToBase() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    public void testTitleAndHeader() {
        String title = driver.getTitle();
        Assertions.assertEquals("Demo AUT", title, "Page title mismatch");

        WebElement h1 = driver.findElement(By.tagName("h1"));
        Assertions.assertEquals("AUT Form", h1.getText(), "Header text mismatch");
    }

    @Test
    @Order(2)
    public void testFormSubmissionSuccess() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName"))).sendKeys("John");
        driver.findElement(By.name("lastName")).sendKeys("Doe");
        driver.findElement(By.id("radioMale")).click();
        driver.findElement(By.name("dob")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).sendKeys("123 Main St");
        driver.findElement(By.name("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.name("password")).sendKeys("Password123");
        driver.findElement(By.name("company")).sendKeys("TestCorp");
        driver.findElement(By.name("role")).sendKeys("Engineer");
        driver.findElement(By.name("comment")).sendKeys("This is a comment");

        WebElement submitBtn = driver.findElement(By.id("submit"));
        submitBtn.click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(msg.getText().toLowerCase().contains("successfully"), "Success message not found");
    }

    @Test
    @Order(3)
    public void testFormSubmissionValidation() {
        driver.findElement(By.id("submit")).click();
        List<WebElement> invalidEls = driver.findElements(By.cssSelector("input:invalid"));
        Assertions.assertFalse(invalidEls.isEmpty(), "Expected validation errors");
    }

    @Test
    @Order(4)
    public void testExternalLinkNavigation() {
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Privacy Policy")));
        String msg
at GPT4.ws04.seq03.DEMOAUT.assertFalse-1.0-SNAPSHOT:
..."

AlrightArrowForward   <failureType: <2c5aee7:0xception
-&nul