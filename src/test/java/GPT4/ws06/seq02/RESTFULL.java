package GPT4.ws06.seq02;

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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Welcome')]")));
        Assertions.assertTrue(heading.getText().contains("Welcome"), "Homepage heading mismatch");
    }

    @Test
    @Order(2)
    public void testSubmitContactForm() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState")).equals("complete");
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("name")));
        nameInput.sendKeys("John Doe");
        driver.findElement(By.name("email")).sendKeys("john@example.com");
        driver.findElement(By.name("phone")).sendKeys("1234567890");
        driver.findElement(By.name("subject")).sendKeys("Test subject");
        driver.findElement(By.name("description")).sendKeys("This is a test message.");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.btn-outline-primary")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.getText().contains("Thanks for getting in touch"), "Success message not displayed");
    }

    @Test
    @Order(3)
    public void testAdminLoginInvalidCredentials() {
        driver.get(BASE_URL + "admin");
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        username.sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("invalid");
        driver.findElement(By.id("doLogin")).click();

        try {
            Thread.sleep(1000); // Wait for error message to appear
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(error.getText().contains("Invalid") || error.getText().contains("incorrect"), "Invalid login error not shown");
    }

    @Test
    @Order(4)
    public void testAdminLoginSuccessAndLogout() {
        driver.get(BASE_URL + "admin");
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        username.clear();
        username.sendKeys("admin");
        WebElement password = driver.findElement(By.id("password"));
        password.clear();
        password.sendKeys("password");
        driver.findElement(By.id("doLogin")).click();

        try {
            Thread.sleep(2000); // Wait for redirect to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin") && !driver.getCurrentUrl().endsWith("/admin"), "Login did not redirect to admin dashboard");

        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout")));
        logoutBtn.click();

        try {
            Thread.sleep(1000); // Wait for logout redirect
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin"), "Did not return to login page after logout");
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        List<WebElement> socialLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("footer a")));

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && href.startsWith("http")) {
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
                try {
                    Thread.sleep(1000); // Wait for new window to open
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                Set<String> windows = driver.getWindowHandles();
                if (windows.size() > 1) {
                    for (String win : windows) {
                        if (!win.equals(originalWindow)) {
                            driver.switchTo().window(win);
                            String currentUrl = driver.getCurrentUrl();
                            if (!currentUrl.equals("about:blank")) {
                                Assertions.assertTrue(currentUrl.contains("twitter") || currentUrl.contains("facebook") || currentUrl.contains("linkedin"),
                                        "Unexpected external domain: " + currentUrl);
                            }
                            driver.close();
                            driver.switchTo().window(originalWindow);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testRoomsDisplayed() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState")).equals("complete");
        List<WebElement> rooms = driver.findElements(By.className("room-info"));
        Assertions.assertFalse(rooms.isEmpty(), "No rooms were displayed on the homepage");
    }

    @Test
    @Order(7)
    public void testBookButtonPresence() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState")).equals("complete");
        WebElement bookButton = driver.findElement(By.xpath("//button[contains(@text(),'Book this room')]"));
        Assertions.assertEquals("Book this room", bookButton.getText().trim(), "Book button text mismatch");
    }
}