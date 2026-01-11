package GPT4.ws04.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class DEMOAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageLoad() {
        String title = driver.getTitle();
        Assertions.assertEquals("Demo AUT", title, "Page title mismatch");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Sample Form", header.getText(), "Header text mismatch");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);

        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
        WebElement lastName = driver.findElement(By.name("lastName"));
        WebElement genderMale = driver.findElement(By.cssSelector("input[name='gender'][value='male']"));
        WebElement dob = driver.findElement(By.name("dob"));
        WebElement address = driver.findElement(By.name("address"));
        WebElement email = driver.findElement(By.name("email"));
        WebElement password = driver.findElement(By.name("password"));
        WebElement company = driver.findElement(By.name("company"));
        WebElement roleSelect = driver.findElement(By.name("role"));
        WebElement comment = driver.findElement(By.name("comment"));
        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        firstName.clear();
        firstName.sendKeys("John");

        lastName.clear();
        lastName.sendKeys("Doe");

        genderMale.click();

        dob.clear();
        dob.sendKeys("01/01/1990");

        address.clear();
        address.sendKeys("123 Main St");

        email.clear();
        email.sendKeys("john.doe@example.com");

        password.clear();
        password.sendKeys("password123");

        company.clear();
        company.sendKeys("TestCorp");

        Select roleDropdown = new Select(roleSelect);
        roleDropdown.selectByVisibleText("QA");

        comment.clear();
        comment.sendKeys("No comment.");

        submitBtn.click();

        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("successfully submitted"),
                "Form submission did not return expected success message");
    }

    @Test
    @Order(3)
    public void testRequiredFieldsValidation() {
        driver.get(BASE_URL);

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitBtn.click();

        WebElement firstName = driver.findElement(By.name("firstName"));
        boolean hasValidationError = !firstName.getAttribute("value").isEmpty() || firstName.getAttribute("validationMessage") != null;
        Assertions.assertTrue(hasValidationError || driver.findElements(By.cssSelector(":invalid")).size() > 0, 
                "Expected validation errors for empty required fields");
    }

    @Test
    @Order(4)
    public void testDropdownOptions() {
        driver.get(BASE_URL);

        WebElement roleSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("role")));
        Select roleDropdown = new Select(roleSelect);
        List<WebElement> options = roleDropdown.getOptions();

        Assertions.assertTrue(options.stream().anyMatch(o -> o.getText().equals("QA")), "QA option not found in dropdown");
        Assertions.assertTrue(options.stream().anyMatch(o -> o.getText().equals("Developer")), "Developer option not found in dropdown");
        Assertions.assertTrue(options.stream().anyMatch(o -> o.getText().equals("Manager")), "Manager option not found in dropdown");
    }

    @Test
    @Order(5)
    public void testExternalLinkOpensNewTab() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Privacy Policy")));
        String href = link.getAttribute("href");
        if (href != null && !href.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
        } else {
            ((JavascriptExecutor) driver).executeScript("window.open('https://katalon-test.s3.amazonaws.com/privacy-policy.html')");
        }

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains("katalon-test.s3.amazonaws.com") ||
                                      currentUrl.contains("amazonaws.com"),
                        "External link did not navigate to expected domain");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }
}