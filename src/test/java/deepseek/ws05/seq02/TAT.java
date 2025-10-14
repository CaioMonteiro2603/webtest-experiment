package deepseek.ws05.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class TatTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
    public void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        WebElement lastName = driver.findElement(By.id("lastName"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement productSelect = driver.findElement(By.id("product"));
        WebElement atendimentoRadio = driver.findElement(By.cssSelector("input[value='ajuda']"));
        WebElement emailCheckbox = driver.findElement(By.id("email-checkbox"));
        WebElement message = driver.findElement(By.id("open-text-area"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        phone.sendKeys("11987654321");
        productSelect.sendKeys("Blog");
        atendimentoRadio.click();
        emailCheckbox.click();
        message.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(),
            "Expected success message after form submission");
    }

    @Test
    @Order(2)
    public void testFormValidation() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstName.sendKeys("");
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(),
            "Expected error message for invalid form submission");
    }

    @Test
    @Order(3)
    public void testPhoneFieldValidation() {
        driver.get(BASE_URL);
        WebElement phone = wait.until(ExpectedConditions.elementToBeClickable(By.id("phone")));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        phone.sendKeys("invalid");
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.getText().contains("Phone"),
            "Expected phone validation error message");
    }

    @Test
    @Order(4)
    public void testProductSelection() {
        driver.get(BASE_URL);
        WebElement productSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("product")));

        productSelect.sendKeys("Cursos");
        Assertions.assertEquals("Cursos", productSelect.getAttribute("value"),
            "Expected product selection to be 'Cursos'");

        productSelect.sendKeys("Mentoria");
        Assertions.assertEquals("Mentoria", productSelect.getAttribute("value"),
            "Expected product selection to be 'Mentoria'");
    }

    @Test
    @Order(5)
    public void testHelpOptionSelection() {
        driver.get(BASE_URL);
        WebElement helpRadio = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='ajuda']")));

        helpRadio.click();
        Assertions.assertTrue(helpRadio.isSelected(),
            "Expected help radio button to be selected");
    }

    @Test
    @Order(6)
    public void testEmailCheckbox() {
        driver.get(BASE_URL);
        WebElement emailCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("email-checkbox")));

        emailCheckbox.click();
        Assertions.assertTrue(emailCheckbox.isSelected(),
            "Expected email checkbox to be selected");

        emailCheckbox.click();
        Assertions.assertFalse(emailCheckbox.isSelected(),
            "Expected email checkbox to be deselected");
    }

    @Test
    @Order(7)
    public void testExternalLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Política de Privacidade")));
        externalLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("cac-tat.s3.eu-central-1.amazonaws.com"),
            "Expected to be on privacy policy page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}