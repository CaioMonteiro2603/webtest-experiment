package deepseek.ws04.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FormTest {

    private static WebDriver driver;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
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
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = driver.findElement(By.id("last-name"));
        WebElement genderMale = driver.findElement(By.id("radio-button-1"));
        WebElement genderFemale = driver.findElement(By.id("radio-button-2"));
        WebElement genderOther = driver.findElement(By.id("radio-button-3"));
        WebElement dob = driver.findElement(By.id("dob"));
        WebElement address = driver.findElement(By.id("address"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement company = driver.findElement(By.id("company"));
        WebElement role = driver.findElement(By.id("role"));
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        WebElement developmentWay = driver.findElement(By.id("development-way"));
        WebElement comment = driver.findElement(By.id("comment"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        genderMale.click();
        dob.sendKeys("01/01/1990");
        address.sendKeys("123 Main St");
        email.sendKeys("john.doe@example.com");
        password.sendKeys("password123");
        company.sendKeys("Acme Inc");
        
        Select roleSelect = new Select(role);
        roleSelect.selectByVisibleText("QA");
        
        Select expectationSelect = new Select(jobExpectation);
        expectationSelect.selectByVisibleText("Good teamwork");
        
        developmentWay.click();
        comment.sendKeys("This is a test comment");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"), "Form should be submitted successfully");
    }

    @Test
    @Order(2)
    public void testRequiredFieldsValidation() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        WebElement firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#first-name + .invalid-feedback")));
        Assertions.assertTrue(firstNameError.isDisplayed(), "First name required validation should be shown");
    }

    @Test
    @Order(3)
    public void testRadioButtonSelection() {
        driver.get(BASE_URL);
        WebElement genderFemale = wait.until(ExpectedConditions.elementToBeClickable(By.id("radio-button-2")));
        genderFemale.click();
        Assertions.assertTrue(genderFemale.isSelected(), "Female radio button should be selected");
    }

    @Test
    @Order(4)
    public void testDropdownSelections() {
        driver.get(BASE_URL);
        WebElement role = wait.until(ExpectedConditions.elementToBeClickable(By.id("role")));
        Select roleSelect = new Select(role);
        roleSelect.selectByVisibleText("Developer");
        Assertions.assertEquals("Developer", roleSelect.getFirstSelectedOption().getText(), "Role should be set to Developer");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement katalonLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Katalon")));
        String originalWindow = driver.getWindowHandle();
        katalonLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("katalon.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}