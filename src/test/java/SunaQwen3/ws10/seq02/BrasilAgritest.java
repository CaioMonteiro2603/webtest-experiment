package SunaQwen3.ws10.seq02;

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
public class SiteTest {
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
        driver.findElement(By.name("email")).sendKeys(LOGIN);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Entrar')]")));
        loginButton.click();

        // Assert successful login by checking URL or presence of a dashboard element
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login");
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Dashboard"), "Dashboard page should contain 'Dashboard' text");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.name("email")).sendKeys("invalid@user.com");
        driver.findElement(By.name("password")).sendKeys("wrongpassword");
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Entrar')]")));
        loginButton.click();

        // Assert error message appears
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        assertNotNull(errorMessage, "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        // Ensure we're logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        // Open menu (assuming burger button exists)
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        // Click All Items (if applicable)
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Produtos")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/products"));
        assertTrue(driver.getCurrentUrl().contains("/products"), "Should navigate to products page");

        // Navigate back to dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        // Click About (external link)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert external URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"), "About link should open brasilagritest.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado")));
        resetLink.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should return to dashboard after reset");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should return to login page after logout");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);

        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("footer a[href*='twitter.com']"));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        // Switch to new tab
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("footer a[href*='facebook.com']"));
        facebookLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open facebook.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("footer a[href*='linkedin.com']"));
        linkedinLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        // Log in if not already
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        // Navigate to products page
        driver.get("https://gestao.brasilagritest.com/products");

        // Find sorting dropdown
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("sort")));
        
        // Test each option
        String[] sortOptions = {"name", "price", "date"};
        for (String option : sortOptions) {
            sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("sort")));
            sortDropdown.click();
            WebElement optionElement = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[name='sort'] option[value='" + option + "']")));
            optionElement.click();

            // Assert URL or content changes accordingly
            assertTrue(driver.getCurrentUrl().contains("/products"), "Should remain on products page after sorting");
        }
    }
}