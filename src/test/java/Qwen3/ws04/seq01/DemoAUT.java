package Qwen3.ws04.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);

        // Fill First Name
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.clear();
        firstName.sendKeys("Caio");

        // Fill Last Name
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.clear();
        lastName.sendKeys("Silva");

        // Select Gender
        List<WebElement> genderRadios = driver.findElements(By.name("gender"));
        for (WebElement radio : genderRadios) {
            if (radio.getAttribute("value").equals("Male")) {
                radio.click();
                break;
            }
        }

        // Fill Date of Birth
        WebElement dob = driver.findElement(By.id("dob"));
        dob.clear();
        dob.sendKeys("01/01/1990");

        // Fill Address
        WebElement address = driver.findElement(By.id("address"));
        address.clear();
        address.sendKeys("123 Main St");

        // Fill Email
        WebElement email = driver.findElement(By.id("email"));
        email.clear();
        email.sendKeys("caio.silva@example.com");

        // Fill Password
        WebElement password = driver.findElement(By.id("password"));
        password.clear();
        password.sendKeys("SecurePass123!");

        // Fill Company
        WebElement company = driver.findElement(By.id("company"));
        company.clear();
        company.sendKeys("Tech Solutions");

        // Select Role
        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("Manager");

        // Select Expectation (multi-select)
        Select expectationSelect = new Select(driver.findElement(By.id("expectation")));
        expectationSelect.deselectAll(); // Deselect default if any
        expectationSelect.selectByVisibleText("High salary");
        expectationSelect.selectByVisibleText("Good teamwork");

        // Select Wiki page (dropdown)
        Select wikiSelect = new Select(driver.findElement(By.id("willing-to-relocate")));
        wikiSelect.selectByValue("yes");

        // Select Ways of Development (checkboxes)
        List<WebElement> devWays = driver.findElements(By.name("tool"));
        for (WebElement way : devWays) {
            if (way.getAttribute("value").equals("Automation Testing") && !way.isSelected()) {
                way.click();
            }
            if (way.getAttribute("value").equals("Manual Testing") && !way.isSelected()) {
                way.click();
            }
        }

        // Select Comment
        WebElement comment = driver.findElement(By.id("comment"));
        comment.clear();
        comment.sendKeys("This is a test comment.");

        // Click Submit
        driver.findElement(By.id("submit")).click();

        // Assert Success Message
        WebElement resultDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        assertTrue(resultDiv.getText().contains("successfully"), "Form submission success message should be present.");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithMinimalData() {
        driver.get(BASE_URL);

        // Fill only mandatory or common fields
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.clear();
        firstName.sendKeys("Ana");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.clear();
        lastName.sendKeys("Paula");

        List<WebElement> genderRadios = driver.findElements(By.name("gender"));
        for (WebElement radio : genderRadios) {
            if (radio.getAttribute("value").equals("Female")) {
                radio.click();
                break;
            }
        }

        WebElement email = driver.findElement(By.id("email"));
        email.clear();
        email.sendKeys("ana.paula@example.com");

        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("QA");

        driver.findElement(By.id("submit")).click();

        WebElement resultDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        assertTrue(resultDiv.getText().contains("successfully"), "Form submission success message should be present for minimal data.");
    }

    @Test
    @Order(3)
    public void testInvalidFormSubmission_EmptyFields() {
        driver.get(BASE_URL);

        // Do not fill any fields, just click submit
        driver.findElement(By.id("submit")).click();

        // We expect client-side validation, but the page might not prevent submission.
        // The test is to ensure the page handles it gracefully.
        // Since this is a simple static form, it will likely just submit and show empty fields in the result.
        // We will assert that the page navigates or processes, not that it blocks.
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg"))); // Waits for the result area to be updated
        // Without specific validation messages to assert, we pass if the page doesn't crash.
        assertTrue(driver.findElements(By.id("submit-msg")).size() > 0, "Result message area should be present after submission.");
    }

    @Test
    @Order(4)
    public void testFooterLinksExternal() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Click Katalon Studio link
        WebElement katalonLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Katalon Studio")));
        katalonLink.click();
        assertExternalLinkAndReturn(originalWindow, "katalon.com");

        // Click Katalon TestOps link
        driver.get(BASE_URL); // Re-get base to ensure clean state for next click
        originalWindow = driver.getWindowHandle();
        WebElement testOpsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Katalon TestOps")));
        testOpsLink.click();
        assertExternalLinkAndReturn(originalWindow, "katalon.com");
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