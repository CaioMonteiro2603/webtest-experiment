package GPT4.ws06.seq05;

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
        driver.get(BASE_URL);
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
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.hero-title")));
        Assertions.assertTrue(heading.getText().contains("Welcome"), "Home page heading should contain 'Welcome'");
    }

    @Test
    @Order(2)
    public void testSubmitContactFormWithoutDataShowsError() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='submit-contact']")));
        submitButton.click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-testid='contact-email-error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed when required fields are missing");
    }

    @Test
    @Order(3)
    public void testSuccessfulContactFormSubmission() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("name"))).sendKeys("John Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        driver.findElement(By.id("subject")).sendKeys("Testing");
        driver.findElement(By.id("description")).sendKeys("This is a test message.");
        WebElement submitButton = driver.findElement(By.cssSelector("button[data-testid='submit-contact']"));
        submitButton.click();
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-success")));
        Assertions.assertTrue(alert.getText().toLowerCase().contains("thanks"), "Success message should contain 'thanks'");
    }

    @Test
    @Order(4)
    public void testRoomsPageLoads() {
        driver.get(BASE_URL + "#rooms");
        WebElement roomHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.room-name")));
        Assertions.assertTrue(roomHeader.isDisplayed(), "Room header should be displayed on rooms page");
    }

    @Test
    @Order(5)
    public void testBookButtonOpensModal() {
        driver.get(BASE_URL + "#rooms");
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.openBooking")));// first visible button
        bookButton.click();
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("booking-modal")));
        Assertions.assertTrue(modal.isDisplayed(), "Booking modal should be visible after clicking Book");
        WebElement closeButton = modal.findElement(By.cssSelector("button.close"));
        closeButton.click();
        wait.until(ExpectedConditions.invisibilityOf(modal));
    }

    @Test
    @Order(6)
    public void testExternalLinksOpenCorrectly() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[target='_blank']"));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
            wait.until(driver -> driver.getWindowHandles().size() > 1);

            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    wait.until(driver1 -> driver1.getCurrentUrl().startsWith("http"));
                    String currentUrl = driver.getCurrentUrl();
                    Assertions.assertTrue(currentUrl.startsWith("http"), "External link should open in new tab with valid URL");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(7)
    public void testFooterPresence() {
        driver.get(BASE_URL);
        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
        Assertions.assertTrue(footer.isDisplayed(), "Footer should be visible on the home page");
    }

    @Test
    @Order(8)
    public void testHeaderNavigationLinks() {
        driver.get(BASE_URL);
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a[href^='#']"));
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
            Assertions.assertTrue(driver.getCurrentUrl().contains(href.substring(href.indexOf("#"))),
                    "URL should update to section: " + href);
        }
    }
}
