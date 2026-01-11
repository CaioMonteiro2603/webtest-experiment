package GPT4.ws06.seq04;

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
    private static final String BASE_URL = "https://automationintesting.online";

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
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".col-sm-10 h1")));
        Assertions.assertTrue(header.isDisplayed(), "Hero title not visible on home page");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("testing"), "Title does not contain expected text");
    }

    @Test
    @Order(2)
    public void testNavBookRoomPage() {
        driver.get(BASE_URL);
        WebElement bookRoomBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='book']")));
        bookRoomBtn.click();
        WebElement bookForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        Assertions.assertTrue(bookForm.isDisplayed(), "Booking form is not visible");
    }

    @Test
    @Order(3)
    public void testBookRoomFormValidation() {
        driver.get(BASE_URL + "#book");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys("John Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("phone")).sendKeys("123456789");
        driver.findElement(By.cssSelector("button[class='btn btn-primary float-right']")).click();
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(alert.isDisplayed(), "Validation error should appear due to missing dates");
    }

    @Test
    @Order(4)
    public void testContactFormSubmission() {
        driver.get(BASE_URL);
        WebElement name = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[id='name']")));
        name.sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        driver.findElement(By.id("subject")).sendKeys("Testing");
        driver.findElement(By.id("description")).sendKeys("This is a test message.");
        driver.findElement(By.cssSelector("button[id='submitContact']")).click();
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-success")));
        Assertions.assertTrue(success.isDisplayed(), "Success alert not shown after form submission");
    }

    @Test
    @Order(5)
    public void testAdminLoginWithInvalidCredentials() {
        driver.get(BASE_URL + "/admin");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        username.sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("invalid");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(6)
    public void testAdminLoginAndLogout() {
        driver.get(BASE_URL + "/admin");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        username.sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement roomsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".col-sm-12 h2")));
        Assertions.assertTrue(roomsHeader.isDisplayed(), "Login failed or dashboard not loaded");

        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='logout']")));
        logout.click();

        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[type='submit']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Logout failed or login page not visible");
    }

    @Test
    @Order(7)
    public void testExternalTwitterLink() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("twitter.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link did not open correct URL");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(8)
    public void testExternalFacebookLink() {
        driver.get(BASE_URL);
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        fbLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("facebook.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link did not open correct URL");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(9)
    public void testExternalInstagramLink() {
        driver.get(BASE_URL);
        WebElement instaLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='instagram.com']")));
        String originalWindow = driver.getWindowHandle();
        instaLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("instagram.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("instagram.com"), "Instagram link did not open correct URL");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(10)
    public void testFooterVisibilityAndText() {
        driver.get(BASE_URL);
        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
        Assertions.assertTrue(footer.isDisplayed(), "Footer is not visible");
        Assertions.assertTrue(footer.getText().toLowerCase().contains("© 2025"), "Footer does not contain expected text");
    }
}