package deepseek.ws10.seq07;

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
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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

    @Test
    @Order(1)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(dashboardHeader.getText().contains("Dashboard"), "Dashboard should be displayed after login");
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
        Assertions.assertTrue(errorMessage.getText().contains("credenciais"), "Should show invalid credentials error");
    }

    @Test
    @Order(3)
    public void testNavigationToFarms() {
        login();
        WebElement farmsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='farms']")));
        farmsLink.click();

        wait.until(ExpectedConditions.urlContains("/farms"));
        WebElement farmsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(farmsHeader.getText().contains("Fazendas"), "Farms page should be displayed");
    }

    @Test
    @Order(4)
    public void testFarmCreation() {
        login();
        driver.get(BASE_URL.replace("/login", "/farms"));
        
        WebElement newFarmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.btn-primary")));
        newFarmButton.click();

        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        WebElement saveButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        nameInput.sendKeys("Test Farm " + System.currentTimeMillis());
        saveButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.getText().contains("sucesso"), "Farm should be created successfully");
    }

    @Test
    @Order(5)
    public void testLogout() {
        login();
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".dropdown-toggle")));
        userMenu.click();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='logout']")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Should return to login page after logout");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Política de Privacidade")));
        privacyLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        driver.getWindowHandles().forEach(windowHandle -> {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("brasilagritest"));
                driver.close();
            }
        });
        
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }
}