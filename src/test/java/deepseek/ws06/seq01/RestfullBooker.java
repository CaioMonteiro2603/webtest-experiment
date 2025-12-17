package deepseek.ws06.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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
    public void testRoomBooking() {
        driver.get(BASE_URL);
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.book-room")));
        bookButton.click();

        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstname")));
        WebElement lastName = driver.findElement(By.id("lastname"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement bookNowButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        phone.sendKeys("1234567890");
        bookNowButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".confirmation-modal")));
        Assertions.assertTrue(confirmation.isDisplayed(), "Booking confirmation should be displayed");
    }

    @Test
    @Order(2)
    public void testContactForm() {
        driver.get(BASE_URL);
        WebElement contactButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("contact-button")));
        contactButton.click();

        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement phoneField = driver.findElement(By.id("phone"));
        WebElement subjectField = driver.findElement(By.id("subject"));
        WebElement messageField = driver.findElement(By.id("description"));
        WebElement submitButton = driver.findElement(By.id("submitContact"));

        nameField.sendKeys("Jane Smith");
        emailField.sendKeys("jane.smith@example.com");
        phoneField.sendKeys("0987654321");
        subjectField.sendKeys("Test Inquiry");
        messageField.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Contact form submission should be successful");
    }

    @Test
    @Order(3)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "#/admin");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        username.sendKeys("admin");
        password.sendKeys("password");
        loginButton.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout")));
        Assertions.assertTrue(logoutButton.isDisplayed(), "Should be logged in successfully");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        username.sendKeys("wrong");
        password.sendKeys("credentials");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();

        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        driver.close();
        driver.switchTo().window(currentWindow);
    }

    @Test
    @Order(6)
    public void testRoomImages() {
        driver.get(BASE_URL);
        List<WebElement> roomImages = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".room-image")));
        Assertions.assertTrue(roomImages.size() > 0, "At least one room image should be displayed");
    }
}