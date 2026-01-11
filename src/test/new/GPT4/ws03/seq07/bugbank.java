package GPT4.ws03.seq07;

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
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entregar')]"));
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }

    private void logoutIfLoggedIn() {
        List<WebElement> logoutBtns = driver.findElements(By.id("btnExit"));
        if (!logoutBtns.isEmpty()) {
            logoutBtns.get(0).click();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("caio@gmail.com", "123");
        WebElement homeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        Assertions.assertTrue(homeMsg.getText().contains("Bem vindo"), "Login should succeed and welcome message should be visible.");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        login("invalid@user.com", "wrongpass");
        WebElement errorModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(errorModal.getText().contains("Usuário ou senha inválido"), "Error message should appear for invalid credentials.");
        WebElement closeBtn = driver.findElement(By.id("btnCloseModal"));
        closeBtn.click();
    }

    @Test
    @Order(3)
    public void testExternalAboutLink() {
        login("caio@gmail.com", "123");
        WebElement aboutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnAbout")));
        String originalWindow = driver.getWindowHandle();
        aboutBtn.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        for (String win : allWindows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("netlify"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("netlify"), "External about link should open netlify domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testResetAppState() {
        login("caio@gmail.com", "123");
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("resetAppState")));
        resetBtn.click();
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Toastify__toast-body")));
        Assertions.assertTrue(toast.getText().contains("estado reiniciado"), "App state should be reset successfully.");
        logoutIfLoggedIn();
    }

    @Test
    @Order(5)
    public void testLogoutFunctionality() {
        login("caio@gmail.com", "123");
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExit")));
        logoutBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Entregar')]")));
        Assertions.assertTrue(driver.getCurrentUrl().contains(BASE_URL), "User should be redirected to login page after logout.");
    }

    @Test
    @Order(6)
    public void testFooterTwitterLink() {
        login("caio@gmail.com", "123");
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
        logoutIfLoggedIn();
    }

    @Test
    @Order(7)
    public void testFooterFacebookLink() {
        login("caio@gmail.com", "123");
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        fbLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open facebook.com domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
        logoutIfLoggedIn();
    }

    @Test
    @Order(8)
    public void testFooterLinkedInLink() {
        login("caio@gmail.com", "123");
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
        logoutIfLoggedIn();
    }
}