package GPT20b.ws04.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class FormTest {

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
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Form"));
        Assertions.assertTrue(
                driver.getTitle().toLowerCase().contains("form"),
                "Page title should contain the word 'Form'.");
    }

    @Test
    @Order(2)
    public void testFormPresence() {
        driver.get(BASE_URL);
        By formLocator = By.tagName("form");
        wait.until(ExpectedConditions.presenceOfElementLocated(formLocator));
        WebElement form = driver.findElement(formLocator);
        Assertions.assertTrue(
                form.isDisplayed(),
                "Form element should be present and visible on the page.");
    }

    @Test
    @Order(3)
    public void testFormFieldsCount() {
        driver.get(BASE_URL);
        By formLocator = By.tagName("form");
        wait.until(ExpectedConditions.presenceOfElementLocated(formLocator));
        List<WebElement> inputFields = driver.findElements(By.cssSelector("form input"));
        Assertions.assertTrue(
                inputFields.size() > 0,
                "Form should contain at least one input field.");
    }

    @Test
    @Order(4)
    public void testSubmitButtonEnabled() {
        driver.get(BASE_URL);
        By submitLocator = By.cssSelector("form button[type='submit'], form input[type='submit']");
        wait.until(ExpectedConditions.presenceOfElementLocated(submitLocator));
        WebElement submit = driver.findElement(submitLocator);
        Assertions.assertTrue(
                submit.isEnabled(),
                "Submit button should be enabled.");
    }

    @Test
    @Order(5)
    public void testExternalLinksNavigation() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.xpath("//a[starts-with(@href,'http') and not(contains(@href,current() ))]"));
        for (WebElement link : externalLinks) {
            String originalWindow = driver.getWindowHandle();
            link.click();
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(
                    currentUrl.contains("katalon") || currentUrl.contains("amazonaws"),
                    "External link should navigate to a URL containing katalon or aws domains.");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}