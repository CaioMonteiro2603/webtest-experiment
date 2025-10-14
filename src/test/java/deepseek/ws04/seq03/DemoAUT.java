package deepseek.ws04.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class KatalonFormTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
    public void testFormSubmission() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.xpath("//label[text()='Male']")).click();
        driver.findElement(By.id("dob")).sendKeys("01/01/1990");
        driver.findElement(By.id("address")).sendKeys("123 Main St");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("company")).sendKeys("Test Company");
        new Select(driver.findElement(By.id("role"))).selectByVisibleText("QA");
        driver.findElement(By.xpath("//label[text()='Read books']")).click();
        driver.findElement(By.id("comment")).sendKeys("This is a test comment");
        driver.findElement(By.id("submit")).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(2)
    public void testRequiredFields() {
        driver.get(BASE_URL);
        driver.findElement(By.id("submit")).click();

        WebElement firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#first-name + .error")));
        Assertions.assertTrue(firstNameError.getText().contains("This field is required."));

        WebElement lastNameError = driver.findElement(By.cssSelector("#last-name + .error"));
        Assertions.assertTrue(lastNameError.getText().contains("This field is required."));

        WebElement genderError = driver.findElement(By.cssSelector(".gender-field + .error"));
        Assertions.assertTrue(genderError.getText().contains("This field is required."));

        WebElement dobError = driver.findElement(By.cssSelector("#dob + .error"));
        Assertions.assertTrue(dobError.getText().contains("This field is required."));
    }

    @Test
    @Order(3)
    public void testInvalidEmail() {
        driver.get(BASE_URL);
        driver.findElement(By.id("email")).sendKeys("invalid-email");
        driver.findElement(By.id("submit")).click();

        WebElement emailError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#email + .error")));
        Assertions.assertTrue(emailError.getText().contains("Please enter a valid email address."));
    }

    @Test
    @Order(4)
    public void testRoleSelection() {
        driver.get(BASE_URL);
        new Select(driver.findElement(By.id("role"))).selectByVisibleText("Developer");
        Assertions.assertEquals("Developer", driver.findElement(By.id("role")).getAttribute("value"));
    }

    @Test
    @Order(5)
    public void testHobbiesSelection() {
        driver.get(BASE_URL);
        driver.findElement(By.xpath("//label[text()='Read books']")).click();
        driver.findElement(By.xpath("//label[text()='Listen to music']")).click();
        Assertions.assertTrue(driver.findElement(By.xpath("//label[text()='Read books']/input")).isSelected());
        Assertions.assertTrue(driver.findElement(By.xpath("//label[text()='Listen to music']/input")).isSelected());
    }

    @Test
    @Order(6)
    public void testCommentField() {
        driver.get(BASE_URL);
        driver.findElement(By.id("comment")).sendKeys("This is a comment");
        Assertions.assertEquals("This is a comment", driver.findElement(By.id("comment")).getAttribute("value"));
    }
}