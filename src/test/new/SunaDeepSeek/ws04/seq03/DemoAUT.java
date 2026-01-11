package SunaDeepSeek.ws04.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {
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
    public void testFormPageLoad() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Demo AUT"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"));
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        List<WebElement> genderElements = driver.findElements(By.name("gender"));
        for (WebElement genderElement : genderElements) {
            if (genderElement.getAttribute("value").equals("Male")) {
                genderElement.click();
                break;
            }
        }
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("password123");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        Select roleSelect = new Select(role);
        roleSelect.selectByValue("QA");
        
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        Select expectationSelect = new Select(jobExpectation);
        expectationSelect.selectByValue("good_salary");
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(3)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();
        
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".invalid-feedback, .error"));
        Assertions.assertTrue(errorMessages.size() > 0 || driver.findElements(By.cssSelector("[class*='error']")).size() > 0);
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("Facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("LinkedIn", "linkedin.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        List<WebElement> links = driver.findElements(By.tagName("a"));
        WebElement targetLink = null;
        for (WebElement link : links) {
            if (link.getText().contains(linkText)) {
                targetLink = link;
                break;
            }
        }
        
        if (targetLink == null) {
            Assertions.fail("Link with text containing '" + linkText + "' not found");
            return;
        }
        
        wait.until(ExpectedConditions.elementToBeClickable(targetLink));
        targetLink.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testFormReset() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Test");
        
        List<WebElement> resetButtons = driver.findElements(By.tagName("button"));
        WebElement resetButton = null;
        for (WebElement button : resetButtons) {
            if (button.getText().toLowerCase().contains("reset") || button.getAttribute("type").equals("reset")) {
                resetButton = button;
                break;
            }
        }
        
        if (resetButton == null) {
            // Try to find by input type reset
            List<WebElement> resetInputs = driver.findElements(By.cssSelector("input[type='reset']"));
            if (!resetInputs.isEmpty()) {
                resetButton = resetInputs.get(0);
            }
        }
        
        if (resetButton == null) {
            Assertions.fail("Reset button not found");
            return;
        }
        
        wait.until(ExpectedConditions.elementToBeClickable(resetButton));
        resetButton.click();
        
        Assertions.assertEquals("", firstName.getAttribute("value"));
    }
}