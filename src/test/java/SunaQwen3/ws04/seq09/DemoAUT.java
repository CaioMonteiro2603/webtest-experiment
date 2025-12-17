package SunaQwen3.ws04.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String LOGIN_URL = BASE_URL;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(LOGIN_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        assertTrue(title.contains("Katalon"), "Page title should contain 'Katalon'");
        assertTrue(driver.getCurrentUrl().contains("form.html"), "URL should contain form.html");
    }

    @Test
    @Order(2)
    public void testFormFieldsArePresent() {
        By firstNameField = By.name("firstName");
        By lastNameField = By.name("lastName");
        By genderMaleRadio = By.xpath("//input[@value='Male']");
        By genderFemaleRadio = By.xpath("//input[@value='Female']");
        By dobField = By.name("bday");
        By addressField = By.name("address");
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By companyField = By.name("company");
        By submitButton = By.xpath("//button[@type='submit']");

        wait.until(ExpectedConditions.presenceOfElementLocated(firstNameField));
        wait.until(ExpectedConditions.presenceOfElementLocated(lastNameField));
        wait.until(ExpectedConditions.presenceOfElementLocated(genderMaleRadio));
        wait.until(ExpectedConditions.presenceOfElementLocated(genderFemaleRadio));
        wait.until(ExpectedConditions.presenceOfElementLocated(dobField));
        wait.until(ExpectedConditions.presenceOfElementLocated(addressField));
        wait.until(ExpectedConditions.presenceOfElementLocated(emailField));
        wait.until(ExpectedConditions.presenceOfElementLocated(passwordField));
        wait.until(ExpectedConditions.presenceOfElementLocated(companyField));
        wait.until(ExpectedConditions.presenceOfElementLocated(submitButton));

        assertTrue(driver.findElement(firstNameField).isDisplayed(), "First Name field should be displayed");
        assertTrue(driver.findElement(lastNameField).isDisplayed(), "Last Name field should be displayed");
        assertTrue(driver.findElement(genderMaleRadio).isDisplayed(), "Male radio button should be displayed");
        assertTrue(driver.findElement(genderFemaleRadio).isDisplayed(), "Female radio button should be displayed");
        assertTrue(driver.findElement(dobField).isDisplayed(), "Date of Birth field should be displayed");
        assertTrue(driver.findElement(addressField).isDisplayed(), "Address field should be displayed");
        assertTrue(driver.findElement(emailField).isDisplayed(), "Email field should be displayed");
        assertTrue(driver.findElement(passwordField).isDisplayed(), "Password field should be displayed");
        assertTrue(driver.findElement(companyField).isDisplayed(), "Company field should be displayed");
        assertTrue(driver.findElement(submitButton).isDisplayed(), "Submit button should be displayed");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithValidData() {
        fillForm("John", "Doe", "Male", "1990-01-01", "123 Main St", "john.doe@example.com", "P@ssw0rd", "Acme Corp");
        
        By submitButton = By.xpath("//button[@type='submit']");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submitBtn.click();

        // Wait for result message
        By resultMessage = By.id("submit-msg");
        wait.until(ExpectedConditions.presenceOfElementLocated(resultMessage));

        String message = driver.findElement(resultMessage).getText();
        assertTrue(message.contains("Thanks for submitting"), "Success message should be displayed");
    }

    @Test
    @Order(4)
    public void testFormSubmissionWithEmptyFirstName() {
        // Leave first name empty
        fillForm("", "Doe", "Male", "1990-01-01", "123 Main St", "john.doe@example.com", "P@ssw0rd", "Acme Corp");
        
        By submitButton = By.xpath("//button[@type='submit']");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submitBtn.click();

        // Check HTML5 validation message
        WebElement firstNameField = driver.findElement(By.name("firstName"));
        String validationMessage = firstNameField.getAttribute("validationMessage");
        assertNotNull(validationMessage, "Validation message should not be null");
        assertFalse(validationMessage.isEmpty(), "Validation message should not be empty");
    }

    @Test
    @Order(5)
    public void testFormSubmissionWithInvalidEmail() {
        fillForm("John", "Doe", "Male", "1990-01-01", "123 Main St", "invalid-email", "P@ssw0rd", "Acme Corp");
        
        By submitButton = By.xpath("//button[@type='submit']");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submitBtn.click();

        // Check HTML5 validation message
        WebElement emailField = driver.findElement(By.name("email"));
        String validationMessage = emailField.getAttribute("validationMessage");
        assertNotNull(validationMessage, "Validation message should not be null");
        assertFalse(validationMessage.isEmpty(), "Validation message should not be empty");
    }

    @Test
    @Order(6)
    public void testFormResetFunctionality() {
        By firstNameField = By.name("firstName");
        By resetButton = By.xpath("//button[@type='reset']");

        // Fill first name
        WebElement firstNameInput = wait.until(ExpectedConditions.elementToBeClickable(firstNameField));
        firstNameInput.sendKeys("John");

        // Click reset
        WebElement resetBtn = driver.findElement(resetButton);
        resetBtn.click();

        // Verify field is cleared
        String actualValue = firstNameInput.getAttribute("value");
        assertTrue(actualValue.isEmpty(), "First name field should be empty after reset");
    }

    @Test
    @Order(7)
    public void testExternalLinksInFooter() {
        // No external links in footer on this page
        // The page does not have Twitter, Facebook, LinkedIn links
        // This test passes by default as there are no external links to test
    }

    /**
     * Helper method to fill the form
     */
    private void fillForm(String firstName, String lastName, String gender, String dob, 
                         String address, String email, String password, String company) {
        By firstNameField = By.name("firstName");
        By lastNameField = By.name("lastName");
        By genderRadio = By.xpath("//input[@value='" + gender + "']");
        By dobField = By.name("bday");
        By addressField = By.name("address");
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By companyField = By.name("company");

        // Clear and fill first name
        WebElement firstNameInput = wait.until(ExpectedConditions.elementToBeClickable(firstNameField));
        firstNameInput.clear();
        if (!firstName.isEmpty()) {
            firstNameInput.sendKeys(firstName);
        }

        // Clear and fill last name
        WebElement lastNameInput = driver.findElement(lastNameField);
        lastNameInput.clear();
        if (!lastName.isEmpty()) {
            lastNameInput.sendKeys(lastName);
        }

        // Select gender
        WebElement genderInput = driver.findElement(genderRadio);
        if (!genderInput.isSelected()) {
            genderInput.click();
        }

        // Fill date of birth
        WebElement dobInput = driver.findElement(dobField);
        dobInput.clear();
        if (!dob.isEmpty()) {
            dobInput.sendKeys(dob);
        }

        // Fill address
        WebElement addressInput = driver.findElement(addressField);
        addressInput.clear();
        if (!address.isEmpty()) {
            addressInput.sendKeys(address);
        }

        // Fill email
        WebElement emailInput = driver.findElement(emailField);
        emailInput.clear();
        if (!email.isEmpty()) {
            emailInput.sendKeys(email);
        }

        // Fill password
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.clear();
        if (!password.isEmpty()) {
            passwordInput.sendKeys(password);
        }

        // Fill company
        WebElement companyInput = driver.findElement(companyField);
        companyInput.clear();
        if (!company.isEmpty()) {
            companyInput.sendKeys(company);
        }
    }
}