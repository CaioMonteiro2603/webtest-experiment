package SunaQwen3.ws10.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
        driver.manage().window().maximize();

        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        emailInput.sendKeys(LOGIN);
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Dashboard')]")));
        assertNotNull(pageTitle, "Dashboard title should be present");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        emailInput.sendKeys("invalid@user.com");
        passwordInput.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        assertNotNull(errorMessage, "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigationAndResetAppState() {
        // Ensure logged in
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("/items"));
        assertTrue(driver.getCurrentUrl().contains("/items"), "Should navigate to items page");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement resetAppStateLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetAppStateLink.click();

        driver.switchTo().alert().accept();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertNotNull(successMessage, "Success message should appear after reset");
    }

    @Test
    @Order(4)
    public void testExternalAboutLinkInNewTab() {
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        String newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.size() > 0 ? handles.iterator().next() : null;
        });

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.titleContains("About"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("about"), "About page should load in new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        loginIfNotOnDashboard();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter.com'], footer a[href*='facebook.com'], footer a[href*='linkedin.com']"));

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String target = link.getAttribute("target");

            if ("_blank".equals(target)) {
                link.click();

                String newWindow = wait.until(d -> {
                    Set<String> handles = d.getWindowHandles();
                    handles.remove(originalWindow);
                    return handles.size() > 0 ? handles.iterator().next() : null;
                });

                driver.switchTo().window(newWindow);

                String currentUrl = driver.getCurrentUrl();
                if (href.contains("twitter.com")) {
                    assertTrue(currentUrl.contains("twitter.com"), "Should open Twitter link");
                } else if (href.contains("facebook.com")) {
                    assertTrue(currentUrl.contains("facebook.com"), "Should open Facebook link");
                } else if (href.contains("linkedin.com")) {
                    assertTrue(currentUrl.contains("linkedin.com"), "Should open LinkedIn link");
                }

                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be redirected to login page after logout");
    }

    @Test
    @Order(7)
    public void testSortingDropdownOptions() {
        loginIfNotOnDashboard();

        WebElement itemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Items")));
        itemsLink.click();

        wait.until(ExpectedConditions.urlContains("/items"));

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("sort")));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));

        for (WebElement option : options) {
            String optionValue = option.getAttribute("value");
            sortDropdown.click();
            option.click();

            wait.until(ExpectedConditions.stalenessOf(sortDropdown));
            sortDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("sort")));

            String selectedValue = new Select(sortDropdown).getFirstSelectedOption().getAttribute("value");
            assertEquals(optionValue, selectedValue, "Selected sort option should match");
        }
    }

    private void loginIfNotOnDashboard() {
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            driver.get(BASE_URL);
            WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
            WebElement passwordInput = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

            emailInput.sendKeys(LOGIN);
            passwordInput.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }
    }
}