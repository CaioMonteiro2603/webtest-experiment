package GPT4.ws05.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class CacTatTest {

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals("Central de Atendimento ao Cliente TAT", heading.getText(), "Page heading mismatch");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("Test message");
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message not displayed");
        Assertions.assertEquals("Mensagem enviada com sucesso.", successMessage.getText(), "Success message text mismatch");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithInvalidEmail() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("invalid-email");
        driver.findElement(By.id("open-text-area")).sendKeys("Test message");
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed");
        Assertions.assertEquals("Valide os campos obrigatórios!", errorMessage.getText(), "Error message text mismatch");
    }

    @Test
    @Order(4)
    public void testPrivacyPolicyLinkOpensInNewTab() {
        driver.get(BASE_URL);

        WebElement privacyLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='privacy.html']")));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", privacyLink.getAttribute("href"));

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains("privacy.html"), "Privacy policy did not open correctly");
                WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
                Assertions.assertEquals("CAC TAT - Política de privacidade", heading.getText(), "Privacy policy heading mismatch");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testPageTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertEquals("CAC TAT", title, "Page title mismatch");
    }
}
