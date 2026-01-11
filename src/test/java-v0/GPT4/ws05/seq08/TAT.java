package GPT4.ws05.seq08;

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
        WebElement heading = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("central de atendimento ao cliente"),
                "Page should contain correct heading");
    }

    @Test
    @Order(2)
    public void testFormSubmissionSuccess() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("This is a test message.");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(success.isDisplayed(), "Success message should be displayed");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithInvalidEmail() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@invalid");
        driver.findElement(By.id("open-text-area")).sendKeys("Invalid email test.");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be shown for invalid email");
    }

    @Test
    @Order(4)
    public void testPhoneFieldRejectsLetters() {
        driver.get(BASE_URL);
        WebElement phone = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("phone")));
        phone.sendKeys("abcde");
        String value = phone.getAttribute("value");
        Assertions.assertEquals("", value, "Phone field should not accept letters");
    }

    @Test
    @Order(5)
    public void testCheckboxesInteraction() {
        driver.get(BASE_URL);
        List<WebElement> checkboxes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("input[type='checkbox']")));
        for (WebElement cb : checkboxes) {
            if (!cb.isSelected()) {
                cb.click();
                Assertions.assertTrue(cb.isSelected(), "Checkbox should be selected after clicking");
            }
        }
    }

    @Test
    @Order(6)
    public void testSelectProductDropdown() {
        driver.get(BASE_URL);
        WebElement select = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("product")));
        select.click();
        WebElement option = select.findElement(By.cssSelector("option[value='blog']"));
        option.click();
        WebElement selected = select.findElement(By.cssSelector("option:checked"));
        Assertions.assertEquals("blog", selected.getAttribute("value"), "Selected product should be 'blog'");
    }

    @Test
    @Order(7)
    public void testPrivacyLinkOpensInNewTab() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        link.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("privacy.html"));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("privacy.html"), "Should navigate to privacy.html");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testTextAreaLimitEnforced() {
        driver.get(BASE_URL);
        WebElement textArea = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("open-text-area")));
        String longText = "A".repeat(2000);
        textArea.sendKeys(longText);
        Assertions.assertTrue(textArea.getAttribute("value").length() <= 2000, "Textarea should not accept more than 2000 characters");
    }

    @Test
    @Order(9)
    public void testUploadFileFieldPresence() {
        driver.get(BASE_URL);
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("file-upload")));
        Assertions.assertTrue(fileInput.isDisplayed(), "File input should be present");
    }

    @Test
    @Order(10)
    public void testSubmitButtonIsClickable() {
        driver.get(BASE_URL);
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        Assertions.assertTrue(button.isEnabled(), "Submit button should be enabled");
    }
}
