package geminiPro.ws10.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String LOGIN_PASSWORD = "10203040";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    // Unique name for the created farm to ensure test isolation
    private static String testFarmName;

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.xpath("//button[text()='Entrar']");
    private final By userDropdown = By.id("user-menu-button");
    private final By logoutButton = By.xpath("//button[text()='Sair']");
    private final By farmsSidebarLink = By.xpath("//nav//span[text()='Fazendas']/..");
    private final By cropsSidebarLink = By.xpath("//nav//span[text()='Culturas']/..");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);

        testFarmName = "Fazenda Teste-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(LOGIN_EMAIL);
        driver.findElement(passwordInput).sendKeys(LOGIN_PASSWORD);
        driver.findElement(loginButton).click();
        // Wait for dashboard to load by checking for the user menu
        wait.until(ExpectedConditions.visibilityOfElementLocated(userDropdown));
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(userDropdown)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
    }

    private void navigateToFarms() {
        wait.until(ExpectedConditions.elementToBeClickable(farmsSidebarLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Fazendas']")));
    }
    
    private void waitForSuccessToast(String expectedMessage) {
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'toast')]//p[2]")));
        assertEquals(expectedMessage, toast.getText(), "Success toast message did not match.");
        // Wait for the toast to disappear to prevent it from obscuring other elements
        wait.until(ExpectedConditions.invisibilityOf(toast));
    }

    @Test
    @Order(1)
    @DisplayName("Test Login Page Elements Visibility")
    void testLoginPageElements() {
        driver.get(BASE_URL);
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).isDisplayed(), "Email input should be visible.");
        assertTrue(driver.findElement(passwordInput).isDisplayed(), "Password input should be visible.");
        assertTrue(driver.findElement(loginButton).isDisplayed(), "Login button should be visible.");
    }

    @Test
    @Order(2)
    @DisplayName("Test Login with Invalid Credentials")
    void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys("invalid@user.com");
        driver.findElement(passwordInput).sendKeys("wrongpassword");
        driver.findElement(loginButton).click();
        
        WebElement errorToast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'toast-error')]")));
        assertTrue(errorToast.isDisplayed(), "Error toast should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        login();
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login.");
        assertTrue(driver.findElement(userDropdown).isDisplayed(), "User menu should be visible after login.");
        logout();
        assertTrue(driver.getCurrentUrl().contains("/login"), "Should be redirected to login page after logout.");
    }

    @Test
    @Order(4)
    @DisplayName("Test Sidebar Navigation")
    void testSidebarNavigation() {
        login();
        navigateToFarms();
        assertEquals("https://beta.brasilagritest.com/farms", driver.getCurrentUrl(), "URL should be for the Farms page.");
        
        wait.until(ExpectedConditions.elementToBeClickable(cropsSidebarLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Culturas']")));
        assertEquals("https://beta.brasilagritest.com/crops", driver.getCurrentUrl(), "URL should be for the Crops page.");
        logout();
    }

    @Test
    @Order(5)
    @DisplayName("Create, Edit, and Delete a Farm")
    void testFarmCrudLifecycle() {
        login();
        navigateToFarms();

        // --- CREATE ---
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Nova Fazenda"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys(testFarmName);
        driver.findElement(By.id("city")).sendKeys("Dois Córregos");
        driver.findElement(By.id("state")).sendKeys("SP");
        driver.findElement(By.xpath("//button[text()='Salvar']")).click();
        waitForSuccessToast("Fazenda salva com sucesso!");

        // --- VERIFY CREATION ---
        navigateToFarms();
        By farmNameCell = By.xpath("//td[text()='" + testFarmName + "']");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(farmNameCell)).isDisplayed(), "Newly created farm should be in the list.");

        // --- EDIT ---
        By farmRow = By.xpath("//tr[td[text()='" + testFarmName + "']]");
        WebElement editButton = driver.findElement(farmRow).findElement(By.xpath(".//button[@aria-label='Editar']"));
        editButton.click();
        
        String updatedCity = "Jaú";
        WebElement cityInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("city")));
        cityInput.clear();
        cityInput.sendKeys(updatedCity);
        driver.findElement(By.xpath("//button[text()='Salvar']")).click();
        waitForSuccessToast("Fazenda salva com sucesso!");

        // --- VERIFY EDIT ---
        navigateToFarms();
        By updatedCityCell = By.xpath("//tr[td[text()='" + testFarmName + "']]/td[2]");
        assertEquals(updatedCity, wait.until(ExpectedConditions.visibilityOfElementLocated(updatedCityCell)).getText(), "Farm city should be updated.");

        // --- DELETE ---
        WebElement deleteButton = driver.findElement(farmRow).findElement(By.xpath(".//button[@aria-label='Deletar']"));
        deleteButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Sim, deletar']"))).click();
        waitForSuccessToast("Fazenda deletada com sucesso!");

        // --- VERIFY DELETION ---
        navigateToFarms();
        assertTrue(wait.until(ExpectedConditions.invisibilityOfElementLocated(farmNameCell)), "Deleted farm should not be in the list.");
        logout();
    }

    @Test
    @Order(6)
    @DisplayName("Test Farm List Sorting by Name")
    void testFarmListSorting() {
        login();
        navigateToFarms();
        
        By nameColumnHeader = By.xpath("//th[text()='Nome']");
        By farmNameCells = By.xpath("//tbody/tr/td[1]");

        // Get initial order
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(farmNameCells, 1));
        List<String> initialNames = driver.findElements(farmNameCells).stream().map(WebElement::getText).collect(Collectors.toList());

        // Sort Ascending
        wait.until(ExpectedConditions.elementToBeClickable(nameColumnHeader)).click();
        wait.until(ExpectedConditions.attributeContains(nameColumnHeader, "aria-sort", "ascending"));
        List<String> ascNames = driver.findElements(farmNameCells).stream().map(WebElement::getText).collect(Collectors.toList());
        Collections.sort(initialNames);
        assertEquals(initialNames, ascNames, "Farm names should be sorted in ascending order.");

        // Sort Descending
        wait.until(ExpectedConditions.elementToBeClickable(nameColumnHeader)).click();
        wait.until(ExpectedConditions.attributeContains(nameColumnHeader, "aria-sort", "descending"));
        List<String> descNames = driver.findElements(farmNameCells).stream().map(WebElement::getText).collect(Collectors.toList());
        Collections.reverse(initialNames);
        assertEquals(initialNames, descNames, "Farm names should be sorted in descending order.");

        logout();
    }
}