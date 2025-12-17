package SunaDeepSeek.ws06.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Restful-booker-platform"));
        Assertions.assertTrue(driver.getCurrentUrl().contains(BASE_URL));
    }

    @Test
    @Order(2)
    public void testRoomInformation() {
        driver.get(BASE_URL);
        WebElement roomSection = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".row.hotel-description")));
        Assertions.assertTrue(roomSection.isDisplayed());
    }

    @Test
    @Order(3)
    public void testContactForm() {
        driver.get(BASE_URL);
        WebElement contactForm = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[data-target='#exampleModal']")));
        contactForm.click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("exampleModal")));
        Assertions.assertTrue(modal.isDisplayed());
    }

    @Test
    @Order(4)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[name='username']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("admin");
        passwordField.sendKeys("password");
        loginButton.click();

        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".admin-container")));
        Assertions.assertTrue(dashboard.isDisplayed());
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[name='username']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("wrong");
        passwordField.sendKeys("credentials");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"));
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        String mainWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(mainWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(7)
    public void testRoomBooking() {
        driver.get(BASE_URL);
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn.btn-outline-primary.float-right")));
        bookButton.click();

        WebElement bookingForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".container.booking-form")));
        Assertions.assertTrue(bookingForm.isDisplayed());
    }

    @Test
    @Order(8)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test About link
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='about']")));
        aboutLink.click();
        
        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"));
        
        // Test Contact link
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='contact']")));
        contactLink.click();
        
        wait.until(ExpectedConditions.urlContains("contact"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("contact"));
    }
}