package deepseek.ws10.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";
    private static WebDriverWait wait;

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement dashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".dashboard")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Dashboard should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be shown for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationToUsersPage() {
        testValidLogin();
        WebElement usersLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='users']")));
        usersLink.click();

        WebElement usersHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(usersHeader.getText().contains("Usuários"), "Users page should be displayed");
    }

    @Test
    @Order(4)
    public void testNavigationToReportsPage() {
        testValidLogin();
        WebElement reportsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='reports']")));
        reportsLink.click();

        WebElement reportsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(reportsHeader.getText().contains("Relatórios"), "Reports page should be displayed");
    }

    @Test
    @Order(5)
    public void testLogout() {
        testValidLogin();
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.logout")));
        logoutButton.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".login-form")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Should return to login page after logout");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='privacy']")));
        String originalWindow = driver.getWindowHandle();
        privacyLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("privacy"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testUserSearch() {
        testValidLogin();
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='search']")));
        searchInput.sendKeys("admin");

        WebElement searchResults = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".search-results")));
        Assertions.assertTrue(searchResults.isDisplayed(), "Search results should be visible");
    }
}