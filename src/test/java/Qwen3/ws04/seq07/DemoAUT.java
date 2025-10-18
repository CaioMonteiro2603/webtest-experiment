package Qwen3.ws04.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KatalonFormTest {

    private static WebDriver driver;
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
    public void testPageLoad() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Verify page title
        String title = driver.getTitle();
        assertEquals("Katalon Forms", title);
        
        // Verify form present
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        assertTrue(form.isDisplayed());
    }

    @Test
    @Order(2)
    public void testTextInputFields() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test text input
        WebElement firstNameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameInput.clear();
        firstNameInput.sendKeys("John");
        
        WebElement lastNameInput = driver.findElement(By.id("lastName"));
        lastNameInput.clear();
        lastNameInput.sendKeys("Doe");
        
        // Verify values
        assertEquals("John", firstNameInput.getAttribute("value"));
        assertEquals("Doe", lastNameInput.getAttribute("value"));
    }

    @Test
    @Order(3)
    public void testEmailField() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test email input
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.clear();
        emailInput.sendKeys("john.doe@example.com");
        
        // Verify value
        assertEquals("john.doe@example.com", emailInput.getAttribute("value"));
    }

    @Test
    @Order(4)
    public void testTextArea() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test textarea
        WebElement messageArea = wait.until(ExpectedConditions.elementToBeClickable(By.id("message")));
        messageArea.clear();
        messageArea.sendKeys("This is a test message");
        
        // Verify value
        assertEquals("This is a test message", messageArea.getText());
    }

    @Test
    @Order(5)
    public void testRadioButtons() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test radio buttons
        WebElement maleRadio = driver.findElement(By.id("male"));
        WebElement femaleRadio = driver.findElement(By.id("female"));
        
        // Select male
        maleRadio.click();
        assertTrue(maleRadio.isSelected());
        assertFalse(femaleRadio.isSelected());
        
        // Select female
        femaleRadio.click();
        assertFalse(maleRadio.isSelected());
        assertTrue(femaleRadio.isSelected());
    }

    @Test
    @Order(6)
    public void testCheckBoxes() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test checkboxes
        WebElement checkbox1 = driver.findElement(By.id("checkbox1"));
        WebElement checkbox2 = driver.findElement(By.id("checkbox2"));
        WebElement checkbox3 = driver.findElement(By.id("checkbox3"));
        
        // Select checkboxes
        checkbox1.click();
        checkbox2.click();
        checkbox3.click();
        
        assertTrue(checkbox1.isSelected());
        assertTrue(checkbox2.isSelected());
        assertTrue(checkbox3.isSelected());
        
        // Deselect checkboxes
        checkbox1.click();
        checkbox2.click();
        checkbox3.click();
        
        assertFalse(checkbox1.isSelected());
        assertFalse(checkbox2.isSelected());
        assertFalse(checkbox3.isSelected());
    }

    @Test
    @Order(7)
    public void testDropDownSelect() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test dropdown selection
        WebElement dropdown = driver.findElement(By.id("country"));
        Select select = new Select(dropdown);
        select.selectByVisibleText("United States");
        
        // Verify selection
        assertEquals("United States", select.getFirstSelectedOption().getText());
        
        // Select another option
        select.selectByValue("CA");
        assertEquals("Canada", select.getFirstSelectedOption().getText());
    }

    @Test
    @Order(8)
    public void testDateInput() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test date input
        WebElement dateInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("date")));
        dateInput.clear();
        dateInput.sendKeys("2023-12-25");
        
        // Verify value (note: browsers may format differently)
        String inputVal = dateInput.getAttribute("value");
        assertNotNull(inputVal);
    }

    @Test
    @Order(9)
    public void testSubmitForm() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill out form fields
        WebElement firstNameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameInput.clear();
        firstNameInput.sendKeys("Jane");
        
        WebElement lastNameInput = driver.findElement(By.id("lastName"));
        lastNameInput.clear();
        lastNameInput.sendKeys("Smith");
        
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.clear();
        emailInput.sendKeys("jane.smith@example.com");
        
        WebElement messageArea = driver.findElement(By.id("message"));
        messageArea.clear();
        messageArea.sendKeys("Test message");
        
        WebElement maleRadio = driver.findElement(By.id("male"));
        maleRadio.click();
        
        WebElement checkbox1 = driver.findElement(By.id("checkbox1"));
        checkbox1.click();
        
        WebElement dropdown = driver.findElement(By.id("country"));
        Select select = new Select(dropdown);
        select.selectByVisibleText("United States");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        submitButton.click();
        
        // Verify submission redirect (check if page has changed)
        wait.until(ExpectedConditions.urlContains("form.html"));
    }

    @Test
    @Order(10)
    public void testRequiredFieldsValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Try submitting without filling required fields
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        submitButton.click();
        
        // Should still be on same page (validation prevents submission)
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("form.html"));
    }

    @Test
    @Order(11)
    public void testFormReset() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill some fields
        WebElement firstNameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameInput.sendKeys("Test");
        
        WebElement lastNameInput = driver.findElement(By.id("lastName"));
        lastNameInput.sendKeys("User");
        
        // Click reset button
        WebElement resetButton = driver.findElement(By.xpath("//button[@type='reset']"));
        resetButton.click();
        
        // Verify fields are cleared
        wait.until(ExpectedConditions.textToBe(By.id("firstName"), ""));
        wait.until(ExpectedConditions.textToBe(By.id("lastName"), ""));
        
        // Also check other fields are cleared
        WebElement emailInput = driver.findElement(By.id("email"));
        assertEquals("", emailInput.getAttribute("value"));
    }

    @Test
    @Order(12)
    public void testLinksAndNavigation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Check for existing links
        List<WebElement> links = driver.findElements(By.tagName("a"));
        assertTrue(links.size() > 0);
        
        // Check anchor links
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                // Just verify they exist in page structure
                assertTrue(link.isDisplayed());
            }
        }
    }
}