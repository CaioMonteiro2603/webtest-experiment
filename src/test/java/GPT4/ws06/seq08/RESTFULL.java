package GPT4.ws06.seq08;

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
        driver.get(BASE_URL);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.hero-title")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("welcome"),
                "Hero title should contain 'Welcome'");
    }

    @Test
    @Order(2)
    public void testNavContactPageLoads() {
        driver.get(BASE_URL);
        WebElement contactNav = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactNav.click();
        wait.until(ExpectedConditions.urlContains("/#contact"));
        WebElement formHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#contact h2")));
        Assertions.assertEquals("Contact", formHeader.getText(), "Contact section should be visible");
    }

    @Test
    @Order(3)
    public void testNavRoomsPageLoads() {
        driver.get(BASE_URL);
        WebElement roomsNav = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Rooms")));
        roomsNav.click();
        wait.until(ExpectedConditions.urlContains("/#rooms"));
        WebElement roomHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rooms h2")));
        Assertions.assertEquals("Rooms", roomHeader.getText(), "Rooms section should be visible");
    }

    @Test
    @Order(4)
    public void testRoomCardElementsPresent() {
        driver.get(BASE_URL + "#rooms");
        List<WebElement> cards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".room-info")));
        Assertions.assertFalse(cards.isEmpty(), "At least one room card should be present");

        WebElement firstCard = cards.get(0);
        WebElement title = firstCard.findElement(By.cssSelector("h3"));
        WebElement price = firstCard.findElement(By.cssSelector(".price"));
        Assertions.assertTrue(title.isDisplayed(), "Room title should be visible");
        Assertions.assertTrue(price.getText().contains("£"), "Price should contain currency symbol");
    }

    @Test
    @Order(5)
    public void testBookThisRoomButtonExists() {
        driver.get(BASE_URL + "#rooms");
        List<WebElement> buttons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".room-info .btn-primary")));
        Assertions.assertFalse(buttons.isEmpty(), "There should be at least one 'Book this room' button");
        for (WebElement btn : buttons) {
            Assertions.assertEquals("Book this room", btn.getText(), "Button text should be 'Book this room'");
        }
    }

    @Test
    @Order(6)
    public void testSubmitContactFormMissingRequiredFields() {
        driver.get(BASE_URL + "#contact");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#contact button[type='submit']")));
        submitBtn.click();
        List<WebElement> errors = driver.findElements(By.cssSelector(".alert-danger"));
        Assertions.assertFalse(errors.isEmpty(), "Should show error alert for missing required fields");
    }

    @Test
    @Order(7)
    public void testSubmitContactFormValid() {
        driver.get(BASE_URL + "#contact");
        wait.until(ExpectedConditions.elementToBeClickable(By.name("name"))).sendKeys("John Doe");
        driver.findElement(By.name("email")).sendKeys("john@example.com");
        driver.findElement(By.name("phone")).sendKeys("1234567890");
        driver.findElement(By.name("subject")).sendKeys("Test Subject");
        driver.findElement(By.name("description")).sendKeys("Test message content.");

        WebElement submitBtn = driver.findElement(By.cssSelector("#contact button[type='submit']"));
        submitBtn.click();

        WebElement alertSuccess = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(alertSuccess.getText().toLowerCase().contains("thanks"),
                "Success alert should confirm submission");
    }

    @Test
    @Order(8)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should open Twitter");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testFooterFacebookLink() {
        driver.get(BASE_URL);
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        fbLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should open Facebook");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        String originalWindow = driver.getWindowHandle();
        linkedInLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should open LinkedIn");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
