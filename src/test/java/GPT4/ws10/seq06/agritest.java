package GPT4.ws10.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class agritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String EMAIL = "superadmin@brasilagritest.com.br";
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
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        Assertions.assertTrue(emailInput.isDisplayed(), "Email input should be visible on login page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        emailInput.sendKeys("wrong@user.com");
        passwordInput.clear();
        passwordInput.sendKeys("wrongpassword");
        loginBtn.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".text-red-500")));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("credenciais inválidas") ||
                              errorMessage.getText().toLowerCase().contains("usuário"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        emailInput.sendKeys(EMAIL);
        passwordInput.clear();
        passwordInput.sendKeys(PASSWORD);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should redirect to /dashboard after login");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("dashboard"), "Dashboard header should be visible");
    }

    @Test
    @Order(4)
    public void testSidebarNavigationLinks() {
        testValidLogin();
        List<WebElement> navLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("aside a")));
        Assertions.assertFalse(navLinks.isEmpty(), "Sidebar should have navigation links");
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("http")) {
                String original = driver.getWindowHandle();
                link.click();
                wait.until(driver -> driver.getWindowHandles().size() > 1);
                Set<String> handles = driver.getWindowHandles();
                handles.remove(original);
                String newTab = handles.iterator().next();
                driver.switchTo().window(newTab);
                wait.until(ExpectedConditions.urlContains("http"));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith("http"), "External link should open a new tab");
                driver.close();
                driver.switchTo().window(original);
            } else {
                link.click();
                wait.until(ExpectedConditions.urlContains("/"));
                WebElement page = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
                Assertions.assertTrue(page.isDisplayed(), "Internal page should show content");
                driver.get("https://beta.brasilagritest.com/dashboard");
            }
        }
    }

    @Test
    @Order(5)
    public void testLogout() {
        testValidLogin();
        WebElement userMenuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-haspopup='true']")));
        userMenuBtn.click();
        WebElement logoutBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Sair')]")));
        logoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "After logout, should return to login page");
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> links = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("footer a")));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http")) {
                String originalWindow = driver.getWindowHandle();
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
                wait.until(driver -> driver.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newTab = windows.iterator().next();
                driver.switchTo().window(newTab);
                wait.until(ExpectedConditions.urlContains(href.replace("https://", "").split("/")[0]));
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.replace("https://", "").split("/")[0]),
                        "External link should open correct domain");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }
}
