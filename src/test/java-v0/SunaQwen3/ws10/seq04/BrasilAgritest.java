package SunaQwen3.ws10.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
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
    void testValidLogin() {
        driver.get(BASE_URL);
        driver.manage().window().maximize();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys(LOGIN);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        // Assert successful login by checking URL or presence of a dashboard element
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "URL should contain /dashboard after login");

        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Dashboard')]")));
        assertTrue(dashboardHeader.isDisplayed(), "Dashboard header should be displayed");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys("invalid@user.com");

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        // Assert error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'alert-danger')]")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        // Ensure we're logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        // Open menu (hamburger button)
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'navbar-toggler')]")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/items"));
        assertTrue(driver.getCurrentUrl().contains("/items"), "URL should contain /items after clicking All Items");

        // Navigate back to dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'navbar-toggler')]")));
        menuButton.click();

        // Click Reset App State
        WebElement resetStateLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado do App")));
        resetStateLink.click();

        // Confirm reset if modal appears
        try {
            WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Confirmar')]")));
            confirmButton.click();
            wait.until(ExpectedConditions.invisibilityOf(confirmButton));
        } catch (TimeoutException e) {
            // No confirmation modal, proceed
        }

        // Assert some reset behavior (e.g., filters cleared, default view)
        WebElement defaultView = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Visão Geral')]")));
        assertTrue(defaultView.isDisplayed(), "Default view should be restored after reset");
    }

    @Test
    @Order(4)
    void testExternalAboutLinkInMenu() {
        // Ensure we're on dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'navbar-toggler')]")));
        menuButton.click();

        // Click About (assumed external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        aboutLink.click();

        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.stream().findFirst().orElse(null);
        });
        assertNotNull(newHandle, "New tab should open for About link");
        driver.switchTo().window(newHandle);

        // Assert URL contains expected domain (e.g., company site)
        wait.until(d -> d.getCurrentUrl().contains("brasilagritest.com"));
        assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"), "About link should open brasilagritest.com domain");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);

        // Assert back on original page
        assertEquals("https://gestao.brasilagritest.com/dashboard", driver.getCurrentUrl(), "Should return to dashboard after closing About tab");
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Find all footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter.com'], footer a[href*='facebook.com'], footer a[href*='linkedin.com']"));

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String domain;

            if (href.contains("twitter.com")) {
                domain = "twitter.com";
            } else if (href.contains("facebook.com")) {
                domain = "facebook.com";
            } else if (href.contains("linkedin.com")) {
                domain = "linkedin.com";
            } else {
                continue;
            }

            // Open link in new tab via JavaScript to avoid interception
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);

            // Switch to new tab
            String newHandle = wait.until(d -> {
                Set<String> handles = d.getWindowHandles();
                handles.remove(originalHandle);
                return handles.stream().findFirst().orElse(null);
            });
            driver.switchTo().window(newHandle);

            // Assert URL contains expected domain
            wait.until(d -> d.getCurrentUrl().contains(domain));
            assertTrue(driver.getCurrentUrl().contains(domain), "Social link should open " + domain);

            // Close tab
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(6)
    void testLogoutFunctionality() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'navbar-toggler')]")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Assert redirected to login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");

        // Assert login form is present
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("form")));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }
}