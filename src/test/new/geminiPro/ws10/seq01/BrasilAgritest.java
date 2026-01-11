package geminiPro.ws10.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A comprehensive JUnit 5 test suite for the Brasil Agri Test application.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test the login process,
 * navigation through the main dashboard modules, and the logout functionality.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest{

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Credentials ---
    private static final String BASE_URL = "https://localhost:3000/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    // --- Locators ---
    // Login
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By ERROR_TOAST_MESSAGE = By.cssSelector(".toasted.error");

    // Dashboard & Navigation
    private static final By DASHBOARD_CONTAINER = By.className("main-content");
    private static final By PAGE_HEADER = By.tagName("h1");
    private static final By USER_MENU_BUTTON = By.id("user-menu-button");
    private static final By LOGOUT_LINK = By.xpath("//a[normalize-space()='Sair']");


    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Helper method to perform login.
     */
    private void login(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }
    
    /**
     * Helper method to navigate to a module from the sidebar and verify the landing page.
     * @param menuDataCy The data-cy attribute of the menu link.
     * @param expectedUrlPath The expected path in the URL after navigation.
     * @param expectedHeaderText The expected text of the H1 tag on the new page.
     */
    private void navigateToAndVerify(String menuDataCy, String expectedUrlPath, String expectedHeaderText) {
        By menuLinkLocator = By.cssSelector("a[data-cy='" + menuDataCy + "']");
        wait.until(ExpectedConditions.elementToBeClickable(menuLinkLocator)).click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPath));
        WebElement pageHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_HEADER));
        
        Assertions.assertEquals(expectedHeaderText, pageHeader.getText(), "Page header for " + menuDataCy + " is incorrect.");
    }

    @Test
    @Order(1)
    @DisplayName("Should fail to login with an invalid password")
    void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).sendKeys("invalidpassword");
        driver.findElement(LOGIN_BUTTON).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_TOAST_MESSAGE));
        Assertions.assertTrue(errorMessage.getText().contains("E-mail ou senha incorreta"), "Error message for invalid login was not displayed or incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("Should login successfully and display the dashboard")
    void testSuccessfulLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.findElement(DASHBOARD_CONTAINER).isDisplayed(), "Dashboard container not found after login.");
    }

    @Test
    @Order(3)
    @DisplayName("Should navigate to the 'Análises' page from the dashboard")
    void testNavigateToAnalises() {
        // This test depends on being logged in. A full suite would handle this
        // with a @BeforeEach, but for this ordered flow, we rely on the previous test.
        navigateToAndVerify("Análises", "/analysis", "Análises");
    }
    
    @Test
    @Order(4)
    @DisplayName("Should navigate to the 'Amostras' page from the dashboard")
    void testNavigateToAmostras() {
        navigateToAndVerify("Amostras", "/samples", "Amostras");
    }

    @Test
    @Order(5)
    @DisplayName("Should navigate to the 'Clientes' page from the dashboard")
    void testNavigateToClientes() {
        navigateToAndVerify("Clientes", "/customers", "Clientes");
    }

    @Test
    @Order(6)
    @DisplayName("Should navigate to the 'Orçamentos' page from the dashboard")
    void testNavigateToOrcamentos() {
        navigateToAndVerify("Orçamentos", "/budgets", "Orçamentos");
    }

    @Test
    @Order(7)
    @DisplayName("Should navigate to the 'Resultados' page from the dashboard")
    void testNavigateToResultados() {
        navigateToAndVerify("Resultados", "/results", "Resultados");
    }
    
    @Test
    @Order(8)
    @DisplayName("Should navigate to the 'Financeiro' page from the dashboard")
    void testNavigateToFinanceiro() {
        navigateToAndVerify("Financeiro", "/financial", "Financeiro");
    }

    @Test
    @Order(9)
    @DisplayName("Should log out successfully and return to the login page")
    void testLogout() {
        // Assumes user is logged in and on a dashboard page
        wait.until(ExpectedConditions.elementToBeClickable(USER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/")); // Redirects to /
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).isDisplayed(), "Logout failed or did not return to the login page.");
    }
}