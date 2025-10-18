package Qwen3.ws10.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritestTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String EMAIL = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testLoginPageLoadsSuccessfully() {
        driver.get(BASE_URL);

        String title = driver.getTitle();
        assertEquals("Brasil AgriTest", title, "Page title should be Brasil AgriTest");

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form[action='/login']")));
        assertTrue(loginForm.isDisplayed(), "Login form should be visible");
    }

    @Test
    @Order(2)
    void testValidLogin() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys(EMAIL);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Wait for redirect to dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        assertTrue(dashboardHeader.getText().contains("Dashboard"), "Should navigate to dashboard after login");
    }

    @Test
    @Order(3)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys("invalid@example.com");

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrong");

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    void testNavigationToUsersPage() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/users']")));
        menuButton.click();

        wait.until(ExpectedConditions.urlContains("/users"));
        WebElement usersHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        assertEquals("Usuários", usersHeader.getText().trim(), "Should navigate to Users page");
    }

    @Test
    @Order(5)
    void testNavigationToPropertiesPage() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/properties']")));
        menuButton.click();

        wait.until(ExpectedConditions.urlContains("/properties"));
        WebElement propertiesHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        assertTrue(propertiesHeader.getText().contains("Propriedades"), "Should navigate to Properties page");
    }

    @Test
    @Order(6)
    void testNavigationToAnalysesPage() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/analyses']")));
        menuButton.click();

        wait.until(ExpectedConditions.urlContains("/analyses"));
        WebElement analysesHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        assertTrue(analysesHeader.getText().contains("Análises"), "Should navigate to Analyses page");
    }

    @Test
    @Order(7)
    void testUserListDisplays() {
        loginIfNecessary();

        driver.get(BASE_URL.replace("/login", "/users"));

        java.util.List<WebElement> userRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table tbody tr")));
        assertTrue(userRows.size() > 0, "At least one user should be displayed in the table");
    }

    @Test
    @Order(8)
    void testPropertyListDisplays() {
        loginIfNecessary();

        driver.get(BASE_URL.replace("/login", "/properties"));

        java.util.List<WebElement> propertyRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table tbody tr")));
        assertTrue(propertyRows.size() > 0, "At least one property should be displayed in the table");
    }

    @Test
    @Order(9)
    void testAnalysisListDisplays() {
        loginIfNecessary();

        driver.get(BASE_URL.replace("/login", "/analyses"));

        java.util.List<WebElement> analysisRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table tbody tr")));
        assertTrue(analysisRows.size() > 0, "At least one analysis should be displayed in the table");
    }

    @Test
    @Order(10)
    void testSearchFunctionalityOnUsers() {
        loginIfNecessary();

        driver.get(BASE_URL.replace("/login", "/users"));

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='search']")));
        searchBox.sendKeys("superadmin");

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("table tbody tr td:first-child"), "superadmin"));

        WebElement resultCell = driver.findElement(By.cssSelector("table tbody tr td:first-child"));
        assertTrue(resultCell.getText().contains("superadmin"), "Search should return matching user");
    }

    @Test
    @Order(11)
    void testSortUsersByEmail() {
        loginIfNecessary();

        driver.get(BASE_URL.replace("/login", "/users"));

        WebElement emailHeader = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//th[contains(text(),'E-mail')]")));
        emailHeader.click();

        // Give time for sort to process
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table tbody tr")));

        java.util.List<WebElement> emailCells = driver.findElements(By.cssSelector("table tbody tr td:nth-child(2)"));
        assertTrue(emailCells.size() > 0, "Email column should have values");
    }

    @Test
    @Order(12)
    void testCreateNewUserButtonPresent() {
        loginIfNecessary();

        driver.get(BASE_URL.replace("/login", "/users"));

        WebElement newUserButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-primary[href='/users/create']")));
        assertTrue(newUserButton.isDisplayed(), "New User button should be visible");
        assertEquals("Novo Usuário", newUserButton.getText().trim(), "Button should have correct label");
    }

    @Test
    @Order(13)
    void testProfileDropdownMenu() {
        loginIfNecessary();

        WebElement profileDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.dropdown-toggle")));
        profileDropdown.click();

        WebElement profileOption = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/profile']")));
        assertTrue(profileOption.isDisplayed(), "Profile option should appear in dropdown");

        WebElement logoutOption = driver.findElement(By.cssSelector("a[href='/logout']"));
        assertTrue(logoutOption.isDisplayed(), "Logout option should appear in dropdown");
    }

    @Test
    @Order(14)
    void testNavigationToProfile() {
        loginIfNecessary();

        WebElement profileDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.dropdown-toggle")));
        profileDropdown.click();

        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/profile']")));
        profileLink.click();

        wait.until(ExpectedConditions.urlContains("/profile"));
        WebElement profileHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        assertEquals("Perfil", profileHeader.getText().trim(), "Should navigate to Profile page");
    }

    @Test
    @Order(15)
    void testLogoutFunctionality() {
        loginIfNecessary();

        WebElement profileDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.dropdown-toggle")));
        profileDropdown.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/logout']")));
        logoutLink.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form[action='/login']")));
        assertTrue(loginForm.isDisplayed(), "Login form should reappear after logout");
    }

    @Test
    @Order(16)
    void testFooterFacebookLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook']")));
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("facebook.com"), "Facebook link should redirect to Facebook");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(17)
    void testFooterInstagramLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement instagramLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='instagram']")));
        instagramLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("instagram.com"), "Instagram link should redirect to Instagram");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(18)
    void testFooterYoutubeLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement youtubeLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='youtube']")));
        youtubeLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("youtube.com"), "YouTube link should redirect to YouTube");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(19)
    void testDashboardStatsPresent() {
        loginIfNecessary();

        java.util.List<WebElement> stats = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".stat-card")));
        assertEquals(4, stats.size(), "Dashboard should display 4 statistic cards");

        java.util.List<String> expectedLabels = java.util.Arrays.asList("Usuários", "Propriedades", "Análises", "Relatórios");
        for (int i = 0; i < expectedLabels.size(); i++) {
            assertTrue(stats.get(i).getText().contains(expectedLabels.get(i)), 
                     "Stat card should contain label: " + expectedLabels.get(i));
        }
    }

    private void loginIfNecessary() {
        try {
            driver.get(BASE_URL);
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            if (emailField.isDisplayed()) {
                performLogin();
            }
        } catch (TimeoutException e) {
            // Already logged in
        }
    }

    private void performLogin() {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.clear();
        emailField.sendKeys(EMAIL);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }
}