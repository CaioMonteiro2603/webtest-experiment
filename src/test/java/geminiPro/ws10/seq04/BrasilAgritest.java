package geminiPRO.ws10.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A JUnit 5 test suite for the Brasil Agri Test administrative platform.
 * It uses Selenium WebDriver with Firefox in headless mode to test login,
 * main dashboard navigation, page verification, and logout functionalities.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgriTestHeadlessFirefoxTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String DASHBOARD_URL = "https://beta.brasilagritest.com/dashboard";
    private static final String VALID_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASSWORD = "10203040";

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void teardownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    /**
     * Helper method to perform login action.
     * @param email The user's email.
     * @param password The user's password.
     */
    private void performLogin(String email, String password) {
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Test
    @Order(1)
    @DisplayName("Test Login with Invalid Credentials")
    void testInvalidLogin() {
        performLogin(VALID_EMAIL, "invalidpassword");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'Não foi possível validar os seus dados')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message for invalid credentials should be displayed.");
    }

    @Test
    @Order(2)
    @DisplayName("Test Successful Login and Dashboard Verification")
    void testSuccessfulLoginAndDashboardLoad() {
        performLogin(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlToBe(DASHBOARD_URL));
        Assertions.assertEquals(DASHBOARD_URL, driver.getCurrentUrl(), "User should be redirected to the dashboard after successful login.");

        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Dashboard']")));
        Assertions.assertTrue(dashboardHeader.isDisplayed(), "Dashboard header should be visible after login.");
    }

    @Test
    @Order(3)
    @DisplayName("Navigate to 'Clientes' page and verify content")
    void testNavigateToClientesPage() {
        performLogin(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlToBe(DASHBOARD_URL));

        WebElement clientesLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/clientes']")));
        clientesLink.click();

        wait.until(ExpectedConditions.urlContains("/clientes"));
        WebElement clientesHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Clientes']")));
        Assertions.assertTrue(clientesHeader.isDisplayed(), "'Clientes' page header should be visible.");
    }

    @Test
    @Order(4)
    @DisplayName("Navigate to 'Equipamentos' page and verify content")
    void testNavigateToEquipamentosPage() {
        performLogin(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlToBe(DASHBOARD_URL));

        WebElement equipamentosLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/equipamentos']")));
        equipamentosLink.click();

        wait.until(ExpectedConditions.urlContains("/equipamentos"));
        WebElement equipamentosHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Equipamentos']")));
        Assertions.assertTrue(equipamentosHeader.isDisplayed(), "'Equipamentos' page header should be visible.");
    }

    @Test
    @Order(5)
    @DisplayName("Navigate to 'Técnicos' page and verify content")
    void testNavigateToTecnicosPage() {
        performLogin(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlToBe(DASHBOARD_URL));

        WebElement tecnicosLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/tecnicos']")));
        tecnicosLink.click();

        wait.until(ExpectedConditions.urlContains("/tecnicos"));
        WebElement tecnicosHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Técnicos']")));
        Assertions.assertTrue(tecnicosHeader.isDisplayed(), "'Técnicos' page header should be visible.");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test Search functionality on the 'Clientes' page")
    void testSearchFunctionalityOnClientesPage() {
        performLogin(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/clientes']"))).click();
        wait.until(ExpectedConditions.urlContains("/clientes"));

        // Wait for the table to load at least one row
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));
        
        // Use a known search term from the test data
        String searchTerm = "A FAZENDA";
        WebElement searchInput = driver.findElement(By.id("search"));
        searchInput.sendKeys(searchTerm);

        // Wait for the table to update after search
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("tbody tr"), 0));

        // Verify that all visible rows contain the search term
        java.util.List<WebElement> tableRows = driver.findElements(By.cssSelector("tbody tr"));
        Assertions.assertTrue(tableRows.size() > 0, "At least one result should be found for the search term.");
        for (WebElement row : tableRows) {
            Assertions.assertTrue(row.getText().toUpperCase().contains(searchTerm.toUpperCase()), 
                "Row should contain the search term '" + searchTerm + "'.");
        }
    }


    @Test
    @Order(7)
    @DisplayName("Test Successful Logout")
    void testLogout() {
        performLogin(VALID_EMAIL, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlToBe(DASHBOARD_URL));

        // Click the user profile dropdown to reveal the logout button
        WebElement profileDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("radix-:r1:")));
        profileDropdown.click();
        
        // Click the logout button
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Sair']")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "User should be redirected to the login page after logout.");
        
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        Assertions.assertTrue(emailInput.isDisplayed(), "Login form should be visible after logout.");
    }
}