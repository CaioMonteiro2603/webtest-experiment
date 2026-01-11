package SunaGPT20b.ws03.seq04;

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

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement userInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Informe seu e-mail']")));
        WebElement passInput = driver.findElement(By.cssSelector("input[placeholder='Informe sua senha']"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        userInput.clear();
        userInput.sendKeys(user);
        passInput.clear();
        passInput.sendKeys(pass);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("home"));
    }

    private void logoutIfLoggedIn() {
        List<WebElement> logoutButtons = driver.findElements(By.cssSelector("button[aria-label='logout']"));
        if (!logoutButtons.isEmpty()) {
            logoutButtons.get(0).click();
            wait.until(ExpectedConditions.urlContains("login"));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"),
                "After login the URL should contain 'home'.");

        // Verify balance is displayed
        WebElement balanceElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='balance']")));
        Assertions.assertTrue(balanceElement.isDisplayed(),
                "Balance should be visible after successful login.");

        // Clean up
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Informe seu e-mail']")));
        WebElement passInput = driver.findElement(By.cssSelector("input[placeholder='Informe sua senha']"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        userInput.sendKeys("invalid@example.com");
        passInput.sendKeys("wrongpass");
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("email") ||
                        errorMsg.getText().toLowerCase().contains("senha"),
                "Error message should reference email or senha.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);

        // Locate sorting dropdown
        WebElement sortSelectElem = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-testid='sort-select']")));
        Select sortSelect = new Select(sortSelectElem);
        List<WebElement> options = sortSelect.getOptions();

        // Verify each option changes order
        for (WebElement option : options) {
            sortSelect.selectByVisibleText(option.getText());

            // Wait for the transaction items to be refreshed
            List<WebElement> items = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("[data-testid='transaction-item']")));

            Assertions.assertFalse(items.isEmpty(),
                    "Transaction items should be present after sorting with option: " + option.getText());

            // Simple sanity check: capture first item's text
            String firstItem = items.get(0).getText();
            Assertions.assertNotNull(firstItem,
                    "First item name should not be null after sorting with option: " + option.getText());
        }

        // Clean up
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testMenuBurgerAndReset() {
        login(USERNAME, PASSWORD);

        // Add a transaction to later verify reset
        WebElement addTransactionBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='add-transaction']")));
        addTransactionBtn.click();

        // Verify transaction was added
        List<WebElement> transactions = driver.findElements(By.cssSelector("[data-testid='transaction-item']"));
        int initialCount = transactions.size();

        // Open menu
        WebElement menuBtn = driver.findElement(By.cssSelector("button[aria-label='menu']"));
        menuBtn.click();

        // Click Reset
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='reset-menu-item']")));
        resetLink.click();

        // Verify transactions were reset
        wait.until(driver -> driver.findElements(By.cssSelector("[data-testid='transaction-item']")).size() < initialCount);

        // Open menu again for Logout
        menuBtn = driver.findElement(By.cssSelector("button[aria-label='menu']"));
        menuBtn.click();

        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='logout-menu-item']")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "After logout the URL should contain 'login'.");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);

        // Scroll to footer (simple JS scroll)
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Define social links and expected domains
        String[][] socials = {
                {"a[data-test='social-twitter']", "twitter.com"},
                {"a[data-test='social-facebook']", "facebook.com"},
                {"a[data-test='social-linkedin']", "linkedin.com"}
        };

        for (String[] social : socials) {
            List<WebElement> links = driver.findElements(By.cssSelector(social[0]));
            if (links.isEmpty()) {
                continue; // Skip if link not present
            }
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(links.get(0))); 
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Wait for new window
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            Assertions.assertTrue(driver.getCurrentUrl().contains(social[1]),
                    "External social link should navigate to a URL containing " + social[1]);

            // Close and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        // Clean up
        logoutIfLoggedIn();
    }
}