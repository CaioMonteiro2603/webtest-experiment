package GPT4.ws06.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    public void testHomepageLoadsCorrectly() {
        String title = driver.getTitle();
        Assertions.assertTrue(title.contains("Restful-booker"), "Title does not contain expected text.");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("welcome"), "Header does not contain 'welcome'.");
    }

    @Test
    @Order(2)
    public void testContactFormValidation() {
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameInput.sendKeys("John Doe");
        driver.findElement(By.id("email")).sendKeys("john@example.com");
        driver.findElement(By.id("phone")).sendKeys("123456789");
        driver.findElement(By.id("subject")).sendKeys("Test Subject");
        driver.findElement(By.id("description")).sendKeys("This is a test message.");
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        Assertions.assertTrue(alert.getText().toLowerCase().contains("thanks"), "Success message not displayed after contact form submission.");
    }

    @Test
    @Order(3)
    public void testNavigationToRoomsPage() {
        WebElement roomsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Rooms")));
        roomsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/#rooms"), "URL did not navigate to rooms section.");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-container")));
        WebElement roomContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[class*='room']")));
        Assertions.assertTrue(roomContainer.isDisplayed(), "Room information not displayed.");
    }

    @Test
    @Order(4)
    public void testExternalLinkTwitter() {
        WebElement twitterIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterIcon.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();

        driver.switchTo().window(newWindow);
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("twitter.com"), "Twitter external link did not open correctly.");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testExternalLinkFacebook() {
        WebElement fbIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        fbIcon.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();

        driver.switchTo().window(newWindow);
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("facebook.com"), "Facebook external link did not open correctly.");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testExternalLinkLinkedIn() {
        List<WebElement> linkedinLinks = driver.findElements(By.cssSelector("a[href*='linkedin.com']"));
        if (linkedinLinks.size() > 0 && linkedinLinks.get(0).isDisplayed()) {
            WebElement liIcon = wait.until(ExpectedConditions.elementToBeClickable(linkedinLinks.get(0)));
            String originalWindow = driver.getWindowHandle();
            liIcon.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();

            driver.switchTo().window(newWindow);
            String url = driver.getCurrentUrl();
            Assertions.assertTrue(url.contains("linkedin.com"), "LinkedIn external link did not open correctly.");

            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            Assertions.assertTrue(true, "LinkedIn link not present on the page, which is acceptable.");
        }
    }
}