package geminiPro.ws10.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A complete JUnit 5 test suite for the Brasil Agri Test beta website
 * using Selenium WebDriver with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String VALID_USERNAME = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASSWORD = "10203040";
    private static final String INVALID_PASSWORD = "wrongpassword";

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[@type='submit']");
    private static final By DASHBOARD_HEADER = By.xpath("//h1[normalize-space()='Dashboard']");
    private static final By USER_AVATAR_BUTTON = By.id("user-avatar");
    private static final By LOGOUT_BUTTON = By.xpath("//div[contains(text(), 'Sair')]");
    private static final By ERROR_MESSAGE_LOGIN = By.xpath("//p[contains(text(),'Credenciais incorretas.')]");

    // Sidebar Menu Locators
    private static final By MENU_CADASTROS = By.id("menu-cadastros");
    private static final By SUBMENU_CLIENTES = By.id("menu-clientes");
    private static final By SUBMENU_FORNECEDORES = By.id("menu-fornecedores");
    private static final By SUBMENU_PRODUTOS = By.id("menu-produtos");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        // This application can be slow to load, a longer timeout is safer.
        options.setPageLoadTimeout(Duration.ofSeconds(30));
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Increased wait time for this app
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Helper method to perform login.
     */
    private void performLogin(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    /**
     * Helper method to perform logout.
     */
    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(USER_AVATAR_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON));
    }

    @Test
    @Order(1)
    void testLoginWithInvalidCredentials() {
        performLogin(VALID_USERNAME, INVALID_PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_LOGIN));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message for invalid credentials was not found.");
    }

    @Test
    @Order(2)
    void testSuccessfulLoginAndLogout() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_HEADER));
        Assertions.assertTrue(dashboardHeader.isDisplayed(), "Dashboard header not found, login may have failed.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "URL did not redirect to /dashboard after login.");
        
        performLogout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "URL did not redirect to /login after logout.");
    }
    
    /**
     * Helper method for navigation tests. It logs in, clicks menu items, and verifies the result.
     * @param menuId The ID of the main menu item to click (can be null if not a submenu).
     * @param submenuId The ID of the submenu item to click.
     * @param expectedUrlPart The part of the URL to verify.
     * @param expectedHeaderText The text of the H1 header to verify.
     */
    private void runNavigationTest(By menuId, By submenuId, String expectedUrlPart, String expectedHeaderText) {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_HEADER));

        if (menuId != null) {
             wait.until(ExpectedConditions.elementToBeClickable(menuId)).click();
        }
       
        wait.until(ExpectedConditions.elementToBeClickable(submenuId)).click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart), "URL is incorrect for page: " + expectedHeaderText);
        
        WebElement pageHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='" + expectedHeaderText + "']")));
        Assertions.assertTrue(pageHeader.isDisplayed(), "Header for '" + expectedHeaderText + "' page was not found.");
    }

    @Test
    @Order(3)
    void testNavigationToClientesPage() {
        runNavigationTest(MENU_CADASTROS, SUBMENU_CLIENTES, "/clientes", "Clientes");
    }

    @Test
    @Order(4)
    void testNavigationToFornecedoresPage() {
        runNavigationTest(MENU_CADASTROS, SUBMENU_FORNECEDORES, "/fornecedores", "Fornecedores");
    }
    
    @Test
    @Order(5)
    void testNavigationToProdutosPage() {
        runNavigationTest(MENU_CADASTROS, SUBMENU_PRODUTOS, "/produtos", "Produtos");
    }

    @Test
    @Order(6)
    void testNavigationToContasAPagarPage() {
        // This item is under the "Financeiro" menu
        By menuFinanceiro = By.id("menu-financeiro");
        By submenuContasPagar = By.id("menu-contas-a-pagar");
        runNavigationTest(menuFinanceiro, submenuContasPagar, "/contas-a-pagar", "Contas a Pagar");
    }
}