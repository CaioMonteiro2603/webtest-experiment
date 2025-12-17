package deepseek.ws10.seq01;

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".dashboard")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Dashboard should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get(BASE_URL);
        login();

        WebElement farmsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fazendas")));
        farmsLink.click();
        wait.until(ExpectedConditions.urlContains("farms"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Fazendas"), "Should navigate to Farms page");

        WebElement reportsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Relatórios")));
        reportsLink.click();
        wait.until(ExpectedConditions.urlContains("reports"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Relatórios"), "Should navigate to Reports page");
    }

    @Test
    @Order(4)
    public void testFarmCreation() {
        driver.get(BASE_URL);
        login();

        WebElement farmsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fazendas")));
        farmsLink.click();
        wait.until(ExpectedConditions.urlContains("farms"));
        
        WebElement newFarmButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("new-farm-btn")));
        newFarmButton.click();

        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("name")));
        WebElement locationField = driver.findElement(By.name("location"));
        WebElement saveButton = driver.findElement(By.cssSelector("button[type='submit']"));

        nameField.sendKeys("Test Farm");
        locationField.sendKeys("Test Location");
        saveButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Farm should be created successfully");
    }

    @Test
    @Order(5)
    public void testLogout() {
        driver.get(BASE_URL);
        login();

        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".user-menu")));
        userMenu.click();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("form")).isDisplayed(), "Should be back to login page after logout");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        if (driver.findElements(By.linkText("Help")).size() > 0) {
            WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Help")));
            
            String originalWindow = driver.getWindowHandle();
            helpLink.click();
            
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    wait.until(ExpectedConditions.urlContains("help"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        }
    }

    private void login() {
        if (driver.getCurrentUrl().contains("login")) {
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

            emailField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}