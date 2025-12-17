package GPT4.ws03.seq05;

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
public class bugbank {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login']")));
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys(user);
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys(pass);
        loginBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        WebElement homeBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(homeBanner.getText().contains("Olá"), "Login should succeed and welcome message should be shown.");
        driver.findElement(By.id("btnExit")).click();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("wrong@wrong.com", "wrongpass");
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(modal.getText().toLowerCase().contains("problema"), "Should show error modal for invalid credentials.");
        driver.findElement(By.id("btnCloseModal")).click();
    }

    @Test
    @Order(3)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("footer a[href^='http']"));
        String originalWindow = driver.getWindowHandle();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            link.click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> allWindows = driver.getWindowHandles();
            for (String handle : allWindows) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com") || 
                                          driver.getCurrentUrl().contains("facebook.com") ||
                                          driver.getCurrentUrl().contains("linkedin.com"),
                            "External URL should contain expected domain.");
                    driver.close();
                    break;
                }
            }
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(4)
    public void testRegisterAndReturnToLogin() {
        driver.get(BASE_URL);
        WebElement registerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='register']")));
        registerBtn.click();
        WebElement registerTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(registerTitle.getText().contains("Registrar"), "Should be on the register page.");
        WebElement backToLogin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test='back-btn']")));
        backToLogin.click();
        WebElement loginTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(loginTitle.getText().contains("BugBank"), "Should be back on login page.");
    }

    @Test
    @Order(5)
    public void testResetAppStateIfExists() {
        login(USERNAME, PASSWORD);
        List<WebElement> resetBtns = driver.findElements(By.id("btnReset"));
        if (!resetBtns.isEmpty()) {
            resetBtns.get(0).click();
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
            Assertions.assertTrue(modal.getText().toLowerCase().contains("sucesso"), "Should show success message for reset.");
            driver.findElement(By.id("btnCloseModal")).click();
        }
        driver.findElement(By.id("btnExit")).click();
    }
}
