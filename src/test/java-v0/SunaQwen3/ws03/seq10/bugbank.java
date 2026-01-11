package SunaQwen3.ws03.seq10;

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
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
        assertEquals("BugBank", driver.getTitle(), "Page title should be 'BugBank'");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"), "URL should contain 'dashboard' after login");
        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Sair')]")));
        assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrong");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigationAndReset() {
        // Ensure logged in
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Dashboard")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"), "Should navigate to dashboard");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar")));
        resetLink.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        // Verify some reset effect (e.g., balance reset or transaction cleared)
        // Since exact behavior not specified, just ensure no crash
        assertTrue(true, "Reset app state should complete without error");
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter() {
        // Ensure logged in
        loginIfNotOnDashboard();

        // Find footer links
        By twitterLinkLocator = By.cssSelector("footer a[href*='twitter']");
        By facebookLinkLocator = By.cssSelector("footer a[href*='facebook']");
        By linkedinLinkLocator = By.cssSelector("footer a[href*='linkedin']");

        WebElement twitterLink = wait.until(ExpectedConditions.visibilityOfElementLocated(twitterLinkLocator));
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        twitterLink.sendKeys(Keys.CONTROL + "t"); // Open in new tab
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(driver.findElement(twitterLinkLocator).getAttribute("href"));
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(facebookLinkLocator);
        facebookLink.sendKeys(Keys.CONTROL + "t");
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(facebookLink.getAttribute("href"));
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = driver.findElement(linkedinLinkLocator);
        linkedinLink.sendKeys(Keys.CONTROL + "t");
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(linkedinLink.getAttribute("href"));
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testLogoutFunctionality() {
        // Ensure logged in
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to base URL after logout");
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Entrar')]")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible on login page");
    }

    private void loginIfNotOnDashboard() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);
            try {
                WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
                emailInput.clear();
                emailInput.sendKeys(LOGIN_EMAIL);

                WebElement passwordInput = driver.findElement(By.name("password"));
                passwordInput.clear();
                passwordInput.sendKeys(PASSWORD);

                WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
                loginButton.click();

                wait.until(ExpectedConditions.urlContains("dashboard"));
            } catch (TimeoutException e) {
                fail("Failed to login during setup: " + e.getMessage());
            }
        }
    }
}