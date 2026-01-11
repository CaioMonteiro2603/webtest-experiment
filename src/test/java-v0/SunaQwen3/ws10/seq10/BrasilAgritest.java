package SunaQwen3.ws10.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

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
        Assertions.assertEquals("https://gestao.brasilagritest.com/login", driver.getCurrentUrl(), "Should be on login page");

        WebElement emailInput = wait.until(elementToBeClickable(By.name("email")));
        emailInput.sendKeys(LOGIN);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        wait.until(urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login");

        WebElement pageTitle = wait.until(visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Dashboard')]")));
        Assertions.assertTrue(pageTitle.isDisplayed(), "Dashboard title should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(elementToBeClickable(By.name("email")));
        emailInput.sendKeys("invalid@user.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        WebElement errorMessage = wait.until(visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        Assertions.assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigationAllItems() {
        navigateToDashboard();

        WebElement menuButton = wait.until(elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();

        wait.until(urlContains("/produtos"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/produtos"), "Should navigate to produtos page");
    }

    @Test
    @Order(4)
    public void testMenuNavigationAboutExternal() {
        navigateToDashboard();

        WebElement menuButton = wait.until(elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement aboutLink = wait.until(elementToBeClickable(By.linkText("Sobre")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(numberOfWindowsToBe(2));
        Set<String> windowHandles = driver.getWindowHandles();
        for (String windowHandle : windowHandles) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testLogoutFunctionality() {
        navigateToDashboard();

        WebElement menuButton = wait.until(elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement logoutLink = wait.until(elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        wait.until(urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        navigateToDashboard();

        WebElement menuButton = wait.until(elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement resetLink = wait.until(elementToBeClickable(By.linkText("Resetar Estado")));
        resetLink.click();

        wait.until(alertIsPresent());
        driver.switchTo().alert().accept();

        WebElement successMessage = wait.until(visibilityOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should appear after reset");
        Assertions.assertTrue(successMessage.getText().contains("Estado resetado"), "Success message should confirm reset");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinksTwitter() {
        navigateToDashboard();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter']"));
        if (!socialLinks.isEmpty()) {
            String originalWindow = driver.getWindowHandle();
            socialLinks.get(0).click();

            wait.until(numberOfWindowsToBe(2));
            Set<String> windowHandles = driver.getWindowHandles();
            for (String windowHandle : windowHandles) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com") || driver.getCurrentUrl().contains("x.com"), "Twitter link should open correct domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(8)
    public void testFooterSocialLinksFacebook() {
        navigateToDashboard();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='facebook']"));
        if (!socialLinks.isEmpty()) {
            String originalWindow = driver.getWindowHandle();
            socialLinks.get(0).click();

            wait.until(numberOfWindowsToBe(2));
            Set<String> windowHandles = driver.getWindowHandles();
            for (String windowHandle : windowHandles) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(9)
    public void testFooterSocialLinksLinkedIn() {
        navigateToDashboard();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='linkedin']"));
        if (!socialLinks.isEmpty()) {
            String originalWindow = driver.getWindowHandle();
            socialLinks.get(0).click();

            wait.until(numberOfWindowsToBe(2));
            Set<String> windowHandles = driver.getWindowHandles();
            for (String windowHandle : windowHandles) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(10)
    public void testSortingDropdownOptions() {
        navigateToDashboard();

        WebElement menuButton = wait.until(elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        menuButton.click();

        WebElement produtosLink = wait.until(elementToBeClickable(By.linkText("Todos os Itens")));
        produtosLink.click();

        wait.until(urlContains("/produtos"));

        WebElement sortSelect = wait.until(elementToBeClickable(By.name("sort")));
        List<WebElement> options = sortSelect.findElements(By.tagName("option"));

        for (WebElement option : options) {
            String optionValue = option.getAttribute("value");
            sortSelect.click();
            option.click();

            wait.until(stalenessOf(sortSelect));
            sortSelect = wait.until(elementToBeClickable(By.name("sort")));

            String selectedValue = sortSelect.findElement(By.cssSelector("option:checked")).getAttribute("value");
            Assertions.assertEquals(optionValue, selectedValue, "Selected sort option should match clicked option");
        }
    }

    private void navigateToDashboard() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(elementToBeClickable(By.name("email")));
        emailInput.clear();
        emailInput.sendKeys(LOGIN);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.clear();
        passwordInput.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        wait.until(urlContains("/dashboard"));
    }
}