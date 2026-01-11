package deepseek.ws10.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";
    private static WebDriverWait wait;

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
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email' or @name='email']")));
        email.sendKeys(LOGIN);
        
        WebElement password = driver.findElement(By.xpath("//input[@type='password' or @name='password']"));
        password.sendKeys(PASSWORD);
        
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@type,'submit') or contains(text(),'Entrar')]"));
        loginButton.click();
        
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(),'Dashboard') or contains(@class,'dashboard') or contains(@href,'dashboard') or contains(@src,'dashboard')]")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Login failed - dashboard not displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email' or @name='email']")));
        email.sendKeys("invalid@email.com");
        
        WebElement password = driver.findElement(By.xpath("//input[@type='password' or @name='password']"));
        password.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(@type,'submit') or contains(text(),'Entrar')]"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(@class,'alert-danger') or contains(@class,'error') or contains(@role,'alert')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        loginIfNeeded();
        
        // Test Clients navigation
        navigateAndVerify("//*[contains(text(),'Clientes')]", "clientes", "Clientes");
        
        // Test Products navigation
        navigateAndVerify("//*[contains(text(),'Produtos')]", "produtos", "Produtos");
        
        // Test Orders navigation
        navigateAndVerify("//*[contains(text(),'Pedidos')]", "pedidos", "Pedidos");
    }

    @Test
    @Order(4)
    public void testClientCreation() {
        loginIfNeeded();
        
        driver.get("https://gestao.brasilagritest.com/clientes");
        WebElement newClientButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(),'Novo Cliente') or contains(@class,'novo-cliente')]")));
        newClientButton.click();
        
        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@type='text' and (@name='name' or @placeholder or @label or contains(@class,'nome'))]")));
        nameField.sendKeys("Test Client");
        
        WebElement emailField = driver.findElement(By.xpath("//input[@type='email' or @name='email']"));
        emailField.sendKeys("client@test.com");
        
        WebElement saveButton = driver.findElement(By.xpath("//*[contains(text(),'Salvar') or contains(@type,'submit')]"));
        saveButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(@class,'alert-success') or contains(@class,'success') or contains(text(),'sucesso')]")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Client creation failed");
    }

    @Test
    @Order(5)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(@class,'dropdown') or contains(@class,'menu-usuario') or contains(@class,'user-menu')]")));
        userMenu.click();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(),'Sair') or contains(@href,'logout')]")));
        logoutLink.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(@class,'login') or contains(@action,'login') or contains(@class,'form')]")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Logout failed - login form not visible");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        loginIfNeeded();
        
        // Test Help link
        testExternalLink("Ajuda", "brasilagritest.com");
        
        // Test Privacy Policy link
        testExternalLink("Política de Privacidade", "brasilagritest.com");
    }

    private void navigateAndVerify(String menuXpath, String expectedUrlPart, String expectedTitle) {
        WebElement menuItem = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(menuXpath)));
        menuItem.click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        WebElement pageTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), '" + expectedTitle + "')]")));
        Assertions.assertTrue(pageTitle.isDisplayed(), expectedTitle + " page not displayed");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), '" + linkText + "')]")));
        link.click();
        
        // Switch to new window if opened
        if (driver.getWindowHandles().size() > 1) {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " link failed - wrong domain");
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);
            WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email' or @name='email']")));
            email.sendKeys(LOGIN);
            
            WebElement password = driver.findElement(By.xpath("//input[@type='password' or @name='password']"));
            password.sendKeys(PASSWORD);
            
            WebElement loginButton = driver.findElement(By.xpath("//button[contains(@type,'submit') or contains(text(),'Entrar')]"));
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}