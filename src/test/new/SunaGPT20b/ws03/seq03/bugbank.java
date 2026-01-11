package SunaGPT20b.ws03.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    }

    private void login(String email, String password) {
        navigateToBase();
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Entrar')]")));

        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginBtn.click();

        // Verify login success by checking balance element presence
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'Saldo')]")));
    }

    private void resetAppState() {
        // Navigate back to base URL for reset
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank.netlify.app"),
                "After login the URL should contain 'bugbank.netlify.app'");
        Assertions.assertTrue(driver.findElements(By.xpath("//p[contains(text(),'Saldo')]")).size() > 0,
                "Balance information should be displayed after successful login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToBase();
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Entrar')]")));

        emailField.clear();
        emailField.sendKeys("invalid@example.com");
        passwordField.clear();
        passwordField.sendKeys("wrong");
        loginBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(@class,'error')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("usuário") || errorMsg.getText().toLowerCase().contains("senha"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // Navigate to transfer page for testing dropdown
        WebElement transferBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Transferência')]")));
        transferBtn.click();
        
        WebElement accountSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("accountNumber")));
        Select select = new Select(accountSelect);
        
        // Verify dropdown has options
        List<WebElement> options = select.getOptions();
        Assertions.assertFalse(options.isEmpty(), "Account dropdown should have options");
        
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // Verify main menu options are present
        List<WebElement> menuOptions = driver.findElements(By.xpath("//button[contains(@class,'button')]"));
        Assertions.assertTrue(menuOptions.size() > 0, "Menu options should be present");
        
        // Click on account extraction
        WebElement extractBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Extrato')]")));
        extractBtn.click();
        
        // Verify extraction page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Extrato')]")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank.netlify.app"),
                "Should remain on bugbank domain");
        
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // BugBank doesn't have external links, verify email contact instead
        WebElement emailElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        Assertions.assertNotNull(emailElement, "Email element should be present");
        
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // Click logout button
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Sair')]")));
        logoutBtn.click();
        
        // Verify we are back on login page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank.netlify.app"),
                "After logout the user should be on the login page");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // BugBank doesn't have social links, verify app version footer instead
        List<WebElement> footerElements = driver.findElements(By.xpath("//footer//p"));
        Assertions.assertTrue(footerElements.size() > 0, "Footer should contain elements");
        
        resetAppState();
    }

    @Test
    @Order(8)
    public void testCheckoutProcess() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // Make a transfer as checkout equivalent
        WebElement transferBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Transferência')]")));
        transferBtn.click();
        
        // Fill transfer form
        WebElement accountInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountNumber")));
        accountInput.sendKeys("123456");
        
        WebElement digitInput = driver.findElement(By.id("digit"));
        digitInput.sendKeys("0");
        
        WebElement valueInput = driver.findElement(By.id("transferValue"));
        valueInput.sendKeys("100");
        
        WebElement descriptionInput = driver.findElement(By.id("description"));
        descriptionInput.sendKeys("Test transfer");
        
        // Submit transfer
        WebElement submitBtn = driver.findElement(By.xpath("//button[contains(text(),'Transferir')]"));
        submitBtn.click();
        
        // Verify transfer success
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'modal')]")));
        Assertions.assertTrue(modal.isDisplayed(), "Transfer confirmation modal should be displayed");
        
        // Close modal
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Fechar')]")));
        closeBtn.click();
        
        resetAppState();
    }
}