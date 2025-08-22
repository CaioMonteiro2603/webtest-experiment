package GPT4.ws06.seq01;

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
public class AutomationInTestingTest {

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

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected domain not found in URL: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("room"), "Header should mention rooms or booking.");
    }

    @Test
    @Order(2)
    public void testNavigateToRooms() {
        driver.get(BASE_URL);
        WebElement roomsNav = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#rooms']")));
        roomsNav.click();
        WebElement roomsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rooms")));
        Assertions.assertTrue(roomsSection.isDisplayed(), "Rooms section should be visible after clicking navigation.");
    }

    @Test
    @Order(3)
    public void testBookRoomFormPresent() {
        driver.get(BASE_URL);
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-roomid='1']")));
        bookButton.click();
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".popup-content")));
        Assertions.assertTrue(modal.isDisplayed(), "Booking modal should be displayed.");
    }

    @Test
    @Order(4)
    public void testContactFormValidationError() {
        driver.get(BASE_URL);
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement subjectInput = driver.findElement(By.id("subject"));
        WebElement messageInput = driver.findElement(By.id("description"));
        WebElement submitBtn = driver.findElement(By.id("submitContact"));

        nameInput.sendKeys("Test User");
        emailInput.sendKeys("invalid-email");
        subjectInput.sendKeys("Test Subject");
        messageInput.sendKeys("Test Message");
        submitBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(error.isDisplayed(), "Error alert should be displayed for invalid email.");
    }

    @Test
    @Order(5)
    public void testContactFormSuccess() {
        driver.get(BASE_URL);
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement subjectInput = driver.findElement(By.id("subject"));
        WebElement messageInput = driver.findElement(By.id("description"));
        WebElement submitBtn = driver.findElement(By.id("submitContact"));

        nameInput.clear();
        emailInput.clear();
        subjectInput.clear();
        messageInput.clear();

        nameInput.sendKeys("Tester");
        emailInput.sendKeys("tester@example.com");
        subjectInput.sendKeys("Subject");
        messageInput.sendKeys("This is a message.");
        submitBtn.click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-success")));
        Assertions.assertTrue(success.isDisplayed(), "Success message should be displayed after form submission.");
    }

    @Test
    @Order(6)
    public void testTwitterExternalLink() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        switchToNewTabAndVerify("twitter.com");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }

    @Test
    @Order(7)
    public void testFacebookExternalLink() {
        driver.get(BASE_URL);
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();
        switchToNewTabAndVerify("facebook.com");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }

    @Test
    @Order(8)
    public void testLinkedInExternalLink() {
        driver.get(BASE_URL);
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();
        switchToNewTabAndVerify("linkedin.com");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }

    @Test
    @Order(9)
    public void testAdminLoginInvalidCredentials() {
        driver.get(BASE_URL + "admin");
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        username.sendKeys("wronguser");
        password.sendKeys("wrongpass");
        loginBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should appear for invalid login.");
    }

    @Test
    @Order(10)
    public void testAdminLoginValidCredentials() {
        driver.get(BASE_URL + "admin");
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        username.clear();
        password.clear();
        username.sendKeys("admin");
        password.sendKeys("password");
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/admin"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin"), "Admin should be redirected to dashboard.");
    }
}
