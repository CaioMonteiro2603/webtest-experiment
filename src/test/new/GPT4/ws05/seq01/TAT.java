package GPT4.ws05.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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

    private void fillRequiredFields() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys("Caio");
        driver.findElement(By.id("lastName")).sendKeys("Montes");
        driver.findElement(By.id("email")).sendKeys("caio@teste.com");
        driver.findElement(By.id("open-text-area")).sendKeys("Mensagem de teste");
    }

    @Test
    @Order(1)
    public void testSuccessfulFormSubmission() {
        fillRequiredFields();
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitBtn.click();
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMsg.isDisplayed(), "Success message should be displayed after form submission.");
    }

    @Test
    @Order(2)
    public void testInvalidEmailSubmission() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys("Caio");
        driver.findElement(By.id("lastName")).sendKeys("Montes");
        driver.findElement(By.id("email")).sendKeys("invalid-email");
        driver.findElement(By.id("open-text-area")).sendKeys("Mensagem");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid email.");
    }

    @Test
    @Order(3)
    public void testPhoneFieldOnlyAcceptsNumbers() {
        driver.get(BASE_URL);
        WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneInput.sendKeys("abcde");
        String value = phoneInput.getAttribute("value");
        Assertions.assertEquals("", value, "Phone field should remain empty when non-numeric input is entered.");
    }

    @Test
    @Order(4)
    public void testRequiredPhoneWhenCheckboxChecked() {
        driver.get(BASE_URL);
        fillRequiredFields();
        WebElement checkbox = driver.findElement(By.id("phone-checkbox"));
        checkbox.click();
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitBtn.click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed when phone is required but not filled.");
    }

    @Test
    @Order(5)
    public void testExternalPrivacyPolicyLink() {
        driver.get(BASE_URL);
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        link.click();
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"), "Should navigate to the privacy.html page.");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test
    @Order(6)
    public void testHiddenCatImageInteraction() {
        driver.get(BASE_URL);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.querySelector('#cat').style.display = 'block';");
        WebElement catImg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cat")));
        Assertions.assertTrue(catImg.isDisplayed(), "Cat image should be visible after removing 'hidden' attribute.");
    }

    @Test
    @Order(7)
    public void testInputClearing() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        WebElement lastName = driver.findElement(By.id("lastName"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement textarea = driver.findElement(By.id("open-text-area"));

        firstName.sendKeys("Caio");
        lastName.sendKeys("Montes");
        email.sendKeys("caio@test.com");
        textarea.sendKeys("Test message");

        firstName.clear();
        lastName.clear();
        email.clear();
        textarea.clear();

        Assertions.assertEquals("", firstName.getAttribute("value"), "First name field should be empty.");
        Assertions.assertEquals("", lastName.getAttribute("value"), "Last name field should be empty.");
        Assertions.assertEquals("", email.getAttribute("value"), "Email field should be empty.");
        Assertions.assertEquals("", textarea.getAttribute("value"), "Text area should be empty.");
    }
}