package deepseek.ws04.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {
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
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        // Fill form fields
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("lastName"));
        lastName.sendKeys("Doe");
        
        WebElement genderMale = driver.findElement(By.id("genderMale"));
        genderMale.click();
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("secure123");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("Acme Inc");
        
        WebElement roleDropdown = driver.findElement(By.id("role"));
        Select roleSelect = new Select(roleDropdown);
        roleSelect.selectByVisibleText("QA");
        
        WebElement expectationDropdown = driver.findElement(By.id("expectation"));
        Select expectationSelect = new Select(expectationDropdown);
        expectationSelect.selectByVisibleText("Good teamwork");
        expectationSelect.selectByVisibleText("High salary");
        expectationSelect.selectByVisibleText("Challenging");
        
        WebElement waysOfDevelopment = driver.findElement(By.id("ways"));
        waysOfDevelopment.sendKeys("Practice coding daily");
        
        WebElement comment = driver.findElement(By.id("comment"));
        comment.sendKeys("This is a test comment");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        // Verify submission
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        WebElement firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName-error")));
        Assertions.assertTrue(firstNameError.isDisplayed());
        
        WebElement lastNameError = driver.findElement(By.id("lastName-error"));
        Assertions.assertTrue(lastNameError.isDisplayed());
        
        WebElement genderError = driver.findElement(By.id("gender-error"));
        Assertions.assertTrue(genderError.isDisplayed());
        
        WebElement dobError = driver.findElement(By.id("dob-error"));
        Assertions.assertTrue(dobError.isDisplayed());
    }

    @Test
    @Order(3)
    public void testResetForm() {
        driver.get(BASE_URL);
        
        // Fill some fields
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        firstName.sendKeys("Test");
        
        WebElement lastName = driver.findElement(By.id("lastName"));
        lastName.sendKeys("User");
        
        // Reset form
        WebElement resetButton = driver.findElement(By.id("reset"));
        resetButton.click();

        // Verify fields are cleared
        Assertions.assertEquals("", firstName.getAttribute("value"));
        Assertions.assertEquals("", lastName.getAttribute("value"));
    }

    @Test
    @Order(4)
    public void testOptionSelection() {
        driver.get(BASE_URL);
        
        WebElement roleDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("role")));
        Select roleSelect = new Select(roleDropdown);
        
        // Test all role options
        roleSelect.selectByVisibleText("QA");
        Assertions.assertEquals("QA", roleSelect.getFirstSelectedOption().getText());
        
        roleSelect.selectByVisibleText("Manager");
        Assertions.assertEquals("Manager", roleSelect.getFirstSelectedOption().getText());
        
        roleSelect.selectByVisibleText("Developer");
        Assertions.assertEquals("Developer", roleSelect.getFirstSelectedOption().getText());
    }

    @Test
    @Order(5)
    public void testMultiSelectExpectations() {
        driver.get(BASE_URL);
        
        WebElement expectationDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("expectation")));
        Select expectationSelect = new Select(expectationDropdown);
        
        // Select multiple options
        expectationSelect.selectByVisibleText("Good teamwork");
        expectationSelect.selectByVisibleText("High salary");
        
        Assertions.assertEquals(2, expectationSelect.getAllSelectedOptions().size());
        
        // Deselect one
        expectationSelect.deselectByVisibleText("High salary");
        Assertions.assertEquals(1, expectationSelect.getAllSelectedOptions().size());
    }
}