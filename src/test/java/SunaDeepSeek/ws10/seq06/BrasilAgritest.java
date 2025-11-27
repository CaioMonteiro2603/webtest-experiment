package SunaDeepSeek.ws10.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgriTestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard"), "Login failed - not redirected to dashboard");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        // First login
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        // Test menu items
        testMenuItem("Dashboard", "dashboard");
        testMenuItem("Users", "users");
        testMenuItem("Settings", "settings");
        
        // Logout test
        menuButton.click();
        WebElement logoutItem = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Logout')]")));
        logoutItem.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Logout failed - not redirected to login page");
    }

    private void testMenuItem(String itemText, String expectedUrlPart) {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        WebElement menuItem = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'" + itemText + "')]")));
        menuItem.click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart), 
            "Navigation to " + itemText + " failed");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        testSuccessfulLogin();
        
        // Test social media links in footer
        testExternalLink("Twitter", "twitter.com");
        testExternalLink("Facebook", "facebook.com");
        testExternalLink("LinkedIn", "linkedin.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.partialLinkText(linkText));
        if (links.size() > 0) {
            String originalWindow = driver.getWindowHandle();
            
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText(linkText)));
            link.click();
            
            // Switch to new window
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            // Verify domain and close
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                "External link " + linkText + " did not go to expected domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    public void testDataSorting() {
        testSuccessfulLogin();
        
        // Navigate to a page with sortable data (assuming users page has this)
        driver.get(BASE_URL.replace("login", "users"));
        
        // Test each sorting option
        testSortOption("Name", "name");
        testSortOption("Email", "email");
        testSortOption("Role", "role");
    }

    private void testSortOption(String optionText, String sortField) {
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select.form-select")));
        sortDropdown.click();
        
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//option[contains(text(),'" + optionText + "')]")));
        option.click();
        
        // Verify URL contains sort parameter
        wait.until(ExpectedConditions.urlContains("sort=" + sortField));
        Assertions.assertTrue(driver.getCurrentUrl().contains("sort=" + sortField), 
            "Sorting by " + optionText + " failed");
        
        // Add more specific assertions based on actual page content if needed
    }
}