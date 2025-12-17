package GPT4.ws06.seq09;

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
public class RESTFULL {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    private void openHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("header")));
    }

    @Test
    @Order(1)
    public void testHomepageLoadsCorrectly() {
        openHomePage();
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("restful-booker"), "Title should contain 'Restful-Booker'");
        WebElement heading = driver.findElement(By.cssSelector("h1"));
        Assertions.assertEquals("Welcome to Restful Booker Platform", heading.getText(), "Main heading should match expected");
    }

    @Test
    @Order(2)
    public void testRoomListPresence() {
        openHomePage();
        List<WebElement> rooms = driver.findElements(By.cssSelector(".room-info"));
        Assertions.assertFalse(rooms.isEmpty(), "Rooms list should not be empty");
    }

    @Test
    @Order(3)
    public void testBookRoomButtonPresence() {
        openHomePage();
        List<WebElement> bookButtons = driver.findElements(By.cssSelector(".openBooking"));
        Assertions.assertFalse(bookButtons.isEmpty(), "Each room should have a Book button");
        for (WebElement button : bookButtons) {
            Assertions.assertEquals("Book this room", button.getText(), "Button text should be 'Book this room'");
        }
    }

    @Test
    @Order(4)
    public void testContactFormSubmission() {
        openHomePage();
        WebElement name = driver.findElement(By.id("name"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement subject = driver.findElement(By.id("subject"));
        WebElement description = driver.findElement(By.id("description"));
        WebElement submitButton = driver.findElement(By.id("submitContact"));

        name.sendKeys("Caio");
        email.sendKeys("caio@example.com");
        phone.sendKeys("1234567890");
        subject.sendKeys("Test Subject");
        description.sendKeys("Test message for contact form");

        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(success.isDisplayed(), "Success alert should be visible after contact form submission");
    }

    @Test
    @Order(5)
    public void testContactFormValidation() {
        openHomePage();
        WebElement submitButton = driver.findElement(By.id("submitContact"));
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();

        List<WebElement> errors = driver.findElements(By.cssSelector(".alert-danger"));
        Assertions.assertFalse(errors.isEmpty(), "Error alerts should appear when submitting empty form");
    }

    @Test
    @Order(6)
    public void testExternalTwitterLink() {
        openHomePage();
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        twitter.click();

        wait.until(d -> d.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("twitter.com"), "New tab should contain twitter.com in URL");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testExternalFacebookLink() {
        openHomePage();
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        facebook.click();

        wait.until(d -> d.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("facebook.com"));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("facebook.com"), "New tab should contain facebook.com in URL");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testExternalLinkedInLink() {
        openHomePage();
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        linkedin.click();

        wait.until(d -> d.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("linkedin.com"), "New tab should contain linkedin.com in URL");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testAdminLoginWithInvalidCredentials() {
        openHomePage();
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("adminlogin")));
        adminLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys("wrong");
        driver.findElement(By.id("password")).sendKeys("wrongpass");
        driver.findElement(By.id("doLogin")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"), "Invalid login should show error message");
    }
}
