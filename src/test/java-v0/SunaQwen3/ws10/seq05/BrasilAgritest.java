package SunaQwen3.ws10.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
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

        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login");

        WebElement pageTitle = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertNotNull(pageTitle, "Dashboard page should have a title");
        assertTrue(pageTitle.getText().toLowerCase().contains("dashboard"), "Page title should contain 'dashboard'");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@user.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
        assertNotNull(errorMessage, "Error message should appear for invalid login");
        assertTrue(errorMessage.getText().toLowerCase().contains("credenciais") || 
                   errorMessage.getText().toLowerCase().contains("invalid") ||
                   errorMessage.getText().toLowerCase().contains("erro"),
                   "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigation() {
        // Ensure we're logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("/items"));
        assertTrue(driver.getCurrentUrl().contains("/items"), "Should navigate to items page");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"), "About link should open brasilagritest.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado")));
        resetButton.click();

        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should return to dashboard after reset");
    }

    @Test
    @Order(4)
    void testFooterSocialLinks() {
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        String originalWindow = driver.getWindowHandle();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter.com'], footer a[href*='facebook.com'], footer a[href*='linkedin.com']"));
        assertFalse(socialLinks.isEmpty(), "Footer should contain social media links");

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String target = link.getAttribute("target");
            assertTrue(target == null || target.isEmpty() || target.equals("_blank"), "Social links should open in new tab");

            ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('target')", link);
            link.click();

            wait.until(d -> d.getWindowHandles().size() > 1);
            String newWindow = driver.getWindowHandles().stream()
                .filter(handle -> !handle.equals(originalWindow))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new window opened"));

            driver.switchTo().window(newWindow);

            if (href.contains("twitter.com")) {
                assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");
            } else if (href.contains("facebook.com")) {
                assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
            } else if (href.contains("linkedin.com")) {
                assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
            }

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    void testLogoutFunctionality() {
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testValidLogin();
        }

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");

        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        assertNotNull(loginForm, "Login form should be present after logout");
    }
}