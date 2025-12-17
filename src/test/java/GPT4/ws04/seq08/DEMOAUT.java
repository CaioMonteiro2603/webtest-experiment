package GPT4.ws04.seq08;

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
public class DEMOAUT {

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
    public void testFormPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("AUT Test Form", header.getText(), "Header text should match");
    }

    @Test
    @Order(2)
    public void testFormSubmissionValidData() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.elementToBeClickable(By.name("first_name"))).sendKeys("John");
        driver.findElement(By.name("last_name")).sendKeys("Doe");
        driver.findElement(By.name("gender")).click(); // assuming first radio button
        driver.findElement(By.name("dob")).sendKeys("01011990");
        driver.findElement(By.name("address")).sendKeys("123 Main St");
        driver.findElement(By.name("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.name("password")).sendKeys("Password123");
        driver.findElement(By.name("company")).sendKeys("TestCorp");
        driver.findElement(By.name("role")).sendKeys("QA");
        driver.findElement(By.name("comment")).sendKeys("This is a test.");
        driver.findElement(By.id("submit")).click();

        WebElement successMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMsg.getText().contains("Successfully submitted"),
                "Success message should be displayed after submission");
    }

    @Test
    @Order(3)
    public void testFormSubmissionMissingFields() {
        driver.get(BASE_URL);

        driver.findElement(By.name("first_name")).sendKeys("Jane");
        driver.findElement(By.id("submit")).click();

        WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("please fill"),
                "Error message should indicate missing fields");
    }

    @Test
    @Order(4)
    public void testGenderRadioButtons() {
        driver.get(BASE_URL);

        List<WebElement> genderOptions = driver.findElements(By.name("gender"));
        Assertions.assertTrue(genderOptions.size() >= 2, "At least two gender options should be present");

        for (WebElement gender : genderOptions) {
            gender.click();
            Assertions.assertTrue(gender.isSelected(), "Gender option should be selectable");
        }
    }

    @Test
    @Order(5)
    public void testExternalLinkPrivacyPolicy() {
        driver.get(BASE_URL);

        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Privacy Policy")));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("katalon"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("katalon"), "External link should navigate to katalon domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testExternalLinkTermsOfService() {
        driver.get(BASE_URL);

        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Terms of Service")));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("katalon"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("katalon"), "External link should navigate to katalon domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testResetFormClearsFields() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.elementToBeClickable(By.name("first_name"))).sendKeys("Test");
        driver.findElement(By.name("last_name")).sendKeys("User");
        driver.findElement(By.id("reset")).click();

        String firstNameValue = driver.findElement(By.name("first_name")).getAttribute("value");
        String lastNameValue = driver.findElement(By.name("last_name")).getAttribute("value");

        Assertions.assertEquals("", firstNameValue, "First name field should be empty after reset");
        Assertions.assertEquals("", lastNameValue, "Last name field should be empty after reset");
    }

    @Test
    @Order(8)
    public void testDropdownCountrySelection() {
        driver.get(BASE_URL);

        WebElement countrySelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("country")));
        countrySelect.click();
        WebElement option = countrySelect.findElement(By.cssSelector("option[value='USA']"));
        option.click();

        String selected = countrySelect.findElement(By.cssSelector("option:checked")).getText();
        Assertions.assertEquals("USA", selected, "Selected country should be USA");
    }
}
