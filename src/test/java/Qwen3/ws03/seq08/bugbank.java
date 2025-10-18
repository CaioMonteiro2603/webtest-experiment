package Qwen3.ws03.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BugBankTest {

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

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        assertEquals("BugBank - Dashboard", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid_email");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Usuário ou senha inválidos"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://bugbank.netlify.app/dashboard");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".menu-button")));
        menuButton.click();

        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        assertEquals("https://bugbank.netlify.app/dashboard", driver.getCurrentUrl());

        WebElement transferLink = driver.findElement(By.linkText("Transferências"));
        transferLink.click();
        assertTrue(driver.getCurrentUrl().contains("/transfer"));
        
        driver.get("https://bugbank.netlify.app/dashboard");
        WebElement extractLink = driver.findElement(By.linkText("Extrato"));
        extractLink.click();
        assertTrue(driver.getCurrentUrl().contains("/extract"));

        driver.get("https://bugbank.netlify.app/dashboard");
        WebElement profileLink = driver.findElement(By.linkText("Perfil"));
        profileLink.click();
        assertTrue(driver.getCurrentUrl().contains("/profile"));

        driver.get("https://bugbank.netlify.app/dashboard");
        WebElement logoutLink = driver.findElement(By.linkText("Sair"));
        logoutLink.click();
        assertTrue(driver.getCurrentUrl().contains("/"));
        assertEquals("BugBank - Login", driver.getTitle());
    }

    @Test
    @Order(4)
    public void testRegisterAndLogout() {
        driver.get("https://bugbank.netlify.app/");
        WebElement registerLink = driver.findElement(By.linkText("Cadastrar"));
        registerLink.click();
        assertTrue(driver.getCurrentUrl().contains("/register"));

        WebElement registerButton = driver.findElement(By.cssSelector("button[type='submit']"));
        registerButton.click();

        WebElement errorElements = driver.findElement(By.cssSelector(".error"));
        assertTrue(errorElements.isDisplayed());

        driver.get("https://bugbank.netlify.app/");
        WebElement loginEmail = driver.findElement(By.id("email"));
        WebElement loginPassword = driver.findElement(By.id("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        loginEmail.sendKeys("caio@gmail.com");
        loginPassword.sendKeys("123");
        submitButton.click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement logout = driver.findElement(By.linkText("Sair"));
        logout.click();
        assertTrue(driver.getCurrentUrl().contains("/"));
        assertEquals("BugBank - Login", driver.getTitle());
    }
    
    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get("https://bugbank.netlify.app/");
        WebElement footer = driver.findElement(By.className("footer"));
        List<WebElement> links = footer.findElements(By.tagName("a"));

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < links.size(); i++) {
            WebElement link = links.get(i);
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                link.click();
                Set<String> windowHandles = driver.getWindowHandles();
                String newWindow = windowHandles.stream()
                        .filter(w -> !w.equals(originalWindow))
                        .findFirst()
                        .orElse(null);

                if (newWindow != null) {
                    driver.switchTo().window(newWindow);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("bugbank.netlify.app") || 
                               currentUrl.contains("github.com") ||
                               currentUrl.contains("linkedin.com") ||
                               currentUrl.contains("twitter.com"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}