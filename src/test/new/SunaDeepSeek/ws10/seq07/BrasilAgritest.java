package SunaDeepSeek.ws10.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginPage() {
        driver.get(BASE_URL);
        
        // Verify login page elements
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        
        Assertions.assertTrue(usernameField.isDisplayed(), "Username field should be visible");
        Assertions.assertTrue(passwordField.isDisplayed(), "Password field should be visible");
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        
        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Verify successful login by checking dashboard elements
        WebElement dashboardHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(dashboardHeader.isDisplayed(), "Dashboard should be visible after login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard") || driver.getCurrentUrl().contains("gestao"), "URL should contain 'dashboard' or 'gestao' after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        
        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Erro')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Test menu button
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'menu')]")));
        menuButton.click();
        
        // Verify menu items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Estoque')]")));
        WebElement aboutLink = driver.findElement(By.xpath("//a[contains(text(),'Sobre')]"));
        WebElement logoutLink = driver.findElement(By.xpath("//a[contains(text(),'Sair')]"));
        WebElement resetLink = driver.findElement(By.xpath("//a[contains(text(),'Resetar')]"));
        
        Assertions.assertTrue(allItemsLink.isDisplayed(), "All Items link should be visible");
        Assertions.assertTrue(aboutLink.isDisplayed(), "About link should be visible");
        Assertions.assertTrue(logoutLink.isDisplayed(), "Logout link should be visible");
        Assertions.assertTrue(resetLink.isDisplayed(), "Reset link should be visible");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'menu')]")));
        menuButton.click();
        
        // Test About link (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Sobre')]")));
        aboutLink.click();
        
        // Switch to new tab and verify URL
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"), "About page URL should contain 'about'");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test social media links in footer
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("a[href*='twitter'], a[href*='facebook'], a[href*='linkedin']"));
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href.contains("twitter") || href.contains("facebook") || href.contains("linkedin")) {
                link.click();
                
                // Switch to new tab and verify domain
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.equals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                if (href.contains("twitter")) {
                    Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should be on Twitter domain");
                } else if (href.contains("facebook")) {
                    Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should be on Facebook domain");
                } else if (href.contains("linkedin")) {
                    Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should be on LinkedIn domain");
                }
                
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'menu')]")));
        menuButton.click();
        
        // Click reset link
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Resetar')]")));
        resetLink.click();
        
        // Verify reset confirmation
        WebElement resetMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'confirmado')]")));
        Assertions.assertTrue(resetMessage.isDisplayed(), "Reset confirmation message should be displayed");
    }

    @Test
    @Order(7)
    public void testLogout() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'menu')]")));
        menuButton.click();
        
        // Click logout link
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Sair')]")));
        logoutLink.click();
        
        // Verify we're back on login page
        WebElement loginButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(),'Entrar')]")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be back on login page after logout");
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "URL should contain 'login' after logout");
    }
}