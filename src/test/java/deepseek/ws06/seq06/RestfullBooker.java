package deepseek.ws06.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List; 

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static WebDriverWait wait;

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
        
        // Book a room
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[onclick='openBooking()']")));
        bookButton.click();
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstname")));
        WebElement lastName = driver.findElement(By.id("lastname"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement bookNowButton = driver.findElement(By.cssSelector("button[onclick='sendBooking()']"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        phone.sendKeys("1234567890");
        bookNowButton.click();

        WebElement bookingConfirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(bookingConfirmation.isDisplayed(), "Booking should be successful");
    }

    @Test
    @Order(2)
    public void testContactFormSubmission() {
        driver.get(BASE_URL);
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='#contact']")));
        contactLink.click();

        WebElement name = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement subject = driver.findElement(By.id("subject"));
        WebElement message = driver.findElement(By.id("description"));
        WebElement submitButton = driver.findElement(By.id("submitContact"));

        name.sendKeys("Test User");
        email.sendKeys("test@example.com");
        phone.sendKeys("9876543210");
        subject.sendKeys("Test Subject");
        message.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Contact form should be submitted successfully");
    }

    @Test
    @Order(3)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        username.sendKeys("admin");
        password.sendKeys("password");
        loginButton.click();

        WebElement dashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".dashboard")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Admin should be logged in successfully");
    }

    @Test
    @Order(4)
    public void testInvalidAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        username.sendKeys("wronguser");
        password.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error should be shown for invalid login");
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Website link
        WebElement websiteLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        websiteLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testRoomCount() {
        driver.get(BASE_URL);
        List<WebElement> rooms = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".room")));
        Assertions.assertTrue(rooms.size() > 0, "There should be rooms displayed");
    }
}