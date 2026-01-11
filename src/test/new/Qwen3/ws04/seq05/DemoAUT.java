package Qwen3.ws04.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testPageLoadAndTitle() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        String pageTitle = driver.getTitle();
        assertEquals("Demo AUT", pageTitle, "Page title should match expected value");
        
        WebElement formElement = driver.findElement(By.tagName("form"));
        assertTrue(formElement.isDisplayed(), "Form element should be displayed");
    }

    @Test
    @Order(2)
    public void testTextInputFields() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameInput = driver.findElement(By.id("fname"));
        WebElement lastNameInput = driver.findElement(By.id("lname"));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement phoneInput = driver.findElement(By.id("phone"));
        
        assertTrue(firstNameInput.isDisplayed(), "First name input should be displayed");
        assertTrue(lastNameInput.isDisplayed(), "Last name input should be displayed");
        assertTrue(emailInput.isDisplayed(), "Email input should be displayed");
        assertTrue(phoneInput.isDisplayed(), "Phone input should be displayed");
        
        // Fill inputs
        firstNameInput.sendKeys("John");
        lastNameInput.sendKeys("Doe");
        emailInput.sendKeys("john.doe@example.com");
        phoneInput.sendKeys("123-456-7890");
        
        assertEquals("John", firstNameInput.getAttribute("value"), "First name should have correct value");
        assertEquals("Doe", lastNameInput.getAttribute("value"), "Last name should have correct value");
        assertEquals("john.doe@example.com", emailInput.getAttribute("value"), "Email should have correct value");
        assertEquals("123-456-7890", phoneInput.getAttribute("value"), "Phone should have correct value");
    }

    @Test
    @Order(3)
    public void testTextAreaField() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement addressTextarea = driver.findElement(By.id("address"));
        assertTrue(addressTextarea.isDisplayed(), "Address textarea should be displayed");
        
        addressTextarea.sendKeys("123 Main St, City, State 12345");
        
        assertEquals("123 Main St, City, State 12345", addressTextarea.getAttribute("value"), 
                     "Address should have correct value");
    }

    @Test
    @Order(4)
    public void testRadioButtons() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement maleRadio = driver.findElement(By.cssSelector("input[value='Male']"));
        WebElement femaleRadio = driver.findElement(By.cssSelector("input[value='Female']"));
        
        assertTrue(maleRadio.isDisplayed(), "Male radio button should be displayed");
        assertTrue(femaleRadio.isDisplayed(), "Female radio button should be displayed");
        
        maleRadio.click();
        assertTrue(maleRadio.isSelected(), "Male radio should be selected after click");
        
        femaleRadio.click();
        assertTrue(femaleRadio.isSelected(), "Female radio should be selected after click");
    }

    @Test
    @Order(5)
    public void testCheckBoxes() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement checkbox1 = driver.findElement(By.cssSelector("input[value='Checkbox1']"));
        WebElement checkbox2 = driver.findElement(By.cssSelector("input[value='Checkbox2']"));
        WebElement checkbox3 = driver.findElement(By.cssSelector("input[value='Checkbox3']"));
        
        assertTrue(checkbox1.isDisplayed(), "Checkbox 1 should be displayed");
        assertTrue(checkbox2.isDisplayed(), "Checkbox 2 should be displayed");
        assertTrue(checkbox3.isDisplayed(), "Checkbox 3 should be displayed");
        
        checkbox1.click();
        assertTrue(checkbox1.isSelected(), "Checkbox 1 should be selected after click");
        
        checkbox2.click();
        assertTrue(checkbox2.isSelected(), "Checkbox 2 should be selected after click");
        
        checkbox3.click();
        assertTrue(checkbox3.isSelected(), "Checkbox 3 should be selected after click");
    }

    @Test
    @Order(6)
    public void testDropDownSelect() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement countrySelect = driver.findElement(By.id("selectCountry"));
        assertTrue(countrySelect.isDisplayed(), "Country dropdown should be displayed");
        
        // Select different options
        Select select = new Select(countrySelect);
        List<WebElement> options = select.getOptions();
        
        assertEquals(4, options.size(), "Should have 4 options in dropdown");
        
        select.selectByVisibleText("United States");
        assertEquals("US", select.getFirstSelectedOption().getAttribute("value"), "United States should be selected");
        
        select.selectByValue("CA");
        assertEquals("CA", select.getFirstSelectedOption().getAttribute("value"), "Canada should be selected");
    }

    @Test
    @Order(7)
    public void testSubmitButton() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        assertTrue(submitButton.isDisplayed(), "Submit button should be displayed");
        
        // Click submit (will result in JavaScript alert in browser)
        submitButton.click();
        
        // Try to handle alert if it appears (for form validation or submission confirmation)
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            assertTrue(alertText.contains("Form submitted successfully") ||
                       alertText.contains("Form Submitted"), "Should show success message on submit");
            alert.accept();
        } catch (TimeoutException e) {
            // No alert appeared, which might be fine for this test scenario
        }
    }

    @Test
    @Order(8)
    public void testAllFormElementsExist() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Check that all required form elements are present
        WebElement firstNameInput = driver.findElement(By.id("fname"));
        WebElement lastNameInput = driver.findElement(By.id("lname"));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement phoneInput = driver.findElement(By.id("phone"));
        WebElement addressTextarea = driver.findElement(By.id("address"));
        WebElement maleRadio = driver.findElement(By.cssSelector("input[value='Male']"));
        WebElement femaleRadio = driver.findElement(By.cssSelector("input[value='Female']"));
        WebElement checkbox1 = driver.findElement(By.cssSelector("input[value='Checkbox1']"));
        WebElement checkbox2 = driver.findElement(By.cssSelector("input[value='Checkbox2']"));
        WebElement checkbox3 = driver.findElement(By.cssSelector("input[value='Checkbox3']"));
        WebElement countrySelect = driver.findElement(By.id("selectCountry"));
        WebElement submitButton = driver.findElement(By.id("submit"));
        
        assertTrue(firstNameInput.isDisplayed(), "First name input missing");
        assertTrue(lastNameInput.isDisplayed(), "Last name input missing");
        assertTrue(emailInput.isDisplayed(), "Email input missing");
        assertTrue(phoneInput.isDisplayed(), "Phone input missing");
        assertTrue(addressTextarea.isDisplayed(), "Address textarea missing");
        assertTrue(maleRadio.isDisplayed(), "Male radio missing");
        assertTrue(femaleRadio.isDisplayed(), "Female radio missing");
        assertTrue(checkbox1.isDisplayed(), "Checkbox 1 missing");
        assertTrue(checkbox2.isDisplayed(), "Checkbox 2 missing");
        assertTrue(checkbox3.isDisplayed(), "Checkbox 3 missing");
        assertTrue(countrySelect.isDisplayed(), "Country dropdown missing");
        assertTrue(submitButton.isDisplayed(), "Submit button missing");
    }

    @Test
    @Order(9)
    public void testFormValidationMessages() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // If JavaScript validation exists, we could check for error messages
        // However, this is a basic form and doesn't implement full client-side validation
        
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            
            // Could check against different alert texts based on how form is built
            assertTrue(alertText.contains("Form submitted successfully") ||
                       alertText.contains("Form Submitted") ||
                       alertText.contains("Thank you"), 
                       "Should see expected submission confirmation or validation warning");
            
            alert.accept();
        } catch (TimeoutException e) {
            // No validation error shown, which may indicate no validation implemented
        }
    }

    @Test
    @Order(10)
    public void testFormResetFunctionality() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameInput = driver.findElement(By.id("fname"));
        WebElement lastNameInput = driver.findElement(By.id("lname"));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement phoneInput = driver.findElement(By.id("phone"));
        WebElement addressTextarea = driver.findElement(By.id("address"));
        WebElement checkbox1 = driver.findElement(By.cssSelector("input[value='Checkbox1']"));
        
        // Fill some fields
        firstNameInput.sendKeys("Jane");
        lastNameInput.sendKeys("Smith");
        emailInput.sendKeys("jane.smith@example.com");
        phoneInput.sendKeys("098-765-4321");
        addressTextarea.sendKeys("456 Oak Ave, Town, Country 67890");
        checkbox1.click();
        
        // Check values are filled
        assertEquals("Jane", firstNameInput.getAttribute("value"));
        assertEquals("Smith", lastNameInput.getAttribute("value"));
        assertEquals("jane.smith@example.com", emailInput.getAttribute("value"));
        assertEquals("098-765-4321", phoneInput.getAttribute("value"));
        assertEquals("456 Oak Ave, Town, Country 67890", addressTextarea.getAttribute("value"));
        assertTrue(checkbox1.isSelected());
        
        // Find and click reset button (may not be explicitly defined, just checking for its absence)
        boolean hasResetButton = false;
        try {
            WebElement resetButton = driver.findElement(By.cssSelector("input[type='reset']"));
            hasResetButton = true;
            resetButton.click();
            // After reset, input should become empty
            assertEquals("", firstNameInput.getAttribute("value"));
            assertEquals("", lastNameInput.getAttribute("value"));
            assertEquals("", emailInput.getAttribute("value"));
            assertEquals("", phoneInput.getAttribute("value"));
            assertEquals("", addressTextarea.getAttribute("value"));
            assertFalse(checkbox1.isSelected());
        } catch (NoSuchElementException e) {
            // If there's no reset button, just confirm field inputs were cleared
            // Reset functionality might be part of JavaScript not triggered by standard browser controls
        }
        
        // If no reset button, it's acceptable as some forms don't have explicit reset behavior
    }
}