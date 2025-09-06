package GPT4.ws05.seq10;

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
public class CacTatFormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageTitleAndHeader() {
        Assertions.assertTrue(driver.getTitle().contains("Central de Atendimento ao Cliente TAT"), "Title should contain 'Central de Atendimento ao Cliente TAT'");
        WebElement header = driver.findElement(By.tagName("h1"));
        Assertions.assertTrue(header.getText().contains("CAC TAT"), "Header should contain 'CAC TAT'");
    }

    @Test
    @Order(2)
    public void testFormSubmissionWithValidData() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("João");
        driver.findElement(By.id("lastName")).sendKeys("Silva");
        driver.findElement(By.id("email")).sendKeys("joao.silva@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("Teste de envio de mensagem.");
        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
        submit.click();
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.getText().contains("Mensagem enviada com sucesso"), "Success message should be displayed");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithInvalidEmail() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("Maria");
        driver.findElement(By.id("lastName")).sendKeys("Oliveira");
        driver.findElement(By.id("email")).sendKeys("maria.oliveira@example"); // invalid email
        driver.findElement(By.id("open-text-area")).sendKeys("Mensagem com e-mail inválido.");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.getText().contains("Valide os campos obrigatórios"), "Error message should be displayed");
    }

    @Test
    @Order(4)
    public void testPhoneFieldAcceptsOnlyNumbers() {
        driver.navigate().refresh();
        WebElement phone = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phone.sendKeys("abcd");
        Assertions.assertEquals("", phone.getAttribute("value"), "Phone field should not accept non-numeric input");
    }

    @Test
    @Order(5)
    public void testDropdownSelection() {
        driver.navigate().refresh();
        Select select = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.id("product"))));
        select.selectByVisibleText("Blog");
        Assertions.assertEquals("blog", select.getFirstSelectedOption().getAttribute("value"), "Selected value should be 'blog'");
    }

    @Test
    @Order(6)
    public void testRadioButtonAndCheckbox() {
        driver.navigate().refresh();
        WebElement radio = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='radio'][value='ajuda']")));
        radio.click();
        Assertions.assertTrue(radio.isSelected(), "Radio button 'ajuda' should be selected");

        WebElement checkbox = driver.findElement(By.id("email-checkbox"));
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
        Assertions.assertTrue(checkbox.isSelected(), "Email checkbox should be selected");
    }

    @Test
    @Order(7)
    public void testExternalPrivacyPolicyLink() {
        driver.navigate().refresh();
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='privacy.html']")));
        link.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(mainWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"), "Should navigate to privacy.html");
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(8)
    public void testFormResetBehavior() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName"))).sendKeys("Reset");
        driver.findElement(By.id("lastName")).sendKeys("Test");
        driver.findElement(By.id("email")).sendKeys("reset@example.com");
        driver.findElement(By.id("open-text-area")).sendKeys("To be cleared.");

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should appear");

        driver.navigate().refresh();
        Assertions.assertEquals("", driver.findElement(By.id("firstName")).getAttribute("value"), "First name should be reset");
        Assertions.assertEquals("", driver.findElement(By.id("email")).getAttribute("value"), "Email should be reset");
    }
}
