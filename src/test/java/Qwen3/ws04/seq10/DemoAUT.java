package Qwen3.ws04.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class DemoAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
    void testPageTitleAndHeader_DisplayedCorrectly() {
        driver.get(BASE_URL);

        assertEquals("Katalon Studio", driver.getTitle(), "Page title should be 'Katalon Studio'");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Katalon Studio", header.getText(), "Main header should match page title");
    }

    @Test
    @Order(2)
    void testFormFields_ArePresentAndEditable() {
        driver.get(BASE_URL);

        // First Name
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
        assertTrue(firstName.isEnabled(), "First Name field should be enabled");
        firstName.sendKeys("John");
        assertEquals("John", firstName.getAttribute("value"), "First Name should contain entered value");

        // Last Name
        WebElement lastName = driver.findElement(By.name("lastName"));
        assertTrue(lastName.isEnabled(), "Last Name field should be enabled");
        lastName.sendKeys("Doe");
        assertEquals("Doe", lastName.getAttribute("value"), "Last Name should contain entered value");

        // Email
        WebElement email = driver.findElement(By.name("email"));
        assertTrue(email.isEnabled(), "Email field should be enabled");
        email.sendKeys("john.doe@example.com");
        assertEquals("john.doe@example.com", email.getAttribute("value"), "Email should contain entered value");

        // Password
        WebElement password = driver.findElement(By.name("password"));
        assertTrue(password.isEnabled(), "Password field should be enabled");
        password.sendKeys("SecurePass123!");
        assertEquals("SecurePass123!", password.getAttribute("value"), "Password should be filled");

        // Gender selection
        WebElement maleRadio = driver.findElement(By.xpath("//input[@value='male']"));
        WebElement femaleRadio = driver.findElement(By.xpath("//input[@value='female']"));

        assertFalse(maleRadio.isSelected(), "Male radio should be unselected initially");
        maleRadio.click();
        assertTrue(maleRadio.isSelected(), "Male radio should be selectable");
        assertFalse(femaleRadio.isSelected(), "Female radio should not be selected when male is selected");

        // Date of Birth
        WebElement dob = driver.findElement(By.name("dob"));
        assertTrue(dob.isEnabled(), "Date of Birth field should be enabled");
        dob.sendKeys("1990-01-01");
        assertEquals("1990-01-01", dob.getAttribute("value"), "DOB should contain entered value");
    }

    @Test
    @Order(3)
    void testAddressTextArea_AcceptsInput() {
        driver.get(BASE_URL);

        WebElement address = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("address")));
        assertTrue(address.isEnabled(), "Address textarea should be enabled");
        String longAddress = "123 Main Street\nApt 4B\nNew York, NY 10001";
        address.sendKeys(longAddress);
        assertEquals(longAddress, address.getAttribute("value"), "Address should contain multi-line input");
    }

    @Test
    @Order(4)
    void testAgreementCheckbox_ToggleAndValidation() {
        driver.get(BASE_URL);

        WebElement agreement = wait.until(ExpectedConditions.elementToBeClickable(By.id("inlineCheckbox1")));
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(), 'Submit')]"));

        assertFalse(agreement.isSelected(), "Agreement checkbox should be unchecked initially");
        agreement.click();
        assertTrue(agreement.isSelected(), "Agreement checkbox should be selectable");
        assertTrue(submitButton.isEnabled(), "Submit button should be enabled when agreement is checked");
    }

    @Test
    @Order(5)
    void testSubmitForm_SuccessMessageDisplayed() {
        driver.get(BASE_URL);

        fillFormWithValidData();

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Submit')]")));
        submitButton.click();

        // Success alert
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(alert.isDisplayed(), "Success alert should be displayed after submission");
        String alertText = alert.getText();
        assertTrue(alertText.contains("Customer has been added successfully"), "Success message should confirm customer addition");
    }

    @Test
    @Order(6)
    void testSubmitWithoutAgreement_ErrorMessageDisplayed() {
        driver.get(BASE_URL);

        fillFormWithoutAgreement();

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Submit')]")));
        submitButton.click();

        // Error alert
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(alert.isDisplayed(), "Error alert should be displayed if agreement is not checked");
        assertTrue(alert.getText().contains("Please agree to the terms"), "Error message should indicate agreement is required");
    }

    @Test
    @Order(7)
    void testEmailValidation_InvalidFormatShowsError() {
        driver.get(BASE_URL);

        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement firstName = driver.findElement(By.name("firstName"));
        email.sendKeys("not-an-email");
        firstName.click(); // Blur email field

        // Wait for validation message
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Please provide a valid email')]")));
        assertTrue(errorMsg.isDisplayed(), "Validation error should appear for invalid email");
    }

    @Test
    @Order(8)
    void testPasswordValidation_StrengthIndicator() {
        driver.get(BASE_URL);

        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        WebElement feedback = driver.findElement(By.id("passwordHelp"));

        password.sendKeys("123");
        assertEquals("Weak", feedback.getText(), "Should show 'Weak' for short password");

        password.clear();
        password.sendKeys("SecurePass123!");
        assertEquals("Strong", feedback.getText(), "Should show 'Strong' for complex password");
    }

    @Test
    @Order(9)
    void testExternalFooterLink_GitHub_OpenInNewTab() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github.com']")));
        String originalWindow = driver.getWindowHandle();

        footerLink.sendKeys(Keys.CONTROL, Keys.RETURN); // Open in new tab

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("github.com"));
                assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub link should open github.com domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback: same tab navigation
        driver.navigate().refresh(); // Ensure element is attached
        footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github.com']")));
        footerLink.click();

        wait.until(ExpectedConditions.urlContains("github.com"));
        assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub link should redirect to github.com");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(10)
    void testFormReset_ClearsAllFields() {
        driver.get(BASE_URL);

        fillFormWithValidData();

        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Reset')]")));
        resetButton.click();

        // Verify fields are cleared
        assertEquals("", driver.findElement(By.name("firstName")).getAttribute("value"), "First Name should be cleared");
        assertEquals("", driver.findElement(By.name("lastName")).getAttribute("value"), "Last Name should be cleared");
        assertEquals("", driver.findElement(By.name("email")).getAttribute("value"), "Email should be cleared");
        assertEquals("", driver.findElement(By.name("password")).getAttribute("value"), "Password should be cleared");
        assertEquals("", driver.findElement(By.name("address")).getAttribute("value"), "Address should be cleared");
        assertFalse(driver.findElement(By.id("inlineCheckbox1")).isSelected(), "Agreement checkbox should be unchecked");
        assertFalse(driver.findElement(By.xpath("//input[@value='male']")).isSelected(), "Gender should be deselected");
        assertEquals("", driver.findElement(By.name("dob")).getAttribute("value"), "DOB should be cleared");
    }

    @Test
    @Order(11)
    void testPageFooter_CopyrightTextPresent() {
        driver.get(BASE_URL);

        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
        String footerText = footer.getText();
        assertTrue(footerText.contains("Katalon LLC"), "Footer should contain copyright information");
        assertTrue(footerText.contains("All rights reserved"), "Footer should include 'All rights reserved'");
    }

    private void fillFormWithValidData() {
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
        WebElement lastName = driver.findElement(By.name("lastName"));
        WebElement email = driver.findElement(By.name("email"));
        WebElement password = driver.findElement(By.name("password"));
        WebElement address = driver.findElement(By.name("address"));
        WebElement maleRadio = driver.findElement(By.xpath("//input[@value='male']"));
        WebElement dob = driver.findElement(By.name("dob"));
        WebElement agreement = driver.findElement(By.id("inlineCheckbox1"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        password.sendKeys("SecurePass123!");
        address.sendKeys("123 Main Street, New York, NY");
        maleRadio.click();
        dob.sendKeys("1990-01-01");
        agreement.click();
    }

    private void fillFormWithoutAgreement() {
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
        WebElement lastName = driver.findElement(By.name("lastName"));
        WebElement email = driver.findElement(By.name("email"));
        WebElement password = driver.findElement(By.name("password"));
        WebElement address = driver.findElement(By.name("address"));
        WebElement maleRadio = driver.findElement(By.xpath("//input[@value='male']"));
        WebElement dob = driver.findElement(By.name("dob"));

        firstName.sendKeys("Jane");
        lastName.sendKeys("Smith");
        email.sendKeys("jane.smith@example.com");
        password.sendKeys("AnotherPass456!");
        address.sendKeys("456 Oak Avenue, Boston, MA");
        maleRadio.click();
        dob.sendKeys("1985-05-15");
    }
}