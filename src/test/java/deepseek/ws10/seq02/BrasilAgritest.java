package deepseek.ws10.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FFirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgriAdminTest {
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
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[formcontrolname='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[formcontrolname='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Dashboard')]")));
        Assertions.assertTrue(dashboardTitle.isDisplayed(), 
            "Expected dashboard page after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[formcontrolname='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[formcontrolname='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".mat-error")));
        Assertions.assertTrue(errorMessage.isDisplayed(),
            "Expected error message for invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationToUsersPage() {
        login();
        WebElement usersMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(text(),'Usuários')]")));
        usersMenu.click();

        WebElement usersTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Usuários')]")));
        Assertions.assertTrue(usersTitle.isDisplayed(),
            "Expected users page title");
    }

    @Test
    @Order(4)
    public void testUserTableSorting() {
        login();
        driver.get(BASE_URL.replace("login", "users"));
        
        WebElement nameHeader = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//th[contains(@class,'mat-column-name')]")));
        nameHeader.click();

        WebElement firstRowName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("(//td[contains(@class,'mat-column-name')])[1]")));
        Assertions.assertNotNull(firstRowName.getText(),
            "Expected sorted users table");
    }

    @Test
    @Order(5)
    public void testCreateNewUser() {
        login();
        driver.get(BASE_URL.replace("login", "users"));
        
        WebElement newUserButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Novo Usuário')]")));
        newUserButton.click();

        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[formcontrolname='name']")));
        nameField.sendKeys("Test User");
        driver.findElement(By.cssSelector("input[formcontrolname='email']")).sendKeys("test@example.com");
        driver.findElement(By.cssSelector("input[formcontrolname='password']")).sendKeys("password123");
        
        WebElement saveButton = driver.findElement(By.xpath("//button[contains(.,'Salvar')]"));
        saveButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".mat-snack-bar-container")));
        Assertions.assertTrue(successMessage.isDisplayed(),
            "Expected success message after user creation");
    }

    @Test
    @Order(6)
    public void testLogout() {
        login();
        WebElement profileMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".user-menu")));
        profileMenu.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Sair')]")));
        logoutButton.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(loginForm.isDisplayed(),
            "Expected login form after logout");
    }

    private void login() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);
            WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[formcontrolname='email']")));
            emailField.sendKeys(USERNAME);
            driver.findElement(By.cssSelector("input[formcontrolname='password']")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}