package GPT5.ws03.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;package GPT5.ws03.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankWebTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    /* ======================= Helpers ======================= */

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                return els.get(0);
            }
        }
        throw new NoSuchElementException("None of the locators matched: " + Arrays.toString(locators));
    }

    private WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.tagName("body")),
                ExpectedConditions.urlContains("bugbank.netlify.app")
        ));
    }

    private void closeAnyModalIfOpen() {
        List<WebElement> modals = driver.findElements(By.cssSelector(".modal-content"));
        if (!modals.isEmpty()) {
            List<WebElement> closeButtons = modals.get(0).findElements(By.cssSelector("button, .close, [data-test='modal-close']"));
            if (!closeButtons.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(closeButtons.get(0))).click();
                wait.until(ExpectedConditions.invisibilityOf(modals.get(0)));
            }
        }
    }

    private void ensureLoginFormVisible() {
        // If email field is not visible, try clicking a "login" or "acessar" button to reveal it
        if (driver.findElements(By.name("email")).isEmpty() && driver.findElements(By.cssSelector("input[name='email']")).isEmpty()) {
            List<By> openLoginButtons = Arrays.asList(
                    By.cssSelector("button[data-test='login']"),
                    By.xpath("//button[contains(.,'Acessar')]"),
                    By.xpath("//button[contains(.,'Login')]")
            );
            for (By by : openLoginButtons) {
                List<WebElement> btns = driver.findElements(by);
                if (!btns.isEmpty()) {
                    waitClickable(by).click();
                    break;
                }
            }
        }
        // Wait for the email input to be present/visible
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.name("email")),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']"))
        ));
    }

    private void performLogin(String email, String password) {
        openHome();
        ensureLoginFormVisible();

        WebElement emailField = firstPresent(
                By.name("email"),
                By.cssSelector("input[name='email']")
        );
        WebElement passField = firstPresent(
                By.name("password"),
                By.cssSelector("input[name='password']")
        );

        emailField.clear();
        emailField.sendKeys(email);
        passField.clear();
        passField.sendKeys(password);

        WebElement submit = firstPresent(
                By.cssSelector("button[data-test='login-submit']"),
                By.xpath("//button[@type='submit' and (contains(.,'Acessar') or contains(.,'Login'))]"),
                By.xpath("//button[contains(.,'Acessar')]")
        );
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();
    }

    private boolean isLoggedIn() {
        // Heuristics for logged-in state: welcome message or logout button/container change
        if (!driver.findElements(By.cssSelector("p[data-test='welcome-message']")).isEmpty()) return true;
        if (!driver.findElements(By.cssSelector("button[data-test='logout']")).isEmpty()) return true;
        // Some apps redirect or change layout; as a fallback, look for an account area
        return !driver.findElements(By.xpath("//*[contains(.,'Bem vindo') or contains(.,'Bem-vindo') or contains(.,'Welcome')]")).isEmpty();
    }

    private void logoutIfPossible() {
        List<WebElement> logout = driver.findElements(By.cssSelector("button[data-test='logout']"));
        if (!logout.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logout.get(0))).click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.name("email")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']"))
            ));
        }
    }

    private void switchToNewTabAndVerifyDomain(String expectedDomain) {
        String original = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String w : driver.getWindowHandles()) {
            if (!w.equals(original)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected domain not found in URL: " + expectedDomain);
                driver.close();
                driver.switchTo().window(original);
                return;
            }
        }
        Assertions.fail("No new tab was opened for external link.");
    }

    private void clickExternalAndAssert(By by, String expectedDomain) {
        int windowsBefore = driver.getWindowHandles().size();
        WebElement link = waitClickable(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();
        // If same tab navigation, assert and go back; else switch to new tab
        if (driver.getWindowHandles().size() == windowsBefore) {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Expected to navigate to external domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("bugbank"),
                    ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
            ));
        } else {
            switchToNewTabAndVerifyDomain(expectedDomain);
        }
    }

    /* ======================= Tests ======================= */

    @Test
    @Order(1)
    public void validLoginShouldSucceedOrShowDashboard() {
        performLogin(USERNAME, PASSWORD);
        // Success criteria
        Assertions.assertTrue(isLoggedIn(), "Expected to be logged in and see a welcome/dashboard element.");
        logoutIfPossible();
    }

    @Test
    @Order(2)
    public void invalidLoginShouldShowErrorModal() {
        performLogin("invalid@example.com", "wrongpass");
        WebElement modal = waitVisible(By.cssSelector(".modal-content"));
        String text = modal.getText().toLowerCase();
        Assertions.assertTrue(text.contains("erro") || text.contains("error") || text.contains("inválid"),
                "An error modal/message should appear for invalid login. Found: " + text);
        // close modal
        List<WebElement> close = modal.findElements(By.cssSelector("button, .close, [data-test='modal-close']"));
        if (!close.isEmpty()) waitClickable(close.get(0).getTagName().equals("button") ? By.tagName("button") : By.cssSelector("[data-test='modal-close']")).click();
        else driver.findElement(By.cssSelector("body")).click(); // fallback
        closeAnyModalIfOpen();
    }

    @Test
    @Order(3)
    public void registerModalShouldOpenAndClose() {
        openHome();
        WebElement registerBtn = firstPresent(
                By.cssSelector("button[data-test='register']"),
                By.xpath("//button[contains(.,'Registrar') or contains(.,'Cadastre-se') or contains(.,'Register')]")
        );
        waitClickable(registerBtn.getTagName().equalsIgnoreCase("button") ? By.cssSelector("button[data-test='register']") : By.xpath("//button[contains(.,'Registrar') or contains(.,'Cadastre-se') or contains(.,'Register')]")).click();
        WebElement modal = waitVisible(By.cssSelector(".modal-content"));
        Assertions.assertTrue(modal.isDisplayed(), "Registration modal should be visible.");
        closeAnyModalIfOpen();
        Assertions.assertTrue(driver.findElements(By.cssSelector(".modal-content")).isEmpty(), "Registration modal should be closed.");
    }

    @Test
    @Order(4)
    public void externalGithubLinkShouldOpen() {
        openHome();
        // try common footer/header external links
        clickExternalAndAssert(By.cssSelector("a[href*='github.com']"), "github.com");
    }

    @Test
    @Order(5)
    public void externalLinkedInLinkShouldOpen() {
        openHome();
        clickExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }
}


