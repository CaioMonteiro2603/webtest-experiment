package SunaDeepSeek.ws04.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public void testFormPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Demo AUT", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.cssSelector("input[type='radio'][value='male']"));
        gender.click();
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("Test1234");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Corp");
        
        WebElement role = driver.findElement(By.id("role"));
        role.sendKeys("QA Engineer");
        
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        jobExpectation.sendKeys("Challenging work");
        
        WebElement development = driver.findElement(By.cssSelector("input[type='checkbox'][value='WEB_DEVELOPMENT']"));
        development.click();
        
        WebElement comments = driver.findElement(By.id("comment"));
        comments.sendKeys("This is a test comment");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(3)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
        submitButton.click();
        
        List<WebElement> errorMessages = driver.findElements(By.className("validate-error"));
        Assertions.assertTrue(errorMessages.size() > 0, "Validation errors should be displayed");
    }

    @Test
    @Order(4)
    public void testEmailFormatValidation() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        email.sendKeys("invalid-email");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement emailError = driver.findElement(By.id("email-error"));
        Assertions.assertTrue(emailError.isDisplayed(), "Email format error should be displayed");
    }

    @Test
    @Order(5)
    public void testPasswordValidation() {
        driver.get(BASE_URL);
        
        WebElement password = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
        password.sendKeys("short");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        List<WebElement> passwordErrors = driver.findElements(By.className("validate-error"));
        boolean passwordErrorFound = passwordErrors.stream()
            .anyMatch(element -> element.getAttribute("innerHTML").toLowerCase().contains("password"));
        Assertions.assertTrue(passwordErrorFound, "Password validation error should be displayed");
    }

    @Test
    @Order(6)
    public void testRadioButtonSelection() {
        driver.get(BASE_URL);
        
        WebElement webDev = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='checkbox'][value='WEB_DEVELOPMENT']")));
        webDev.click();
        Assertions.assertTrue(webDev.isSelected(), "Web development should be selected");
        
        WebElement mobileDev = driver.findElement(By.cssSelector("input[type='checkbox'][value='MOBILE_DEVELOPMENT']"));
        mobileDev.click();
        Assertions.assertTrue(mobileDev.isSelected(), "Mobile development should be selected");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        String originalWindow = driver.getWindowHandle();
        
        // Test Help link
        List<WebElement> helpLinks = driver.findElements(By.linkText("Help"));
        if (helpLinks.size() > 0) {
            WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(helpLinks.get(0)));
            helpLink.click();
            
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            Assertions.assertTrue(driver.getCurrentUrl().contains("katalon.com"), "Help link should redirect to Katalon site");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}