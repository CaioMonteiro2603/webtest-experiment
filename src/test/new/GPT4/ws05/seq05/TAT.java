package GPT4.ws05.seq05;

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
        driver.get(BASE_URL);
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
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals("CAC TAT", heading.getText(), "Home page heading should be 'CAC TAT'");
    }

    @Test
    @Order(2)
    public void testFormValidationFailsWithoutRequiredFields() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("valide"), "Error message should indicate invalid submission");
    }

    @Test
    @Order(3)
    public void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("This is a test message.");
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should be displayed after valid form submission");
    }

    @Test
    @Order(4)
    public void testExternalPrivacyLink() {
        driver.get(BASE_URL);
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='privacy']")));
        privacyLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        for (String handle : windowHandles) {
            if (!handle.equals(driver.getWindowHandle())) {
                driver.switchTo().window(handle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("política"), "Privacy page heading should mention 'Política'");
    }

    @Test
    @Order(5)
    public void testBackToHomeFromPrivacy() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/privacy.html");
        WebElement backButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='index']")));
        backButton.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("CAC TAT", heading.getText(), "Should return to home page after clicking back");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
            wait.until(driver -> driver.getWindowHandles().size() > 1);

            Set<String> windowHandles = driver.getWindowHandles();
            for (String handle : windowHandles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    wait.until(ExpectedConditions.urlContains("http"));
                    String currentUrl = driver.getCurrentUrl();
                    Assertions.assertTrue(currentUrl.startsWith("http"), "Should navigate to external URL");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}