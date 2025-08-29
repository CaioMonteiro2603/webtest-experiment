package GPT4.ws03.seq04;

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
public class BugBankTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
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

    private void openLoginModal() {
        driver.get(BASE_URL);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogin")));
        loginButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));
    }

    private void performLogin(String email, String password) {
        openLoginModal();
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement accessBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        accessBtn.click();
    }

    private void logoutIfLoggedIn() {
        List<WebElement> logoutBtn = driver.findElements(By.id("btnExit"));
        if (!logoutBtn.isEmpty()) {
            logoutBtn.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogin")));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin(LOGIN, PASSWORD);
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        Assertions.assertTrue(welcome.getText().contains("Bem vindo"), "Login failed or welcome message not displayed");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        performLogin("invalid@email.com", "wrongpass");
        WebElement errorModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(errorModal.getText().toLowerCase().contains("dados"), "Expected error message not displayed");
        WebElement closeBtn = driver.findElement(By.id("btnCloseModal"));
        closeBtn.click();
    }

    @Test
    @Order(3)
    public void testAboutExternalLink() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("linkedin"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin"), "Did not navigate to LinkedIn page");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(4)
    public void testRegisterAccountButton() {
        driver.get(BASE_URL);
        WebElement registerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnRegister")));
        registerBtn.click();
        WebElement registerTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(registerTitle.getText().contains("Registro"), "Register page not displayed");
    }

    @Test
    @Order(5)
    public void testFooterLinksPresence() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("footer a"));
        Assertions.assertFalse(links.isEmpty(), "No footer social links found");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            String domain = href.contains("twitter") ? "twitter" :
                            href.contains("facebook") ? "facebook" :
                            href.contains("linkedin") ? "linkedin" : "";
            if (!domain.isEmpty()) {
                String originalWindow = driver.getWindowHandle();
                link.click();
                wait.until(driver -> driver.getWindowHandles().size() > 1);
                Set<String> handles = driver.getWindowHandles();
                for (String handle : handles) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        wait.until(ExpectedConditions.urlContains(domain));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "External link did not open " + domain);
                        driver.close();
                        driver.switchTo().window(originalWindow);
                        break;
                    }
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testTransferButtonVisibilityAfterLogin() {
        performLogin(LOGIN, PASSWORD);
        WebElement transferBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btnTransfer")));
        Assertions.assertTrue(transferBtn.isDisplayed(), "Transfer button not visible after login");
        logoutIfLoggedIn();
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        performLogin(LOGIN, PASSWORD);
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnReset")));
        resetBtn.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        Assertions.assertTrue(welcome.isDisplayed(), "Reset App State failed");
        logoutIfLoggedIn();
    }
}
