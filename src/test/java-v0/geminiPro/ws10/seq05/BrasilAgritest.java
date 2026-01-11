package geminiPro.ws10.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * JUnit 5 test suite for the Brasil Agrimest beta platform.
 * This suite uses Selenium WebDriver with headless Firefox to test core application
 * functionality including login, navigation, and a full CRUD (Create, Read, Update, Delete)
 * lifecycle for a "Client" entity.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    // --- Locators ---
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.xpath("//button[@type='submit']");
    private final By userMenuButton = By.id("user-menu");
    private final By logoutButton = By.xpath("//button[contains(text(), 'Sair')]");
    private final By toastMessage = By.cssSelector("div[role='status']");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Longer wait for this complex app
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        driver.get(BASE_URL);
    }

    /**
     * Helper method to perform a login with the default credentials.
     */
    private void performLogin() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(USERNAME);
        driver.findElement(passwordInput).sendKeys(PASSWORD);
        driver.findElement(loginButton).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    @Test
    @Order(1)
    @DisplayName("Should show an error message for invalid login credentials")
    void testInvalidLogin() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(USERNAME);
        driver.findElement(passwordInput).sendKeys("wrongpassword");
        driver.findElement(loginButton).click();

        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
        Assertions.assertTrue(
            toast.getText().contains("Credenciais inválidas"),
            "Error toast message for invalid login is not correct."
        );
    }

    @Test
    @Order(2)
    @DisplayName("Should successfully log in and then log out")
    void testSuccessfulLoginAndLogout() {
        performLogin();
        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[normalize-space()='Dashboard']")
        ));
        Assertions.assertTrue(dashboardTitle.isDisplayed(), "Dashboard title should be visible after login.");

        wait.until(ExpectedConditions.elementToBeClickable(userMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(
            driver.findElement(emailInput).isDisplayed(),
            "Email input should be visible after logout."
        );
    }

    @Test
    @Order(3)
    @DisplayName("Should navigate to different pages using the main menu")
    void testMainMenuNavigation() {
        performLogin();

        // Navigate to Clients page
        wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-cadastros"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/clients']"))).click();
        wait.until(ExpectedConditions.urlContains("/clients"));
        WebElement clientsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[normalize-space()='Clientes']")
        ));
        Assertions.assertEquals("Clientes", clientsTitle.getText(), "Should be on the Clients page.");

        // Navigate to Products page
        wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-cadastros"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/products']"))).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        WebElement productsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[normalize-space()='Produtos']")
        ));
        Assertions.assertEquals("Produtos", productsTitle.getText(), "Should be on the Products page.");
    }

    @Test
    @Order(4)
    @DisplayName("Should create, read, and delete a new client")
    void testClientCreateReadDeleteFlow() {
        long timestamp = System.currentTimeMillis();
        String uniqueClientName = "Cliente Teste Automatizado " + timestamp;

        performLogin();

        // --- Navigate to Clients page ---
        wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-cadastros"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/clients']"))).click();
        wait.until(ExpectedConditions.urlContains("/clients"));

        // --- Create New Client ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Novo Cliente')]"))).click();
        wait.until(ExpectedConditions.urlContains("/clients/create"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys(uniqueClientName);
        driver.findElement(By.xpath("//button[@type='submit' and contains(text(), 'Salvar')]")).click();

        // --- Assert Creation and Read ---
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
        Assertions.assertTrue(
            toast.getText().contains("Cliente cadastrado com sucesso"),
            "Success toast for client creation did not appear."
        );
        wait.until(ExpectedConditions.urlContains("/clients"));
        driver.findElement(By.id("search")).sendKeys(uniqueClientName); // Search to confirm
        WebElement clientRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//td[contains(text(), '" + uniqueClientName + "')]")
        ));
        Assertions.assertTrue(clientRow.isDisplayed(), "Newly created client was not found in the list.");

        // --- Delete Client ---
        WebElement deleteButton = clientRow.findElement(By.xpath("./following-sibling::td//button[contains(@aria-label, 'Deletar')]"));
        deleteButton.click();
        WebElement confirmDeleteButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Sim, deletar!')]")
        ));
        confirmDeleteButton.click();
        
        // --- Assert Deletion ---
        toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
        Assertions.assertTrue(
            toast.getText().contains("Cliente deletado com sucesso"),
            "Success toast for client deletion did not appear."
        );
        // Wait for the row to disappear
        wait.until(ExpectedConditions.invisibilityOf(clientRow));
        Assertions.assertTrue(
            driver.findElements(By.xpath("//td[contains(text(), '" + uniqueClientName + "')]")).isEmpty(),
            "Client should be removed from the list after deletion."
        );
    }
}