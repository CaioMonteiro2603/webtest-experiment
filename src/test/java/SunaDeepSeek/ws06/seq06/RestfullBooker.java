package SunaDeepSeek.ws06.seq06;

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
        Assertions.assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(2)
    public void testRoomInformationDisplayed() {
        driver.get(BASE_URL);
        List<WebElement> rooms = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".row.hotel-room-info")));
        Assertions.assertTrue(rooms.size() > 0, "Rooms should be displayed");
    }

    @Test
    @Order(3)
    public void testContactFormSubmission() {
        driver.get(BASE_URL);
        WebElement contactForm = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[data-target='#exampleModal']")));
        contactForm.click();

        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameInput.sendKeys("Test User");

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("test@example.com");

        WebElement phoneInput = driver.findElement(By.id("phone"));
        phoneInput.sendKeys("1234567890");

        WebElement subjectInput = driver.findElement(By.id("subject"));
        subjectInput.sendKeys("Test Subject");

        WebElement messageInput = driver.findElement(By.id("description"));
        messageInput.sendKeys("Test Message");

        WebElement submitButton = driver.findElement(By.id("submitContact"));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should be displayed");
    }

    @Test
    @Order(4)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        usernameInput.sendKeys("admin");

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("password");

        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout")));
        Assertions.assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String mainWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        switchToNewWindowAndVerify("twitter.com", mainWindow);

        // Test Facebook link
        driver.get(BASE_URL);
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        switchToNewWindowAndVerify("facebook.com", mainWindow);

        // Test LinkedIn link
        driver.get(BASE_URL);
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();
        switchToNewWindowAndVerify("linkedin.com", mainWindow);
    }

    private void switchToNewWindowAndVerify(String expectedDomain, String mainWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "URL should contain " + expectedDomain);
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(6)
    public void testRoomBookingFlow() {
        driver.get(BASE_URL);
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-outline-primary.book-room")));
        bookButton.click();

        WebElement firstNameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstname")));
        firstNameInput.sendKeys("Test");

        WebElement lastNameInput = driver.findElement(By.id("lastname"));
        lastNameInput.sendKeys("User");

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("test@example.com");

        WebElement phoneInput = driver.findElement(By.id("phone"));
        phoneInput.sendKeys("1234567890");

        WebElement bookNowButton = driver.findElement(By.cssSelector(".btn-outline-primary.float-right.book-room"));
        bookNowButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Booking success message should be displayed");
    }
}