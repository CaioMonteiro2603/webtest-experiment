package geminiPRO.ws10.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

/**
 * A complete JUnit 5 test suite for the Brasil Agri Test beta application.
 * This test uses Selenium WebDriver with Firefox in headless mode and covers
 * login, navigation, and a full CRUD (Create, Read, Delete) flow for a product.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgriTest {

    // Constants for configuration
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15); // Increased for this slower app
    private static final String VALID_USERNAME = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASSWORD = "10203040";

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // Static variable to share the unique product name between test steps
    private static String uniqueProductName;

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);

        uniqueProductName = "Produto Teste " + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToLogin() {
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should show an error for invalid login credentials")
    void testInvalidLogin() {
        performLogin(VALID_USERNAME, "wrongpassword");
        By errorToastDetail = By.cssSelector(".p-toast-message-error .p-toast-detail");
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(errorToastDetail));
        Assertions.assertEquals("Credenciais inválidas", toast.getText(), "Error message for invalid login is incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("Should log in successfully and land on the dashboard")
    void testSuccessfulLogin() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Dashboard", dashboardHeader.getText(), "Should be on the Dashboard page after login.");
    }

    @Test
    @Order(3)
    @DisplayName("Should navigate to different pages using the sidebar menu")
    void testSidebarNavigation() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        // Navigate to "Ensaios"
        navigateTo("Ensaios");
        wait.until(ExpectedConditions.urlContains("/trials"));
        Assertions.assertEquals("Ensaios", wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1"))).getText());

        // Navigate to "Clientes"
        navigateTo("Clientes");
        wait.until(ExpectedConditions.urlContains("/clients"));
        Assertions.assertEquals("Clientes", wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1"))).getText());
    }

    @Test
    @Order(4)
    @DisplayName("Should create a new product and verify its creation")
    void testCreateProduct() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        navigateTo("Produtos");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[.//span[text()='Adicionar Produto']]"))).click();

        // Fill out the form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys(uniqueProductName);
        driver.findElement(By.id("manufacturer")).sendKeys("Gemini PRO");
        driver.findElement(By.xpath("//label[text()='Ingrediente Ativo']/following-sibling::div//input")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[text()='OUTROS']"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[.//span[text()='Salvar']]"))).click();

        // Assert success message
        By successToast = By.cssSelector(".p-toast-message-success .p-toast-detail");
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
        Assertions.assertEquals("Produto cadastrado com sucesso", toast.getText());

        // Verify the product is in the table
        driver.findElement(By.id("search")).sendKeys(uniqueProductName);
        By productInTable = By.xpath("//td[text()='" + uniqueProductName + "']");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(productInTable)).isDisplayed());
    }

    @Test
    @Order(5)
    @DisplayName("Should delete the created product and verify its removal")
    void testDeleteProduct() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        navigateTo("Produtos");

        // Search for the product to delete
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search"))).sendKeys(uniqueProductName);
        By productRowCell = By.xpath("//td[text()='" + uniqueProductName + "']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(productRowCell));

        // Click the delete button in the product's row
        By deleteButton = By.xpath("//td[text()='" + uniqueProductName + "']/following-sibling::td//button[contains(@class, 'p-button-danger')]");
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();

        // Confirm deletion in the modal
        By confirmButton = By.xpath("//button[.//span[text()='Sim']]");
        wait.until(ExpectedConditions.elementToBeClickable(confirmButton)).click();
        
        // Assert success message
        By successToast = By.cssSelector(".p-toast-message-success .p-toast-detail");
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
        Assertions.assertEquals("Produto excluído com sucesso", toast.getText());

        // Verify the product is no longer in the table
        driver.findElement(By.id("search")).clear();
        driver.findElement(By.id("search")).sendKeys(uniqueProductName);
        By noResultsMessage = By.xpath("//td[text()='Nenhum produto encontrado']");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(noResultsMessage)).isDisplayed());
    }
    
    @Test
    @Order(6)
    @DisplayName("Should log out successfully")
    void testLogout() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        performLogout();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.findElement(By.id("email")).isDisplayed(), "Should be on login page after logout.");
    }


    // --- Helper Methods ---

    /**
     * Fills the login form and submits. Assumes driver is on the login page.
     */
    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[.//span[text()='Acessar']]")).click();
    }

    /**
     * Logs out using the user profile dropdown menu. Assumes user is logged in.
     */
    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".avatar-profile"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Sair']"))).click();
    }
    
    /**
     * Navigates to a page using the left sidebar menu.
     */
    private void navigateTo(String linkText) {
        By linkLocator = By.xpath("//span[text()='" + linkText + "']/ancestor::a");
        wait.until(ExpectedConditions.elementToBeClickable(linkLocator)).click();
    }
}