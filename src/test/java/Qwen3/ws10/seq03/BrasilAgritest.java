package Qwen3.ws10.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginPageLoad() {
        driver.get("https://gestao.brasilagritest.com/login");
        assertEquals("Brasil Agritest - Login", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("gestao.brasilagritest.com/login"));
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Fill login form
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        // Submit login
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();
        
        // Wait for login redirect
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Verify successful login
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        assertTrue(driver.getTitle().contains("Dashboard"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Fill invalid login form
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalidpassword");
        
        // Submit login
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();
        
        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".alert-danger")).getText().contains("Credenciais inválidas"));
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        driver.get("https://gestao.brasilagritest.com/dashboard");
        
        // Click Dashboard link
        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        
        // Click Usuarios link
        driver.get("https://gestao.brasilagritest.com/dashboard");
        WebElement usuariosLink = driver.findElement(By.linkText("Usuários"));
        usuariosLink.click();
        wait.until(ExpectedConditions.urlContains("/usuarios"));
        assertTrue(driver.getCurrentUrl().contains("/usuarios"));
        
        // Navigate back to dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");
        
        // Click Produtos link
        WebElement produtosLink = driver.findElement(By.linkText("Produtos"));
        produtosLink.click();
        wait.until(ExpectedConditions.urlContains("/produtos"));
        assertTrue(driver.getCurrentUrl().contains("/produtos"));
        
        // Navigate back to dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");
        
        // Click Pedidos link
        WebElement pedidosLink = driver.findElement(By.linkText("Pedidos"));
        pedidosLink.click();
        wait.until(ExpectedConditions.urlContains("/pedidos"));
        assertTrue(driver.getCurrentUrl().contains("/pedidos"));
    }

    @Test
    @Order(5)
    public void testUserManagement() {
        driver.get("https://gestao.brasilagritest.com/usuarios");
        
        // Verify user management page loaded
        assertTrue(driver.getTitle().contains("Usuários"));
        
        // Check if users table is present
        WebElement usersTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".users-table")));
        assertTrue(usersTable.isDisplayed());
        
        // Check if there are users listed
        if (driver.findElements(By.cssSelector(".user-row")).size() > 0) {
            WebElement firstUser = driver.findElement(By.cssSelector(".user-row"));
            assertTrue(firstUser.isDisplayed());
        }
    }

    @Test
    @Order(6)
    public void testProductManagement() {
        driver.get("https://gestao.brasilagritest.com/produtos");
        
        // Verify product management page loaded
        assertTrue(driver.getTitle().contains("Produtos"));
        
        // Check if products table is present
        WebElement productsTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".products-table")));
        assertTrue(productsTable.isDisplayed());
        
        // Check if there are products listed
        if (driver.findElements(By.cssSelector(".product-row")).size() > 0) {
            WebElement firstProduct = driver.findElement(By.cssSelector(".product-row"));
            assertTrue(firstProduct.isDisplayed());
        }
    }

    @Test
    @Order(7)
    public void testOrderManagement() {
        driver.get("https://gestao.brasilagritest.com/pedidos");
        
        // Verify order management page loaded
        assertTrue(driver.getTitle().contains("Pedidos"));
        
        // Check if orders table is present
        WebElement ordersTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".orders-table")));
        assertTrue(ordersTable.isDisplayed());
        
        // Check if there are orders listed
        if (driver.findElements(By.cssSelector(".order-row")).size() > 0) {
            WebElement firstOrder = driver.findElement(By.cssSelector(".order-row"));
            assertTrue(firstOrder.isDisplayed());
        }
    }

    @Test
    @Order(8)
    public void testLogout() {
        driver.get("https://gestao.brasilagritest.com/dashboard");
        
        // Click logout button
        WebElement logoutButton = driver.findElement(By.xpath("//a[contains(text(), 'Sair')]"));
        logoutButton.click();
        
        // Wait for redirect to login page
        wait.until(ExpectedConditions.urlContains("/login"));
        
        // Verify logout successful
        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(driver.getTitle().contains("Login"));
    }

    @Test
    @Order(9)
    public void testFooterLinks() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Click Terms link
        WebElement termsLink = driver.findElement(By.linkText("Termos"));
        termsLink.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
        
        // Click Privacy link
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement privacyLink = driver.findElement(By.linkText("Privacidade"));
        privacyLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    @Test
    @Order(10)
    public void testDashboardOverview() {
        driver.get("https://gestao.brasilagritest.com/dashboard");
        
        // Verify dashboard elements
        assertTrue(driver.getTitle().contains("Dashboard"));
        
        // Check dashboard summary cards
        WebElement summaryCards = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".summary-cards")));
        assertTrue(summaryCards.isDisplayed());
        
        // Check if there are summary cards
        if (driver.findElements(By.cssSelector(".summary-card")).size() > 0) {
            WebElement firstCard = driver.findElement(By.cssSelector(".summary-card"));
            assertTrue(firstCard.isDisplayed());
        }
    }

    @Test
    @Order(11)
    public void testBreadcrumbNavigation() {
        driver.get("https://gestao.brasilagritest.com/usuarios");
        
        // Verify breadcrumb navigation is present
        WebElement breadcrumb = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".breadcrumb")));
        assertTrue(breadcrumb.isDisplayed());
        
        // Check first breadcrumb item
        WebElement homeBreadcrumb = driver.findElement(By.cssSelector(".breadcrumb-item:first-child a"));
        assertTrue(homeBreadcrumb.isDisplayed());
        assertTrue(homeBreadcrumb.getText().contains("Início"));
        
        // Check current page in breadcrumb
        WebElement currentPageBreadcrumb = driver.findElement(By.cssSelector(".breadcrumb-item:last-child"));
        assertTrue(currentPageBreadcrumb.isDisplayed());
        assertTrue(currentPageBreadcrumb.getText().contains("Usuários"));
    }

    @Test
    @Order(12)
    public void testResetAppState() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Navigate to users page
        WebElement usuariosLink = driver.findElement(By.linkText("Usuários"));
        usuariosLink.click();
        wait.until(ExpectedConditions.urlContains("/usuarios"));
        
        // Return to dashboard to reset state
        driver.get("https://gestao.brasilagritest.com/dashboard");
        
        // Verify we are back on dashboard
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        assertTrue(driver.getTitle().contains("Dashboard"));
    }
}