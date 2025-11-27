package SunaDeepSeek.ws04.seq01;

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
import java.util.List;

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
    public void testFormPageLoad() {
        driver.get(BASE_URL);
        Assertions.assertEquals("AUT Form", driver.getTitle());
        Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"));
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.id("gender"));
        Select genderSelect = new Select(gender);
        genderSelect.selectByVisibleText("Male");
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("password123");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        Select roleSelect = new Select(role);
        roleSelect.selectByVisibleText("QA");
        
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        Select expectationSelect = new Select(jobExpectation);
        expectationSelect.selectByVisibleText("Good teamwork");
        
        WebElement development = driver.findElement(By.id("development"));
        development.click();
        
        WebElement comment = driver.findElement(By.id("comment"));
        comment.sendKeys("This is a test comment");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertEquals("Successfully submitted!", successMessage.getText());
    }

    @Test
    @Order(3)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();
        
        WebElement firstNameError = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name-helper")));
        Assertions.assertEquals("Please enter your first name", firstNameError.getText());
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("GitHub")));
        githubLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
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
        WebElement katalonLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Katalon")));
        katalonLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
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
    public void testRadioButtons() {
        driver.get(BASE_URL);
        
        WebElement radio1 = wait.until(ExpectedConditions.elementToBeClickable(By.id("radio-1")));
        radio1.click();
        Assertions.assertTrue(radio1.isSelected());
        
        WebElement radio2 = driver.findElement(By.id("radio-2"));
        radio2.click();
        Assertions.assertTrue(radio2.isSelected());
        Assertions.assertFalse(radio1.isSelected());
    }

    @Test
    @Order(6)
    public void testCheckboxes() {
        driver.get(BASE_URL);
        
        WebElement checkbox1 = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkbox-1")));
        checkbox1.click();
        Assertions.assertTrue(checkbox1.isSelected());
        
        WebElement checkbox2 = driver.findElement(By.id("checkbox-2"));
        checkbox2.click();
        Assertions.assertTrue(checkbox2.isSelected());
        
        checkbox1.click();
        Assertions.assertFalse(checkbox1.isSelected());
        Assertions.assertTrue(checkbox2.isSelected());
    }
}