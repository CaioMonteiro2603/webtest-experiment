package SunaDeepSeek.ws04.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testFormPageLoad() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Demo AUT", driver.getTitle());
        Assertions.assertTrue(driver.findElement(By.id("first-name")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        List<WebElement> genderRadios = driver.findElements(By.name("gender"));
        for (WebElement radio : genderRadios) {
            if (radio.getAttribute("value").equals("male")) {
                radio.click();
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
        expectationSelect.selectByValue("good-teammate");
        
        WebElement development = driver.findElement(By.id("development"));
        development.sendKeys("Automation testing");
        
        WebElement comment = driver.findElement(By.id("comment"));
        comment.sendKeys("This is a test comment");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertEquals("Successfully submitted!", successMessage.getText());
    }

    @Test
    @Order(3)
    public void testFormValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();
        
        List<WebElement> errorMessages = driver.findElements(By.className("error"));
        Assertions.assertTrue(errorMessages.size() > 0, "Validation errors should be displayed");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link
        String originalWindow = driver.getWindowHandle();
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("https://github.com/katalon-studio")));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }
}