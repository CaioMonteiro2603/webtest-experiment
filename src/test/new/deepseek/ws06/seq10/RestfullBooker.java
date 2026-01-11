package deepseek.ws06.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)

public class RestfullBooker {
    private static WebDriver driver;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        
        // Book a room
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Book this room')]")));
        bookButton.click();
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@name='firstname']")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.xpath("//input[@name='lastname']"));
        lastName.sendKeys("Doe");
        
        WebElement email = driver.findElement(By.xpath("//input[@name='email']"));
        email.sendKeys("john.doe@example.com");
        
        WebElement phone = driver.findElement(By.xpath("//input[@name='phone']"));
        phone.sendKeys("1234567890");
        
        WebElement bookDate = driver.findElement(By.xpath("//input[@name='bookDate']"));
        bookDate.sendKeys("2024-01-01");
        
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(), 'Book')]"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'Booking Successful!')]")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Room booking failed");
    }

    @Test
    @Order(2)
    public void testContactFormSubmission() {
        driver.get(BASE_URL);
        
        // Navigate to contact page
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Contact')]")));
        contactLink.click();
        
        // Fill contact form
        WebElement name = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@placeholder='Name']")));
        name.sendKeys("Test User");
        
        WebElement contactEmail = driver.findElement(By.xpath("//input[@placeholder='Email']"));
        contactEmail.sendKeys("test@example.com");
        
        WebElement phone = driver.findElement(By.xpath("//input[@placeholder='Phone']"));
        phone.sendKeys("1234567890");
        
        WebElement subject = driver.findElement(By.xpath("//input[@placeholder='Subject']"));
        subject.sendKeys("Test Subject");
        
        WebElement message = driver.findElement(By.xpath("//textarea[@data-testid='ContactDescription']"));
        message.sendKeys("This is a test message");
        
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(), 'Submit')]"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'Thanks for getting in touch')]")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Contact form submission failed");
    }

    @Test
    @Order(3)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@placeholder='Username']")));
        username.sendKeys("admin");
        
        WebElement password = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        password.sendKeys("password");
        
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Login')]"));
        loginButton.click();
        
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), 'Rooms')]")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Admin login failed");
    }

    @Test
    @Order(4)
    public void testInvalidAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@placeholder='Username']")));
        username.sendKeys("wrong");
        
        WebElement password = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        password.sendKeys("wrong");
        
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Login')]"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'Bad credentials')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Invalid login error not shown");
    }

    @Test
    @Order(5)
    public void testRoomDetails() {
        driver.get(BASE_URL);
        
        WebElement roomDetails = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'View more')]")));
        roomDetails.click();
        
        WebElement roomDescription = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'room-description')]")));
        Assertions.assertTrue(roomDescription.isDisplayed(), "Room details not shown");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("Facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("LinkedIn", "linkedin.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, '" + expectedDomain + "')]")));
        link.click();
        
        // Switch to new window if opened
        if (driver.getWindowHandles().size() > 1) {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " link failed - wrong domain");
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }
}