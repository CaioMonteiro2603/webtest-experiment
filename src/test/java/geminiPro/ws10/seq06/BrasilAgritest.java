package geminiPRO.ws10.seq06;

import org.junit.jupiter.api.AfterAll;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for the BrasilAgriTest beta application.
 * This suite covers login, navigation, and a full CRUD (Create, Read, Update, Delete)
 * workflow for a "Cliente" (Client) entity.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgriTest {

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final String VALID_USER = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASS = "10203040";

    // Static variable for the unique client used in the CRUD test
    private static String testClientName;
    private static String testClientCpf;

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use headless mode via arguments ONLY
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        
        long timestamp = System.currentTimeMillis();
        testClientName = "Cliente Teste Gemini " + timestamp;
        testClientCpf = generateRandomCpf();
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Tests both failed and successful login scenarios.
     */
    @Test
    @Order(1)
    void loginFunctionalityTest() {
        driver.get(BASE_URL);

        // Test Failed Login
        performLogin(VALID_USER, "wrongpassword");
        WebElement errorToast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("p-toast-summary")));
        assertEquals("Erro", errorToast.getText(), "Error toast title for failed login is incorrect.");
        // Close the toast to proceed
        wait.until(ExpectedConditions.elementToBeClickable(By.className("p-toast-icon-close"))).click();

        // Test Successful Login
        performLogin(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Dashboard']")));
        assertTrue(dashboardHeader.isDisplayed(), "Dashboard header not found after successful login.");
    }

    /**
     * Tests the left-side navigation menu by navigating to different pages.
     */
    @Test
    @Order(2)
    void leftMenuNavigationTest() {
        loginIfNecessary();
        
        WebElement menuCadastros = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Cadastros']")));
        menuCadastros.click();
        
        WebElement menuClientes = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/clientes']")));
        menuClientes.click();

        wait.until(ExpectedConditions.urlContains("/clientes"));
        WebElement clientesHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Clientes']")));
        assertTrue(clientesHeader.isDisplayed(), "Failed to navigate to the Clientes page.");
    }

    /**
     * Tests the complete Create, Read, Update, and Delete lifecycle for a Client.
     * This single test ensures the full flow works and handles its own state cleanup.
     */
    @Test
    @Order(3)
    void clientCrudFlowTest() {
        loginIfNecessary();
        driver.get("https://beta.brasilagritest.com/clientes");

        // --- CREATE ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Adicionar Cliente')]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Adicionar Cliente']")));
        driver.findElement(By.id("nome")).sendKeys(testClientName);
        driver.findElement(By.id("cpfCnpj")).sendKeys(testClientCpf);
        driver.findElement(By.id("telefone")).sendKeys("11999999999");
        driver.findElement(By.id("email")).sendKeys("test." + System.currentTimeMillis() + "@gemini.com");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Salvar']"))).click();
        assertSuccessToast("Cliente cadastrado com sucesso!");

        // --- READ ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Clientes']")));
        WebElement searchInput = driver.findElement(By.id("filter-input"));
        searchInput.sendKeys(testClientName);
        WebElement clientRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//td[contains(text(), '" + testClientName + "')]")));
        assertTrue(clientRow.isDisplayed(), "Created client was not found in the list.");

        // --- UPDATE ---
        String updatedClientName = testClientName + " - Editado";
        WebElement editButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + testClientName + "')]]//button[@aria-label='Editar']"));
        editButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Editar Cliente']")));
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));
        nameInput.clear();
        nameInput.sendKeys(updatedClientName);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Salvar']"))).click();
        assertSuccessToast("Cliente atualizado com sucesso!");

        // --- VERIFY UPDATE & DELETE ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Clientes']")));
        searchInput.clear();
        searchInput.sendKeys(updatedClientName);
        WebElement updatedRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//td[contains(text(), '" + updatedClientName + "')]")));
        assertTrue(updatedRow.isDisplayed(), "Updated client name was not found.");
        
        WebElement deleteButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + updatedClientName + "')]]//button[@aria-label='Deletar']"));
        deleteButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Sim']"))).click();
        assertSuccessToast("Cliente deletado com sucesso!");
        
        // Final verification of deletion
        searchInput.clear();
        searchInput.sendKeys(updatedClientName);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[text()='Nenhum cliente encontrado.']")));
    }
    
    /**
     * Tests the logout functionality.
     */
    @Test
    @Order(4)
    void logoutTest() {
        loginIfNecessary();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.className("user-profile-avatar"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Sair']"))).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(By.id("email")).isDisplayed(), "Email input not found after logout, indicating logout failed.");
    }
    
    // --- Helper Methods ---

    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Entrar']")).click();
    }
    
    private void loginIfNecessary() {
        try {
            driver.findElement(By.className("user-profile-avatar"));
        } catch (Exception e) {
            driver.get(BASE_URL);
            performLogin(VALID_USER, VALID_PASS);
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }
    }

    private void assertSuccessToast(String expectedMessage) {
        WebElement toastMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("p-toast-detail")));
        assertEquals(expectedMessage, toastMessage.getText());
        wait.until(ExpectedConditions.elementToBeClickable(By.className("p-toast-icon-close"))).click();
        wait.until(ExpectedConditions.invisibilityOf(toastMessage));
    }
    
    private static String generateRandomCpf() {
        // Generates a mathematically valid (but random) CPF number for testing
        int[] cpf = new int[11];
        for (int i = 0; i < 9; i++) {
            cpf[i] = (int) (Math.random() * 10);
        }
        cpf[9] = calculateVerifierDigit(cpf, 9);
        cpf[10] = calculateVerifierDigit(cpf, 10);
        
        StringBuilder sb = new StringBuilder();
        for (int digit : cpf) {
            sb.append(digit);
        }
        return sb.toString();
    }

    private static int calculateVerifierDigit(int[] digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * (length + 1 - i);
        }
        int remainder = sum % 11;
        return (remainder < 2) ? 0 : 11 - remainder;
    }
}