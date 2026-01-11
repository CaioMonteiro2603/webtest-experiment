package GPT4.ws05.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals("CAC TAT", title.getText(), "Title should match expected header");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "URL should contain index.html");
    }

    @Test
    @Order(2)
    public void testFormValidationErrors() {
        driver.get(BASE_URL);
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();
        List<WebElement> errors = driver.findElements(By.cssSelector("span[class='error']"));
        Assertions.assertFalse(errors.isEmpty(), "Validation errors should be shown when submitting empty form");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("This is a sample feedback message.");
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submit.click();
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[class='success']")));
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("mensagem enviada com sucesso"), "Form should be submitted successfully");
    }

    @Test
    @Order(4)
    public void testExternalLinkPrivacyPolicy() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("privacy.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"), "New tab should contain privacy.html in URL");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testCheckboxesAndRadioButtons() {
        driver.get(BASE_URL);
        WebElement phoneCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.id("phone-checkbox")));
        phoneCheckbox.click();
        Assertions.assertTrue(phoneCheckbox.isSelected(), "Phone checkbox should be selected");

        WebElement radio = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='radio'][value='ajuda']")));
        radio.click();
        Assertions.assertTrue(radio.isSelected(), "Radio option 'ajuda' should be selected");
    }

    @Test
    @Order(6)
    public void testDropdownOptions() {
        driver.get(BASE_URL);
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("product")));
        dropdown.click();
        List<WebElement> options = dropdown.findElements(By.tagName("option"));
        boolean found = false;
        for (WebElement option : options) {
            if (option.getAttribute("value").equals("cursos")) {
                option.click();
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Dropdown should have 'cursos' as an option");
        WebElement selected = dropdown.findElement(By.cssSelector("option:checked"));
        Assertions.assertEquals("cursos", selected.getAttribute("value"), "Selected option should be 'cursos'");
    }

    @Test
    @Order(7)
    public void testFileUpload() throws Exception {
        driver.get(BASE_URL);
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("file-upload")));
        
        java.io.File tempFile = java.io.File.createTempFile("testfile", ".txt");
        try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
            writer.write("Test file content");
        }
        
        fileInput.sendKeys(tempFile.getAbsolutePath());
        WebElement uploadedFile = driver.findElement(By.cssSelector("span[class='file-name']"));
        Assertions.assertTrue(uploadedFile.getText().contains(tempFile.getName()), "Uploaded file name should be shown");
        
        tempFile.delete();
    }
}