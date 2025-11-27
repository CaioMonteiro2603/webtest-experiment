package SunaDeepSeek.ws06.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AutomationInTestingTest {

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

    @Test
    @Order(1)
    public void testHomePage() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Restful-booker-platform demo", driver.getTitle());
        
        // Test header elements
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("header.navbar")));
        Assertions.assertTrue(header.isDisplayed());
        
        // Test main page content
        WebElement heroSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("section.hero")));
        Assertions.assertTrue(heroSection.isDisplayed());
    }

    @Test
    @Order(2)
    public void testRoomListingPage() {
        driver.get(BASE_URL + "#/rooms");
        WebElement roomsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Rooms", roomsHeader.getText());
        
        // Test room cards
        List<WebElement> rooms = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".room-card")));
        Assertions.assertTrue(rooms.size() > 0);
    }

    @Test
    @Order(3)
    public void testAdminLoginPage() {
        driver.get(BASE_URL + "#/admin");
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(loginForm.isDisplayed());
        
        // Test login with invalid credentials
        driver.findElement(By.name("username")).sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("invalid");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        
        // Test Facebook link
        testExternalLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        
        // Test LinkedIn link
        testExternalLink(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testContactPage() {
        driver.get(BASE_URL + "#/contact");
        WebElement contactForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(contactForm.isDisplayed());
        
        // Test form elements
        Assertions.assertTrue(driver.findElement(By.name("name")).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.name("email")).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.name("phone")).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.name("subject")).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.name("description")).isDisplayed());
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}