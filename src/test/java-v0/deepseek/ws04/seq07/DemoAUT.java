package deepseek.ws04.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT{
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
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = driver.findElement(By.id("last-name"));
        WebElement genderMale = driver.findElement(By.id("gender-male"));
        WebElement dob = driver.findElement(By.id("dob"));
        WebElement address = driver.findElement(By.id("address"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement company = driver.findElement(By.id("company"));
        WebElement role = driver.findElement(By.id("role"));
        Select roleSelect = new Select(role);
        WebElement jobExpectation = driver.findElement(By.id("job-expectation"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        genderMale.click();
        dob.sendKeys("01/01/1990");
        address.sendKeys("123 Main St");
        email.sendKeys("john.doe@example.com");
        password.sendKeys("password123");
        company.sendKeys("ACME Corp");
        roleSelect.selectByVisibleText("QA");
        jobExpectation.sendKeys("Looking for challenging QA position");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertEquals("Successfully submitted!", successMessage.getText());
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithMissingRequiredFields() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".invalid-feedback"));
        Assertions.assertTrue(errorMessages.size() > 0, "Should show validation errors for required fields");
    }

    @Test
    @Order(3)
    public void testRadioButtonSelection() {
        driver.get(BASE_URL);
        WebElement genderFemale = wait.until(ExpectedConditions.elementToBeClickable(By.id("gender-female")));
        genderFemale.click();

        Assertions.assertTrue(genderFemale.isSelected(), "Female radio button should be selected");
        Assertions.assertFalse(driver.findElement(By.id("gender-male")).isSelected(), "Male radio button should not be selected");
    }

    @Test
    @Order(4)
    public void testDropdownOptions() {
        driver.get(BASE_URL);
        WebElement role = wait.until(ExpectedConditions.elementToBeClickable(By.id("role")));
        Select roleSelect = new Select(role);

        Assertions.assertEquals(5, roleSelect.getOptions().size(), "Should have 5 role options");
        Assertions.assertEquals("Developer", roleSelect.getOptions().get(1).getText());
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement katalonLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Katalon Website")));
        katalonLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("katalon.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}