package SunaDeepSeek.ws03.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    public static void setup() {
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("BugBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("netlify.app"));
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email' or @id='email' or @name='email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' or @id='password' or @name='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' or contains(@class,'login') or text()='Entrar']"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Bem vindo') or contains(@class,'welcome') or contains(@class,'bemvindo')]")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Bem vindo"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email' or @id='email' or @name='email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' or @id='password' or @name='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' or contains(@class,'login') or text()='Entrar']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'usuário') or contains(text(),'senha') or contains(text(),'inválido') or contains(@class,'error') or contains(@class,'alert-danger')]")));
        String msg = errorMessage.getText().toLowerCase();
        Assertions.assertTrue(msg.contains("usuário") || msg.contains("senha") || msg.contains("inválido"));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='menu-button' or contains(@class,'menu') or *[@*='menu']]")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='inventory_sidebar_link' or contains(@href,'inventory') or contains(text(),'Itens')]")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory"));
        
        // Test About (external)
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='about_sidebar_link' or contains(@href,'about') or contains(text(),'Sobre')]")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='logout_sidebar_link' or contains(@href,'logout') or contains(text(),'Sair')]")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("login"));
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        login();
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='menu-button' or contains(@class,'menu')]")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='reset_sidebar_link' or contains(@href,'reset') or contains(text(),'Limpar')]")));
        reset.click();
        
        // Verify reset by checking some initial state
        try {
            WebElement cartBadge = driver.findElement(By.xpath("//*[contains(@class,'shopping_cart_badge') or contains(@class,'cart-badge')]"));
            Assertions.assertEquals("0", cartBadge.getText().trim());
        } catch (NoSuchElementException e) {
            // Badge might be hidden when empty
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(6)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'twitter.com') or contains(@class,'twitter')]")));
        twitterLink.click();
        
        handleExternalLink("twitter.com");
        
        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'facebook.com') or contains(@class,'facebook')]")));
        facebookLink.click();
        
        handleExternalLink("facebook.com");
        
        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'linkedin.com') or contains(@class,'linkedin')]")));
        linkedinLink.click();
        
        handleExternalLink("linkedin.com");
    }

    private void handleExternalLink(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email' or @id='email' or @name='email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password' or @id='password' or @name='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' or contains(@class,'login') or text()='Entrar']"));

        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("home"));
    }
}