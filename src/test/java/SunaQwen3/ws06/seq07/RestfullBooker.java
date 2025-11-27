package deepseek.ws06.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HotelTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "password";

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
    public void testRoomBooking() {
        driver.get(BASE_URL);
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.btn-outline-primary.float-right")));
        bookButton.click();

        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstname")));
        WebElement lastName = driver.findElement(By.id("lastname"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement submitButton = driver.findElement(By.cssSelector(".btn.btn-outline-primary.float-right"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        phone.sendKeys("1234567890");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-success")));
        Assertions.assertTrue(successMessage.getText().contains("Booking Successful!"), "Booking should be successful");
    }

    @Test
    @Order(2)
    public void testAdminLogin() {
        driver.get(BASE_URL + "admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(ADMIN_USER);
        passwordField.sendKeys(ADMIN_PASS);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("admin/brooms"));
        WebElement roomsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(roomsHeader.getText().contains("Rooms"), "Admin should be logged in");
    }

    @Test
    @Order(3)
    public void testInvalidAdminLogin() {
        driver.get(BASE_URL + "admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("wronguser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"), "Should show login error");
    }

    @Test
    @Order(4)
    public void testContactFormSubmission() {
        driver.get(BASE_URL);
        WebElement contactButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("contactButton")));
        contactButton.click();

        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement phoneField = driver.findElement(By.id("phone"));
        WebElement subjectField = driver.findElement(By.id("subject"));
        WebElement messageField = driver.findElement(By.id("description"));
        WebElement submitButton = driver.findElement(By.id("submitContact"));

        nameField.sendKeys("Test User");
        emailField.sendKeys("test@example.com");
        phoneField.sendKeys("1234567890");
        subjectField.sendKeys("Test Subject");
        messageField.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Contact form should submit successfully");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}