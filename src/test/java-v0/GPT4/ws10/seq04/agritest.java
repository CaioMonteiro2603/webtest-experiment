package GPT4.ws10.seq04;

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
    public void testLoginPageLoad() {
        driver.get(BASE_URL);
        WebElement loginTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Entrar')]")));
        Assertions.assertEquals("Entrar", loginTitle.getText().trim(), "Login page title mismatch");
    }

    @Test
    @Order(2)
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        passwordInput.clear();
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        passwordInput.sendKeys("10203040");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Login did not redirect to dashboard");
        WebElement sidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("aside")));
        Assertions.assertTrue(sidebar.isDisplayed(), "Sidebar should be visible after login");
    }

    @Test
    @Order(3)
    public void testLogoutFunctionality() {
        testLoginWithValidCredentials(); // ensure logged in
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu']")));
        userMenu.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(.,'Sair')]")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should return to login page");
    }

    @Test
    @Order(4)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.clear();
        passwordInput.clear();
        emailInput.sendKeys("wrong@user.com");
        passwordInput.sendKeys("wrongpass");
        loginButton.click();

        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".v-alert")));
        Assertions.assertTrue(alert.getText().toLowerCase().contains("usuário ou senha inválidos") ||
                              alert.getText().toLowerCase().contains("erro"), "Expected error message not shown");
    }

    @Test
    @Order(5)
    public void testSidebarNavigationLinks() {
        testLoginWithValidCredentials();
        WebElement sidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("aside")));

        List<WebElement> links = sidebar.findElements(By.cssSelector("a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("/dashboard")) {
                link.click();
                wait.until(ExpectedConditions.urlContains("/dashboard"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Expected dashboard URL");
                break;
            }
        }
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
        List<WebElement> links = footer.findElements(By.tagName("a"));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com"))) {
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
                wait.until(d -> driver.getWindowHandles().size() > 1);

                Set<String> windows = driver.getWindowHandles();
                for (String window : windows) {
                    if (!window.equals(originalWindow)) {
                        driver.switchTo().window(window);
                        wait.until(ExpectedConditions.urlContains(href.contains("twitter") ? "twitter.com" :
                                                                  href.contains("facebook") ? "facebook.com" : "linkedin.com"));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(href.contains("twitter") ? "twitter.com" :
                                                                             href.contains("facebook") ? "facebook.com" : "linkedin.com"),
                                "External link domain mismatch");
                        driver.close();
                        driver.switchTo().window(originalWindow);
                    }
                }
            }
        }
    }

    @Test
    @Order(7)
    public void testDashboardSortingDropdownIfPresent() {
        testLoginWithValidCredentials();
        List<WebElement> dropdowns = driver.findElements(By.cssSelector("select"));
        if (dropdowns.size() > 0) {
            WebElement dropdown = dropdowns.get(0);
            List<WebElement> options = dropdown.findElements(By.tagName("option"));
            if (options.size() > 1) {
                String firstValue = options.get(0).getText();
                dropdown.click();
                options.get(1).click();
                wait.until(d -> !dropdown.getAttribute("value").equals(firstValue));
                Assertions.assertNotEquals(firstValue, dropdown.getAttribute("value"), "Dropdown value should change");
            }
        }
    }
}
