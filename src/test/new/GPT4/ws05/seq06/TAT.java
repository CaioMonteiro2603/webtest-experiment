package GPT4.ws05.seq06;

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
    public void testBasePageLoads() {
        driver.get(BASE_URL);
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals("CAC TAT", title.getText(), "Page title should match expected");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("This is a test message.");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMsg.isDisplayed(), "Success message should be displayed");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithInvalidEmail() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("invalid-email");
        driver.findElement(By.id("open-text-area")).sendKeys("Test message");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed due to invalid email");
    }

    @Test
    @Order(4)
    public void testPhoneFieldOnlyAcceptsNumbers() {
        driver.get(BASE_URL);
        WebElement phoneField = wait.until(ExpectedConditions.elementToBeClickable(By.id("phone")));
        phoneField.sendKeys("abcdef");
        Assertions.assertEquals("", phoneField.getAttribute("value"), "Phone field should not accept letters");
    }

    @Test
    @Order(5)
    public void testCheckboxesAndRadioButtons() {
        driver.get(BASE_URL);
        WebElement radio = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='radio'][value='ajuda']")));
        radio.click();
        Assertions.assertTrue(radio.isSelected(), "Radio button should be selected");

        WebElement checkbox = driver.findElement(By.id("phone-checkbox"));
        checkbox.click();
        Assertions.assertTrue(checkbox.isSelected(), "Checkbox should be selected");
    }

    @Test
    @Order(6)
    public void testResetButtonClearsFields() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("Test");
        WebElement resetButton = driver.findElement(By.xpath("//button[text()='Limpar']"));
        resetButton.click();
        Assertions.assertEquals("", driver.findElement(By.id("firstName")).getAttribute("value"), "First name should be cleared");
        Assertions.assertEquals("", driver.findElement(By.id("lastName")).getAttribute("value"), "Last name should be cleared");
        Assertions.assertEquals("", driver.findElement(By.id("email")).getAttribute("value"), "Email should be cleared");
        Assertions.assertEquals("", driver.findElement(By.id("open-text-area")).getAttribute("value"), "Text area should be cleared");
    }

    @Test
    @Order(7)
    public void testPrivacyPolicyLinkOpensExternalPage() {
        driver.get(BASE_URL);
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("privacy.html"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("privacy.html"), "Should open privacy.html page");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testHiddenCatImageViaConsole() {
        driver.get(BASE_URL);
        ((JavascriptExecutor) driver).executeScript("document.querySelector('#cat').style.display='block';");
        WebElement catImg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cat")));
        Assertions.assertTrue(catImg.isDisplayed(), "Hidden cat image should be visible after JS execution");
    }

    @Test
    @Order(9)
    public void testTypingLongTextInTextArea() {
        driver.get(BASE_URL);
        String longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(10);
        WebElement textArea = wait.until(ExpectedConditions.elementToBeClickable(By.id("open-text-area")));
        textArea.sendKeys(longText);
        Assertions.assertEquals(longText, textArea.getAttribute("value"), "Text area should contain the long text");
    }

    @Test
    @Order(10)
    public void testDropdownOptionsAndSelection() {
        driver.get(BASE_URL);
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("product")));
        List<WebElement> options = dropdown.findElements(By.tagName("option"));
        Assertions.assertTrue(options.size() >= 4, "Dropdown should have at least 4 options");
        options.get(2).click();
        Assertions.assertTrue(options.get(2).isSelected(), "Selected option should be active");
    }
}