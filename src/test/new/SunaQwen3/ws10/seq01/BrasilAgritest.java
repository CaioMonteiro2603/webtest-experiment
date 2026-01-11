package SunaQwen3.ws10.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLoginSuccess() {
        driver.get(BASE_URL);
        driver.manage().window().maximize();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email'], input[placeholder*='email' i]")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], input[placeholder*='senha' i]"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Entrar'), button:contains('Login')"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/dashboard"),
            ExpectedConditions.urlContains("/home"),
            ExpectedConditions.titleContains("Dashboard"),
            ExpectedConditions.titleContains("Home")
        ));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/dashboard") || currentUrl.contains("/home") || currentUrl.equals("https://gestao.brasilagritest.com/"), 
                "Should be redirected to dashboard or home after login");
        
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(pageTitle.getText().toLowerCase().contains("dashboard") || 
                pageTitle.getText().toLowerCase().contains("bem-vindo") || 
                pageTitle.getText().toLowerCase().contains("home") ||
                driver.getPageSource().toLowerCase().contains("bem-vindo"), 
                "Dashboard page should load with welcome message");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email'], input[placeholder*='email' i]")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], input[placeholder*='senha' i]"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button:contains('Entrar'), button:contains('Login')"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-danger, .alert, .error, [class*='error'], [class*='danger']")
            ));
            assertNotNull(errorMessage, "Error message should appear on invalid login");
            assertTrue(errorMessage.getText().toLowerCase().contains("credenciais") || 
                    errorMessage.getText().toLowerCase().contains("invalid") ||
                    errorMessage.getText().toLowerCase().contains("erro") ||
                    errorMessage.getText().toLowerCase().contains("senha"),
                    "Error message should indicate invalid credentials");
        } catch (TimeoutException e) {
            // If no specific error element, check for alert or the page remains on login
            assertTrue(driver.getCurrentUrl().contains("/login") || 
                    driver.getCurrentUrl().equals("https://gestao.brasilagritest.com/"), 
                    "Should remain on login page after failed login");
        }
    }

    @Test
    @Order(3)
    public void testMenuNavigationAllItems() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler, [data-toggle='collapse'], .menu-toggle, button[class*='navbar']")
        ));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Todos os Itens') or contains(text(), 'Produtos') or contains(text(), 'Items')]")
        ));
        allItemsLink.click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/produtos"),
            ExpectedConditions.urlContains("/products"),
            ExpectedConditions.urlContains("/items")
        ));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/produtos") || currentUrl.contains("/products") || currentUrl.contains("/items"), 
                "Should navigate to products page");
        
        WebElement productsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(productsHeader.getText().toLowerCase().contains("produtos") || 
                productsHeader.getText().toLowerCase().contains("itens") ||
                productsHeader.getText().toLowerCase().contains("products"),
                "Products page header should be visible");
    }

    @Test
    @Order(4)
    public void testMenuNavigationAboutExternal() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler, [data-toggle='collapse'], .menu-toggle, button[class*='navbar']")
        ));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sobre') or contains(text(), 'About')]")
        ));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        try {
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            String aboutUrl = driver.getCurrentUrl();
            assertTrue(aboutUrl.contains("github.com") || aboutUrl.contains("brasilagritest") || 
                    aboutUrl.contains("about") || aboutUrl.contains("sobre"), 
                    "About link should open GitHub or project site");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            // If no new window opened, check if current page navigated to about
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("about") || currentUrl.contains("sobre") || 
                    driver.getPageSource().toLowerCase().contains("sobre"),
                    "Should navigate to about page");
        }
    }

    @Test
    @Order(5)
    public void testLogoutFunctionality() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler, [data-toggle='collapse'], .menu-toggle, button[class*='navbar']")
        ));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sair') or contains(text(), 'Logout') or contains(text(), 'Sair')]")
        ));
        logoutLink.click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlToBe("https://gestao.brasilagritest.com/"),
            ExpectedConditions.urlContains("/login"),
            ExpectedConditions.titleContains("Login")
        ));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.equals("https://gestao.brasilagritest.com/") || 
                currentUrl.contains("/login") || currentUrl.contains("login"),
                "Should return to login page after logout");
        assertTrue(driver.getPageSource().toLowerCase().contains("login") || 
                driver.getPageSource().toLowerCase().contains("entrar") ||
                driver.getPageSource().toLowerCase().contains("email"),
                "Login page should be displayed");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler, [data-toggle='collapse'], .menu-toggle, button[class*='navbar']")
        ));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Resetar Estado') or contains(text(), 'Reset')]")
        ));
        resetLink.click();

        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            // If no alert, the reset might be immediate
        }

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/dashboard"),
            ExpectedConditions.urlContains("/home"),
            ExpectedConditions.titleContains("Dashboard")
        ));
        
        assertTrue(driver.getCurrentUrl().contains("/dashboard") || 
                driver.getCurrentUrl().contains("/home") ||
                driver.getCurrentUrl().equals("https://gestao.brasilagritest.com/"),
                "Should remain on dashboard after reset");
        
        try {
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-success, .alert, .success")
            ));
            assertTrue(successMessage.getText().toLowerCase().contains("resetado") || 
                    successMessage.getText().toLowerCase().contains("sucesso") ||
                    successMessage.getText().toLowerCase().contains("reset"),
                    "Success message should confirm reset");
        } catch (TimeoutException e) {
            // If no success message, check that we're still on a valid page
            assertTrue(driver.getCurrentUrl().contains("dashboard") || 
                    driver.getCurrentUrl().contains("home") ||
                    driver.getCurrentUrl().equals("https://gestao.brasilagritest.com/"),
                    "Should remain on a valid page after reset");
        }
    }

    @Test
    @Order(7)
    public void testFooterSocialLinksTwitter() {
        navigateToDashboard();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("footer a[href*='twitter'], footer a[href*='x.com'], a[href*='twitter']")
        ));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        try {
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            String twitterUrl = driver.getCurrentUrl();
            assertTrue(twitterUrl.contains("twitter.com") || twitterUrl.contains("x.com"), 
                    "Twitter link should open correct domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            // If no new window, check if href is correct
            String href = twitterLink.getAttribute("href");
            assertTrue(href.contains("twitter.com") || href.contains("x.com"), 
                    "Twitter link should have correct href");
        }
    }

    @Test
    @Order(8)
    public void testFooterSocialLinksFacebook() {
        navigateToDashboard();

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("footer a[href*='facebook'], a[href*='facebook']")
        ));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();

        try {
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            String facebookUrl = driver.getCurrentUrl();
            assertTrue(facebookUrl.contains("facebook.com"), "Facebook link should open correct domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            // If no new window, check if href is correct
            String href = facebookLink.getAttribute("href");
            assertTrue(href.contains("facebook.com"), "Facebook link should have correct href");
        }
    }

    @Test
    @Order(9)
    public void testFooterSocialLinksLinkedIn() {
        navigateToDashboard();

        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("footer a[href*='linkedin'], a[href*='linkedin']")
        ));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();

        try {
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            String linkedinUrl = driver.getCurrentUrl();
            assertTrue(linkedinUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            // If no new window, check if href is correct
            String href = linkedinLink.getAttribute("href");
            assertTrue(href.contains("linkedin.com"), "LinkedIn link should have correct href");
        }
    }

    @Test
    @Order(10)
    public void testSortingDropdownOptions() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler, [data-toggle='collapse'], .menu-toggle, button[class*='navbar']")
        ));
        menuButton.click();

        WebElement productsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Todos os Itens') or contains(text(), 'Produtos')]")
        ));
        productsLink.click();

        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/produtos"),
            ExpectedConditions.urlContains("/products")
        ));

        WebElement sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("select[name='sort'], select[class*='sort'], select[id*='sort']")
        ));
        String[] sortOptions = {"nome_asc", "nome_desc", "preco_asc", "preco_desc"};

        for (String optionValue : sortOptions) {
            sortSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("select[name='sort'], select[class*='sort'], select[id*='sort']")
            ));
            
            try {
                sortSelect.sendKeys(optionValue);
            } catch (ElementNotInteractableException e) {
                // Try clicking and selecting via JavaScript
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", sortSelect, optionValue);
            }

            wait.until(ExpectedConditions.stalenessOf(sortSelect));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("select[name='sort'], select[class*='sort'], select[id*='sort']")
            ));

            WebElement selectedOption = driver.findElement(
                By.cssSelector("select[name='sort'] option:checked, select[class*='sort'] option:checked, select[id*='sort'] option:checked")
            );
            assertEquals(optionValue, selectedOption.getAttribute("value"), "Selected sort option should match");
        }
    }

    private void navigateToDashboard() {
        driver.get(BASE_URL);
        try {
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email'], input[name='email'], input[placeholder*='email' i]")
            ));
            if (emailField.isDisplayed()) {
                emailField.sendKeys(LOGIN);
                driver.findElement(By.cssSelector("input[type='password'], input[name='password'], input[placeholder*='senha' i]")).sendKeys(PASSWORD);
                driver.findElement(By.cssSelector("button[type='submit'], button:contains('Entrar'), button:contains('Login')")).click();
                
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard"),
                    ExpectedConditions.urlContains("/home"),
                    ExpectedConditions.titleContains("Dashboard")
                ));
            }
        } catch (TimeoutException e) {
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/dashboard") && !currentUrl.contains("/home") && 
                !currentUrl.equals("https://gestao.brasilagritest.com/")) {
                throw new RuntimeException("Failed to login and reach dashboard", e);
            }
        }
    }
}