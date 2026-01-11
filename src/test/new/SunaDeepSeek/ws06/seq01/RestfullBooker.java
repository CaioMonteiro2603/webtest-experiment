package SunaDeepSeek.ws06.seq01;

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
        Assertions.assertEquals("Shady Meadows B&B", driver.getTitle());
        
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(header.getText().contains("Shady Meadows"));
    }

    @Test
    @Order(2)
    public void testRoomInformationPage() {
        driver.get(BASE_URL);
        WebElement roomsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Rooms")));
        roomsLink.click();

        wait.until(ExpectedConditions.urlContains("#rooms"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#rooms"));
        
        List<WebElement> roomCards = driver.findElements(By.cssSelector(".container-fluid"));
        Assertions.assertTrue(roomCards.size() > 0);
    }

    @Test
    @Order(3)
    public void testAdminLoginPage() {
        driver.get(BASE_URL);
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Admin")));
        adminLink.click();

        wait.until(ExpectedConditions.urlContains("/admin"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin"));
        
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(loginForm.isDisplayed());
    }

    @Test
    @Order(4)
    public void testValidAdminLogin() {
        driver.get(BASE_URL + "admin#/login");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("admin");
        passwordField.sendKeys("password");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/admin"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin"));
    }

    @Test
    @Order(5)
    public void testInvalidAdminLogin() {
        driver.get(BASE_URL + "admin#/login");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("credentials");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"));
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(7)
    public void testContactPage() {
        driver.get(BASE_URL);
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Contact")));
        contactLink.click();

        wait.until(ExpectedConditions.urlContains("#contact"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#contact"));
        
        WebElement contactForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(contactForm.isDisplayed());
    }

    @Test
    @Order(8)
    public void testAboutPage() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("About")));
        aboutLink.click();

        wait.until(ExpectedConditions.urlContains("#about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#about"));
        
        WebElement aboutContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".container-fluid")));
        Assertions.assertTrue(aboutContent.isDisplayed());
    }
}