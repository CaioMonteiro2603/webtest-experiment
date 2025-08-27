package GTP4.ws04.seq03;

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
public class FormTest {

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
        Assertions.assertEquals("Sample Form", title, "Page title mismatch");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("AUT Form", header.getText(), "Header text mismatch");
    }

    @Test
    @Order(2)
    public void testFormSubmissionSuccess() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName"))).sendKeys("John");
        driver.findElement(By.name("lastName")).sendKeys("Doe");
        driver.findElement(By.name("gender")).click();
        driver.findElement(By.name("dob")).sendKeys("2000-01-01");
        driver.findElement(By.name("address")).sendKeys("123 Main St");
        driver.findElement(By.name("email")).sendKeys("john@example.com");
        driver.findElement(By.name("password")).sendKeys("Password123!");
        driver.findElement(By.name("company")).sendKeys("TestCorp");
        driver.findElement(By.name("role")).sendKeys("QA");
        driver.findElement(By.name("comment")).sendKeys("No comments");

        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("success"), "Expected success message not shown");
    }

    @Test
    @Order(3)
    public void testFormSubmissionValidation() {
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();

        List<WebElement> invalids = driver.findElements(By.cssSelector(":invalid"));
        Assertions.assertFalse(invalids.isEmpty(), "Expected validation errors but none found");
    }

    @Test
    @Order(4)
    public void testExternalLinkNavigation() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Privacy Policy")));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();

        driver.switchTo().window(newWindow);
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("katalon.com"), "Expected katalon.com domain in external link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
