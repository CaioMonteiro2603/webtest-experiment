package deepseek.ws10.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().setSize(new Dimension(1920, 1080));
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
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@type='email' or @formcontrolname='email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' or @formcontrolname='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' or contains(.,'Entrar')]"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Dashboard') or contains(text(),'Painel')]")));
        Assertions.assertTrue(dashboardTitle.isDisplayed(), 
            "Expected dashboard page after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@type='email' or @formcontrolname='email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' or @formcontrolname='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' or contains(.,'Entrar')]"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'error') or contains(@class,'alert')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(),
            "Expected error message for invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationToUsersPage() {
        login();
        WebElement usersMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(text(),'Usuários') or contains(text(),'Users')]")));
        usersMenu.click();

        WebElement usersTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Usuários') or contains(text(),'Users')]")));
        Assertions.assertTrue(usersTitle.isDisplayed(),
            "Expected users page title");
    }

    @Test
    @Order(4)
    public void testUserTableSorting() {
        login();
        driver.get(BASE_URL.replace("login", "users"));
        
        WebElement nameHeader = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//th[contains(@class,'name') or contains(text(),'Nome')]")));
        nameHeader.click();

        WebElement firstRowName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("(//td[contains(@class,'name') or contains(@class,'mat-column-name')])[1]")));
        Assertions.assertNotNull(firstRowName.getText(),
            "Expected sorted users table");
    }

    @Test
    @Order(5)
    public void testCreateNewUser() {
        login();
        driver.get(BASE_URL.replace("login", "users"));
        
        WebElement newUserButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Novo Usuário') or contains(.,'Novo') or contains(.,'Adicionar')]")));
        newUserButton.click();

        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@formcontrolname='name' or @placeholder='Nome']")));
        nameField.sendKeys("Test User");
        driver.findElement(By.xpath("//input[@formcontrolname='email' or @type='email']")).sendKeys("test@example.com");
        driver.findElement(By.xpath("//input[@formcontrolname='password' or @type='password']")).sendKeys("password123");
        
        WebElement saveButton = driver.findElement(By.xpath("//button[contains(.,'Salvar') or contains(.,'Save')]"));
        saveButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'snack') or contains(@class,'success') or contains(@class,'alert-success')]")));
        Assertions.assertTrue(successMessage.isDisplayed(),
            "Expected success message after user creation");
    }

    @Test
    @Order(6)
    public void testLogout() {
        login();
        WebElement profileMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(@class,'user') or contains(@class,'profile')]")));
        profileMenu.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Sair') or contains(text(),'Logout')]")));
        logoutButton.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//form")));
        Assertions.assertTrue(loginForm.isDisplayed(),
            "Expected login form after logout");
    }

    private void login() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@type='email' or @formcontrolname='email']")));
            emailField.sendKeys(USERNAME);
            driver.findElement(By.xpath("//input[@type='password' or @formcontrolname='password']")).sendKeys(PASSWORD);
            driver.findElement(By.xpath("//button[@type='submit' or contains(.,'Entrar')]")).click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}