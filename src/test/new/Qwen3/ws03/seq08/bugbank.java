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

public class bugbank {

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
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email'], input[name*='email'], input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha'], input[name*='password'], input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Entrar'), button:contains('Acessar')"));

        emailField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email'], input[name*='email'], input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha'], input[name*='password'], input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Entrar'), button:contains('Acessar')"));

        emailField.sendKeys("invalid_email");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error, .alert, [role='alert']")));
        assertTrue(errorElement.isDisplayed());
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email'], input[name*='email'], input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder*='senha'], input[name*='password'], input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Entrar'), button:contains('Acessar')"));

        emailField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label*='menu'], .menu-button, button:contains('Menu'), nav button")));
        menuButton.click();

        WebElement dashboardLink = driver.findElement(By.xpath("//a[contains(text(),'Dashboard') or contains(@href,'dashboard')]"));
        dashboardLink.click();
        assertEquals("https://bugbank.netlify.app/dashboard", driver.getCurrentUrl());

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label*='menu'], .menu-button, button:contains('Menu'), nav button")));
        menuButton.click();
        WebElement transferLink = driver.findElement(By.xpath("//a[contains(text(),'Transferência') or contains(@href,'transfer')]"));
        transferLink.click();
        assertTrue(driver.getCurrentUrl().contains("/transfer"));
        
        driver.get("https://bugbank.netlify.app/dashboard");
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label*='menu'], .menu-button, button:contains('Menu'), nav button")));
        menuButton.click();
        WebElement extractLink = driver.findElement(By.xpath("//a[contains(text(),'Extrato') or contains(@href,'extract')]"));
        extractLink.click();
        assertTrue(driver.getCurrentUrl().contains("/extract"));

        driver.get("https://bugbank.netlify.app/dashboard");
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label*='menu'], .menu-button, button:contains('Menu'), nav button")));
        menuButton.click();
        WebElement profileLink = driver.findElement(By.xpath("//a[contains(text(),'Perfil') or contains(@href,'profile')]"));
        profileLink.click();
        assertTrue(driver.getCurrentUrl().contains("/profile"));

        driver.get("https://bugbank.netlify.app/dashboard");
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label*='menu'], .menu-button, button:contains('Menu'), nav button")));
        menuButton.click();
        WebElement logoutLink = driver.findElement(By.xpath("//a[contains(text(),'Sair') or contains(@href,'logout') or contains(@data-testid,'logout')]"));
        logoutLink.click();
        assertTrue(driver.getCurrentUrl().contains("/"));
    }

    @Test
    @Order(4)
    public void testRegisterAndLogout() {
        driver.get("https://bugbank.netlify.app/");
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Cadastrar') or contains(@href,'register') or contains(text(),'Registrar')]")));
        registerLink.click();
        assertTrue(driver.getCurrentUrl().contains("/register"));

        WebElement registerButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Cadastrar'), button:contains('Registrar')"));
        registerButton.click();

        WebElement errorElements = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error, .alert, [role='alert']")));
        assertTrue(errorElements.isDisplayed());

        driver.get("https://bugbank.netlify.app/");
        WebElement loginEmail = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder*='email'], input[name*='email'], input[type='email']")));
        WebElement loginPassword = driver.findElement(By.cssSelector("input[placeholder*='senha'], input[name*='password'], input[type='password']"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Entrar'), button:contains('Acessar')"));

        loginEmail.sendKeys("caio@gmail.com");
        loginPassword.sendKeys("123");
        submitButton.click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label*='menu'], .menu-button, button:contains('Menu'), nav button")));
        menuButton.click();
        WebElement logout = driver.findElement(By.xpath("//a[contains(text(),'Sair') or contains(@href,'logout') or contains(@data-testid,'logout')]"));
        logout.click();
        assertTrue(driver.getCurrentUrl().contains("/"));
    }
    
    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get("https://bugbank.netlify.app/");
        WebElement footer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("footer, .footer, [role='contentinfo']")));
        List<WebElement> links = footer.findElements(By.tagName("a"));

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < links.size(); i++) {
            WebElement link = links.get(i);
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
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