package Qwen3.ws10.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BrasilAgritest {

    private static WebDriver driver;
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
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        assertEquals("Brasil Agritest - Painel Principal", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Credenciais inválidas"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".menu-toggle")));
        menuButton.click();
        
        WebElement menuItems = driver.findElement(By.cssSelector(".sidebar-menu"));
        assertTrue(menuItems.isDisplayed());
        
        List<WebElement> links = menuItems.findElements(By.tagName("a"));
        assertTrue(links.size() >= 4);
        
        WebElement dashboardLink = links.get(0);
        assertEquals("Dashboard", dashboardLink.getText());
        
        WebElement usersLink = links.get(1);
        assertEquals("Usuários", usersLink.getText());
        
        WebElement productsLink = links.get(2);
        assertEquals("Produtos", productsLink.getText());
        
        WebElement settingsLink = links.get(3);
        assertEquals("Configurações", settingsLink.getText());
    }

    @Test
    @Order(4)
    public void testUserManagement() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement usersLink = driver.findElement(By.linkText("Usuários"));
        usersLink.click();
        
        wait.until(ExpectedConditions.urlContains("/users"));
        assertTrue(driver.getCurrentUrl().contains("/users"));
        assertEquals("Brasil Agritest - Gerenciamento de Usuários", driver.getTitle());
        
        List<WebElement> userRows = driver.findElements(By.cssSelector(".user-row"));
        assertTrue(userRows.size() > 0);
        
        for (WebElement row : userRows) {
            assertTrue(row.isDisplayed());
        }
    }

    @Test
    @Order(5)
    public void testProductManagement() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement productsLink = driver.findElement(By.linkText("Produtos"));
        productsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/products"));
        assertTrue(driver.getCurrentUrl().contains("/products"));
        assertEquals("Brasil Agritest - Gerenciamento de Produtos", driver.getTitle());
        
        List<WebElement> productRows = driver.findElements(By.cssSelector(".product-row"));
        assertTrue(productRows.size() > 0);
        
        for (WebElement row : productRows) {
            assertTrue(row.isDisplayed());
        }
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement logoutLink = driver.findElement(By.linkText("Sair"));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertEquals("Brasil Agritest - Login", driver.getTitle());
    }

    @Test
    @Order(7)
    public void testExternalLinksInFooter() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement footer = driver.findElement(By.cssSelector(".footer"));
        List<WebElement> links = footer.findElements(By.tagName("a"));
        
        String originalWindow = driver.getWindowHandle();
        
        for (int i = 0; i < links.size(); i++) {
            WebElement link = links.get(i);
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                link.click();
                Set<String> windowHandles = driver.getWindowHandles();
                String newWindow = windowHandles.stream()
                        .filter(w -> !w.equals(originalWindow))
                        .findFirst()
                        .orElse(null);

                if (newWindow != null) {
                    driver.switchTo().window(newWindow);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("brasilagritest.com") || 
                               currentUrl.contains("github.com") ||
                               currentUrl.contains("linkedin.com") ||
                               currentUrl.contains("twitter.com"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(8)
    public void testDashboardNavigation() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        assertEquals("Brasil Agritest - Painel Principal", driver.getTitle());
    }

    @Test
    @Order(9)
    public void testSettingsAccess() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement settingsLink = driver.findElement(By.linkText("Configurações"));
        settingsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/settings"));
        assertTrue(driver.getCurrentUrl().contains("/settings"));
        assertEquals("Brasil Agritest - Configurações", driver.getTitle());
    }
}