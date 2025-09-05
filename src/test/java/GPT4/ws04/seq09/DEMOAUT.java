package GPT4.ws04.seq09;

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
public class KatalonFormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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

    private void openFormPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    @Test
    @Order(1)
    public void testFormPresence() {
        openFormPage();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElement(By.name("first_name")).isDisplayed(), "First name field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("last_name")).isDisplayed(), "Last name field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("gender")).isDisplayed(), "Gender radio buttons should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("dob")).isDisplayed(), "DOB field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("address")).isDisplayed(), "Address field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("email")).isDisplayed(), "Email field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("password")).isDisplayed(), "Password field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("company")).isDisplayed(), "Company field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("role")).isDisplayed(), "Role dropdown should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("expectation")).isDisplayed(), "Expectation checkboxes should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("comment")).isDisplayed(), "Comment field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("button[type='submit']")).isDisplayed(), "Submit button should be visible")
        );
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        openFormPage();
        driver.findElement(By.name("first_name")).sendKeys("Caio");
        driver.findElement(By.name("last_name")).sendKeys("Silva");
        driver.findElements(By.name("gender")).get(0).click();
        driver.findElement(By.name("dob")).sendKeys("1990-01-01");
        driver.findElement(By.name("address")).sendKeys("123 Test Street");
        driver.findElement(By.name("email")).sendKeys("caio@example.com");
        driver.findElement(By.name("password")).sendKeys("password123");
        driver.findElement(By.name("company")).sendKeys("ExampleCorp");
        new Select(driver.findElement(By.name("role"))).selectByVisibleText("Manager");
        driver.findElements(By.name("expectation")).get(0).click();
        driver.findElement(By.name("comment")).sendKeys("No comments.");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        alert.accept();

        Assertions.assertTrue(alertText.toLowerCase().contains("success"), "Alert should confirm successful submission");
    }

    @Test
    @Order(3)
    public void testEmptyFormSubmissionShowsValidation() {
        openFormPage();
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        boolean alertAppeared = false;
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.accept();
            alertAppeared = true;
        } catch (TimeoutException ignored) {}

        Assertions.assertFalse(alertAppeared, "Form submission should fail silently or show field errors when required fields are empty");
    }

    @Test
    @Order(4)
    public void testExternalLinkIfPresent() {
        openFormPage();
        List<WebElement> links = driver.findElements(By.cssSelector("a[target='_blank']"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            String originalWindow = driver.getWindowHandle();
            Set<String> oldWindows = driver.getWindowHandles();
            link.click();

            wait.until(driver -> driver.getWindowHandles().size() > oldWindows.size());

            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(oldWindows);
            String newWindow = newWindows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));

            Assertions.assertTrue(driver.getCurrentUrl().startsWith("http"), "New window should have navigated to external URL");

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}
