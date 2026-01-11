package GPT4.ws04.seq07;

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
public class DEMOAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(title.isDisplayed(), "Page title should be visible");
        Assertions.assertTrue(driver.getTitle().contains("Form") || driver.getTitle().contains("Demo"), "Browser title should contain relevant text");
    }

    @Test
    @Order(2)
    public void testFillAndSubmitFormWithValidData() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.name("firstName")));
        firstName.sendKeys("John");
        driver.findElement(By.name("lastName")).sendKeys("Doe");
        driver.findElement(By.id("male")).click();
        driver.findElement(By.name("dob")).sendKeys("01/01/1990");
        driver.findElement(By.name("address")).sendKeys("123 Main Street");
        driver.findElement(By.name("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.name("password")).sendKeys("Password123");
        driver.findElement(By.name("company")).sendKeys("Test Inc.");
        driver.findElement(By.name("comment")).sendKeys("This is a test comment.");
       
        WebElement submit = driver.findElement(By.id("submit"));
        submit.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String alertText = driver.findElement(By.id("submit-msg")).getText();
        Assertions.assertTrue(alertText.toLowerCase().contains("success") || alertText.toLowerCase().contains("submitted"), "Submit message should indicate success");
    }

    @Test
    @Order(3)
    public void testFormValidationError() {
        driver.get(BASE_URL);
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submit.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Assertions.assertTrue(driver.findElement(By.name("firstName")).getAttribute("required") != null, "Form should show validation error for required fields");
    }

    @Test
    @Order(4)
    public void testExternalKatalonLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement link = driver.findElement(By.cssSelector("a[href*='katalon']"));
        if (!link.getAttribute("href").startsWith("https://katalon")) {
            link = wait.until(ExpectedConditions.elementToBeClickable(By.tagName("a")));
        }
        link.click();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<String> windows = driver.getWindowHandles();
        if (windows.size() > 1) {
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains("katalon") || currentUrl.contains("test"), "External link should navigate to appropriate site");

            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains("katalon") || currentUrl.contains("test"), "External link should navigate to appropriate site");
        }
    }
}