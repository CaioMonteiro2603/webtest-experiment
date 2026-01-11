package deepseek.ws06.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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
        
        // Scroll to booking form to ensure elements are in view
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight / 2);");
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstname")));
        WebElement lastName = driver.findElement(By.name("lastname"));
        WebElement email = driver.findElement(By.name("email"));
        WebElement phone = driver.findElement(By.name("phone"));
        WebElement bookButton = driver.findElement(By.cssSelector("button.btn-book-room"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        phone.sendKeys("1234567890");
        bookButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".confirmation-modal")));
        Assertions.assertTrue(confirmation.isDisplayed(),
            "Expected booking confirmation dialog");
    }

    @Test
    @Order(2)
    public void testInvalidBooking() {
        driver.get(BASE_URL);
        
        // Scroll to booking form
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight / 2);");
        
        WebElement bookButton = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("button.btn-book-room")));
        bookButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(),
            "Expected error message for invalid booking");
    }

    @Test
    @Order(3)
    public void testContactFormSubmission() {
        driver.get(BASE_URL);
        
        // Scroll to contact form
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        
        WebElement contactName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("name")));
        WebElement contactEmail = driver.findElement(By.name("email"));
        WebElement contactPhone = driver.findElement(By.name("phone"));
        WebElement contactSubject = driver.findElement(By.name("subject"));
        WebElement contactMessage = driver.findElement(By.name("description"));
        WebElement submitButton = driver.findElement(By.id("submitContact"));

        contactName.sendKeys("Test User");
        contactEmail.sendKeys("test@example.com");
        contactPhone.sendKeys("1234567890");
        contactSubject.sendKeys("Test Subject");
        contactMessage.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".contact")));
        Assertions.assertTrue(successMessage.getText().contains("Thanks for getting in touch"),
            "Expected contact form success message");
    }

    @Test
    @Order(4)
    public void testRoomDetails() {
        driver.get(BASE_URL);
        WebElement roomDetails = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".room-details")));
        roomDetails.click();

        WebElement roomTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".room-title")));
        Assertions.assertTrue(roomTitle.isDisplayed(),
            "Expected room details to be displayed");
    }

    @Test
    @Order(5)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        username.sendKeys("admin");
        password.sendKeys("password");
        loginButton.click();

        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("logout")));
        Assertions.assertTrue(logoutButton.isDisplayed(),
            "Expected to be logged in to admin panel");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".social_twitter a")));
        twitterLink.click();
        assertExternalLink("twitter.com", originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".social_facebook a")));
        facebookLink.click();
        assertExternalLink("facebook.com", originalWindow);
    }

    private void assertExternalLink(String expectedDomain, String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Expected to be on " + expectedDomain + " after clicking social link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}