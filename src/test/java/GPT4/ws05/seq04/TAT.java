package GPT4.ws05.seq04;

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
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("central"), "Home page heading does not match");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("Test message");

        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();

        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMsg.isDisplayed(), "Success message not displayed after form submission");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithInvalidEmail() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("Jane");
        driver.findElement(By.id("lastName")).sendKeys("Smith");
        driver.findElement(By.id("email")).sendKeys("invalid-email");
        driver.findElement(By.id("open-text-area")).sendKeys("Test invalid email");

        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message not shown for invalid email");
    }

    @Test
    @Order(4)
    public void testPhoneNumberFieldOnlyAcceptsNumbers() {
        driver.get(BASE_URL);
        WebElement phoneInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phoneInput.sendKeys("abcde");
        Assertions.assertEquals("", phoneInput.getAttribute("value"), "Phone field should not accept letters");
    }

    @Test
    @Order(5)
    public void testDropdownSelection() {
        driver.get(BASE_URL);
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("product")));
        Select select = new Select(dropdown);
        select.selectByVisibleText("Blog");
        Assertions.assertEquals("blog", select.getFirstSelectedOption().getAttribute("value"), "Dropdown selection failed");
    }

    @Test
    @Order(6)
    public void testExternalPrivacyPolicyLink() {
        driver.get(BASE_URL);
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("privacy.html"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"), "Privacy policy URL mismatch");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(7)
    public void testCheckboxesInteraction() {
        driver.get(BASE_URL);
        List<WebElement> checkboxes = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("input[type='checkbox']"), 0));
        for (WebElement checkbox : checkboxes) {
            if (!checkbox.isSelected()) {
                checkbox.click();
                Assertions.assertTrue(checkbox.isSelected(), "Checkbox was not selected");
            }
        }
    }

    @Test
    @Order(8)
    public void testRadioButtonsInteraction() {
        driver.get(BASE_URL);
        List<WebElement> radios = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("input[type='radio']"), 0));
        for (WebElement radio : radios) {
            radio.click();
            Assertions.assertTrue(radio.isSelected(), "Radio button was not selected");
        }
    }

    @Test
    @Order(9)
    public void testFileUpload() {
        driver.get(BASE_URL);
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("file-upload")));
        fileInput.sendKeys(System.getProperty("user.dir") + "/src/test/resources/testfile.txt");
        Assertions.assertTrue(fileInput.getAttribute("value").contains("testfile.txt"), "File upload failed");
    }

    @Test
    @Order(10)
    public void testLinkToPrivacyPolicyOpensCorrectly() {
        driver.get(BASE_URL);
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        privacyLink.click();
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"), "Did not navigate to privacy policy page");
    }
}
