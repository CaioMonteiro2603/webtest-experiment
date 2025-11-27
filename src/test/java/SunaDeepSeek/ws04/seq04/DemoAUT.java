package SunaDeepSeek.ws04.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class FormPageTest {

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
    public void testFormPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertEquals("AUT Form", driver.getTitle());
        Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"));
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.id("gender"));
        gender.sendKeys("Male");
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("Password123");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        role.sendKeys("QA Engineer");
        
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        jobExpectation.sendKeys("Challenging work");
        
        WebElement development = driver.findElement(By.id("development"));
        development.sendKeys("Backend");
        
        WebElement comment = driver.findElement(By.id("comment"));
        comment.sendKeys("This is a test comment");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(3)
    public void testFormValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();
        
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".invalid-feedback"));
        Assertions.assertTrue(errorMessages.size() > 0, "Validation errors should be displayed");
        
        for (WebElement error : errorMessages) {
            Assertions.assertTrue(error.isDisplayed(), "Error message should be visible");
        }
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Twitter")));
        twitterLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Facebook")));
        facebookLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("LinkedIn")));
        linkedinLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testRadioButtonsAndCheckboxes() {
        driver.get(BASE_URL);
        
        WebElement radioMale = wait.until(ExpectedConditions.elementToBeClickable(By.id("male")));
        radioMale.click();
        Assertions.assertTrue(radioMale.isSelected());
        
        WebElement radioFemale = driver.findElement(By.id("female"));
        Assertions.assertFalse(radioFemale.isSelected());
        
        WebElement checkboxReadBooks = driver.findElement(By.id("read-books"));
        checkboxReadBooks.click();
        Assertions.assertTrue(checkboxReadBooks.isSelected());
        
        WebElement checkboxPlayGames = driver.findElement(By.id("play-games"));
        checkboxPlayGames.click();
        Assertions.assertTrue(checkboxPlayGames.isSelected());
    }
}