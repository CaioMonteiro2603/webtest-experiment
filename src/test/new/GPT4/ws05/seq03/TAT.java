package GPT4.ws05.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @BeforeEach
    public void loadBaseUrl() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("central de atendimento ao cliente"), "Page title incorrect.");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("central de atendimento ao cliente"), "Heading missing or incorrect.");
    }

    @Test
    @Order(2)
    public void testSuccessfulFormSubmission() {
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstName.sendKeys("Maria");

        driver.findElement(By.id("lastName")).sendKeys("Silva");
        driver.findElement(By.id("email")).sendKeys("maria@exemplo.com");
        driver.findElement(By.id("open-text-area")).sendKeys("Mensagem de teste automatizado.");

        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
        submit.click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(success.isDisplayed(), "Success message not displayed.");
    }

    @Test
    @Order(3)
    public void testFormValidationError() {
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstName.sendKeys("João");

        driver.findElement(By.id("lastName")).sendKeys("Oliveira");
        driver.findElement(By.id("email")).sendKeys("email-invalido");
        driver.findElement(By.id("open-text-area")).sendKeys("Mensagem inválida.");

        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
        submit.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(error.isDisplayed(), "Validation error not displayed for invalid email.");
    }

    @Test
    @Order(4)
    public void testPrivacyPolicyExternalLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("privacy.html"), "Privacy policy did not open correctly.");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testFileUpload() throws IOException {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, "Test content".getBytes());
        
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("file-upload")));
        fileInput.sendKeys(tempFile.toAbsolutePath().toString());

        WebElement fileNameDisplay = driver.findElement(By.id("file-upload")).findElement(By.xpath("..//span"));
        Assertions.assertTrue(fileNameDisplay.getText().contains(tempFile.getFileName().toString()), "Uploaded file name not displayed.");
        
        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(6)
    public void testRadioAndCheckboxInteraction() {
        List<WebElement> radios = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("input[type='radio']")));
        radios.get(1).click();
        Assertions.assertTrue(radios.get(1).isSelected(), "Second radio button not selected.");

        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        checkboxes.get(0).click();
        Assertions.assertTrue(checkboxes.get(0).isSelected(), "First checkbox not selected.");
    }

    @Test
    @Order(7)
    public void testDropdownSelection() {
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("product")));
        dropdown.click();

        WebElement option = dropdown.findElement(By.cssSelector("option[value='youtube']"));
        option.click();

        Assertions.assertEquals("youtube", option.getAttribute("value"), "Dropdown option not selected correctly.");
    }

    @Test
    @Order(8)
    public void testPhoneFieldAcceptsOnlyNumbers() {
        WebElement phoneField = wait.until(ExpectedConditions.elementToBeClickable(By.id("phone")));
        phoneField.sendKeys("abcde");
        Assertions.assertEquals("", phoneField.getAttribute("value"), "Phone field accepted non-numeric input.");
    }
}