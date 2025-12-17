package GPT4.ws06.seq06;

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
public class RESTFULL {

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("header h1")));
        Assertions.assertEquals("Welcome to Restful-booker", heading.getText(), "Main heading should be visible");
    }

    @Test
    @Order(2)
    public void testRoomsSectionPresent() {
        driver.get(BASE_URL);
        WebElement roomsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("section#rooms h2")));
        Assertions.assertEquals("Rooms", roomsHeader.getText(), "Rooms section should be present on homepage");
    }

    @Test
    @Order(3)
    public void testContactFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("testuser@example.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        driver.findElement(By.id("subject")).sendKeys("Test Subject");
        driver.findElement(By.id("description")).sendKeys("This is a test message.");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMsg.isDisplayed(), "Success alert should appear after valid form submission");
    }

    @Test
    @Order(4)
    public void testContactFormSubmissionWithInvalidEmail() {
        driver.get(BASE_URL);
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("invalid-email");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        driver.findElement(By.id("subject")).sendKeys("Test Subject");
        driver.findElement(By.id("description")).sendKeys("Invalid email test.");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".alert-danger"));
        Assertions.assertFalse(errorMessages.isEmpty(), "Error alert should appear for invalid email");
    }

    @Test
    @Order(5)
    public void testAdminPageLoginWithInvalidCredentials() {
        driver.get(BASE_URL + "admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalidUser");
        driver.findElement(By.name("password")).sendKeys("wrongPass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(6)
    public void testAdminPageLoginWithValidCredentials() {
        driver.get(BASE_URL + "admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.clear();
        usernameField.sendKeys("admin");
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement adminPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        Assertions.assertEquals("Rooms", adminPanel.getText(), "Admin login should redirect to Rooms panel");
    }

    @Test
    @Order(7)
    public void testExternalLinkTwitter() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter.com']")));
        twitterLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("twitter.com"), "Twitter link should navigate to twitter.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testExternalLinkFacebook() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook.com']")));
        facebookLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("facebook.com"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("facebook.com"), "Facebook link should navigate to facebook.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testExternalLinkLinkedIn() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='linkedin.com']")));
        linkedinLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should navigate to linkedin.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testBookNowModalOpens() {
        driver.get(BASE_URL);
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.openBooking")) );
        bookButton.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".popupOverlay")));
        Assertions.assertTrue(modal.isDisplayed(), "Booking modal should be displayed after clicking 'Book this room'");
    }
}
