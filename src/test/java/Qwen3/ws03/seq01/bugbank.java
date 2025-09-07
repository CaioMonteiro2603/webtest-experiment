package Qwen3.ws03.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String DASHBOARD_URL = "https://bugbank.netlify.app/home";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

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
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Entrar')]"))).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@data-testid='email']"))).sendKeys(LOGIN_EMAIL);
        driver.findElement(By.xpath("//input[@data-testid='password']")).sendKeys(LOGIN_PASSWORD);
        driver.findElement(By.xpath("//button[@data-testid='entrar']")).click();

        wait.until(ExpectedConditions.urlToBe(DASHBOARD_URL));
        assertTrue(driver.findElement(By.xpath("//button[@data-testid='btn-movimentacao']")).isDisplayed(),
                "Transfer button should be visible after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Entrar')]"))).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@data-testid='email']"))).sendKeys("invalid_user@gmail.com");
        driver.findElement(By.xpath("//input[@data-testid='password']")).sendKeys("wrong_pass");
        driver.findElement(By.xpath("//button[@data-testid='entrar']")).click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[@data-testid='modalText']")));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid login.");
        assertTrue(errorElement.getText().contains(" Inválido!"), "Error message text is incorrect.");
        
        driver.findElement(By.xpath("//button[@data-testid='btnCloseModal']")).click();
        wait.until(ExpectedConditions.invisibilityOf(errorElement));
    }

    @Test
    @Order(3)
    public void testNavigationToTransferPage() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.xpath("//button[@data-testid='btn-movimentacao']")).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));

        assertTrue(driver.findElement(By.xpath("//p[contains(text(), 'Preencha os dados da transferência')]")).isDisplayed(),
                "Transfer page header should be visible.");
    }

    @Test
    @Order(4)
    public void testNavigationToExtractPage() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.xpath("//button[@data-testid='btn-extrato']")).click();
        wait.until(ExpectedConditions.urlContains("/extract"));

        assertTrue(driver.findElement(By.xpath("//th[contains(text(), 'Saldo')]")).isDisplayed(),
                "Extract page balance header should be visible.");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Click Facebook link
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='facebook']")));
        fbLink.click();
        assertExternalLinkAndReturn(originalWindow, "facebook.com");

        // Click Twitter link
        driver.get(BASE_URL); // Reset to base URL
        originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='twitter']")));
        twitterLink.click();
        assertExternalLinkAndReturn(originalWindow, "twitter.com");

        // Click Instagram link
        driver.get(BASE_URL); // Reset to base URL
        originalWindow = driver.getWindowHandle();
        WebElement instaLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='instagram']")));
        instaLink.click();
        assertExternalLinkAndReturn(originalWindow, "instagram.com");
    }

    @Test
    @Order(6)
    public void testLogout() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.xpath("//button[@data-testid='btn-settings']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-testid='btn-sair']"))).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]")).isDisplayed(),
                "Should be back on home page after logout.");
    }

    // --- Helper Methods ---

    private void assertExternalLinkAndReturn(String originalWindow, String expectedDomain) {
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        assertNotNull(newWindow, "A new window should have been opened for " + expectedDomain);
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "New window URL should contain " + expectedDomain + ". URL was: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}