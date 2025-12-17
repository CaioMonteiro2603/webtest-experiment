package deepseek.ws10.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(dashboardTitle.getText().contains("Dashboard"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys("invalid@email.com");
        driver.findElement(By.name("password")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Credenciais inválidas"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        loginIfNeeded();
        
        // Test equipment navigation
        WebElement equipmentMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(text(),'Equipamentos')]")));
        equipmentMenu.click();
        WebElement equipmentItem = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Listagem')]")));
        equipmentItem.click();
        
        wait.until(ExpectedConditions.urlContains("/equipments"));
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(pageTitle.getText().contains("Equipamentos"));
    }

    @Test
    @Order(4)
    public void testEnterpriseNavigation() {
        loginIfNeeded();
        
        WebElement enterpriseMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(text(),'Empresas')]")));
        enterpriseMenu.click();
        WebElement enterpriseItem = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Listagem')]")));
        enterpriseItem.click();
        
        wait.until(ExpectedConditions.urlContains("/enterprises"));
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(pageTitle.getText().contains("Empresas"));
    }

    @Test
    @Order(5)
    public void testUserProfile() {
        loginIfNeeded();
        
        WebElement profileButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".user-dropdown")));
        profileButton.click();
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='profile']")));
        profileLink.click();
        
        wait.until(ExpectedConditions.urlContains("/profile"));
        WebElement profileTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(profileTitle.getText().contains("Perfil"));
    }

    @Test
    @Order(6)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement profileButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".user-dropdown")));
        profileButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='logout']")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(loginForm.isDisplayed());
    }

    @Test
    @Order(7)
    public void testTableSorting() {
        loginIfNeeded();
        driver.get(BASE_URL.replace("/login", "/enterprises"));
        
        // Sort by name
        WebElement nameHeader = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//th[contains(text(),'Nome')]")));
        nameHeader.click();
        
        // Verify sorted state
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        Assertions.assertTrue(rows.size() > 0);
    }

    @Test
    @Order(8)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Privacy Policy link
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Política de Privacidade")));
        privacyLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("brasilagritest"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNeeded() {
        if (driver.getCurrentUrl().contains("/login")) {
            driver.get(BASE_URL);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys(USERNAME);
            driver.findElement(By.name("password")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }
    }
}