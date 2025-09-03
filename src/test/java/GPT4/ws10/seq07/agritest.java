package GPT5.ws10.seq07;

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
public class BrasilAgriTest {

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
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        Assertions.assertTrue(emailInput.isDisplayed(), "Email input should be visible");
        Assertions.assertTrue(passwordInput.isDisplayed(), "Password input should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        passwordInput.clear();
        emailInput.sendKeys("invalid@example.com");
        passwordInput.sendKeys("wrongpassword");
        loginButton.click();

        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".v-alert__content")));
        Assertions.assertTrue(alert.getText().toLowerCase().contains("credenciais inválidas"), "Should display invalid credentials message");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        passwordInput.clear();
        emailInput.sendKeys(LOGIN);
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();

        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("header h1")));
        Assertions.assertTrue(dashboardTitle.getText().toLowerCase().contains("dashboard"), "Should land on dashboard after login");
    }

    @Test
    @Order(4)
    public void testSidebarNavigation() {
        testValidLogin();
        WebElement sidebarToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Toggle Navigation']")));
        sidebarToggle.click();

        WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(.,'Análises')]")));
        menuLink.click();

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main h1")));
        Assertions.assertTrue(pageTitle.getText().toLowerCase().contains("análises"), "Should navigate to 'Análises' page");
    }

    @Test
    @Order(5)
    public void testExternalLinkOpensCorrectly() {
        testValidLogin();
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        String originalWindow = driver.getWindowHandle();
        footerLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "External link should open LinkedIn");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testLogout() {
        testValidLogin();
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Usuário']")));
        userMenu.click();

        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class,'v-list-item__title') and text()='Sair']")));
        logoutBtn.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Should be redirected to login page after logout");
    }
}
