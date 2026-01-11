package Qwen3.ws10.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private final String LOGIN = "superadmin@brasilagritest.com.br";
    private final String PASSWORD = "10203040";

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
    void testLoginPageTitleAndLogo_DisplayedCorrectly() {
        driver.get(BASE_URL);

        assertEquals("GRAS | Painel Administrativo", driver.getTitle(), "Page title should be 'GRAS | Painel Administrativo'");
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".login-logo img")));
        assertTrue(logo.isDisplayed(), "Login logo should be visible");
        assertTrue(logo.getAttribute("alt").contains("Brasil AgriTest"), "Logo should have correct alt text");
    }

    @Test
    @Order(2)
    void testLoginFields_ArePresentAndEditable() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password']"));

        assertTrue(emailField.isEnabled(), "Email field should be enabled");
        assertTrue(passwordField.isEnabled(), "Password field should be enabled");

        emailField.sendKeys(LOGIN);
        assertEquals(LOGIN, emailField.getAttribute("value"), "Email field should contain entered value");

        passwordField.sendKeys(PASSWORD);
        assertTrue(passwordField.getAttribute("value").length() > 0, "Password field should accept input");
    }

    @Test
    @Order(3)
    void testValidLogin_SuccessfulRedirectToDashboard() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Wait for dashboard redirect (URL change)
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "After login should be redirected to dashboard");

        // Check for presence of dashboard elements
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        assertTrue(pageTitle.getText().toLowerCase().contains("dashboard"), "Dashboard page should have a title");
    }

    @Test
    @Order(4)
    void testInvalidLogin_ShowErrorMessage() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        // Wait for error message
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(alert.isDisplayed(), "Error alert should be displayed");
        assertTrue(alert.getText().contains("credenciais") || alert.getText().contains("invalid"),
                   "Error message should indicate invalid credentials");
    }

    @Test
    @Order(5)
    void testNavigationMenu_ItemsArePresentAfterLogin() {
        loginIfNotAlready();

        WebElement sidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".sidebar, nav")));
        assertTrue(sidebar.isDisplayed(), "Sidebar menu should be visible after login");

        // Verify key menu items
        assertTrue(isElementPresent(By.linkText("Dashboard")), "Navigation should contain 'Dashboard' link");
        assertTrue(isElementPresent(By.linkText("Empresas")), "Navigation should contain 'Empresas' link");
        assertTrue(isElementPresent(By.linkText("Usuários")), "Navigation should contain 'Usuários' link");
        assertTrue(isElementPresent(By.linkText("Análises")), "Navigation should contain 'Análises' link");
        assertTrue(isElementPresent(By.linkText("Relatórios")), "Navigation should contain 'Relatórios' link");
    }

    @Test
    @Order(6)
    void testDashboardCharts_AreVisible() {
        loginIfNotAlready();

        // Ensure dashboard is loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".content-header h1, h1")));

        java.util.List<WebElement> charts = driver.findElements(By.cssSelector(".chart-container, .card-body canvas, canvas"));
        assertTrue(charts.size() >= 1, "At least one chart should be present on dashboard");

        WebElement chartCard = driver.findElement(By.cssSelector(".card[data-card-name], .card"));
        assertTrue(chartCard.isDisplayed(), "Chart card should be visible");
    }

    @Test
    @Order(7)
    void testLogout_ReturnsToLoginPage() {
        loginIfNotAlready();

        // Open user menu
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".dropdown-user, .user-menu, button.dropdown-toggle")));
        userMenu.click();

        // Wait for logout item
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/logout'], a:contains('Logout')")));
        logoutLink.click();

        // Wait for redirect to login
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "After logout should return to login page");

        assertTrue(isElementPresent(By.cssSelector("input[type='email'], input[name='email']")), "Login email field should be present");
        assertTrue(isElementPresent(By.cssSelector("input[type='password'], input[name='password']")), "Login password field should be present");
    }

    @Test
    @Order(8)
    void testUserMenu_ProfileOption_NavigatesToProfile() {
        loginIfNotAlready();

        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".dropdown-user, .user-menu, button.dropdown-toggle")));
        userMenu.click();

        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Perfil")));
        profileLink.click();

        wait.until(ExpectedConditions.urlContains("/profile"));
        assertTrue(driver.getCurrentUrl().contains("/profile"), "Profile link should navigate to profile page");

        WebElement profileHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        assertTrue(profileHeading.getText().contains("Perfil") || profileHeading.getText().contains("Profile"),
                   "Profile page should have correct heading");
    }

    @Test
    @Order(9)
    void testFooterSocialLinks_Twitter_OpenInNewTab() {
        loginIfNotAlready();

        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer, .footer")));
        WebElement twitterLink = footer.findElement(By.cssSelector("a[href*='twitter.com']"));
        String originalWindow = driver.getWindowHandle();

        twitterLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("twitter.com"));
                assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback: same tab
        driver.navigate().refresh();
        footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer, .footer")));
        twitterLink = footer.findElement(By.cssSelector("a[href*='twitter.com']"));
        twitterLink.click();

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should redirect");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(driver.getCurrentUrl())); // Wait for any page load
    }

    @Test
    @Order(10)
    void testFooterSocialLinks_LinkedIn_OpenInNewTab() {
        loginIfNotAlready();

        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer, .footer")));
        WebElement linkedinLink = footer.findElement(By.cssSelector("a[href*='linkedin.com']"));
        String originalWindow = driver.getWindowHandle();

        linkedinLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("linkedin.com"));
                assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback
        driver.navigate().refresh();
        footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer, .footer")));
        linkedinLink = footer.findElement(By.cssSelector("a[href*='linkedin.com']"));
        linkedinLink.click();

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should redirect");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(driver.getCurrentUrl()));
    }

    @Test
    @Order(11)
    void testSidebarToggle_CollapsesAndExpandsMenu() {
        loginIfNotAlready();

        WebElement collapseButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar-toggle, button[data-widget='pushmenu']")));
        WebElement sidebar = driver.findElement(By.cssSelector(".sidebar, nav"));

        // Check initial state (expanded)
        assertTrue(sidebar.getAttribute("class").contains("sidebar-collapse") == false, "Sidebar should be expanded initially");

        // Collapse
        collapseButton.click();
        wait.until(ExpectedConditions.attributeContains(sidebar, "class", "sidebar-collapse"));
        assertTrue(sidebar.getAttribute("class").contains("sidebar-collapse"), "Sidebar should be collapsed after toggle");

        // Expand
        collapseButton.click();
        wait.until(ExpectedConditions.not(ExpectedConditions.attributeContains(sidebar, "class", "sidebar-collapse")));
        assertTrue(sidebar.getAttribute("class").contains("sidebar-collapse") == false, "Sidebar should be expanded after second toggle");
    }

    @Test
    @Order(12)
    void testPageTitle_ChangesOnNavigation_Empresas() {
        loginIfNotAlready();

        WebElement empresasLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Empresas")));
        empresasLink.click();

        wait.until(ExpectedConditions.urlContains("/companies"));
        assertTrue(driver.getCurrentUrl().contains("/companies"), "Empresas link should navigate to companies page");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        assertTrue(pageTitle.getText().contains("Empresas") || pageTitle.getText().contains("Companies"),
                   "Companies page should have correct title");
    }

    private void loginIfNotAlready() {
        driver.get(BASE_URL);
        if (driver.getCurrentUrl().equals(BASE_URL)) {
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email']")));
            WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password']"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

            emailField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            try {
                wait.until(ExpectedConditions.urlContains("/dashboard"));
            } catch (Exception e) {
                if (driver.getCurrentUrl().equals(BASE_URL)) {
                    fail("Login failed: still on login page");
                }
            }
        }
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}