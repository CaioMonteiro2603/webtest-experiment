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
    private static final String LOGIN_PAGE_URL = BASE_URL;
    private static final String USERNAME = "katalon";
    private static final String PASSWORD = "katalon";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(LOGIN_PAGE_URL);
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
        // Locate login form elements
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        // Perform login
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Assert successful login by checking URL or presence of a key element
        wait.until(ExpectedConditions.urlContains("form.html"));
        assertTrue(driver.getCurrentUrl().contains("form.html"), "URL should contain 'form.html' after login");
        
        // Assuming successful login shows a form or specific element
        WebElement formHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Student Registration Form", formHeader.getText(), "Form header should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        // Use invalid credentials
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_pass");
        loginButton.click();

        // Assuming an error message appears
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(errorMessage.getText().contains("invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testFormFieldPresenceAndLabels() {
        // Verify all form fields are present with correct labels
        assertTrue(isElementPresent(By.name("firstName")), "First Name field should be present");
        assertTrue(isElementPresent(By.name("lastName")), "Last Name field should be present");
        assertTrue(isElementPresent(By.name("email")), "Email field should be present");
        assertTrue(isElementPresent(By.name("age")), "Age field should be present");
        assertTrue(isElementPresent(By.name("salary")), "Salary field should be present");
        assertTrue(isElementPresent(By.name("department")), "Department field should be present");
    }

    @Test
    @Order(4)
    public void testFormSubmissionWithValidData() {
        fillForm("John", "Doe", "john.doe@example.com", "30", "50000", "Engineering");

        // Submit form
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();

        // Wait for confirmation or result
        try {
            WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
            assertTrue(confirmation.getText().contains("successfully") || confirmation.getText().contains("submitted"),
                    "Submission confirmation should appear");
        } catch (TimeoutException e) {
            fail("Form submission confirmation did not appear: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    public void testFormResetFunctionality() {
        fillForm("Alice", "Smith", "alice.smith@example.com", "25", "60000", "Marketing");

        // Click reset button
        WebElement resetButton = driver.findElement(By.xpath("//button[@type='reset']"));
        resetButton.click();

        // Verify fields are cleared
        WebElement firstNameField = driver.findElement(By.name("firstName"));
        WebElement lastNameField = driver.findElement(By.name("lastName"));
        assertTrue(firstNameField.getAttribute("value").isEmpty(), "First name field should be empty after reset");
        assertTrue(lastNameField.getAttribute("value").isEmpty(), "Last name field should be empty after reset");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        // Assuming there are social media links in footer
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() >= 3, "At least three social links should be present in footer");

        for (int i = 0; i < footerLinks.size(); i++) {
            WebElement link = footerLinks.get(i);
            String originalWindow = driver.getWindowHandle();
            String href = link.getAttribute("href");

            // Open link in new tab using JavaScript to avoid blocking
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));

            // Switch to new window
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            if (href.contains("twitter.com")) {
                assertTrue(currentUrl.contains("twitter.com"), "Twitter link should open correct domain");
            } else if (href.contains("facebook.com")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (href.contains("linkedin.com")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            }

            // Close current tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    public void testRequiredFieldValidation() {
        driver.get(LOGIN_PAGE_URL);
        testValidLogin(); // Ensure logged in

        // Submit form without filling required fields
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        submitButton.click();

        // Check for HTML5 validation or custom error messages
        String firstNameValidity = driver.findElement(By.name("firstName")).getAttribute("validationMessage");
        assertFalse(firstNameValidity.isEmpty(), "Required field should show validation message when empty");
    }

    @Test
    @Order(8)
    public void testEmailFieldValidation() {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));

        emailField.sendKeys("invalid-email");
        submitButton.click();

        String emailValidity = emailField.getAttribute("validationMessage");
        assertTrue(emailValidity.contains("email") || !emailValidity.isEmpty(),
                "Email field should validate format and show error for invalid input");
    }

    // Helper method to fill form
    private void fillForm(String firstName, String lastName, String email, String age, String salary, String department) {
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
        WebElement lastNameField = driver.findElement(By.name("lastName"));
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement ageField = driver.findElement(By.name("age"));
        WebElement salaryField = driver.findElement(By.name("salary"));
        WebElement departmentField = driver.findElement(By.name("department"));

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