package geminiPRO.ws04.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for a sample HTML form.
 * This suite tests various form elements including text fields, radio buttons,
 * dropdowns, checkboxes, and validates successful form submission.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HtmlFormComprehensiveTest {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments for headless mode as required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    void testPageTitleAndHeader() {
        assertEquals("Demo Form", driver.getTitle(), "Page title should be correct.");
        WebElement header = driver.findElement(By.tagName("h1"));
        assertEquals("HTML Form Example", header.getText(), "Main header text should be correct.");
    }

    @Test
    @Order(2)
    void testTextFieldInteractions() {
        String firstName = "Caio";
        String lastName = "Gemini";
        
        WebElement firstNameField = driver.findElement(By.id("first-name"));
        firstNameField.sendKeys(firstName);
        
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys(lastName);

        assertEquals(firstName, firstNameField.getAttribute("value"), "First name field should contain the entered value.");
        assertEquals(lastName, lastNameField.getAttribute("value"), "Last name field should contain the entered value.");
    }

    @Test
    @Order(3)
    void testGenderRadioButtonSelection() {
        WebElement maleRadio = driver.findElement(By.xpath("//input[@name='gender' and @value='Male']"));
        WebElement femaleRadio = driver.findElement(By.xpath("//input[@name='gender' and @value='Female']"));
        WebElement inBetweenRadio = driver.findElement(By.xpath("//input[@name='gender' and @value='In Between']"));

        // Select Female
        femaleRadio.click();
        assertTrue(femaleRadio.isSelected(), "Female radio button should be selected.");
        assertFalse(maleRadio.isSelected(), "Male radio button should not be selected.");
        assertFalse(inBetweenRadio.isSelected(), "In Between radio button should not be selected.");

        // Select Male
        maleRadio.click();
        assertTrue(maleRadio.isSelected(), "Male radio button should be selected.");
        assertFalse(femaleRadio.isSelected(), "Female radio button should not be selected.");
    }
    
    @Test
    @Order(4)
    void testRoleDropdownSelection() {
        WebElement roleDropdownElement = driver.findElement(By.id("role"));
        Select roleSelect = new Select(roleDropdownElement);

        // Select by visible text
        roleSelect.selectByVisibleText("QA");
        assertEquals("QA", roleSelect.getFirstSelectedOption().getText(), "QA should be selected by visible text.");

        // Select by value
        roleSelect.selectByValue("dev");
        assertEquals("Developer", roleSelect.getFirstSelectedOption().getText(), "Developer should be selected by value.");
    }

    @Test
    @Order(5)
    void testDevelopmentCheckboxes() {
        WebElement readBooksCheckbox = driver.findElement(By.xpath("//label[contains(., 'Read books')]/input"));
        WebElement onlineCoursesCheckbox = driver.findElement(By.xpath("//label[contains(., 'Take online courses')]/input"));
        WebElement contributeCheckbox = driver.findElement(By.xpath("//label[contains(., 'Contribute to open source')]/input"));

        // Select two checkboxes
        readBooksCheckbox.click();
        onlineCoursesCheckbox.click();

        assertTrue(readBooksCheckbox.isSelected(), "'Read books' checkbox should be selected.");
        assertTrue(onlineCoursesCheckbox.isSelected(), "'Take online courses' checkbox should be selected.");
        assertFalse(contributeCheckbox.isSelected(), "'Contribute' checkbox should remain unselected.");

        // Unselect one checkbox
        readBooksCheckbox.click();
        assertFalse(readBooksCheckbox.isSelected(), "'Read books' checkbox should be unselected.");
    }
    
    @Test
    @Order(6)
    void testSuccessfulFormSubmission() {
        // Fill out all fields
        String firstName = "Test";
        String lastName = "User";
        driver.findElement(By.id("first-name")).sendKeys(firstName);
        driver.findElement(By.id("last-name")).sendKeys(lastName);
        driver.findElement(By.xpath("//input[@name='gender' and @value='Male']")).click();
        driver.findElement(By.id("dob")).sendKeys("09/03/2025");
        driver.findElement(By.id("address")).sendKeys("123 Test Street");
        driver.findElement(By.id("email")).sendKeys("test.user@example.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("company")).sendKeys("Gemini Corp");
        
        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("Manager");

        driver.findElement(By.id("expectation")).sendKeys("Great");
        driver.findElement(By.xpath("//label[contains(., 'Take online courses')]/input")).click();
        driver.findElement(By.id("comment")).sendKeys("This is a test comment.");

        // Submit the form
        driver.findElement(By.id("submit")).click();
        
        // Wait for and verify the success page
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        assertEquals("Successfully submitted!", successMessage.getText(), "Success message should be displayed after submission.");
        
        // Verify URL contains submitted data
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("first-name=" + firstName), "URL should contain the submitted first name.");
        assertTrue(currentUrl.contains("last-name=" + lastName), "URL should contain the submitted last name.");
        assertTrue(currentUrl.contains("gender=Male"), "URL should contain the submitted gender.");
    }
}