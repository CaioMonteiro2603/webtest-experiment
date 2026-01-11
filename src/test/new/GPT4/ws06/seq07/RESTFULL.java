package GPT4.ws06.seq07;

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
public class RESTFULL {

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals("Welcome to Restful-booker Platform", header.getText(), "Hero title should be present");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("restful-booker"), "Title should mention 'restful-booker'");
    }

    @Test
    @Order(2)
    public void testBookRoomNavigation() {
        driver.get(BASE_URL);
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-primary")));
        bookButton.click();
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.row")));
        Assertions.assertTrue(form.isDisplayed(), "Booking form should be visible after clicking 'Book this room'");
    }

    @Test
    @Order(3)
    public void testSubmitContactFormSuccess() {
        driver.get(BASE_URL);
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement phoneInput = driver.findElement(By.id("phone"));
        WebElement subjectInput = driver.findElement(By.id("subject"));
        WebElement descriptionInput = driver.findElement(By.id("description"));
        WebElement submitBtn = driver.findElement(By.cssSelector("button.btn.btn-primary"));

        nameInput.sendKeys("John Doe");
        emailInput.sendKeys("john@example.com");
        phoneInput.sendKeys("1234567890");
        subjectInput.sendKeys("Booking Inquiry");
        descriptionInput.sendKeys("Please provide more details about the room.");

        submitBtn.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.col-sm-12 h2")));
        Assertions.assertEquals("Thanks for getting in touch", successMessage.getText(), "Successful submission message should appear");
    }

    @Test
    @Order(4)
    public void testSubmitContactFormValidationError() {
        driver.get(BASE_URL);
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-primary")));
        submitBtn.click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("please fill out this field"), "Validation message should appear when submitting empty form");
    }

    @Test
    @Order(5)
    public void testAdminLoginFailure() {
        driver.get(BASE_URL + "admin");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("doLogin"));

        username.sendKeys("wrong");
        password.sendKeys("wrong");
        loginBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(6)
    public void testAdminLoginSuccess() {
        driver.get(BASE_URL + "admin");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("doLogin"));

        username.clear();
        password.clear();
        username.sendKeys("admin");
        password.sendKeys("password");
        loginBtn.click();

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals("Rooms", header.getText(), "Login should redirect to Rooms admin page");
    }

    @Test
    @Order(7)
    public void testExternalTwitterLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        handles.remove(originalWindow);
        driver.switchTo().window(handles.iterator().next());

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter URL should contain twitter.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testExternalFacebookLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
        fbLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        handles.remove(originalWindow);
        driver.switchTo().window(handles.iterator().next());

        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook URL should contain facebook.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testExternalLinkedInLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        linkedinLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        handles.remove(originalWindow);
        driver.switchTo().window(handles.iterator().next());

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn URL should contain linkedin.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}