package Qwen3.ws05.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TatTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor jsExecutor;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        jsExecutor = (JavascriptExecutor) driver;
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
        driver.get(BASE_URL);
        assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "Page title is incorrect.");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithRequiredFields() {
        driver.get(BASE_URL);

        // Wait for and fill First Name
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstName.sendKeys("Caio");

        // Fill Last Name
        driver.findElement(By.id("lastName")).sendKeys("Silva");

        // Fill Email
        driver.findElement(By.id("email")).sendKeys("caio.silva@example.com");

        // Fill Open Text Area (required)
        driver.findElement(By.id("open-text-area")).sendKeys("Test message for required fields submission.");

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert Success Message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success span")));
        assertTrue(successMessage.getText().contains("Mensagem enviada com sucesso!"), "Success message should be displayed.");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithAllFields() {
        driver.get(BASE_URL);

        // Wait for and fill First Name
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstName.sendKeys("Ana");

        // Fill Last Name
        driver.findElement(By.id("lastName")).sendKeys("Paula");

        // Fill Email
        driver.findElement(By.id("email")).sendKeys("ana.paula@example.com");

        // Fill Phone
        driver.findElement(By.id("phone")).sendKeys("1234567890");

        // Fill Open Text Area
        driver.findElement(By.id("open-text-area")).sendKeys("Test message with all fields filled.");

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert Success Message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success span")));
        assertTrue(successMessage.getText().contains("Mensagem enviada com sucesso!"), "Success message should be displayed for all fields.");
    }

    @Test
    @Order(4)
    public void testFormSubmission_InvalidEmail() {
        driver.get(BASE_URL);

        // Fill fields with invalid email
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName"))).sendKeys("Test");
        driver.findElement(By.id("lastName")).sendKeys("User");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("invalid-email"); // Intentionally invalid
        driver.findElement(By.id("open-text-area")).sendKeys("Test with invalid email.");

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert Error Message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error span")));
        assertTrue(errorMessage.getText().contains("Valide os campos obrigatórios!"), "Error message for invalid email should be displayed.");
    }

    @Test
    @Order(5)
    public void testDelayedResponse() {
        driver.get(BASE_URL);

        // Check Delayed Response checkbox
        WebElement delayedCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkbox")));
        delayedCheckbox.click();

        // Fill required fields
        driver.findElement(By.id("firstName")).sendKeys("Delayed");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("email")).sendKeys("delayed.test@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("This is a delayed response test.");

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert Success Message (should take longer due to delay)
        // The test passes if the message appears eventually, which the WebDriverWait handles.
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success span")));
        assertTrue(successMessage.getText().contains("Mensagem enviada com sucesso!"), "Success message should be displayed for delayed response.");
    }

    @Test
    @Order(6)
    public void testClearFieldsFunctionality() {
        driver.get(BASE_URL);

        // Fill several fields
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstName.sendKeys("ToBeCleared");

        driver.findElement(By.id("lastName")).sendKeys("Name");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("phone")).sendKeys("987654321");
        driver.findElement(By.id("open-text-area")).sendKeys("This text should be cleared.");

        // Click Clear Fields button
        driver.findElement(By.cssSelector("button[type='reset']")).click();

        // Assert fields are cleared
        assertEquals("", firstName.getAttribute("value"), "First name field should be cleared.");
        assertEquals("", driver.findElement(By.id("lastName")).getAttribute("value"), "Last name field should be cleared.");
        assertEquals("", driver.findElement(By.id("email")).getAttribute("value"), "Email field should be cleared.");
        assertEquals("", driver.findElement(By.id("phone")).getAttribute("value"), "Phone field should be cleared.");
        assertEquals("", driver.findElement(By.id("open-text-area")).getAttribute("value"), "Text area should be cleared.");
    }

    @Test
    @Order(7)
    public void testTextAreaValidation() {
        driver.get(BASE_URL);

        // Fill required fields but leave text area empty
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName"))).sendKeys("Text");
        driver.findElement(By.id("lastName")).sendKeys("Area");
        driver.findElement(By.id("email")).sendKeys("text.area@example.com");
        // Intentionally leave open-text-area empty

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert Error Message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error span")));
        assertTrue(errorMessage.getText().contains("Valide os campos obrigatórios!"), "Error message for empty text area should be displayed.");
    }

    @Test
    @Order(8)
    public void testPhoneValidation_Required() {
        driver.get(BASE_URL);

        // Check phone required checkbox
        WebElement phoneRequiredCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("label[for='phone-checkbox']")));
        phoneRequiredCheckbox.click();

        // Fill other required fields
        driver.findElement(By.id("firstName")).sendKeys("Phone");
        driver.findElement(By.id("lastName")).sendKeys("Required");
        driver.findElement(By.id("email")).sendKeys("phone.required@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("Test phone required validation.");

        // Submit form without phone number
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert Error Message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error span")));
        assertTrue(errorMessage.getText().contains("Valide os campos obrigatórios!"), "Error message for missing required phone should be displayed.");
    }

    @Test
    @Order(9)
    public void testFooterLinksExternal() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Click Política de Privacidade link (one level deep within the same domain)
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Política de Privacidade")));
        privacyLink.click();
        wait.until(ExpectedConditions.urlContains("privacy"));

        // The "privacy" link is an anchor within the same page, so we just assert the URL changed.
        assertTrue(driver.getCurrentUrl().contains("#"), "URL should change to include anchor for Política de Privacidade.");

        // Go back to main page for external links
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));

        // Click W3C link (external)
        originalWindow = driver.getWindowHandle();
        WebElement w3cLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("W3C")));
        // Scroll to element to ensure it's clickable (sometimes needed in headless)
        jsExecutor.executeScript("arguments[0].scrollIntoView(true);", w3cLink);
        w3cLink.click();
        assertExternalLinkAndReturn(originalWindow, "w3c.br");

        // Click Home link (internal, but one level below - it's just an anchor)
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        // Asserting the URL is the base URL suffices for an anchor link
        assertEquals(BASE_URL, driver.getCurrentUrl(), "URL should be base URL after clicking Home anchor.");
    }

    // --- Helper Methods ---

    private void assertExternalLinkAndReturn(String originalWindow, String expectedDomain) {
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        assertNotNull(newWindow, "A new window should have been opened for " + expectedDomain);
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "New window URL should contain " + expectedDomain + ". URL was: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}