package SunaQwen3.ws10.seq06;

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
public class SiteTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys(LOGIN);
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login");
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Dashboard"), "Dashboard page should contain 'Dashboard' text");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys("invalid@user.com");
        passwordInput.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().contains("credenciais"), "Error message should mention invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigation() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            driver.get("https://gestao.brasilagritest.com/dashboard");
        }

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click All Items (assuming it's a link in the menu)
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Produtos")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/produtos"));
        assertTrue(driver.getCurrentUrl().contains("/produtos"), "Should navigate to produtos page");

        // Navigate back to dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click About (external link)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert external URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"), "About link should open brasilagritest.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Click Reset App State
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado")));
        resetLink.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @Test
    @Order(4)
    void testFooterSocialLinks() {
        driver.get("https://gestao.brasilagritest.com/dashboard");
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("footer a[href*='twitter']"));
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("footer a[href*='facebook']"));
        facebookLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open facebook.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("footer a[href*='linkedin']"));
        linkedinLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    void testSortingDropdown() {
        driver.get("https://gestao.brasilagritest.com/produtos");
        WebElement sortSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("sort")));

        // Test Name A to Z
        sortSelect.click();
        WebElement optionAtoZ = driver.findElement(By.cssSelector("option[value='name_asc']"));
        optionAtoZ.click();
        wait.until(ExpectedConditions.stalenessOf(sortSelect));
        sortSelect = driver.findElement(By.name("sort"));
        assertEquals("name_asc", sortSelect.getAttribute("value"), "Sort should be set to name A to Z");

        // Test Name Z to A
        sortSelect.click();
        WebElement optionZtoA = driver.findElement(By.cssSelector("option[value='name_desc']"));
        optionZtoA.click();
        wait.until(ExpectedConditions.stalenessOf(sortSelect));
        sortSelect = driver.findElement(By.name("sort"));
        assertEquals("name_desc", sortSelect.getAttribute("value"), "Sort should be set to name Z to A");

        // Test Price Low to High
        sortSelect.click();
        WebElement optionLowHigh = driver.findElement(By.cssSelector("option[value='price_asc']"));
        optionLowHigh.click();
        wait.until(ExpectedConditions.stalenessOf(sortSelect));
        sortSelect = driver.findElement(By.name("sort"));
        assertEquals("price_asc", sortSelect.getAttribute("value"), "Sort should be set to price low to high");

        // Test Price High to Low
        sortSelect.click();
        WebElement optionHighLow = driver.findElement(By.cssSelector("option[value='price_desc']"));
        optionHighLow.click();
        wait.until(ExpectedConditions.stalenessOf(sortSelect));
        sortSelect = driver.findElement(By.name("sort"));
        assertEquals("price_desc", sortSelect.getAttribute("value"), "Sort should be set to price high to low");
    }

    @Test
    @Order(6)
    void testLogoutFunctionality() {
        driver.get("https://gestao.brasilagritest.com/dashboard");
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe("https://gestao.brasilagritest.com/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"), "Should be redirected to login page after logout");
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Login"), "Login page should contain 'Login' text");
    }

    private void switchToNewWindow(String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                return;
            }
        }
        fail("No new window opened");
    }
}