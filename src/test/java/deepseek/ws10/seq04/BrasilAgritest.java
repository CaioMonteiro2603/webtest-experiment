package deepseek.ws10.seq04;

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h4[contains(text(),'Dashboard')]")));
        Assertions.assertTrue(dashboardTitle.isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'alert-danger')]")));
        Assertions.assertTrue(errorMessage.getText().contains("Credenciais inválidas"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        login();
        
        // Test Agroindústria menu
        testMenuNavigation("Agroindústria", "Lotes");
        // Test Safras menu
        testMenuNavigation("Safras", "Lista de safras");
        // Test Usuários menu 
        testMenuNavigation("Usuários", "Lista de usuários");
    }

    private void testMenuNavigation(String menuItem, String expectedHeader) {
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(text(),'" + menuItem + "')]")));
        menu.click();
        
        WebElement submenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Lotes') or contains(text(),'Lista')]")));
        submenu.click();
        
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h3[contains(text(),'" + expectedHeader + "')]")));
        Assertions.assertTrue(header.isDisplayed());
    }

    @Test
    @Order(4)
    public void testCreateUser() {
        login();
        
        // Navigate to Users page
        driver.findElement(By.xpath("//span[contains(text(),'Usuários')]")).click();
        WebElement usersLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Lista de usuários')]")));
        usersLink.click();
        
        // Click Add User
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Adicionar usuário')]")));
        addButton.click();
        
        // Fill user form
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.sendKeys("Test User");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("test.user@example.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("Test1234");
        
        WebElement saveButton = driver.findElement(By.xpath("//button[contains(text(),'Salvar')]"));
        saveButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'alert-success')]")));
        Assertions.assertTrue(successMessage.isDisplayed());
    }

    @Test
    @Order(5)
    public void testLogout() {
        login();
        
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(@class,'user-name')]")));
        userMenu.click();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'logout')]")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.findElement(By.id("email")).isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        if (driver.findElements(By.xpath("//h4[contains(text(),'Dashboard')]")).size() == 0) {
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            emailField.sendKeys(USERNAME);
            
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys(PASSWORD);
            
            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
            loginButton.click();
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h4[contains(text(),'Dashboard')]")));
        }
    }
}