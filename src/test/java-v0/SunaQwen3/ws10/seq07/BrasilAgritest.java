package SunaQwen3.ws10.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
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
    public void testValidLogin() {
        driver.get(BASE_URL);
        driver.manage().window().maximize();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Assert successful login by checking URL or presence of a dashboard element
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "URL should contain /dashboard after login");

        // Verify dashboard has a specific element (e.g., title)
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1, .page-title")));
        assertNotNull(pageTitle, "Page title should be present after login");
        assertTrue(pageTitle.getText().toLowerCase().contains("dashboard") || pageTitle.getText().toLowerCase().contains("bem-vindo"),
                "Page title should indicate dashboard or welcome");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger, .error-message")));
        assertNotNull(errorMessage, "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().toLowerCase().contains("credenciais") || errorMessage.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigationAndResetAppState() {
        // Ensure we're logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        // Locate and click the menu (burger) button
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler, .menu-toggle")));
        menuButton.click();

        // Click All Items (assuming it navigates to inventory)
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();

        // Assert navigation to inventory page
        wait.until(ExpectedConditions.urlContains("/itens"));
        assertTrue(driver.getCurrentUrl().contains("/itens"), "Should navigate to /itens after clicking 'Todos os Itens'");

        // Click Reset App State
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado do App")));
        resetButton.click();

        // Confirm reset (if confirmation dialog appears, handle it)
        try {
            WebElement confirmButton = driver.findElement(By.cssSelector("button.confirm, button.btn-primary"));
            if (confirmButton.isDisplayed()) {
                confirmButton.click();
            }
        } catch (NoSuchElementException e) {
            // No confirmation dialog, proceed
        }

        // Wait for reset confirmation or page reload
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success, .toast-message")));
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter() {
        // Ensure we're on a page with footer
        if (!driver.getCurrentUrl().contains("/itens")) {
            driver.get("https://gestao.brasilagritest.com/itens");
            wait.until(ExpectedConditions.urlContains("/itens"));
        }

        // Find footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter'], footer a[href*='facebook'], footer a[href*='linkedin']"));

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String linkText = link.getText().toLowerCase();

            // Open in new tab using JavaScript to avoid blocking
            ((JavascriptExecutor) driver)
                    .executeScript("window.open(arguments[0], '_blank');", href);

            // Wait until a new window/tab is opened
            wait.until(d -> d.getWindowHandles().size() > 1);


            // Switch to new tab
            String originalHandle = driver.getWindowHandle();
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            if (linkText.contains("twitter") || href.contains("twitter")) {
                assertTrue(currentUrl.contains("twitter.com"), "Twitter link should open twitter.com");
            } else if (linkText.contains("facebook") || href.contains("facebook")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open facebook.com");
            } else if (linkText.contains("linkedin") || href.contains("linkedin")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open linkedin.com");
            }

            // Close the tab and switch back
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(5)
    public void testSortingDropdownFunctionality() {
        // Navigate to items page
        driver.get("https://gestao.brasilagritest.com/itens");
        wait.until(ExpectedConditions.urlContains("/itens"));

        // Locate sorting dropdown
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("sort")));

        // Get initial list of items
        List<WebElement> initialItems = driver.findElements(By.cssSelector(".item-name, .product-title"));
        assertTrue(initialItems.size() > 0, "At least one item should be present");

        // Test each sorting option
        Select select = new Select(sortDropdown);
        List<WebElement> options = select.getOptions();

        for (WebElement option : options) {
            String optionValue = option.getAttribute("value");
            select.selectByValue(optionValue);

            // Wait for items to reload
            wait.until(ExpectedConditions.stalenessOf(initialItems.get(0)));
            List<WebElement> sortedItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".item-name, .product-title")));

            // Assert that list is updated (basic check)
            assertNotEquals(initialItems.get(0).getText(), sortedItems.get(0).getText(),
                    "First item should change after sorting with option: " + optionValue);
        }
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        // Click menu to reveal logout
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Assert redirected to login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");

        // Verify login form is present
        assertTrue(driver.findElements(By.name("email")).size() > 0, "Login form should be visible after logout");
    }
}