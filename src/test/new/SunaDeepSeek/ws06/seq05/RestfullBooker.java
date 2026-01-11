package SunaDeepSeek.ws06.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

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
    public void testHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Restful-booker-platform"));
        
        // Verify main page elements
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(header.getText().contains("Hotel")));
        
        // Verify navigation links
        verifyNavLink("Rooms", "/#/"));
        verifyNavLink("Admin", "/#/admin");
        verifyNavLink("Branding", "/#/branding");
    }

    @Test
    @Order(2)
    public void testRoomsPage() {
        driver.get(BASE_URL + "#/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".row")));
        
        // Verify room cards are displayed
        List<WebElement> rooms = driver.findElements(By.cssSelector(".row"));
        Assertions.assertTrue(rooms.size() > 0, "No rooms found on rooms page");
        
        // Verify room details
        WebElement firstRoom = rooms.get(0);
        Assertions.assertTrue(firstRoom.findElement(By.cssSelector(".col-sm-4")).isDisplayed());
        Assertions.assertTrue(firstRoom.findElement(By.cssSelector(".col-sm-6")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testAdminPage() {
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("username")));
        
        // Test login with invalid credentials
        driver.findElement(By.id("username")).sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("invalid");
        driver.findElement(By.id("doLogin")).click();
        
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(error.getText().contains("Bad credentials"));
        
        // Test login with valid credentials
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("doLogin")).click();
        
        wait.until(ExpectedConditions.urlContains("/admin"));
        Assertions.assertTrue(driver.findElement(By.id("logout")).isDisplayed());
        
        // Logout
        driver.findElement(By.id("logout")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
    }

    @Test
    @Order(4)
    public void testBrandingPage() {
        driver.get(BASE_URL + "#/branding");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".container")));
        
        // Verify branding elements
        WebElement title = driver.findElement(By.cssSelector(".col-sm-8"));
        Assertions.assertTrue(title.isDisplayed());
        
        WebElement description = driver.findElement(By.cssSelector(".form-group"));
        Assertions.assertTrue(description.isDisplayed());
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("Twitter", "https://twitter.com/", "twitter");
        
        // Test GitHub link
        testExternalLink("GitHub", "https://github.com/", "github");
    }

    private void verifyNavLink(String linkText, String expectedUrlPart) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText(linkText)));
        link.click();
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart));
    }

    private void testExternalLink(String linkText, String expectedDomain, String linkId) {
        String mainWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("[href*='twitter.com'], [href*='github.com']")));
        link.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        
        // Close window and switch back
        driver.close();
        driver.switchTo().window(mainWindow);
    }
}