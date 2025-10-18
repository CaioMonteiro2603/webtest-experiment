package Qwen3.ws02.seq08;

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

public class ParaBankTest {

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
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/parabank/overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("/parabank/overview.htm"));
        assertEquals("ParaBank | Accounts Overview", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Error"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        assertEquals("https://parabank.parasoft.com/parabank/index.htm", driver.getCurrentUrl());

        WebElement aboutLink = driver.findElement(By.linkText("About"));
        aboutLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        String originalWindow = driver.getWindowHandle();
        for (String handle : windowHandles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        WebElement registerLink = driver.findElement(By.linkText("Register"));
        registerLink.click();
        assertTrue(driver.getCurrentUrl().contains("/parabank/register.htm"));
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        assertTrue(driver.getCurrentUrl().contains("/parabank/contact.htm"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement footer = driver.findElement(By.className("footer"));
        List<WebElement> links = footer.findElements(By.tagName("a"));

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < links.size(); i++) {
            WebElement link = links.get(i);
            link.click();

            Set<String> windowHandles = driver.getWindowHandles();
            String newWindow = windowHandles.stream()
                    .filter(w -> !w.equals(originalWindow))
                    .findFirst()
                    .orElse(null);

            if (newWindow != null) {
                driver.switchTo().window(newWindow);
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("parasoft.com") || currentUrl.contains("linkedin.com") || 
                           currentUrl.contains("twitter.com") || currentUrl.contains("facebook.com"));
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(5)
    public void testAccountServices() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement accountsLink = driver.findElement(By.linkText("Accounts"]);
        accountsLink.click();
        assertTrue(driver.getCurrentUrl().contains("/parabank/bank/accounts.htm"));

        WebElement transfersLink = driver.findElement(By.linkText("Transfers"));
        transfersLink.click();
        assertTrue(driver.getCurrentUrl().contains("/parabank/bank/transfers.htm"));
        
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement billsLink = driver.findElement(By.linkText("Bills"));
        billsLink.click();
        assertTrue(driver.getCurrentUrl().contains("/parabank/bank/bills.htm"));

        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        assertTrue(driver.getCurrentUrl().contains("/parabank/bank/services.htm"));
    }

    @Test
    @Order(6)
    public void testLogout() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement logoutLink = driver.findElement(By.linkText("Logout"));
        logoutLink.click();
        assertTrue(driver.getCurrentUrl().contains("/parabank/index.htm"));
        assertEquals("ParaBank | Welcome", driver.getTitle());
    }
}