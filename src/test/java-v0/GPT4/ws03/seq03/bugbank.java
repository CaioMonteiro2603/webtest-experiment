package GPT4.ws03.seq03;

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
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void login(String email, String password) {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login']")));
        loginBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[data-test='login-btn']")).click();
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.cssSelector("button[data-test='logout']")).size() > 0) {
            driver.findElement(By.cssSelector("button[data-test='logout']")).click();
        }
    }

    private void assertDashboard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[data-test='balance']")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("p[data-test='balance']")).isDisplayed(), "Dashboard balance not visible");
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        assertDashboard();
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid@user.com", "wrongpass");
        WebElement errorModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(errorModal.getText().toLowerCase().contains("erro") || errorModal.getText().toLowerCase().contains("dados"), "Expected error message not shown");
        driver.findElement(By.id("btnCloseModal")).click();
    }

    @Test
    @Order(3)
    public void testOpenTransferPage() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='transfer-btn']"))).click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("transferência"), "Transfer page not loaded");
        driver.navigate().back();
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testOpenStatementPage() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='extract-btn']"))).click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("extrato"), "Statement page not loaded");
        driver.navigate().back();
        logoutIfLoggedIn();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        By[] socialLinks = {
            By.cssSelector("a[href*='twitter.com']"),
            By.cssSelector("a[href*='facebook.com']"),
            By.cssSelector("a[href*='linkedin.com']")
        };
        String[] expectedDomains = {
            "twitter.com", "facebook.com", "linkedin.com"
        };
        for (int i = 0; i < socialLinks.length; i++) {
            List<WebElement> links = driver.findElements(socialLinks[i]);
            if (!links.isEmpty()) {
                links.get(0).click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newWindow = windows.iterator().next();
                driver.switchTo().window(newWindow);
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomains[i]), "Expected domain not found in new tab");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        login(USERNAME, PASSWORD);
        assertDashboard();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='logout']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank"), "Expected to be on login page after logout");
    }
}
