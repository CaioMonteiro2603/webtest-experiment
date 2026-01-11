package GPT4.ws03.seq01;

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
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caiomont@gmail.com";
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

    private void performLogin(String username, String password) {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='button'][class*='login']")));
        loginBtn.click();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement accessBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.clear();
        emailField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        accessBtn.click();
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.cssSelector("button[data-test='logout']")).size() > 0) {
            driver.findElement(By.cssSelector("button[data-test='logout']")).click();
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
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        WebElement welcomeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[data-test='welcome-message']")));
        Assertions.assertTrue(welcomeMsg.isDisplayed(), "Login successful, welcome message should be visible.");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        performLogin("wrong@email.com", "wrongpass");
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-content")));
        Assertions.assertTrue(alert.getText().toLowerCase().contains("erro"), "Error message should appear for invalid credentials.");
        WebElement closeBtn = alert.findElement(By.cssSelector("button"));
        closeBtn.click();
    }

    @Test
    @Order(3)
    public void testRegisterPageOpen() {
        driver.get(BASE_URL);
        WebElement registerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='button'][class*='register']")));
        registerBtn.click();
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-content")));
        Assertions.assertTrue(modal.getText().toLowerCase().contains("cadastre"), "Registration modal should appear.");
        WebElement closeBtn = modal.findElement(By.cssSelector("button"));
        closeBtn.click();
    }

    @Test
    @Order(4)
    public void testExternalGithubLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com']")));
        footerLink.click();
        switchToNewTabAndVerify("github.com");
    }

    @Test
    @Order(5)
    public void testExternalLinkedInLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        footerLink.click();
        switchToNewTabAndVerify("linkedin.com");
    }
}