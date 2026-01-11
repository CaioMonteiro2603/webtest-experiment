package SunaQwen3.ws03.seq03;

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
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

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
        assertEquals("BugBank | O banco com bugs e falhas do seu jeito", driver.getTitle(), "Page title should be 'BugBank'");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
        loginButton.click();

        // Wait for navigation or element that indicates login success
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "URL should contain 'home' after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginError() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".input__warging")));
        assertNotNull(errorMessage, "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        // Ensure we're logged in
        loginIfNotOnHomePage();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".home__ContainerTitle-sc-1au8bn7-2")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os itens")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "Should navigate to home after clicking 'Todos os itens'");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".home__ContainerTitle-sc-1au8bn7-2")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar estado do app")));
        resetLink.click();

        // Confirm reset (if modal appears)
        try {
            WebElement confirmButton = driver.findElement(By.xpath("//button[contains(text(), 'Resetar')]"));
            if (confirmButton.isDisplayed()) {
                confirmButton.click();
            }
        } catch (NoSuchElementException ignored) {
            // No confirmation modal
        }

        // Wait to ensure reset completed
        wait.until(ExpectedConditions.stalenessOf(menuButton));
    }

    @Test
    @Order(4)
    void testLogoutFunctionality() {
        loginIfNotOnHomePage();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".home__ContainerTitle-sc-1au8bn7-2")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Wait for login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");
        assertTrue(driver.getPageSource().contains("Acessar"), "Login button should be visible on login page");
    }

    @Test
    @Order(5)
    void testExternalLink_About() {
        loginIfNotOnHomePage();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".home__ContainerTitle-sc-1au8bn7-2")));
        menuButton.click();

        // Click About (assumed to be external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("github") || currentUrl.contains("bugbank"), "About link should open GitHub or project page");

        // Close tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testFooterSocialLinks() {
        loginIfNotOnHomePage();

        // Find all footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter'], footer a[href*='facebook'], footer a[href*='linkedin']"));

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            assertTrue(href != null && !href.isEmpty(), "Social link should have href attribute");

            // Open link in new tab via JavaScript to avoid interference
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);

            // Switch to new tab
            wait.until(d -> driver.getWindowHandles().size() > 1);
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            if (href.contains("twitter")) {
                assertTrue(currentUrl.contains("twitter.com") || currentUrl.contains("x.com"), "Twitter link should open correct domain");
            } else if (href.contains("facebook")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (href.contains("linkedin")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            }

            // Close tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    void testAccountCreationFormValidation() {
        driver.get(BASE_URL);

        // Click register link
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Registre-se")));
        registerLink.click();

        // Wait for registration form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));

        // Submit empty form
        WebElement registerButton = driver.findElement(By.xpath("//button[contains(text(), 'Cadastrar')]"));
        registerButton.click();

        // Check for validation messages
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".input__warging"));
        assertTrue(errorMessages.size() >= 2, "Should show at least two validation errors for empty fields");

        boolean emailErrorFound = false;
        boolean passwordErrorFound = false;

        for (WebElement error : errorMessages) {
            String text = error.getText();
            if (text.contains("email") && text.contains("obrigatório")) {
                emailErrorFound = true;
            }
            if (text.contains("senha") && text.contains("obrigatória")) {
                passwordErrorFound = true;
            }
        }

        assertTrue(emailErrorFound, "Should show email required error");
        assertTrue(passwordErrorFound, "Should show password required error");
    }

    @Test
    @Order(8)
    void testSuccessfulAccountCreation() {
        driver.get(BASE_URL);

        // Click register link
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Registre-se")));
        registerLink.click();

        // Fill registration form with valid data
        String newEmail = "testuser_" + System.currentTimeMillis() + "@example.com";

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(newEmail);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("Test@123");

        WebElement confirmPasswordInput = driver.findElement(By.name("passwordConfirmation"));
        confirmPasswordInput.sendKeys("Test@123");

        WebElement registerButton = driver.findElement(By.xpath("//button[contains(text(), 'Cadastrar')]"));
        registerButton.click();

        // Wait for success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal__BtnText-sc-1a2q6s-7")));
        assertNotNull(successMessage, "Success message should be displayed");
        assertTrue(successMessage.getText().contains("sucesso"), "Success message should confirm registration");
    }

    private void loginIfNotOnHomePage() {
        if (!driver.getCurrentUrl().contains("home")) {
            driver.get(BASE_URL);

            // Check if already logged in via session
            try {
                WebElement logoutLink = driver.findElement(By.linkText("Sair"));
                if (logoutLink.isDisplayed()) return;
            } catch (NoSuchElementException ignored) {}

            // Perform login
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.clear();
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.clear();
            passwordInput.sendKeys(LOGIN_PASSWORD);

            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
            loginButton.click();

            // Wait for home
            wait.until(ExpectedConditions.urlContains("home"));
        }
    }
}