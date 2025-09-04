package GPT4.ws10.seq08;

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
public class BrasilAgriTestSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1920, 1080));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        emailInput.clear();
        emailInput.sendKeys(LOGIN);
        passwordInput.clear();
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    private void logoutIfNeeded() {
        if (driver.getCurrentUrl().contains("/dashboard")) {
            List<WebElement> menuButtons = driver.findElements(By.cssSelector("button[aria-label='open drawer']"));
            if (!menuButtons.isEmpty()) {
                menuButtons.get(0).click();
                WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Logout')]")));
                logout.click();
                wait.until(ExpectedConditions.urlContains("/login"));
            }
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        logoutIfNeeded();
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be on dashboard after login");
        WebElement welcomeText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h6")));
        Assertions.assertTrue(welcomeText.getText().toLowerCase().contains("bem vindo"), "Welcome text should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        emailInput.clear();
        emailInput.sendKeys("wrong@user.com");
        passwordInput.clear();
        passwordInput.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".MuiAlert-message")));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("credenciais"), "Should show credentials error");
    }

    @Test
    @Order(3)
    public void testSidebarNavigationLinks() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='open drawer']")));
        menuButton.click();

        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Dashboard')]")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should navigate to dashboard");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='open drawer']")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Sobre')]")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("brasilagritest"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("brasilagritest"), "External link should contain 'brasilagritest'");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testLogout() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='open drawer']")));
        menuButton.click();

        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Logout')]")));
        logout.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Should redirect to login page after logout");
    }
}
