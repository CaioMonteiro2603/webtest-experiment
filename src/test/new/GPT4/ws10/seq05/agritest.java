package GPT4.ws10.seq05;

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
public class agritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://brasilagro-qa.azurewebsites.net/login";
    private static final String VALID_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASSWORD = "10203040";

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

    private void login(String email, String password) {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));

        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }
    @Test
    @Order(1)
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("form")));
        Assertions.assertTrue(form.isDisplayed(), "Login form should be visible");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("login"), "Title should contain 'login'");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        login("invalid@brasilagritest.com.br", "wrongpass");
        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'error') or contains(@class, 'alert')]")));
        Assertions.assertTrue(errorAlert.getText().toLowerCase().contains("email ou senha inválidos") ||
                              errorAlert.getText().toLowerCase().contains("inválidos"),
                              "Should show error for invalid credentials");
    }

    @Test
    @Order(3)
    public void testLoginWithValidCredentials() {
        login(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard");
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("main")));
        Assertions.assertTrue(welcome.isDisplayed(), "Main dashboard should be visible");
    }

    @Test
    @Order(4)
    public void testNavigateMenuOptions() {
        login(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label='menu' or @aria-label='Menu']")));
        menuBtn.click();

        WebElement dashboardLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Dashboard') or contains(text(), 'Painel')]")));
        dashboardLink.click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should navigate to Dashboard");

        menuBtn.click();
        WebElement samplesLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Amostras') or contains(text(), 'Samples')]")));
        samplesLink.click();
        wait.until(ExpectedConditions.urlContains("/samples"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/samples"), "Should navigate to Samples");
    }

    @Test
    @Order(5)
    public void testExternalAboutLink() {
        login(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//footer//a[contains(@href, 'github')]")));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", footerLink.getAttribute("href"));

        wait.until(d -> driver.getWindowHandles().size() > 1);

        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(d -> d.getCurrentUrl().startsWith("https://"));
                String url = driver.getCurrentUrl();
                Assertions.assertTrue(url.contains("github.com"), "External link should lead to GitHub");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(6)
    public void testLogout() {
        login(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label='menu' or @aria-label='Menu']")));
        menuBtn.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Logout') or contains(text(), 'Sair')]")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Should be redirected to login after logout");
    }
}