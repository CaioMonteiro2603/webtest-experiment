package GPT20b.ws10.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.net.URI;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
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

    /* ---------- Utility Methods ---------- */

    private static boolean elementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    private static WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        driver.findElement(locator).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newHandle);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private static void ensureLoggedIn() {
        // Attempt to locate an element only available after login
        By dashboardLocator = By.cssSelector("div.dashboard");
        if (!elementPresent(dashboardLocator)) {
            login();
        }
    }

    private static void login() {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));
        
        By emailField = locateFirst(By.id("email"),
                By.name("email"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[name='email']"),
                By.xpath("//input[@type='email' or @name='email' or @id='email']"));
        By passwordField = locateFirst(By.id("password"),
                By.name("password"),
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[name='password']"),
                By.xpath("//input[@type='password' or @name='password' or @id='password']"));
        By loginBtn = locateFirst(By.id("loginBtn"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[@type='submit' or contains(text(),'Entrar') or contains(text(),'Login')]"));

        WebElement email = waitClickable(emailField);
        WebElement password = waitClickable(passwordField);
        WebElement button = waitClickable(loginBtn);

        email.clear();
        email.sendKeys(USERNAME);
        password.clear();
        password.sendKeys(PASSWORD);
        button.click();

        wait.until(d -> 
                d.getCurrentUrl().contains("dashboard") || 
                d.getCurrentUrl().equals("https://gestao.brasilagritest.com/") ||
                elementPresent(By.cssSelector("div.dashboard")) ||
                elementPresent(By.cssSelector("[class*='dashboard']")) ||
                elementPresent(By.xpath("//*[contains(@class,'dashboard')]"))
        );
        
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("dashboard") || 
                driver.getCurrentUrl().equals("https://gestao.brasilagritest.com/") ||
                elementPresent(By.cssSelector("div.dashboard")) ||
                elementPresent(By.cssSelector("[class*='dashboard']")) ||
                elementPresent(By.xpath("//*[contains(@class,'dashboard')]")),
                "Login did not navigate to dashboard after valid credentials");
    }

    private static void logout() {
        By avatar = By.cssSelector("div.user-avatar");
        if (elementPresent(avatar)) {
            waitClickable(avatar).click();
            By logoutLink = By.id("logout");
            if (elementPresent(logoutLink)) {
                waitClickable(logoutLink).click();
                wait.until(ExpectedConditions.urlToBe(LOGIN_URL));
            }
        }
    }

    private static By locateFirst(By... locators) {
        for (By locator : locators) {
            try {
                if (driver.findElements(locator).size() > 0) {
                    return locator;
                }
            } catch (Exception e) {
                // Continue to next locator
            }
        }
        throw new RuntimeException("None of the provided locators matched any element");
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testLoginValid() {
        login();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("dashboard") || 
                driver.getCurrentUrl().equals("https://gestao.brasilagritest.com/") ||
                elementPresent(By.cssSelector("div.dashboard")) ||
                elementPresent(By.cssSelector("[class*='dashboard']")) ||
                elementPresent(By.xpath("//*[contains(@class,'dashboard')]")),
                "User not on dashboard after valid login");
        logout();
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));
        
        By emailField = locateFirst(By.id("email"),
                By.name("email"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[name='email']"),
                By.xpath("//input[@type='email' or @name='email' or @id='email']"));
        By passwordField = locateFirst(By.id("password"),
                By.name("password"),
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[name='password']"),
                By.xpath("//input[@type='password' or @name='password' or @id='password']"));
        By loginBtn = locateFirst(By.id("loginBtn"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[@type='submit' or contains(text(),'Entrar') or contains(text(),'Login')]"));

        WebElement email = waitClickable(emailField);
        WebElement password = waitClickable(passwordField);
        WebElement button = waitClickable(loginBtn);

        email.clear();
        email.sendKeys("invalid@user.com");
        password.clear();
        password.sendKeys("wrongpass");
        button.click();

        wait.until(d -> 
                elementPresent(By.cssSelector(".error-msg")) ||
                elementPresent(By.cssSelector("[class*='error']")) ||
                elementPresent(By.xpath("//*[contains(@class,'error')]")) ||
                elementPresent(By.cssSelector(".alert")) ||
                elementPresent(By.cssSelector("[class*='alert']")) ||
                elementPresent(By.xpath("//*[contains(@class,'alert')]"))
        );
        
        By errorMsg = locateFirst(By.cssSelector(".error-msg"),
                By.cssSelector("[class*='error']"),
                By.xpath("//*[contains(@class,'error')]"),
                By.cssSelector(".alert"),
                By.cssSelector("[class*='alert']"),
                By.xpath("//*[contains(@class,'alert')]"));
                
        Assertions.assertTrue(
                elementPresent(errorMsg),
                "Error message not displayed for invalid credentials");
        Assertions.assertEquals(
                LOGIN_URL,
                driver.getCurrentUrl(),
                "URL changed unexpectedly after invalid login");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        ensureLoggedIn();

        By sortingLocator = locateFirst(By.id("produtoOrdenar"),
                By.name("orderby"),
                By.cssSelector("select[name='sortBy']"),
                By.cssSelector("select[class*='sort']"),
                By.xpath("//select[@name='orderby' or @name='sortBy' or contains(@class,'sort')]"));

        Assumptions.assumeTrue(elementPresent(sortingLocator), "Sorting dropdown not found");
        WebElement sortDropdown = waitClickable(sortingLocator);
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Insufficient sorting options");

        // Capture initial order of first product title
        By productTitle = By.cssSelector(".produto-item .titulo");
        List<WebElement> titlesBefore = driver.findElements(productTitle);
        Assumptions.assumeTrue(!titlesBefore.isEmpty(), "No product titles found before sorting");
        String firstBefore = titlesBefore.get(0).getText();

        for (int i = 0; i < options.size(); i++) {
            options.get(i).click();

            wait.until(ExpectedConditions.stalenessOf(titlesBefore.get(0)));

            List<WebElement> titlesAfter = driver.findElements(productTitle);
            String firstAfter = titlesAfter.get(0).getText();

            Assertions.assertNotEquals(
                    firstBefore,
                    firstAfter,
                    "Sorting option '" + options.get(i).getText() + "' did not change order");
            firstBefore = firstAfter;
            titlesBefore = titlesAfter;
        }

        logout();
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        ensureLoggedIn();

        // In this system the menu is a standard navigation bar; use link texts
        // All Items
        By allItemsLink = locateFirst(By.linkText("Todos os Produtos"),
                By.linkText("Produtos"),
                By.partialLinkText("Produtos"),
                By.xpath("//a[contains(text(),'Produtos')]"));
        if (elementPresent(allItemsLink)) {
            waitClickable(allItemsLink).click();
            wait.until(ExpectedConditions.urlContains("produtos"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("produtos"),
                    "Did not navigate to All Items page");
            driver.navigate().back();
        }

        // About (external)
        By aboutLink = locateFirst(By.linkText("Sobre"),
                By.partialLinkText("Sobre"),
                By.xpath("//a[contains(text(),'Sobre')]"));
        if (elementPresent(aboutLink)) {
            String href = driver.findElement(aboutLink).getAttribute("href");
            if (href != null && !extractHost(href).equals(extractHost(BASE_URL))) {
                openExternalLink(aboutLink, extractHost(href));
            }
        }

        // Reset App State
        By resetLink = locateFirst(By.linkText("Reset App State"),
                By.linkText("Resetar"),
                By.partialLinkText("Reset"),
                By.xpath("//a[contains(text(),'Reset')]"));
        if (elementPresent(resetLink)) {
            waitClickable(resetLink).click();
            wait.until(d -> 
                    d.getCurrentUrl().contains("dashboard") || 
                    d.getCurrentUrl().equals("https://gestao.brasilagritest.com/") ||
                    elementPresent(By.cssSelector("div.dashboard")) ||
                    elementPresent(By.cssSelector("[class*='dashboard']")) ||
                    elementPresent(By.xpath("//*[contains(@class,'dashboard')]"))
            );
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("dashboard") || 
                    driver.getCurrentUrl().equals("https://gestao.brasilagritest.com/") ||
                    elementPresent(By.cssSelector("div.dashboard")) ||
                    elementPresent(By.cssSelector("[class*='dashboard']")) ||
                    elementPresent(By.xpath("//*[contains(@class,'dashboard')]")),
                    "Reset App State did not keep user on dashboard");
        }

        // Logout
        By logoutLink = locateFirst(By.linkText("Logout"),
                By.linkText("Sair"),
                By.partialLinkText("Logout"),
                By.partialLinkText("Sair"),
                By.xpath("//a[contains(text(),'Logout') or contains(text(),'Sair')]"));
        if (elementPresent(logoutLink)) {
            waitClickable(logoutLink).click();
            wait.until(ExpectedConditions.urlToBe(LOGIN_URL));
            Assertions.assertEquals(
                    LOGIN_URL,
                    driver.getCurrentUrl(),
                    "Logout did not redirect to login page");
        }
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        By footerLinks = By.cssSelector("footer a[href]");
        Assumptions.assumeTrue(elementPresent(footerLinks), "Footer links not found");

        List<WebElement> links = driver.findElements(footerLinks);
        String internalHost = extractHost(BASE_URL);
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            String host = extractHost(href);
            if (host.isEmpty() || host.equals(internalHost)) continue; // skip internal
            openExternalLink(By.cssSelector("footer a[href='" + href + "']"),
                    host);
        }
    }
}