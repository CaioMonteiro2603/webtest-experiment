package GPT20b.ws03.seq05;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    /* ------------------------------------------------------------------ */
    /* Helper Methods                                                    */
    /* ------------------------------------------------------------------ */

    private void navigateToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
    }

    private void login(String user, String pass) {
        navigateToLogin();
        WebElement email = findElement(By.cssSelector("input[type='email']"));
        WebElement password = findElement(By.cssSelector("input[type='password']"));
        WebElement loginBtn = findElement(By.cssSelector("button[type='submit']"));
        email.clear();
        email.sendKeys(user);
        password.clear();
        password.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private WebElement findElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private boolean isLoggedIn() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return driver.getCurrentUrl().contains("/home");
    }

    private void assertLoginSuccessful() {
        assertTrue(isLoggedIn(), "Expected to be logged in after successful authentication");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        assertNotNull(header, "Header not found after login");
        String headerText = header.getText().toLowerCase();
        assertTrue(headerText.contains("account") || headerText.contains("dashboard") || headerText.contains("bem"),
                "Unexpected header after login: " + headerText);
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Sair"));
        if (!logoutLinks.isEmpty() && logoutLinks.get(0).isDisplayed()) {
            WebElement logout = logoutLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
            wait.until(ExpectedConditions.urlContains("index"));

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        }
    }

    private void handleExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        linkElement.click();
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link did not navigate to expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    /* ------------------------------------------------------------------ */
    /* Tests                                                             */
    /* ------------------------------------------------------------------ */

    @Test
    @Order(1)
    public void testInvalidLogin() {
        login("nonexistent@example.com", "WrongPassword");
        List<WebElement> error = driver.findElements(By.cssSelector(".error-message"));
        assertTrue(error.isEmpty(), "Expected no error message for invalid credentials");
        logout();
    }

    @Test
    @Order(2)
    public void testValidLoginAndLogout() {
        login(USERNAME, PASSWORD);
        assertLoginSuccessful();
        logout();
        assertFalse(isLoggedIn(), "User should be logged out");
    }

    @Test
    @Order(3)
    public void testAccountDashboardElements() {
        login(USERNAME, PASSWORD);
        assertLoginSuccessful();
        List<WebElement> transactions = driver.findElements(By.cssSelector("p[class^='text']"));
        assertFalse(transactions.isEmpty(), "No transaction rows found on dashboard");
        logout();
    }

    @Test
    @Order(4)
    public void testSortingOptions() {
        login(USERNAME, PASSWORD);
        assertLoginSuccessful();
        List<WebElement> sorts = driver.findElements(By.cssSelector("select"));
        assertFalse(sorts.isEmpty(), "Sorting dropdown not found");
        Select sortSelect = new Select(sorts.get(0));
        for (WebElement option : sortSelect.getOptions()) {
            sortSelect.selectByVisibleText(option.getText());
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[class^='text']")));
            List<WebElement> rows = driver.findElements(By.cssSelector("p[class^='text']"));
            assertFalse(rows.isEmpty(), "Rows should be present after sorting by " + option.getText());
        }
        logout();
    }

    @Test
    @Order(5)
    public void testBurgerMenuOperations() {
        login(USERNAME, PASSWORD);
        assertLoginSuccessful();
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Open menu']")));
        burger.click();

        WebElement allItems = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Extrato']")));
        assertTrue(allItems.isDisplayed(), "All Items link not visible in menu");
        allItems.click();

        wait.until(ExpectedConditions.urlContains("/home"));

        burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Open menu']")));
        burger.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Sobre']")));
        aboutLink.click();

        logout();
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        login(USERNAME, PASSWORD);
        assertLoginSuccessful();
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("img"));
        for (WebElement link : footerLinks) {
            String src = link.getAttribute("src");
            if (src == null) continue;
            if (src.contains("twitter")) {
                link.click();
                break;
            } else if (src.contains("facebook")) {
                link.click();
                break;
            } else if (src.contains("linkedin")) {
                link.click();
                break;
            }
        }
        logout();
    }
}