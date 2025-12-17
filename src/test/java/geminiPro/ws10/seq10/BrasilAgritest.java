package geminiPro.ws10.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A comprehensive JUnit 5 test suite for the Brasil Agri Test platform.
 * This suite covers the superadmin login flow, sidebar navigation,
 * data interaction on list pages (search and sort), and the logout process.
 * It uses Selenium WebDriver with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Details ---
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String USER_PASSWORD = "10203040";

    // --- Locators ---
    // Login
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By LOGIN_ERROR_MESSAGE = By.className("text-danger");
    
    // Main Layout
    private static final By DASHBOARD_TITLE = By.xpath("//h2[text()='Dashboard']");
    private static final By USER_MENU_DROPDOWN = By.id("user-menu");
    private static final By LOGOUT_BUTTON = By.xpath("//button[text()='Sair']");
    
    // Sidebar
    private static final By CROPS_MENU_LINK = By.cssSelector("a[href='/crops']");

    // Crops Page
    private static final By CROPS_PAGE_TITLE = By.xpath("//h2[text()='Culturas']");
    private static final By SEARCH_INPUT = By.id("search");
    private static final By TABLE_BODY_ROWS = By.cssSelector("tbody tr");
    private static final By NAME_COLUMN_HEADER = By.xpath("//th[contains(., 'Nome')]");


    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as strictly required
        driver = new FirefoxDriver(options);
        // Use a longer wait as the platform can be slow to load data
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Helper method to perform login
    private void login(String email, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(email);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }
    
    @Test
    @Order(1)
    @DisplayName("🧪 Test Login with Invalid and Valid Credentials")
    void testLoginFunctionality() {
        // --- Invalid Login ---
        login(USER_EMAIL, "wrongpassword");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_ERROR_MESSAGE));
        Assertions.assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message for invalid credentials should be displayed.");
        
        // --- Valid Login ---
        login(USER_EMAIL, USER_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_TITLE));
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/dashboard"), "Should be redirected to the dashboard after a successful login.");
    }
    
    @Test
    @Order(2)
    @DisplayName("🧪 Test Sidebar Navigation to Crops Page")
    void testSidebarNavigation() {
        login(USER_EMAIL, USER_PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_TITLE));
        
        // Navigate to "Culturas" (Crops)
        wait.until(ExpectedConditions.elementToBeClickable(CROPS_MENU_LINK)).click();
        
        wait.until(ExpectedConditions.urlContains("/crops"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/crops"), "URL should navigate to the crops page.");
        
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(CROPS_PAGE_TITLE));
        Assertions.assertTrue(pageTitle.isDisplayed(), "Crops page title should be visible.");
    }

    @Test
    @Order(3)
    @DisplayName("🧪 Test Crops Page Search and Sort Functionality")
    void testCropsPageInteraction() {
        // Ensure we are logged in and on the correct page
        if (!driver.getCurrentUrl().contains("/crops")) {
            login(USER_EMAIL, USER_PASSWORD);
            wait.until(ExpectedConditions.elementToBeClickable(CROPS_MENU_LINK)).click();
            wait.until(ExpectedConditions.urlContains("/crops"));
        }
        
        // --- Test Search ---
        String searchTerm = "Soja";
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        searchInput.sendKeys(searchTerm);
        
        // Wait for the table to filter. A good condition is to wait until the number of rows is less than the initial count or a specific row appears.
        wait.until(ExpectedConditions.numberOfElementsToBeLessThan(TABLE_BODY_ROWS, 50)); // Assuming more than 50 rows initially
        
        List<WebElement> rowsAfterSearch = driver.findElements(TABLE_BODY_ROWS);
        Assertions.assertFalse(rowsAfterSearch.isEmpty(), "Search for 'Soja' should return at least one result.");
        for (WebElement row : rowsAfterSearch) {
            Assertions.assertTrue(row.getText().contains(searchTerm), "All visible rows should contain the search term 'Soja'.");
        }
        
        // --- Test Sort ---
        searchInput.clear();
        wait.until(
        	    ExpectedConditions.numberOfElementsToBeLessThan(
        	        TABLE_BODY_ROWS,
        	        rowsAfterSearch.size()
        	    )
        	);

        WebElement nameHeader = wait.until(ExpectedConditions.elementToBeClickable(NAME_COLUMN_HEADER));

        // Get initial order (ascending)
        nameHeader.click();
        wait.until(ExpectedConditions.attributeContains(nameHeader, "class", "sorted-asc")); // Wait for sort indicator
        List<String> ascNames = driver.findElements(TABLE_BODY_ROWS).stream()
                .map(row -> row.findElement(By.cssSelector("td:nth-child(2)")).getText())
                .collect(Collectors.toList());

        // Get descending order
        nameHeader.click();
        wait.until(ExpectedConditions.attributeContains(nameHeader, "class", "sorted-desc")); // Wait for sort indicator
        List<String> descNames = driver.findElements(TABLE_BODY_ROWS).stream()
                .map(row -> row.findElement(By.cssSelector("td:nth-child(2)")).getText())
                .collect(Collectors.toList());
        
        Assertions.assertNotEquals(ascNames.get(0), descNames.get(0), "First elements of ascending and descending sort should be different.");
        Assertions.assertEquals(ascNames.get(0), descNames.get(descNames.size() - 1), "First element of ascending should be last of descending.");
    }

    @Test
    @Order(4)
    @DisplayName("🧪 Test User Logout")
    void testLogout() {
        // Ensure user is logged in
        if (driver.findElements(DASHBOARD_TITLE).isEmpty()) {
            login(USER_EMAIL, USER_PASSWORD);
            wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_TITLE));
        }
        
        wait.until(ExpectedConditions.elementToBeClickable(USER_MENU_DROPDOWN)).click();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to the login page after logout.");
        
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON));
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout.");
    }
}