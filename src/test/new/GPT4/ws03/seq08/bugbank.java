package GPT4.ws03.seq08;

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
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    public static void setup() {
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

    private void login(String email, String password) {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login-btn']")));
        loginBtn.click();

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement accessBtn = driver.findElement(By.cssSelector("button[data-test='login-submit']"));

        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        accessBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(LOGIN_EMAIL, LOGIN_PASSWORD);
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[data-test='dashboard-user-fullname']")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Olá"), "Should greet user with Olá");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        login("invalid@email.com", "wrongpassword");
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".styles__Container-sc-1wsixal-0")));
        WebElement errorText = modal.findElement(By.tagName("p"));
        Assertions.assertTrue(errorText.getText().length() > 0, "Should show error message");
        WebElement closeBtn = modal.findElement(By.tagName("button"));
        closeBtn.click();
    }

    @Test
    @Order(3)
    public void testLogoutButton() {
        testValidLogin();
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='logout']")));
        logoutBtn.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login-btn']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank"), "Should be back to login screen");
    }

    @Test
    @Order(4)
    public void testExternalLinkTermsAndConditions() {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login-btn']")));
        loginBtn.click();

        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Termos e condições")));
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windowHandles = driver.getWindowHandles();
        for (String handle : windowHandles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("netlify"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("netlify"), "Should be redirected to external page");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testOpenAccountButtonVisible() {
        driver.get(BASE_URL);
        WebElement openAccountBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[data-test='create-account']")));
        Assertions.assertTrue(openAccountBtn.isDisplayed(), "Open account button should be visible");
    }

    @Test
    @Order(6)
    public void testSortingDropdownPresence() {
        testValidLogin();
        List<WebElement> sortOptions = driver.findElements(By.cssSelector("select"));
        Assertions.assertFalse(sortOptions.isEmpty(), "Sort dropdown should exist if available");
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='logout']")));
        logoutBtn.click();
    }

    @Test
    @Order(7)
    public void testMenuButtonsIfAny() {
        testValidLogin();
        List<WebElement> menu = driver.findElements(By.cssSelector("button[data-test='logout']"));
        Assertions.assertTrue(menu.size() > 0, "Logout button should exist in menu");
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='logout']")));
        logoutBtn.click();
    }
}