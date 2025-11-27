package SunaDeepSeek.ws04.seq02;

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
public class FormPageTest {

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
    public void testFormPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertEquals("A Simple Form", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testSubmitEmptyForm() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error")));
        Assertions.assertEquals("Please fill out this field.", errorMessage.getText());
    }

    @Test
    @Order(3)
    public void testSubmitValidForm() {
        driver.get(BASE_URL);
        
        WebElement firstName = driver.findElement(By.id("first-name"));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.id("gender"));
        gender.sendKeys("Male");
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("Password123");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        role.sendKeys("QA Engineer");
        
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        jobExpectation.sendKeys("Challenging work");
        
        WebElement developmentWay = driver.findElement(By.id("development-way"));
        developmentWay.sendKeys("Self study");
        
        WebElement comments = driver.findElement(By.id("comments"));
        comments.sendKeys("No additional comments");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github']")));
        githubLink.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Katalon link
        WebElement katalonLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='katalon']")));
        katalonLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("katalon.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testAllFormFieldsArePresent() {
        driver.get(BASE_URL);
        
        List<String> requiredFields = List.of(
            "first-name", "last-name", "gender", "dob", "address",
            "email", "password", "company", "role", "expectation",
            "development-way", "comments", "submit"
        );
        
        for (String fieldId : requiredFields) {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(fieldId)));
            Assertions.assertTrue(element.isDisplayed(), "Field " + fieldId + " is not displayed");
        }
    }
}