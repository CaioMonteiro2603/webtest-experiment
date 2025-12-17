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
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        email.sendKeys(LOGIN);
        
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys(PASSWORD);
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'Dashboard')]")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Login failed - dashboard not displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        email.sendKeys("invalid@email.com");
        
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'alert-danger')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        loginIfNeeded();
        
        // Test Clients navigation
        navigateAndVerify("//span[contains(text(), 'Clientes')]", "clientes", "Clientes");
        
        // Test Products navigation
        navigateAndVerify("//span[contains(text(), 'Produtos')]", "produtos", "Produtos");
        
        // Test Orders navigation
        navigateAndVerify("//span[contains(text(), 'Pedidos')]", "pedidos", "Pedidos");
    }

    @Test
    @Order(4)
    public void testClientCreation() {
        loginIfNeeded();
        
        driver.get("https://gestao.brasilagritest.com/clientes");
        WebElement newClientButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Novo Cliente')]")));
        newClientButton.click();
        
        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("name")));
        nameField.sendKeys("Test Client");
        
        WebElement emailField = driver.findElement(By.name("email"));
        emailField.sendKeys("client@test.com");
        
        WebElement saveButton = driver.findElement(By.xpath("//button[contains(text(), 'Salvar')]"));
        saveButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'alert-success')]")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Client creation failed");
    }

    @Test
    @Order(5)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".dropdown-toggle")));
        userMenu.click();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sair')]")));
        logoutLink.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form")));
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
            By.xpath("//h1[contains(text(), '" + expectedTitle + "')]")));
        Assertions.assertTrue(pageTitle.isDisplayed(), expectedTitle + " page not displayed");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), '" + linkText + "')]")));
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
            WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
            email.sendKeys(LOGIN);
            
            WebElement password = driver.findElement(By.name("password"));
            password.sendKeys(PASSWORD);
            
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}