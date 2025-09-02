package GPT4.ws03.seq06;

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
public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void openLoginModal() {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login']")));
        loginBtn.click();
    }

    private void performLogin(String email, String password) {
        openLoginModal();
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[data-test='email']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[data-test='password']"));
        WebElement loginSubmit = driver.findElement(By.cssSelector("button[data-test='login-submit']"));

        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginSubmit.click();
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.cssSelector("button[data-test='logout']")).size() > 0) {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='logout']")));
            logoutBtn.click();
        }
    }

    @Test
    @Order(1)
    public void testInvalidLogin() {
        performLogin("invalid@email.com", "wrongpass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Toastify__toast--error")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("credenciais"), "Should show error toast for invalid login.");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        performLogin(LOGIN, PASSWORD);
        WebElement homeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1[data-test='home-title']")));
        Assertions.assertTrue(homeHeader.isDisplayed(), "Home header should be visible after login.");
        Assertions.assertTrue(homeHeader.getText().contains("Olá"), "Home page should greet the user.");
        logoutIfLoggedIn();
    }

    @Test
    @Order(3)
    public void testExternalGitHubLink() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "External link should open GitHub page.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testResetStateViaReload() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("bugbank"), "Page title should contain 'BugBank'.");
        driver.navigate().refresh();
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank.netlify.app"), "URL should remain after refresh.");
    }
}
