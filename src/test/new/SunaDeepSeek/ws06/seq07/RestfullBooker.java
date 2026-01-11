package SunaDeepSeek.ws06.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Restful-booker-platform demo"));
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be on home page");
    }

    @Test
    @Order(2)
    public void testRoomInformationPage() {
        driver.get(BASE_URL);
        WebElement roomLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/room']")));
        roomLink.click();
        wait.until(ExpectedConditions.urlContains("/room"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/room"), "Should be on room page");
    }

    @Test
    @Order(3)
    public void testAdminPage() {
        driver.get(BASE_URL);
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/admin']")));
        adminLink.click();
        wait.until(ExpectedConditions.urlContains("/admin"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin"), "Should be on admin page");
    }

    @Test
    @Order(4)
    public void testAboutPage() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/about']")));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("/about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/about"), "Should be on about page");
    }

    @Test
    @Order(5)
    public void testContactPage() {
        driver.get(BASE_URL);
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/contact']")));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("/contact"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/contact"), "Should be on contact page");
    }

    @Test
    @Order(6)
    public void testTwitterLink() {
        testExternalLink("a[href*='twitter.com']", "twitter.com");
    }

    @Test
    @Order(7)
    public void testFacebookLink() {
        testExternalLink("a[href*='facebook.com']", "facebook.com");
    }

    @Test
    @Order(8)
    public void testLinkedInLink() {
        testExternalLink("a[href*='linkedin.com']", "linkedin.com");
    }

    private void testExternalLink(String linkSelector, String expectedDomain) {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(linkSelector)));
        socialLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "Should be on " + expectedDomain + " domain");
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testAdminLogin() {
        driver.get(BASE_URL + "admin/");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name*='username'], input[id*='username'], input[type='text']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name*='password'], input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], input[type='submit'], button:contains('Login'), button:contains('Submit')"));
        
        usernameField.sendKeys("admin");
        passwordField.sendKeys("password");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/admin"));
        WebElement logoutButton = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(),'Logout') or contains(text(),'Log out') or @value='Logout' or @value='Log out']")));
        Assertions.assertTrue(logoutButton.getText().contains("Logout") || logoutButton.getAttribute("value").contains("Logout"), "Should be logged in");
    }

    @Test
    @Order(10)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "admin/");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name*='username'], input[id*='username'], input[type='text']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name*='password'], input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], input[type='submit'], button:contains('Login'), button:contains('Submit')"));
        
        usernameField.sendKeys("wrong");
        passwordField.sendKeys("credentials");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-danger, .error, .alert, [class*='error'], [class*='alert']")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials") || errorMessage.getText().toLowerCase().contains("invalid") || errorMessage.getText().toLowerCase().contains("error"), 
            "Should show error message for invalid login");
    }
}