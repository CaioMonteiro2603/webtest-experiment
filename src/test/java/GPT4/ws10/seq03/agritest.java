package GPT4.ws10.seq03;

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
public class BrasilAgritestLoginTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
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

    @BeforeEach
    public void goToLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));
    }

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Login page did not load.");
        Assertions.assertTrue(driver.findElement(By.tagName("form")).isDisplayed(), "Login form not displayed.");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        emailInput.sendKeys("invalid@user.com");
        passwordInput.clear();
        passwordInput.sendKeys("wrongpass");
        loginBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".MuiAlert-message")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("credenciais inválidas") || error.getText().toLowerCase().contains("usuário ou senha"), "Expected error message for invalid login");
    }

    @Test
    @Order(3)
    public void testValidLoginRedirectsToDashboard() {
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        emailInput.sendKeys(VALID_EMAIL);
        passwordInput.clear();
        passwordInput.sendKeys(VALID_PASSWORD);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Did not redirect to dashboard after login");

        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1, h2, h3")));
        Assertions.assertTrue(title.getText().toLowerCase().contains("dashboard") || title.getText().toLowerCase().contains("painel"), "Expected dashboard heading not found");
    }

    @Test
    @Order(4)
    public void testHeaderMenuLinks() {
        loginIfNotLoggedIn();

        List<WebElement> menuButtons = driver.findElements(By.cssSelector("header button, header a"));
        Assertions.assertTrue(menuButtons.size() > 0, "No header menu items found");

        for (WebElement button : menuButtons) {
            Assertions.assertTrue(button.isDisplayed(), "Menu item not visible: " + button.getText());
        }
    }

    @Test
    @Order(5)
    public void testLogout() {
        loginIfNotLoggedIn();

        List<WebElement> logoutButtons = driver.findElements(By.xpath("//button[contains(text(), 'Logout') or contains(text(), 'Sair')]"));
        if (logoutButtons.size() == 0) {
            Assumptions.abort("No logout button found to test logout");
        }

        WebElement logout = logoutButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(logout)).click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Did not return to login after logout");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        List<WebElement> links = driver.findElements(By.cssSelector("footer a[href^='http']"));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href.contains("facebook.com") || href.contains("twitter.com") || href.contains("linkedin.com")) {
                link.sendKeys(Keys.chord(Keys.CONTROL, Keys.RETURN));
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));

                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newWindow = windows.iterator().next();
                driver.switchTo().window(newWindow);

                wait.until(ExpectedConditions.urlContains(href.split("/")[2]));
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.split("/")[2]), "External URL did not match: " + href);

                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    private void loginIfNotLoggedIn() {
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            goToLoginPage();
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            WebElement passwordInput = driver.findElement(By.name("password"));
            WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

            emailInput.clear();
            emailInput.sendKeys(VALID_EMAIL);
            passwordInput.clear();
            passwordInput.sendKeys(VALID_PASSWORD);
            loginBtn.click();

            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }
    }
}
