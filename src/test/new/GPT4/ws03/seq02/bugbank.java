package GPT4.ws03.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

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
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void openLoginModal() {
        WebElement accessBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Acessar')]")));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", accessBtn);
    }

    private void login(String email, String password) {
        openLoginModal();
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(),'Acessar')]"));

        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", loginBtn);
    }

    private void logoutIfNeeded() {
        List<WebElement> logoutBtn = driver.findElements(By.id("btnExit"));
        if (!logoutBtn.isEmpty()) {
            JavascriptExecutor executor = (JavascriptExecutor)driver;
            executor.executeScript("arguments[0].click();", logoutBtn.get(0));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Acessar')]")));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[data-test='welcome-message']")));
        Assertions.assertTrue(welcome.getText().contains("bem-vindo"), "Login failed: welcome message not found");
        logoutIfNeeded();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("wrong@email.com", "wrongpass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("dados inválidos"), "Expected invalid credentials message");
        WebElement closeBtn = driver.findElement(By.id("btnCloseModal"));
        closeBtn.click();
    }

    @Test
    @Order(3)
    public void testAccountOverview() {
        login(USERNAME, PASSWORD);
        WebElement balance = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[data-test='balance']")));
        Assertions.assertTrue(balance.getText().contains("R$"), "Balance info not displayed");
        logoutIfNeeded();
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        login(USERNAME, PASSWORD);
        String originalWindow = driver.getWindowHandle();

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("facebook.com") || href.contains("linkedin.com") || href.contains("twitter.com"))) {
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com") ||
                                              driver.getCurrentUrl().contains("twitter.com") ||
                                              driver.getCurrentUrl().contains("linkedin.com"),
                                              "External link did not navigate to expected domain");
                        driver.close();
                        break;
                    }
                }
                driver.switchTo().window(originalWindow);
            }
        }
        logoutIfNeeded();
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        login(USERNAME, PASSWORD);
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnReset")));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", resetBtn);
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(modal.getText().toLowerCase().contains("dados reiniciados"), "Reset state failed");
        WebElement closeBtn = driver.findElement(By.id("btnCloseModal"));
        closeBtn.click();
        logoutIfNeeded();
    }

    @Test
    @Order(6)
    public void testLogout() {
        login(USERNAME, PASSWORD);
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExit")));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", logoutBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Acessar')]")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank"), "Logout did not return to base page");
    }
}