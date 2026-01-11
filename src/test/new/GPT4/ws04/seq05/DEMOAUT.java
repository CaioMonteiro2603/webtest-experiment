package GPT4.ws04.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
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
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testFormElementsPresence() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        Assertions.assertTrue(driver.findElement(By.id("first-name")).isDisplayed(), "First Name input should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("last-name")).isDisplayed(), "Last Name input should be displayed");
        Assertions.assertTrue(driver.findElement(By.xpath("//input[@name='gender']")).isDisplayed(), "Gender radio buttons should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("dob")).isDisplayed(), "Date of Birth input should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("address")).isDisplayed(), "Address textarea should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("email")).isDisplayed(), "Email input should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("password")).isDisplayed(), "Password input should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("company")).isDisplayed(), "Company input should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("role")).isDisplayed(), "Role dropdown should be displayed");
        Assertions.assertTrue(driver.findElement(By.xpath("//input[@name='expectation']")).isDisplayed(), "Job expectation checkboxes should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("comment")).isDisplayed(), "Comment textarea should be displayed");
        Assertions.assertTrue(driver.findElement(By.id("submit")).isDisplayed(), "Submit button should be displayed");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name"))).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElements(By.name("gender")).get(0).click();
        driver.findElement(By.id("dob")).sendKeys("01/01/1990");
        driver.findElement(By.id("address")).sendKeys("123 Test Street");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("password")).sendKeys("Password123!");
        driver.findElement(By.id("company")).sendKeys("TestCompany");
        driver.findElement(By.id("role")).sendKeys("QA");
        List<WebElement> expectations = driver.findElements(By.name("expectation"));
        if (!expectations.isEmpty()) {
            expectations.get(0).click();
        }
        driver.findElement(By.id("comment")).sendKeys("This is a test comment.");
        driver.findElement(By.id("submit")).click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        Assertions.assertTrue(alert.getText().toLowerCase().contains("success") || alert.getText().toLowerCase().contains("thank"),
                "Submission alert should confirm success");
        alert.accept();
    }

    @Test
    @Order(3)
    public void testMissingRequiredFields() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submit"))).click();

        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            Assertions.assertTrue(alert.getText().toLowerCase().contains("required") || alert.getText().toLowerCase().contains("missing"),
                    "Alert should mention required fields or missing data");
            alert.accept();
        } catch (TimeoutException e) {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
            Assertions.assertTrue(errorElement.getText().toLowerCase().contains("required") || errorElement.getText().toLowerCase().contains("missing"),
                    "Error message should mention required fields or missing data");
        }
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href^='http']"));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    String currentUrl = driver.getCurrentUrl();
                    Assertions.assertTrue(currentUrl.startsWith("http"), "External link should open with http(s)");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}