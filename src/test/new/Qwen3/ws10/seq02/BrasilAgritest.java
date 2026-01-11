package Qwen3.ws10.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
	public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testLoginPageLoad() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        wait.until(ExpectedConditions.titleContains("GRAS | Painel Administrativo"));
        assertTrue(driver.getTitle().contains("GRAS | Painel Administrativo"));
        assertTrue(driver.getCurrentUrl().contains("login"));
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
        assertTrue(driver.getTitle().contains("Dashboard"));
    }

    @Test
    @Order(3)
    public void testInvalidCredentialsError() {
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("wrongpassword");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger, .alert, .error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().toLowerCase().contains("erro"));
    }

    @Test
    @Order(4)
    public void testMainMenuNavigation() {
        // Perform login first
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Test Menu Toggle Button
        WebElement menuToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar-toggle, .navbar-toggler, [data-target*='menu'], [data-toggle*='menu']")));
        menuToggle.click();
        
        // Test Dashboard Navigation
        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
        
        // Test Products Navigation
        driver.get("https://gestao.brasilagritest.com/dashboard");
        menuToggle.click();
        WebElement productsLink = driver.findElement(By.linkText("Produtos"));
        productsLink.click();
        assertTrue(driver.getCurrentUrl().contains("products"));
        
        // Test Clients Navigation
        driver.get("https://gestao.brasilagritest.com/dashboard");
        menuToggle.click();
        WebElement clientsLink = driver.findElement(By.linkText("Clientes"));
        clientsLink.click();
        assertTrue(driver.getCurrentUrl().contains("clients"));
        
        // Test Orders Navigation
        driver.get("https://gestao.brasilagritest.com/dashboard");
        menuToggle.click();
        WebElement ordersLink = driver.findElement(By.linkText("Pedidos"));
        ordersLink.click();
        assertTrue(driver.getCurrentUrl().contains("orders"));
        
        // Test Reports Navigation
        driver.get("https://gestao.brasilagritest.com/dashboard");
        menuToggle.click();
        WebElement reportsLink = driver.findElement(By.linkText("Relatórios"));
        reportsLink.click();
        assertTrue(driver.getCurrentUrl().contains("reports"));
    }

    @Test
    @Order(5)
    public void testProductsManagement() {
        // Login first
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to products
        WebElement menuToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar-toggle, .navbar-toggler, [data-target*='menu'], [data-toggle*='menu']")));
        menuToggle.click();
        WebElement productsLink = driver.findElement(By.linkText("Produtos"));
        productsLink.click();
        
        // Wait for products page
        wait.until(ExpectedConditions.urlContains("products"));
        assertTrue(driver.getCurrentUrl().contains("products"));
        
        // Verify products table exists
        WebElement productsTable = driver.findElement(By.cssSelector(".table-products, .table, [class*='table']"));
        assertTrue(productsTable.isDisplayed());
        
        // Verify at least one product exists
        List<WebElement> productRows = driver.findElements(By.cssSelector(".product-row, tr, [class*='row']"));
        assertTrue(productRows.size() > 0);
    }

    @Test
    @Order(6)
    public void testClientsManagement() {
        // Login first
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to clients
        WebElement menuToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar-toggle, .navbar-toggler, [data-target*='menu'], [data-toggle*='menu']")));
        menuToggle.click();
        WebElement clientsLink = driver.findElement(By.linkText("Clientes"));
        clientsLink.click();
        
        // Wait for clients page
        wait.until(ExpectedConditions.urlContains("clients"));
        assertTrue(driver.getCurrentUrl().contains("clients"));
        
        // Verify clients table exists
        WebElement clientsTable = driver.findElement(By.cssSelector(".table-clients, .table, [class*='table']"));
        assertTrue(clientsTable.isDisplayed());
        
        // Verify at least one client exists
        List<WebElement> clientRows = driver.findElements(By.cssSelector(".client-row, tr, [class*='row']"));
        assertTrue(clientRows.size() > 0);
    }

    @Test
    @Order(7)
    public void testOrdersManagement() {
        // Login first
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to orders
        WebElement menuToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar-toggle, .navbar-toggler, [data-target*='menu'], [data-toggle*='menu']")));
        menuToggle.click();
        WebElement ordersLink = driver.findElement(By.linkText("Pedidos"));
        ordersLink.click();
        
        // Wait for orders page
        wait.until(ExpectedConditions.urlContains("orders"));
        assertTrue(driver.getCurrentUrl().contains("orders"));
        
        // Verify orders table exists
        WebElement ordersTable = driver.findElement(By.cssSelector(".table-orders, .table, [class*='table']"));
        assertTrue(ordersTable.isDisplayed());
        
        // Verify at least one order exists
        List<WebElement> orderRows = driver.findElements(By.cssSelector(".order-row, tr, [class*='row']"));
        assertTrue(orderRows.size() > 0);
    }

    @Test
    @Order(8)
    public void testReportsFunctionality() {
        // Login first
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to reports
        WebElement menuToggle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar-toggle, .navbar-toggler, [data-target*='menu'], [data-toggle*='menu']")));
        menuToggle.click();
        WebElement reportsLink = driver.findElement(By.linkText("Relatórios"));
        reportsLink.click();
        
        // Wait for reports page
        wait.until(ExpectedConditions.urlContains("reports"));
        assertTrue(driver.getCurrentUrl().contains("reports"));
        
        // Verify reports section exists
        WebElement reportsSection = driver.findElement(By.cssSelector(".reports-section, .reports, [class*='report']"));
        assertTrue(reportsSection.isDisplayed());
        
        // Verify report types
        List<WebElement> reportTypes = driver.findElements(By.cssSelector(".report-type, [class*='report']"));
        assertTrue(reportTypes.size() > 0);
    }

    @Test
    @Order(9)
    public void testUserProfileAndSettings() {
        // Login first
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to profile
        WebElement profileMenu = driver.findElement(By.cssSelector(".user-profile-menu, .user-menu, .profile-menu, [class*='user'], [class*='profile']"));
        profileMenu.click();
        
        // Click on profile settings
        WebElement profileSettingsLink = driver.findElement(By.linkText("Meu Perfil"));
        profileSettingsLink.click();
        
        // Wait for profile page
        wait.until(ExpectedConditions.urlContains("profile"));
        assertTrue(driver.getCurrentUrl().contains("profile"));
        
        // Verify profile fields
        WebElement profileForm = driver.findElement(By.cssSelector(".profile-form, form, [class*='profile']"));
        assertTrue(profileForm.isDisplayed());
    }

    @Test
    @Order(10)
    public void testLogoutFunctionality() {
        // Login first
        driver.get("https://gestao.brasilagritest.com/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email'], #email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Click on user menu
        WebElement userMenu = driver.findElement(By.cssSelector(".user-menu, .user-profile-menu, .profile-menu, [class*='user']"));
        userMenu.click();
        
        // Click on logout
        WebElement logoutLink = driver.findElement(By.linkText("Sair"));
        logoutLink.click();
        
        // Wait for login page
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"));
    }

    @Test
    @Order(11)
    public void testResponsiveLayout() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Verify main elements are present
        WebElement loginForm = driver.findElement(By.cssSelector(".login-form, form, [class*='login']"));
        assertTrue(loginForm.isDisplayed());
        
        WebElement emailField = driver.findElement(By.cssSelector("input[type='email'], input[name='email'], #email"));
        assertTrue(emailField.isDisplayed());
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        assertTrue(passwordField.isDisplayed());
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(loginButton.isDisplayed());
    }

    @Test
    @Order(12)
    public void testFooterLinks() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Verify footer exists
        WebElement footer = driver.findElement(By.cssSelector("footer, [class*='footer'], [id*='footer']"));
        assertTrue(footer.isDisplayed());
        
        // Check if there are footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a, [class*='footer'] a, [id*='footer'] a"));
        assertTrue(footerLinks.size() > 0);
    }
}