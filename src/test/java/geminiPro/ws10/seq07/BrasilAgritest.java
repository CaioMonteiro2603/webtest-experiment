package geminiPro.ws10.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for the BrasilAgriTest beta application.
 * This suite covers admin login, sidebar navigation, creation of a new entity (Farm),
 * and data table sorting functionality.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String LOGIN_PASSWORD = "10203040";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15); // Increased for this enterprise app

    // Unique data for the test run
    private static final String UNIQUE_FARM_NAME = "Fazenda Gemini " + System.currentTimeMillis();

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        // Most tests start by ensuring a specific state (logged in/out),
        // so a generic navigation here is not necessary.
    }

    @Test
    @Order(1)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        performLogin(LOGIN_EMAIL, "invalid-password");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//p[contains(text(), 'Credenciais inválidas')]")));
        assertTrue(errorMessage.isDisplayed(), "Error message for invalid credentials should be shown.");
    }

    @Test
    @Order(2)
    void testSuccessfulLoginAndLogout() {
        driver.get(BASE_URL);
        performLogin(LOGIN_EMAIL, LOGIN_PASSWORD);

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h2[text()='Dashboard']")));
        assertTrue(dashboardHeader.isDisplayed(), "Dashboard header should be visible after login.");

        performLogout();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(By.id("email")).isDisplayed(), "Email field should be visible after logout, indicating return to login page.");
    }

    @Test
    @Order(3)
    void testSidebarNavigation() {
        ensureLoggedIn();
        
        // Navigate to Farms
        WebElement cadastrosMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("cadastros")));
        cadastrosMenu.click();
        WebElement farmsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/farms']")));
        farmsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/farms"));
        assertTrue(driver.findElement(By.xpath("//h2[text()='Fazendas']")).isDisplayed(), "Farms page header should be visible.");

        // Navigate to Users
        cadastrosMenu.click(); // May need to click parent menu again
        WebElement usersLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/users']")));
        usersLink.click();
        
        wait.until(ExpectedConditions.urlContains("/users"));
        assertTrue(driver.findElement(By.xpath("//h2[text()='Usuários']")).isDisplayed(), "Users page header should be visible.");
    }

    @Test
    @Order(4)
    void testCreateNewFarm() {
        ensureLoggedIn();
        driver.get(BASE_URL.replace("/login", "/farms"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Adicionar Fazenda')]"))).click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.sendKeys(UNIQUE_FARM_NAME);
        driver.findElement(By.id("city")).sendKeys("Dois Córregos");
        driver.findElement(By.id("state")).sendKeys("SP");

        driver.findElement(By.xpath("//button[text()='Salvar']")).click();

        WebElement successToast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'Fazenda cadastrada com sucesso!')]")));
        assertTrue(successToast.isDisplayed(), "Success notification for farm creation should appear.");

        // Verify the new farm is in the list
        wait.until(ExpectedConditions.invisibilityOf(successToast));
        WebElement farmInList = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//td[text()='" + UNIQUE_FARM_NAME + "']")));
        assertTrue(farmInList.isDisplayed(), "Newly created farm should be visible in the table.");
    }

    @Test
    @Order(5)
    void testSortFarmsByName() {
        ensureLoggedIn();
        driver.get(BASE_URL.replace("/login", "/farms"));

        // Wait for the table to be fully loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tbody/tr")));
        
        // Click the header to sort ascending
        WebElement nameHeader = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//th[text()='Nome']")));
        nameHeader.click();

        // Wait for a sort indicator/animation if any, or just for the data to re-render
        wait.until(ExpectedConditions.attributeContains(nameHeader, "class", "sorted"));

        List<WebElement> nameCells = driver.findElements(By.xpath("//tbody/tr/td[1]"));
        List<String> farmNames = nameCells.stream().map(WebElement::getText).collect(Collectors.toList());

        List<String> sortedList = farmNames.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
        
        assertEquals(sortedList, farmNames, "Farm names should be sorted alphabetically in ascending order.");
    }
    
    // --- Helper Methods ---

    private void performLogin(String email, String password) {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailField.clear();
        emailField.sendKeys(email);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(password);
        
        driver.findElement(By.xpath("//button[text()='Entrar']")).click();
    }

    private void performLogout() {
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-menu")));
        userMenu.click();
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Sair']")));
        logoutButton.click();
    }

    private void ensureLoggedIn() {
        // A simple check to see if we are already on a page inside the app
        if (!driver.getCurrentUrl().contains("/dashboard") && !driver.getCurrentUrl().contains("/farms")) {
            driver.get(BASE_URL);
            performLogin(LOGIN_EMAIL, LOGIN_PASSWORD);
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }
    }
}