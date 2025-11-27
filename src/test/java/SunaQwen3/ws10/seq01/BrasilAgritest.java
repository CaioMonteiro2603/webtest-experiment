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
public class BrasilAgritestTestSuite {
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
    public void testValidLoginSuccess() {
        driver.get(BASE_URL);
        driver.manage().window().maximize();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login");
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(pageTitle.getText().toLowerCase().contains("dashboard") || driver.getPageSource().toLowerCase().contains("bem-vindo"), "Dashboard page should load with welcome message");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertNotNull(errorMessage, "Error message should appear on invalid login");
        assertTrue(errorMessage.getText().toLowerCase().contains("credenciais") || errorMessage.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigationAllItems() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("/produtos"));
        assertTrue(driver.getCurrentUrl().contains("/produtos"), "Should navigate to products page");
        WebElement productsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(productsHeader.getText().toLowerCase().contains("produtos") || productsHeader.getText().toLowerCase().contains("itens"),
                "Products page header should be visible");
    }

    @Test
    @Order(4)
    public void testMenuNavigationAboutExternal() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String aboutUrl = driver.getCurrentUrl();
        assertTrue(aboutUrl.contains("github.com") || aboutUrl.contains("brasilagritest"), "About link should open GitHub or project site");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testLogoutFunctionality() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");
        assertTrue(driver.getPageSource().toLowerCase().contains("login") || driver.getPageSource().contains("Entrar"),
                "Login page should be displayed");
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado")));
        resetLink.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should remain on dashboard after reset");
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.getText().toLowerCase().contains("resetado") || successMessage.getText().toLowerCase().contains("sucesso"),
                "Success message should confirm reset");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinksTwitter() {
        navigateToDashboard();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String twitterUrl = driver.getCurrentUrl();
        assertTrue(twitterUrl.contains("twitter.com") || twitterUrl.contains("x.com"), "Twitter link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterSocialLinksFacebook() {
        navigateToDashboard();

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook']")));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();

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
    }

    @Test
    @Order(9)
    public void testFooterSocialLinksLinkedIn() {
        navigateToDashboard();

        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='linkedin']")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();

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
    }

    @Test
    @Order(10)
    public void testSortingDropdownOptions() {
        navigateToDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement productsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        productsLink.click();

        wait.until(ExpectedConditions.urlContains("/produtos"));

        WebElement sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("sort")));
        String[] sortOptions = {"nome_asc", "nome_desc", "preco_asc", "preco_desc"};

        for (String optionValue : sortOptions) {
            sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.name("sort")));
            sortSelect.sendKeys(optionValue);

            wait.until(ExpectedConditions.stalenessOf(sortSelect));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("sort")));

            WebElement selectedOption = driver.findElement(By.cssSelector("select[name='sort'] option:checked"));
            assertEquals(optionValue, selectedOption.getAttribute("value"), "Selected sort option should match");
        }
    }

    private void navigateToDashboard() {
        driver.get(BASE_URL);
        try {
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            if (emailField.isDisplayed()) {
                emailField.sendKeys(LOGIN);
                driver.findElement(By.name("password")).sendKeys(PASSWORD);
                driver.findElement(By.xpath("//button[@type='submit']")).click();
                wait.until(ExpectedConditions.urlContains("/dashboard"));
            }
        } catch (TimeoutException e) {
            if (!driver.getCurrentUrl().contains("/dashboard")) {
                throw new RuntimeException("Failed to login and reach dashboard", e);
            }
        }
    }
}