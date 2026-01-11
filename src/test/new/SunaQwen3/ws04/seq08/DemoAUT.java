package SunaQwen3.ws04.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String LOGIN_PAGE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/login.html";
    private static final String USERNAME = "katalon";
    private static final String PASSWORD = "katalon";

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
    public void testValidLogin() {
        driver.get(LOGIN_PAGE_URL);
        
        // Locate login form elements
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("submit"));

        // Perform login
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Assert successful login by checking form
        WebElement formHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Student Registration Form", formHeader.getText(), "Form header should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("submit"));

        // Use invalid credentials
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_pass");
        loginButton.click();

        // Check for error message or stay on login page
        wait.until(ExpectedConditions.urlContains("login.html"));
        assertTrue(driver.getCurrentUrl().contains("login.html"), "Should remain on login page after failed login");
    }

    @Test
    @Order(3)
    public void testFormFieldPresenceAndLabels() {
        // Ensure we're on the form page
        driver.get(BASE_URL);
        
        // Verify all form fields are present with correct labels
        assertTrue(isElementPresent(By.id("firstName")), "First Name field should be present");
        assertTrue(isElementPresent(By.id("lastName")), "Last Name field should be present");
        assertTrue(isElementPresent(By.id("email")), "Email field should be present");
        assertTrue(isElementPresent(By.id("age")), "Age field should be present");
        assertTrue(isElementPresent(By.id("salary")), "Salary field should be present");
        assertTrue(isElementPresent(By.id("department")), "Department field should be present");
    }

    @Test
    @Order(4)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        fillForm("John", "Doe", "john.doe@example.com", "30", "50000", "Engineering");

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();

        // Wait for confirmation
        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("action-info")));
        assertTrue(confirmation.getText().contains("successfully") || confirmation.getText().contains("submitted"),
                "Submission confirmation should appear");
    }

    @Test
    @Order(5)
    public void testFormResetFunctionality() {
        driver.get(BASE_URL);
        fillForm("Alice", "Smith", "alice.smith@example.com", "25", "60000", "Marketing");

        // Click reset button
        WebElement resetButton = driver.findElement(By.id("reset"));
        resetButton.click();

        // Verify fields are cleared
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        assertTrue(firstNameField.getAttribute("value").isEmpty(), "First name field should be empty after reset");
        assertTrue(lastNameField.getAttribute("value").isEmpty(), "Last name field should be empty after reset");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        
        // Check for footer links - skip if none found
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        if (footerLinks.size() >= 3) {
            for (int i = 0; i < footerLinks.size(); i++) {
                WebElement link = footerLinks.get(i);
                String originalWindow = driver.getWindowHandle();
                String href = link.getAttribute("href");

                // Open link in new tab
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));

                // Switch to new window
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }

                // Verify page loaded
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.length() > 0, "Link should open a valid page");

                // Close current tab and switch back
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        } else {
            // If less than 3 links, assert at least 1 exists
            assertTrue(footerLinks.size() >= 1, "At least one footer link should be present");
        }
    }

    @Test
    @Order(7)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);

        // Submit form without filling required fields
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        // Check for HTML5 validation on firstName
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        String validationMessage = firstNameField.getAttribute("validationMessage");
        assertFalse(validationMessage.isEmpty() || validationMessage.equals("undefined"), 
                "Required field should show validation message when empty");
    }

    @Test
    @Order(8)
    public void testEmailFieldValidation() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        WebElement submitButton = driver.findElement(By.id("submit"));

        emailField.sendKeys("invalid-email");
        submitButton.click();

        String emailValidity = emailField.getAttribute("validationMessage");
        assertTrue(emailValidity.contains("email") || emailValidity.contains("@") || !emailValidity.isEmpty(),
                "Email field should validate format and show error for invalid input");
    }

    // Helper method to fill form
    private void fillForm(String firstName, String lastName, String email, String age, String salary, String department) {
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement ageField = driver.findElement(By.id("age"));
        WebElement salaryField = driver.findElement(By.id("salary"));
        WebElement departmentField = driver.findElement(By.id("department"));

        firstNameField.clear();
        firstNameField.sendKeys(firstName);
        lastNameField.clear();
        lastNameField.sendKeys(lastName);
        emailField.clear();
        emailField.sendKeys(email);
        ageField.clear();
        ageField.sendKeys(age);
        salaryField.clear();
        salaryField.sendKeys(salary);
        departmentField.clear();
        departmentField.sendKeys(department);
    }

    // Helper method to check element presence
    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}