package deepseek.ws06.seq09;

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

@TestMethodOrder(OrderAnnotation.class)
public class HotelBookingTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".hotel-header h2")));
        assertTrue(header.getText().contains("Welcome to Shady Meadows"), "Page header should contain welcome message");
    }

    @Test
    @Order(2)
    public void testRoomBooking() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstname")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("lastname"));
        lastName.sendKeys("Doe");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("1234567890");
        
        WebElement bookButton = driver.findElement(By.cssSelector(".btn-outline-primary"));
        bookButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed(), "Booking confirmation should be displayed");
    }

    @Test
    @Order(3)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        username.sendKeys(ADMIN_USERNAME);
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys(ADMIN_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();

        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".dashboard")));
        assertTrue(dashboard.isDisplayed(), "Admin dashboard should be visible after login");
    }

    @Test
    @Order(4)
    public void testInvalidAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        username.sendKeys("invalid");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("wrong");
        
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(5)
    public void testRoomManagement() {
        testAdminLogin();
        
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("createRoom")));
        createButton.click();
        
        WebElement roomNumber = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("roomName")));
        roomNumber.sendKeys("101");
        
        WebElement roomPrice = driver.findElement(By.id("roomPrice"));
        roomPrice.sendKeys("100");
        
        WebElement saveButton = driver.findElement(By.id("createRoomButton"));
        saveButton.click();

        WebElement roomList = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".room-list")));
        assertTrue(roomList.getText().contains("101"), "New room should appear in the list");
    }

    @Test
    @Order(6)
    public void testContactForm() {
        driver.get(BASE_URL + "#/contact");
        
        WebElement name = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
        name.sendKeys("Test User");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("test@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("1234567890");
        
        WebElement subject = driver.findElement(By.id("subject"));
        subject.sendKeys("Test Subject");
        
        WebElement message = driver.findElement(By.id("description"));
        message.sendKeys("This is a test message");
        
        WebElement submitButton = driver.findElement(By.id("submitContact"));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed(), "Contact form submission confirmation should appear");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter']")));
        testExternalLink(twitterLink, "twitter.com");

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='facebook']")));
        testExternalLink(facebookLink,.facebook.com");
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}