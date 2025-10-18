package Qwen3.ws05.seq09;

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
public class CacTatFormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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

        assertEquals("Customer Support", driver.getTitle(), "Page title should be 'Customer Support'");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("CAC TAT", header.getText(), "Main header should be CAC TAT");
    }

    @Test
    @Order(2)
    void testFormFieldsArePresent() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        WebElement lastName = driver.findElement(By.id("last-name"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement requestType = driver.findElement(By.id("request-type"));
        WebElement message = driver.findElement(By.id("message"));
        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        WebElement submitButton = driver.findElement(By.className("btn-primary"));

        assertTrue(firstName.isDisplayed(), "First name field should be displayed");
        assertTrue(lastName.isDisplayed(), "Last name field should be displayed");
        assertTrue(email.isDisplayed(), "Email field should be displayed");
        assertTrue(phone.isDisplayed(), "Phone field should be displayed");
        assertTrue(requestType.isDisplayed(), "Request type dropdown should be displayed");
        assertTrue(message.isDisplayed(), "Message textarea should be displayed");
        assertTrue(agreeCheckbox.isDisplayed(), "Agree checkbox should be displayed");
        assertTrue(submitButton.isDisplayed(), "Submit button should be displayed");
    }

    @Test
    @Order(3)
    void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Silva");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("caio.silva@example.com");

        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("5551234567");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Complaint");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("This is a test message from an automated test.");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-message")));
        assertTrue(successMessage.isDisplayed(), "Success message should appear");
        assertEquals("Thank you for contacting us! We will be in touch shortly.", successMessage.getText().trim(), "Success message should match expected");
    }

    @Test
    @Order(4)
    void testFirstNameIsRequired() {
        driver.get(BASE_URL);

        WebElement lastName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("last-name")));
        lastName.sendKeys("Silva");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("caio@example.com");

        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("5551234567");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Question");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("Need help.");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        submitButton.click();

        String firstNameClass = driver.findElement(By.id("first-name")).getAttribute("class");
        assertTrue(firstNameClass.contains("error"), "First name field should have error class when empty");
    }

    @Test
    @Order(5)
    void testLastNameIsRequired() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("caio@example.com");

        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("5551234567");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Question");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("Need help.");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        submitButton.click();

        String lastNameClass = driver.findElement(By.id("last-name")).getAttribute("class");
        assertTrue(lastNameClass.contains("error"), "Last name field should have error class when empty");
    }

    @Test
    @Order(6)
    void testEmailIsRequired() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Silva");

        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("5551234567");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Question");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("Need help.");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        submitButton.click();

        String emailClass = driver.findElement(By.id("email")).getAttribute("class");
        assertTrue(emailClass.contains("error"), "Email field should have error class when empty");
    }

    @Test
    @Order(7)
    void testEmailInvalidFormat() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Silva");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("invalid-email");

        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("5551234567");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Question");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("Need help.");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        submitButton.click();

        String emailClass = driver.findElement(By.id("email")).getAttribute("class");
        assertTrue(emailClass.contains("error"), "Email field should have error class on invalid format");
    }

    @Test
    @Order(8)
    void testMessageIsRequired() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Silva");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("caio@example.com");

        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("5551234567");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Question");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        submitButton.click();

        String messageClass = driver.findElement(By.id("message")).getAttribute("class");
        assertTrue(messageClass.contains("error"), "Message field should have error class when empty");
    }

    @Test
    @Order(9)
    void testAgreeCheckboxIsRequired() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Silva");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("caio@example.com");

        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("5551234567");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Question");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("Need help.");

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        submitButton.click();

        WebElement agreeLabel = driver.findElement(By.cssSelector("label[for='agree']"));
        String classAttr = agreeLabel.getAttribute("class");
        assertTrue(classAttr.contains("error"), "Agree checkbox label should show error when not checked");
    }

    @Test
    @Order(10)
    void testRequestTypeDropdownOptions() {
        driver.get(BASE_URL);

        Select requestType = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("request-type"))));
        java.util.List<WebElement> options = requestType.getOptions();

        assertEquals(4, options.size(), "Request type dropdown should have 4 options");

        java.util.List<String> expectedOptions = java.util.Arrays.asList("Select an issue...", "Question", "Complaint", "Other");
        for (int i = 0; i < expectedOptions.size(); i++) {
            assertEquals(expectedOptions.get(i), options.get(i).getText(), "Dropdown option should match");
        }
    }

    @Test
    @Order(11)
    void testPhoneFieldAcceptsOnlyDigits() {
        driver.get(BASE_URL);

        WebElement phoneField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneField.sendKeys("abc123xyz");

        assertEquals("123", phoneField.getAttribute("value"), "Phone field should only accept digits");
    }

    @Test
    @Order(12)
    void testRequestTypeSelectionAffectsAppearance() {
        driver.get(BASE_URL);

        Select requestType = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("request-type"))));
        requestType.selectByVisibleText("Complaint");

        WebElement messageField = driver.findElement(By.id("message"));
        String backgroundColor = messageField.getCssValue("background-color");
        assertEquals("rgba(255, 0, 0, 1)", backgroundColor, "Message field should turn red for complaint");
    }

    @Test
    @Order(13)
    void testSubmitButtonDisabledInitially() {
        driver.get(BASE_URL);

        WebElement submitButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("btn-primary")));
        assertFalse(submitButton.isEnabled(), "Submit button should be disabled initially");
    }

    @Test
    @Order(14)
    void testSubmitButtonEnabledWhenFormValid() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Silva");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("caio@example.com");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("Test message");

        WebElement agreeCheckbox = driver.findElement(By.id("agree"));
        if (!agreeCheckbox.isSelected()) {
            agreeCheckbox.click();
        }

        WebElement submitButton = driver.findElement(By.className("btn-primary"));
        assertTrue(submitButton.isEnabled(), "Submit button should be enabled when form is valid");
    }

    @Test
    @Order(15)
    void testFooterLinksOpenInNewTab() {
        driver.get(BASE_URL);

        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github']")));
        String originalWindow = driver.getWindowHandle();

        githubLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("github.com"), "GitHub link should redirect to GitHub");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    void testFormResetButton() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Caio");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Silva");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("test@example.com");

        Select requestType = new Select(driver.findElement(By.id("request-type")));
        requestType.selectByVisibleText("Question");

        WebElement message = driver.findElement(By.id("message"));
        message.sendKeys("Testing reset.");

        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));
        resetButton.click();

        // Verify fields are cleared
        wait.until(ExpectedConditions.textToBe(By.id("first-name"), ""));
        assertEquals("", lastName.getAttribute("value"), "Last name should be cleared");
        assertEquals("", email.getAttribute("value"), "Email should be cleared");
        assertEquals("", message.getAttribute("value"), "Message should be cleared");
        assertEquals("Select an issue...", requestType.getFirstSelectedOption().getText(), "Request type should be reset");
    }
}