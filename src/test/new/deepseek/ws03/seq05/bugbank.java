package deepseek.ws03.seq05;

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

@TestMethodOrder(OrderAnnotation.class)
public class bugbank  {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
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
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Acessar')]"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home")));
        Assertions.assertTrue(driver.findElement(By.xpath("//p[contains(text(),'Saldo em conta')]")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Acessar')]"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'Usuário ou senha inválido')]")));
        Assertions.assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"));
    }

    @Test
    @Order(3)
    public void testMoneyTransfer() {
        testValidLogin();
        
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Transferência')]")));
        transferButton.click();

        WebElement accountNumberField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='accountNumber']")));
        WebElement transferValueField = driver.findElement(By.cssSelector("input[name='transferValue']"));
        WebElement descriptionField = driver.findElement(By.cssSelector("input[name='description']"));
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(),'Transferir agora')]"));

        accountNumberField.sendKeys("1234");
        transferValueField.sendKeys("100");
        descriptionField.sendKeys("Test transfer");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'Transferência realizada com sucesso')]")));
        Assertions.assertTrue(successMessage.getText().contains("Transferência realizada com sucesso"));
    }

    @Test
    @Order(4)
    public void testAccountStatement() {
        testValidLogin();
        
        WebElement statementButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Extrato')]")));
        statementButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".bank-statement-container")));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".bank-statement-container")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testLogout() {
        testValidLogin();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Sair')]")));
        logoutButton.click();

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("input[type='email']")).isDisplayed());
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'github')]")));
        githubLink.click();
        switchToNewWindowAndAssertDomain("github.com", originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'linkedin')]")));
        linkedinLink.click();
        switchToNewWindowAndAssertDomain("linkedin.com", originalWindow);
    }

    private void switchToNewWindowAndAssertDomain(String domain, String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(domain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}