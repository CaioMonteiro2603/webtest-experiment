package GPT5.ws03.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String PROVIDED_LOGIN = "caio@gmail.com";
    private static final String PROVIDED_PASSWORD = "123";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1400, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    /* ---------------- Helpers ---------------- */

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private WebElement firstDisplayed(By... locators) {
        for (By by : locators) {
            for (WebElement el : driver.findElements(by)) {
                if (el.isDisplayed()) return el;
            }
        }
        throw new NoSuchElementException("Element not found");
    }

    private void performLogin(String email, String password) {
        goHome();

        WebElement emailInput = firstDisplayed(
                By.name("email"),
                By.cssSelector("input[type='email']")
        );
        WebElement passInput = firstDisplayed(
                By.name("password"),
                By.cssSelector("input[type='password']")
        );

        emailInput.clear();
        emailInput.sendKeys(email);
        passInput.clear();
        passInput.sendKeys(password);

        firstDisplayed(
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Acessar') or contains(.,'Login')]")
        ).click();
    }

    private boolean isDashboardLoaded() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("home"),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saldo')]"))
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void assertToastContains(String... fragments) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<WebElement> toasts = driver.findElements(
                By.cssSelector("[role='alert'], .Toastify__toast, .notification, .toast")
        );
        boolean found = false;
        for (WebElement t : toasts) {
            String text = t.getText().toLowerCase(Locale.ROOT);
            for (String f : fragments) {
                if (text.contains(f.toLowerCase(Locale.ROOT))) {
                    found = true;
                    break;
                }
            }
        }
        Assertions.assertTrue(found, "Expected toast not found");
    }

    private void clickExternalAndAssert(By locator, String domain) {
        List<WebElement> links = driver.findElements(locator);
        if (links.isEmpty()) return;

        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        links.get(0).click();
        wait.until(d -> d.getWindowHandles().size() > before.size()
                || !d.getCurrentUrl().equals(BASE_URL));

        if (driver.getWindowHandles().size() > before.size()) {
            for (String h : driver.getWindowHandles()) {
                if (!h.equals(original)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
            wait.until(ExpectedConditions.urlContains(domain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain));
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(domain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain));
            driver.navigate().back();
        }
    }

    /* ---------------- Tests ---------------- */

    @Test
    @Order(1)
    void testLandingPageLoads() {
        goHome();
        Assertions.assertFalse(driver.getTitle().isBlank());
        Assertions.assertFalse(
                driver.findElements(By.cssSelector("button[type='submit']")).isEmpty()
        );
    }

    @Test
    @Order(2)
    void testInvalidLoginShowsError() {
        performLogin(PROVIDED_LOGIN, PROVIDED_PASSWORD);
        assertToastContains("erro", "inválid", "invalid", "senha", "incorrect", "wrong");
        Assertions.assertFalse(isDashboardLoaded());
    }

    @Test
    @Order(3)
    void testRegisterLoginAndLogoutIfAvailable() {
        String email = "qa+" + System.currentTimeMillis() + "@example.com";

        goHome();
        firstDisplayed(
                By.xpath("//button[contains(.,'Registrar') or contains(.,'Cadastrar')]")
        ).click();

        WebElement name = firstDisplayed(By.name("name"));
        WebElement mail = firstDisplayed(By.name("email"));
        WebElement pass = firstDisplayed(By.name("password"));

        name.sendKeys("QA Bot");
        mail.sendKeys(email);
        pass.sendKeys("Pass123!");

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Cadastrar')]")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", registerButton);
        registerButton.click();
        
        assertToastContains("sucesso", "success");

        performLogin(email, "Pass123!");
        Assertions.assertTrue(isDashboardLoaded());

        List<WebElement> logout = driver.findElements(
                By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]")
        );
        if (!logout.isEmpty()) {
            logout.get(0).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("button[type='submit']")
            ));
        }
    }

    @Test
    @Order(4)
    void testOptionalExternalLinks() {
        goHome();
        clickExternalAndAssert(By.cssSelector("a[href*='github.com']"), "github.com");
        clickExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    @Test
    @Order(5)
    void testOptionalSortingIfPresent() {
        goHome();
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (selects.isEmpty()) return;

        WebElement select = selects.get(0);
        String initial = select.getAttribute("value");
        select.click();

        List<WebElement> options = select.findElements(By.tagName("option"));
        if (options.size() < 2) return;

        options.get(1).click();
        Assertions.assertNotEquals(initial, select.getAttribute("value"));
    }
}