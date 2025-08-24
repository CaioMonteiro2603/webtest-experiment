package deepseek.ws04.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class KatalonFormTest {
    private static WebDriver driver;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
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

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        WebElement lastName = driver.findElement(By.id("last-name"));
        WebElement genderMale = driver.findElement(By.id("male"));
        WebElement dob = driver.findElement(By.id("dob"));
        WebElement address = driver.findElement(By.id("address"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement company = driver.findElement(By.id("company"));
        WebElement roleDropdown = driver.findElement(By.id("role"));
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        WebElement developmentCheckbox = driver.findElement(By.xpath("//input[@value='Development']"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        genderMale.click();
        dob.sendKeys("01/01/1990");
        address.sendKeys("123 Main St");
        email.sendKeys("john.doe@example.com");
        password.sendKeys("password123");
        company.sendKeys("Test Company");
        
        Select roleSelect = new Select(roleDropdown);
        roleSelect.selectByVisibleText("QA");
        
        Select expectationSelect = new Select(jobExpectation);
        expectationSelect.selectByVisibleText("Good teamwork");
        
        developmentCheckbox.click();

        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"),
            "Form submission was not successful");
    }

    @Test
    @Order(2)
    public void testFormValidation() {
        driver.get(BASE_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        WebElement firstNameValidation = driver.findElement(By.id("first-name-error"));
        Assertions.assertTrue(firstNameValidation.isDisplayed(), 
            "First name validation error not displayed");
        Assertions.assertEquals("Please enter First Name", firstNameValidation.getText(),
            "Incorrect validation message for first name");

        WebElement lastNameValidation = driver.findElement(By.id("last-name-error"));
        Assertions.assertTrue(lastNameValidation.isDisplayed(),
            "Last name validation error not displayed");
    }

    @Test
    @Order(3)
    public void testCheckboxAndRadioButtons() {
        driver.get(BASE_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement genderFemale = wait.until(ExpectedConditions.elementToBeClickable(By.id("female")));
        WebElement developmentCheckbox = driver.findElement(By.xpath("//input[@value='Development']"));
        WebElement automationCheckbox = driver.findElement(By.xpath("//input[@value='Automation']"));

        genderFemale.click();
        developmentCheckbox.click();
        automationCheckbox.click();

        Assertions.assertTrue(genderFemale.isSelected(), "Female radio button not selected");
        Assertions.assertTrue(developmentCheckbox.isSelected(), "Development checkbox not selected");
        Assertions.assertTrue(automationCheckbox.isSelected(), "Automation checkbox not selected");
    }

    @Test
    @Order(4)
    public void testSelectDropdowns() {
        driver.get(BASE_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement roleDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("role")));
        WebElement expectationDropdown = driver.findElement(By.id("expectation"));

        Select roleSelect = new Select(roleDropdown);
        roleSelect.selectByVisibleText("Manager");
        
        Select expectationSelect = new Select(expectationDropdown);
        expectationSelect.selectByVisibleText("High salary");

        Assertions.assertEquals("Manager", roleSelect.getFirstSelectedOption().getText(),
            "Incorrect option selected for role");
        Assertions.assertEquals("High salary", expectationSelect.getFirstSelectedOption().getText(),
            "Incorrect option selected for expectation");
    }

    @Test
    @Order(5)
    public void testCommentField() {
        driver.get(BASE_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement commentField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("comment")));
        String testComment = "This is a test comment for the form";

        commentField.sendKeys(testComment);
        Assertions.assertEquals(testComment, commentField.getAttribute("value"),
            "Comment not properly entered in field");
    }
}