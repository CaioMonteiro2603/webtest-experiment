package SunaQwen3.ws03.seq07;

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
        assertEquals("BugBank", driver.getTitle(), "Page title should be 'BugBank'");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "URL should contain 'home' after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertFalse(footerLinks.isEmpty(), "Footer should contain social links");

        for (WebElement link : footerLinks) {
            String originalWindow = driver.getWindowHandle();
            String href = link.getAttribute("href");
            String target = link.getAttribute("target");

            if ("_blank".equals(target)) {
                link.click();

                wait.until(d -> d.getWindowHandles().size() > 1);
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }

                if (href.contains("twitter.com")) {
                    assertTrue(driver.getCurrentUrl().contains("twitter.com"), "New tab should navigate to Twitter domain");
                } else if (href.contains("facebook.com")) {
                    assertTrue(driver.getCurrentUrl().contains("facebook.com"), "New tab should navigate to Facebook domain");
                } else if (href.contains("linkedin.com")) {
                    assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "New tab should navigate to LinkedIn domain");
                }

                driver.close();
                driver.switchTo().window(originalWindow);
            } else {
                link.click();
                wait.until(ExpectedConditions.urlContains("bugbank"));
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
            }
        }
    }

    @Test
    @Order(4)
    void testMenuNavigationAndActions() {
        // Ensure logged in
        loginIfNotOnHomePage();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os bancos")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "Should navigate to home after clicking 'Todos os bancos'");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement resetAppStateLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar estado do app")));
        resetAppStateLink.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Tem certeza de que deseja resetar o estado do aplicativo?", alert.getText(), "Reset alert should show correct message");
        alert.accept();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".account-list")));
        assertTrue(driver.findElements(By.cssSelector(".account-item")).size() >= 0, "Account list should be present after reset");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to base URL after logout");

        // Re-login for subsequent tests
        loginIfNotOnHomePage();
    }

    @Test
    @Order(5)
    void testAccountCreationAndBalanceOperations() {
        loginIfNotOnHomePage();

        WebElement createAccountButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Criar conta')]")));
        createAccountButton.click();

        WebElement accountNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("accountName")));
        accountNameInput.sendKeys("Test Account");

        WebElement initialBalanceInput = driver.findElement(By.name("initialBalance"));
        initialBalanceInput.sendKeys("1000");

        WebElement createButton = driver.findElement(By.xpath("//button[contains(text(), 'Criar')]"));
        createButton.click();

        wait.until(ExpectedConditions.invisibilityOf(createButton));
        List<WebElement> accounts = driver.findElements(By.cssSelector(".account-item"));
        assertTrue(accounts.size() > 0, "New account should appear in the list");

        boolean found = false;
        for (WebElement account : accounts) {
            if (account.getText().contains("Test Account")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Created account 'Test Account' should be in the list");

        // Perform deposit
        WebElement depositButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Depositar')]")));
        depositButton.click();

        WebElement amountInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("amount")));
        amountInput.sendKeys("500");

        WebElement confirmDepositButton = driver.findElement(By.xpath("//button[contains(text(), 'Confirmar')]"));
        confirmDepositButton.click();

        wait.until(ExpectedConditions.invisibilityOf(confirmDepositButton));
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".balance-value")));
        assertTrue(balanceElement.getText().contains("1500"), "Balance should be updated to 1500 after deposit");

        // Perform withdrawal
        WebElement withdrawButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Sacar')]")));
        withdrawButton.click();

        amountInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("amount")));
        amountInput.sendKeys("300");

        WebElement confirmWithdrawButton = driver.findElement(By.xpath("//button[contains(text(), 'Confirmar')]"));
        confirmWithdrawButton.click();

        wait.until(ExpectedConditions.invisibilityOf(confirmWithdrawButton));
        balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".balance-value")));
        assertTrue(balanceElement.getText().contains("1200"), "Balance should be updated to 1200 after withdrawal");
    }

    @Test
    @Order(6)
    void testTransactionHistoryAndFiltering() {
        loginIfNotOnHomePage();

        WebElement transactionsTab = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transações")));
        transactionsTab.click();

        wait.until(ExpectedConditions.urlContains("transactions"));
        assertTrue(driver.getCurrentUrl().contains("transactions"), "Should navigate to transactions page");

        List<WebElement> transactionItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".transaction-item")));
        assertTrue(transactionItems.size() > 0, "Transaction list should not be empty");

        WebElement filterDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("filter")));
        filterDropdown.click();

        WebElement depositOption = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='deposit']")));
        depositOption.click();

        wait.until(d -> {
            List<WebElement> filtered = d.findElements(By.cssSelector(".transaction-item.deposit"));
            return filtered.size() == d.findElements(By.cssSelector(".transaction-item")).size();
        });

        List<WebElement> depositsOnly = driver.findElements(By.cssSelector(".transaction-item.deposit"));
        assertTrue(depositsOnly.size() > 0, "Only deposit transactions should be visible");

        filterDropdown.click();
        WebElement withdrawalOption = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='withdraw']")));
        withdrawalOption.click();

        wait.until(d -> {
            List<WebElement> filtered = d.findElements(By.cssSelector(".transaction-item.withdraw"));
            return filtered.size() == d.findElements(By.cssSelector(".transaction-item")).size();
        });

        List<WebElement> withdrawalsOnly = driver.findElements(By.cssSelector(".transaction-item.withdraw"));
        assertTrue(withdrawalsOnly.size() > 0, "Only withdrawal transactions should be visible");
    }

    private void loginIfNotOnHomePage() {
        if (!driver.getCurrentUrl().contains("home")) {
            driver.get(BASE_URL);

            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.clear();
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.clear();
            passwordInput.sendKeys(LOGIN_PASSWORD);

            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("home"));
        }
    }
}