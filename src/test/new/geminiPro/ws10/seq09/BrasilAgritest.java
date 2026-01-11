package geminiPro.ws10.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for the Brasil Agri Test beta application.
 * This suite covers login, sidebar navigation, logout, and data table sorting.
 * It uses Selenium WebDriver with headless Firefox.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static final String BASE_URL = "https://accounts.brasilagr.com.br/login";
    private static final String VALID_USERNAME = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASSWORD = "10203040";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.cssSelector("button[type='submit']");
    private final By userMenuButton = By.xpath("//header//button[contains(@class, 'MuiIconButton-root')]");
    private final By logoutMenuItem = By.xpath("//li[text()='Sair']");
    private final By errorMessageToast = By.cssSelector("div[role='alert']");
    private final By dashboardHeader = By.xpath("//h1[text()='Início']");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        // The application can be slow to respond, a longer wait is safer.
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
    }

    @AfterEach
    void logoutIfLoggedIn() {
        try {
            // Check if we are logged in by looking for the user menu button.
            if (!driver.getCurrentUrl().contains("/login") && driver.findElements(userMenuButton).size() > 0) {
                driver.findElement(userMenuButton).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(logoutMenuItem)).click();
                wait.until(ExpectedConditions.urlContains("/login"));
            }
        } catch (NoSuchElementException e) {
            // Not logged in or element not found, which is fine.
        }
    }

    @Test
    @Order(1)
    void testInvalidLogin() {
        performLogin(VALID_USERNAME, "wrongpassword");
        WebElement errorToast = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageToast));
        assertTrue(errorToast.getText().contains("E-mail ou senha incorreta"), "Error message for invalid login did not appear or was incorrect.");
    }

    @Test
    @Order(2)
    void testSuccessfulLoginAndLogout() {
        // Login
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlToBe("https://accounts.brasilagr.com.br/"));
        assertTrue(driver.findElement(dashboardHeader).isDisplayed(), "Dashboard header not found after login.");

        // Logout
        wait.until(ExpectedConditions.elementToBeClickable(userMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutMenuItem)).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.findElement(loginButton).isDisplayed(), "Should be on the login page after logout.");
    }

    @Test
    @Order(3)
    void testSidebarNavigation() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardHeader));
        
        // Navigate to Laudos (Reports)
        navigateToSidebarLinkAndVerify("Laudos", "/reports", "Laudos");
        
        // Navigate to Amostras (Samples)
        navigateToSidebarLinkAndVerify("Amostras", "/samples", "Amostras");

        // Navigate to Relatórios (Financial)
        navigateToSidebarLinkAndVerify("Relatórios", "/financial", "Relatórios");

        // Navigate back to Início (Home)
        navigateToSidebarLinkAndVerify("Início", "/", "Início");
    }

    @Test
    @Order(4)
    void testReportTableSortingByCulture() {
        performLogin(VALID_USERNAME, VALID_PASSWORD);
        navigateToSidebarLinkAndVerify("Laudos", "/reports", "Laudos");

        // Wait for table to load. A simple way is to wait for the first row.
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        // Get initial order of the "Cultura" (Culture) column
        List<String> initialOrder = getColumnData(1);
        assertTrue(initialOrder.size() > 1, "Table should have multiple rows to test sorting.");
        
        WebElement cultureHeader = driver.findElement(By.xpath("//th//span[text()='Cultura']"));

        // Click once for ascending sort
        wait.until(ExpectedConditions.elementToBeClickable(cultureHeader)).click();
        // Wait for a loading spinner to disappear, which indicates data refresh
        waitForLoadingSpinner();
        List<String> ascendingOrder = getColumnData(1);

        // Assert ascending order
        List<String> expectedAscending = initialOrder.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
        assertEquals(expectedAscending, ascendingOrder, "Column 'Cultura' is not sorted in ascending order.");
        assertNotEquals(initialOrder, ascendingOrder, "Ascending order should be different from initial order.");

        // Click again for descending sort
        wait.until(ExpectedConditions.elementToBeClickable(cultureHeader)).click();
        waitForLoadingSpinner();
        List<String> descendingOrder = getColumnData(1);

        // Assert descending order
        Collections.reverse(expectedAscending);
        assertEquals(expectedAscending, descendingOrder, "Column 'Cultura' is not sorted in descending order.");
    }

    // --- Helper Methods ---

    private void performLogin(String username, String password) {
        driver.findElement(emailInput).sendKeys(username);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }
    
    private void navigateToSidebarLinkAndVerify(String linkText, String expectedPath, String expectedHeader) {
        By linkLocator = By.xpath(String.format("//nav//span[text()='%s']/..", linkText));
        wait.until(ExpectedConditions.elementToBeClickable(linkLocator)).click();
        wait.until(ExpectedConditions.urlContains(expectedPath));
        By headerLocator = By.xpath(String.format("//h1[text()='%s']", expectedHeader));
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(headerLocator)).isDisplayed(),
            "Header '" + expectedHeader + "' not found on page " + expectedPath);
    }
    
    private List<String> getColumnData(int columnIndex) {
        // Wait for rows to be present before trying to collect data
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("tbody tr"), 0));
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        return rows.stream()
            .map(row -> row.findElement(By.xpath("./td[" + columnIndex + "]")).getText())
            .filter(text -> !text.isEmpty()) // Filter out empty cells if any
            .collect(Collectors.toList());
    }

    private void waitForLoadingSpinner() {
        By spinnerLocator = By.cssSelector("span[role='progressbar']");
        // First, wait for the spinner to appear briefly (optional, but good practice)
        try {
            wait.withTimeout(Duration.ofSeconds(2)).until(ExpectedConditions.visibilityOfElementLocated(spinnerLocator));
        } catch (Exception e) {
            // Spinner might be too fast to catch, which is okay.
        }
        // Then, wait for the spinner to disappear
        wait.withTimeout(Duration.ofSeconds(15)).until(ExpectedConditions.invisibilityOfElementLocated(spinnerLocator));
    }
}