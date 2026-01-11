package GPT4.ws04.seq04;

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
public class DEMOAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
    public void testPageTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertEquals("Demo AUT", title, "Page title mismatch");
    }

    @Test
    @Order(2)
    public void testFormInputFieldsPresence() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.findElement(By.id("first-name")).isDisplayed(), "First name input not displayed");
        Assertions.assertTrue(driver.findElement(By.id("last-name")).isDisplayed(), "Last name input not displayed");
        Assertions.assertTrue(driver.findElement(By.id("dob")).isDisplayed(), "DOB input not displayed");
        Assertions.assertTrue(driver.findElement(By.id("email")).isDisplayed(), "Email input not displayed");
        Assertions.assertTrue(driver.findElement(By.name("gender")).isDisplayed(), "Gender radio not displayed");
        Assertions.assertTrue(driver.findElement(By.id("role")).isDisplayed(), "Role dropdown not displayed");
    }

    @Test
    @Order(3)
    public void testSubmitFormWithValidData() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name"))).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("dob")).sendKeys("01/01/1990");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");

        List<WebElement> genderRadios = driver.findElements(By.name("gender"));
        if (!genderRadios.isEmpty()) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", genderRadios.get(0));
        }

        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("QA");

        WebElement comment = driver.findElement(By.id("comment"));
        comment.clear();
        comment.sendKeys("Test comment");

        WebElement submitBtn = driver.findElement(By.id("submit"));
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = alert.getText();
        Assertions.assertTrue(alertText.contains("successfully"), "Alert did not indicate success");
        alert.accept();
    }

    @Test
    @Order(4)
    public void testFormValidationErrorOnEmptyFields() {
        driver.get(BASE_URL);
        WebElement submitBtn = driver.findElement(By.id("submit"));
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();

        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            Assertions.assertTrue(alertText.toLowerCase().contains("please fill"), "Expected validation error alert");
            alert.accept();
        } catch (TimeoutException e) {
            WebElement firstName = driver.findElement(By.id("first-name"));
            String validationMessage = firstName.getAttribute("validationMessage");
            Assertions.assertNotNull(validationMessage, "Expected validation message");
            Assertions.assertFalse(validationMessage.isEmpty(), "Expected validation message");
        }
    }

    @Test
    @Order(5)
    public void testExternalLinkNavigation() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.tagName("a"));
        if (links.isEmpty()) {
            Assertions.assertTrue(true, "No external links found, skipping test");
            return;
        }

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("linkedin") || href.contains("facebook") || href.contains("twitter"))) {
                String originalWindow = driver.getWindowHandle();
                link.click();

                wait.until(driver -> driver.getWindowHandles().size() > 1);
                Set<String> allWindows = driver.getWindowHandles();
                for (String window : allWindows) {
                    if (!window.equals(originalWindow)) {
                        driver.switchTo().window(window);
                        wait.until(ExpectedConditions.urlContains(href.contains("linkedin") ? "linkedin" :
                                                                  href.contains("facebook") ? "facebook" :
                                                                  "twitter"));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(href.contains("linkedin") ? "linkedin" :
                                                                             href.contains("facebook") ? "facebook" :
                                                                             "twitter"), "External URL mismatch");
                        driver.close();
                        driver.switchTo().window(originalWindow);
                        break;
                    }
                }
            }
        }
    }
}