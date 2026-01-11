package deepseek.ws04.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

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
    public void testFormSubmission() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = driver.findElement(By.id("last-name"));
        WebElement genderMale = driver.findElement(By.cssSelector("input[value='male']"));
        WebElement dob = driver.findElement(By.id("dob"));
        WebElement address = driver.findElement(By.id("address"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement company = driver.findElement(By.id("company"));
        WebElement role = driver.findElement(By.id("role"));
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        WebElement developmentWays = driver.findElement(By.id("developmentWays"));
        WebElement comment = driver.findElement(By.id("comment"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        genderMale.click();
        dob.sendKeys("01/01/1990");
        address.sendKeys("123 Street");
        email.sendKeys("john.doe@example.com");
        password.sendKeys("Password123");
        company.sendKeys("Example Corp");
        new Select(role).selectByVisibleText("Tech Lead");
        new Select(jobExpectation).selectByVisibleText("Good teamwork");
        developmentWays.sendKeys("Learning new technologies");
        comment.sendKeys("No comments");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert.alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(),
            "Expected form submission success message");
    }

    @Test
    @Order(2)
    public void testInvalidFormSubmission() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement submitButton = driver.findElement(By.id("submit"));

        firstName.sendKeys("");
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div[class*='error']")));
        Assertions.assertTrue(errorMessage.isDisplayed(),
            "Expected error message for invalid form submission");
    }

    @Test
    @Order(3)
    public void testDropdownSelection() {
        driver.get(BASE_URL);
        WebElement role = driver.findElement(By.id("role"));
        Select roleDropdown = new Select(role);

        roleDropdown.selectByVisibleText("Tech Lead");
        Assertions.assertEquals("Tech Lead", roleDropdown.getFirstSelectedOption().getText(),
            "Expected role to be 'Tech Lead'");

        roleDropdown.selectByVisibleText("QA");
        Assertions.assertEquals("QA", roleDropdown.getFirstSelectedOption().getText(),
            "Expected role to be 'QA'");
    }

    @Test
    @Order(4)
    public void testMultipleSelect() {
        driver.get(BASE_URL);
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        Select jobExpectationDropdown = new Select(jobExpectation);

        jobExpectationDropdown.selectByVisibleText("Good teamwork");
        jobExpectationDropdown.selectByVisibleText("Challenging");
        Assertions.assertEquals(2, jobExpectationDropdown.getAllSelectedOptions().size(),
            "Expected 2 selected options in job expectation dropdown");
    }

    @Test
    @Order(5)
    public void testExternalLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Katalon")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", externalLink);

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("katalon"),
            "Expected to be on Katalon's page after clicking external link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}