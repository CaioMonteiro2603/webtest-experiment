package GPT4.ws05.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    private void openHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
    }

    @Test
    @Order(1)
    public void testFormSubmissionWithValidData() {
        openHomePage();
        driver.findElement(By.id("firstName")).sendKeys("Caio");
        driver.findElement(By.id("lastName")).sendKeys("Silva");
        driver.findElement(By.id("email")).sendKeys("caio@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("This is a valid test message.");
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should be visible after valid submission");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithMissingRequiredFields() {
        openHomePage();
        driver.findElement(By.id("firstName")).sendKeys("");
        driver.findElement(By.id("lastName")).sendKeys("");
        driver.findElement(By.id("email")).sendKeys("");
        driver.findElement(By.id("open-text-area")).sendKeys("");
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be visible on invalid form submission");
    }

    @Test
    @Order(3)
    public void testPhoneFieldAcceptsOnlyNumbers() {
        openHomePage();
        WebElement phoneInput = driver.findElement(By.id("phone"));
        phoneInput.sendKeys("abc123xyz");
        String entered = phoneInput.getAttribute("value");
        Assertions.assertEquals("123", entered, "Phone input should only accept numeric characters");
    }

    @Test
    @Order(4)
    public void testExternalPrivacyPolicyLink() {
        openHomePage();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        link.click();

        wait.until(d -> d.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        String newWindow = newWindows.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("privacy.html"));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("privacy.html"), "Privacy page should open in a new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testCheckboxAndRadioSelection() {
        openHomePage();

        List<WebElement> radioButtons = driver.findElements(By.cssSelector("input[type='radio']"));
        for (WebElement radio : radioButtons) {
            wait.until(ExpectedConditions.elementToBeClickable(radio)).click();
            Assertions.assertTrue(radio.isSelected(), "Radio button should be selectable");
        }

        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        for (WebElement checkbox : checkboxes) {
            wait.until(ExpectedConditions.elementToBeClickable(checkbox)).click();
            Assertions.assertTrue(checkbox.isSelected(), "Checkbox should be selectable");
        }
    }

    @Test
    @Order(6)
    public void testFileUpload() {
        openHomePage();
        WebElement fileInput = driver.findElement(By.id("file-upload"));
        String filePath = System.getProperty("user.dir") + "/src/test/resources/example.json";
        fileInput.sendKeys(filePath);

        String uploadedFile = fileInput.getAttribute("value");
        Assertions.assertTrue(uploadedFile.contains("example.json"), "Uploaded file should be example.json");
    }
}
