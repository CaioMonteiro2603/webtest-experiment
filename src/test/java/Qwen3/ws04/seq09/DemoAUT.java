package Qwen3.ws04.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class FormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testPageTitleAndHeader() {
        driver.get(BASE_URL);

        assertEquals("Katalon Automation Recorder", driver.getTitle(), "Page title should match");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(header.getText().contains("Practice Form"), "Header should contain 'Practice Form'");
    }

    @Test
    @Order(2)
    void testFormFieldsPresence() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        WebElement genderMale = driver.findElement(By.cssSelector("input[value='Male']"));
        WebElement genderFemale = driver.findElement(By.cssSelector("input[value='Female']"));
        WebElement dobField = driver.findElement(By.id("dob"));
        WebElement addressField = driver.findElement(By.id("address"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement companyField = driver.findElement(By.id("company"));
        WebElement roleDropdown = driver.findElement(By.id("role"));
        WebElement expectedSalaryField = driver.findElement(By.id("salary"));
        WebElement startDateField = driver.findElement(By.id("start-date"));
        WebElement vacationPlaceField = driver.findElement(By.id("vacation"));
        WebElement resumeField = driver.findElement(By.id("upload-photo"));
        WebElement agreeCheckbox = driver.findElement(By.id("agree"));

        assertTrue(firstNameField.isDisplayed(), "First name field should be visible");
        assertTrue(lastNameField.isDisplayed(), "Last name field should be visible");
        assertTrue(genderMale.isDisplayed(), "Male gender option should be visible");
        assertTrue(genderFemale.isDisplayed(), "Female gender option should be visible");
        assertTrue(dobField.isDisplayed(), "Date of birth field should be visible");
        assertTrue(addressField.isDisplayed(), "Address field should be visible");
        assertTrue(emailField.isDisplayed(), "Email field should be visible");
        assertTrue(passwordField.isDisplayed(), "Password field should be visible");
        assertTrue(companyField.isDisplayed(), "Company field should be visible");
        assertTrue(roleDropdown.isDisplayed(), "Role dropdown should be visible");
        assertTrue(expectedSalaryField.isDisplayed(), "Expected salary field should be visible");
        assertTrue(startDateField.isDisplayed(), "Start date field should be visible");
        assertTrue(vacationPlaceField.isDisplayed(), "Vacation place field should be visible");
        assertTrue(resumeField.isDisplayed(), "Resume upload field should be visible");
        assertTrue(agreeCheckbox.isDisplayed(), "Agree checkbox should be visible");
    }

    @Test
    @Order(3)
    void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement genderRadio = driver.findElement(By.cssSelector("input[value='Male']"));
        if (!genderRadio.isSelected()) {
            genderRadio.click();
        }

        WebElement dobField = driver.findElement(By.id("dob"));
        dobField.sendKeys("1990-05-15");

        WebElement addressField = driver.findElement(By.id("address"));
        addressField.sendKeys("123 Main St, New York, NY");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio.silva@example.com");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("SecurePass123");

        WebElement companyField = driver.findElement(By.id("company"));
        companyField.sendKeys("Tech Corp");

        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("QA Lead");

        WebElement salaryField = driver.findElement(By.id("salary"));
        salaryField.sendKeys("80000");

        WebElement startDateField = driver.findElement(By.id("start-date"));
        startDateField.sendKeys("2023-07-01");

        Select vacationSelect = new Select(driver.findElement(By.id("vacation")));
        vacationSelect.selectByVisibleText("Hawaii");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement confirmationHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".thanks>h1")));
        assertEquals("Thank you!", confirmationHeader.getText(), "Confirmation message should appear");
    }

    @Test
    @Order(4)
    void testFirstNameRequiredValidation() {
        driver.get(BASE_URL);

        WebElement lastNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("last-name")));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement firstNameField = driver.findElement(By.id("first-name"));
        String classAttr = firstNameField.getAttribute("class");
        assertTrue(classAttr.contains("error"), "First name field should have error class when empty");
    }

    @Test
    @Order(5)
    void testLastNameRequiredValidation() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        String classAttr = lastNameField.getAttribute("class");
        assertTrue(classAttr.contains("error"), "Last name field should have error class when empty");
    }

    @Test
    @Order(6)
    void testEmailRequiredValidation() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement emailField = driver.findElement(By.id("email"));
        String classAttr = emailField.getAttribute("class");
        assertTrue(classAttr.contains("error"), "Email field should have error class when empty");
    }

    @Test
    @Order(7)
    void testPasswordRequiredValidation() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement passwordField = driver.findElement(By.id("password"));
        String classAttr = passwordField.getAttribute("class");
        assertTrue(classAttr.contains("error"), "Password field should have error class when empty");
    }

    @Test
    @Order(8)
    void testAgreeCheckboxRequiredValidation() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("SecurePass123");

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement agreeLabel = driver.findElement(By.cssSelector("label[for='agree']"));
        String classAttr = agreeLabel.getAttribute("class");
        assertTrue(classAttr.contains("error"), "Agree label should show error when not checked");
    }

    @Test
    @Order(9)
    void testInvalidEmailFormat() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("invalid-email");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("SecurePass123");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement emailFieldAfterSubmit = driver.findElement(By.id("email"));
        String classAttr = emailFieldAfterSubmit.getAttribute("class");
        assertTrue(classAttr.contains("error"), "Email field should have error class on invalid format");
    }

    @Test
    @Order(10)
    void testGenderSelectionPersistence() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("SecurePass123");

        WebElement genderRadio = driver.findElement(By.cssSelector("input[value='Male']"));
        if (!genderRadio.isSelected()) {
            genderRadio.click();
        }

        assertTrue(genderRadio.isSelected(), "Male gender should be selected");

        // Reload and verify
        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));

        WebElement firstNameAfterRefresh = driver.findElement(By.id("first-name"));
        firstNameAfterRefresh.sendKeys("Caio");

        WebElement lastNameAfterRefresh = driver.findElement(By.id("last-name"));
        lastNameAfterRefresh.sendKeys("Silva");

        WebElement emailAfterRefresh = driver.findElement(By.id("email"));
        emailAfterRefresh.sendKeys("caio@example.com");

        WebElement passwordAfterRefresh = driver.findElement(By.id("password"));
        passwordAfterRefresh.sendKeys("SecurePass123");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        // Reselect radio after refresh
        WebElement genderRadioAfterRefresh = driver.findElement(By.cssSelector("input[value='Male']"));
        genderRadioAfterRefresh.click();

        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement confirmationHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".thanks>h1")));
        assertEquals("Thank you!", confirmationHeader.getText(), "Form should submit successfully after refresh");
    }

    @Test
    @Order(11)
    void testRoleDropdownOptions() {
        driver.get(BASE_URL);

        Select roleSelect = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("role"))));
        java.util.List<WebElement> options = roleSelect.getOptions();

        assertEquals(5, options.size(), "Role dropdown should have 5 options");

        java.util.List<String> expectedOptions = java.util.Arrays.asList("", "QA Lead", "QA Engineer", "Developer", "Manager");
        for (int i = 0; i < expectedOptions.size(); i++) {
            assertEquals(expectedOptions.get(i), options.get(i).getText(), "Dropdown option should match expected");
        }
    }

    @Test
    @Order(12)
    void testVacationPlaceDropdownOptions() {
        driver.get(BASE_URL);

        Select vacationSelect = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("vacation"))));
        java.util.List<WebElement> options = vacationSelect.getOptions();

        assertEquals(4, options.size(), "Vacation dropdown should have 4 options");

        java.util.List<String> expectedOptions = java.util.Arrays.asList("", "Hawaii", "Las Vegas", "California");
        for (int i = 0; i < expectedOptions.size(); i++) {
            assertEquals(expectedOptions.get(i), options.get(i).getText(), "Dropdown option should match expected");
        }
    }

    @Test
    @Order(13)
    void testSubmitButtonDisabledUntilRequiredFilled() {
        driver.get(BASE_URL);

        WebElement submitButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit")));
        assertFalse(submitButton.isEnabled(), "Submit button should be disabled initially");

        WebElement firstNameField = driver.findElement(By.id("first-name"));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("SecurePass123");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        assertTrue(submitButton.isEnabled(), "Submit button should be enabled when all required fields are filled");
    }

    @Test
    @Order(14)
    void testFormResetButton() {
        driver.get(BASE_URL);

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("QA Lead");

        WebElement resetButton = driver.findElement(By.cssSelector("button.btn-reset"));
        resetButton.click();

        // Wait and verify fields are cleared
        wait.until(ExpectedConditions.textToBe(By.id("first-name"), ""));
        wait.until(ExpectedConditions.textToBe(By.id("last-name"), ""));
        wait.until(ExpectedConditions.textToBe(By.id("email"), ""));
        assertEquals("", roleSelect.getFirstSelectedOption().getText(), "Role dropdown should be reset");
    }
}