@TestMethodOrder(OrderAnnotation.class)
public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caiomont@gmail.com";
    private static final String PASSWORD = "123";

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

    private void performLogin(String username, String password) {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='login']")));
        loginBtn.click();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement accessBtn = driver.findElement(By.cssSelector("button[data-test='login-submit']"));

        emailField.clear();
        emailField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        accessBtn.click();
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.cssSelector("button[data-test='logout']")).size() > 0) {
            driver.findElement(By.cssSelector("button[data-test='logout']")).click();
        }
    }

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected domain not found in URL: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        WebElement welcomeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[data-test='welcome-message']")));
        Assertions.assertTrue(welcomeMsg.isDisplayed(), "Login successful, welcome message should be visible.");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        performLogin("wrong@email.com", "wrongpass");
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-content")));
        Assertions.assertTrue(alert.getText().toLowerCase().contains("erro"), "Error message should appear for invalid credentials.");
        WebElement closeBtn = alert.findElement(By.cssSelector("button"));
        closeBtn.click();
    }

    @Test
    @Order(3)
    public void testRegisterPageOpen() {
        driver.get(BASE_URL);
        WebElement registerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='register']")));
        registerBtn.click();
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-content")));
        Assertions.assertTrue(modal.getText().toLowerCase().contains("cadastre"), "Registration modal should appear.");
        WebElement closeBtn = modal.findElement(By.cssSelector("button"));
        closeBtn.click();
    }

    @Test
    @Order(4)
    public void testExternalGithubLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com']")));
        footerLink.click();
        switchToNewTabAndVerify("github.com");
    }

    @Test
    @Order(5)
    public void testExternalLinkedInLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        footerLink.click();
        switchToNewTabAndVerify("linkedin.com");
    }
}
