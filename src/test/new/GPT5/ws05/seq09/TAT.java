package GPT5.ws05.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    // Core field locators (multiple fallbacks to be resilient)
    private static final By FORM = By.cssSelector("form, .container, body");
    private static final By FIRST_NAME = By.cssSelector("#firstName, input[name='firstName'], input[placeholder*='Nome'][type='text'], input[placeholder*='First'][type='text']");
    private static final By LAST_NAME = By.cssSelector("#lastName, input[name='lastName'], input[placeholder*='Sobrenome'][type='text'], input[placeholder*='Last'][type='text']");
    private static final By EMAIL = By.cssSelector("#email, input[type='email'][name='email'], input[placeholder*='mail']");
    private static final By PHONE = By.cssSelector("#phone, input[name='phone'], input[placeholder*='Telefone'], input[placeholder*='Phone']");
    private static final By PRODUCT_SELECT = By.cssSelector("#product, select[name='product'], select");
    private static final By OPEN_TEXT = By.cssSelector("#open-text-area, textarea[name='open-text-area'], textarea");
    private static final By FILE_INPUT = By.cssSelector("input[type='file']");
    private static final By SUBMIT = By.cssSelector("button[type='submit'], #submit-button, .button");
    private static final By SUCCESS = By.cssSelector(".success, .alert-success, #success");
    private static final By ERROR = By.cssSelector(".error, .alert-error, #error");

    // Optional behaviors
    private static final By MAKE_PHONE_REQUIRED_CHECKBOX = By.cssSelector("input[type='checkbox'][name*='phone'], input[type='checkbox'][id*='phone']");

    // Menu-like (not applicable here) - used only to assert absence
    private static final By BURGER = By.id("react-burger-menu-btn");
    private static final By MENU = By.cssSelector(".bm-menu-wrap, nav, aside");
    private static final By MENU_ALL_ITEMS = By.id("inventory_sidebar_link");
    private static final By MENU_ABOUT = By.id("about_sidebar_link");
    private static final By MENU_LOGOUT = By.id("logout_sidebar_link");
    private static final By MENU_RESET = By.id("reset_sidebar_link");

    // Internal privacy link (one level below)
    private static final By PRIVACY_LINK = By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'privacy') or contains(.,'Política') or contains(.,'Privacidade')]");

    @BeforeAll
    public static void setUpClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) driver.quit();
    }

    /* ===================== Helpers ===================== */

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(FORM));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://cac-tat.s3.eu-central-1.amazonaws.com"), "Should stay on expected origin");
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private static String hostOf(String url) {
        try { return Optional.ofNullable(new URI(url).getHost()).orElse(""); }
        catch (Exception e) { return ""; }
    }

    private void assertExternalLinkOpens(WebElement link, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        Set<String> oldHandles = driver.getWindowHandles();
        waitClickable(By.cssSelector("body")); // stabilize
        link.click();

        wait.until(d -> d.getWindowHandles().size() > oldHandles.size() || d.getCurrentUrl().contains(expectedDomain));

        if (driver.getWindowHandles().size() > oldHandles.size()) {
            Set<String> diff = new HashSet<>(driver.getWindowHandles());
            diff.removeAll(oldHandles);
            String newHandle = diff.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External URL should contain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External URL should contain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(FORM));
        }
    }

    /* ===================== Tests ===================== */

    @Test
    @Order(1)
    public void landing_CoreElementsPresent() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(isPresent(FIRST_NAME), "First name should be present"),
                () -> Assertions.assertTrue(isPresent(LAST_NAME), "Last name should be present"),
                () -> Assertions.assertTrue(isPresent(EMAIL), "Email should be present"),
                () -> Assertions.assertTrue(isPresent(OPEN_TEXT), "Message textarea should be present"),
                () -> Assertions.assertTrue(isPresent(PRODUCT_SELECT), "Product dropdown should be present"),
                () -> Assertions.assertTrue(isPresent(SUBMIT), "Submit button should be present")
        );
    }

    @Test
    @Order(2)
    public void dropdown_ExerciseOptionsAndAssertChange() {
        openBase();
        Select product = new Select(waitVisible(PRODUCT_SELECT));
        List<WebElement> optionsList = product.getOptions();
        List<WebElement> enabledOptions = optionsList.stream()
                .filter(WebElement::isEnabled)
                .collect(Collectors.toList());
        
        Assumptions.assumeTrue(enabledOptions.size() >= 2, "Need at least two enabled options in product dropdown");
        
        String initial = product.getFirstSelectedOption().getText();
        product.selectByIndex(optionsList.indexOf(enabledOptions.get(1)));
        String after = product.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(initial, after, "Selecting another product should change selection");
        product.selectByIndex(0);
        String restored = product.getFirstSelectedOption().getText();
        Assertions.assertEquals(product.getOptions().get(0).getText(), restored, "Re-selecting first option should restore initial");
    }

    @Test
    @Order(3)
    public void invalidEmail_ShowsError_NoSuccess() {
        openBase();
        waitVisible(FIRST_NAME).clear();
        driver.findElement(FIRST_NAME).sendKeys("Caio");
        waitVisible(LAST_NAME).clear();
        driver.findElement(LAST_NAME).sendKeys("Silva");
        waitVisible(EMAIL).clear();
        driver.findElement(EMAIL).sendKeys("invalid-email");
        waitVisible(OPEN_TEXT).clear();
        driver.findElement(OPEN_TEXT).sendKeys("Mensagem de teste com e-mail inválido.");
        waitClickable(SUBMIT).click();

        boolean html5Validation = Optional.ofNullable(driver.findElement(EMAIL).getDomProperty("validationMessage"))
                .map(String::trim).orElse("").length() > 0;
        boolean customError = isPresent(ERROR);
        boolean formStillPresent = isPresent(FORM);
        
        Assertions.assertTrue(html5Validation || customError, "Invalid email should trigger validation error");
        if (formStillPresent) {
            Assertions.assertTrue(driver.findElements(SUCCESS).isEmpty(), "Success message must not be present");
        } else {
            // If form is not present, it might have submitted, so we need to check for error
            Assertions.assertTrue(customError, "If form submitted, error should be shown");
        }
    }

    @Test
    @Order(4)
    public void phoneRequiredCheckbox_WhenChecked_PhoneBecomesMandatory() {
        openBase();
        Assumptions.assumeTrue(isPresent(MAKE_PHONE_REQUIRED_CHECKBOX), "Phone required checkbox not available on this page");
        WebElement toggler = driver.findElement(MAKE_PHONE_REQUIRED_CHECKBOX);
        if (!toggler.isSelected()) toggler.click();

        // Fill other required fields but leave phone empty
        waitVisible(FIRST_NAME).clear();
        driver.findElement(FIRST_NAME).sendKeys("Ana");
        waitVisible(LAST_NAME).clear();
        driver.findElement(LAST_NAME).sendKeys("Tester");
        waitVisible(EMAIL).clear();
        driver.findElement(EMAIL).sendKeys("ana.tester@example.com");
        waitVisible(OPEN_TEXT).clear();
        driver.findElement(OPEN_TEXT).sendKeys("Verificando obrigatoriedade do telefone.");
        waitClickable(SUBMIT).click();

        boolean phoneInvalid = Optional.ofNullable(driver.findElement(PHONE).getDomProperty("validationMessage"))
                .map(String::trim).orElse("").length() > 0;
        boolean errorShown = isPresent(ERROR);
        Assertions.assertTrue(phoneInvalid || errorShown, "Phone should be required when checkbox is checked");
    }

    @Test
    @Order(5)
    public void validSubmission_ShowsSuccessMessage() {
        openBase();
        // Ensure phone requirement off if toggle exists
        if (isPresent(MAKE_PHONE_REQUIRED_CHECKBOX)) {
            WebElement t = driver.findElement(MAKE_PHONE_REQUIRED_CHECKBOX);
            if (t.isSelected()) t.click();
        }

        waitVisible(FIRST_NAME).clear();
        driver.findElement(FIRST_NAME).sendKeys("Maria");
        waitVisible(LAST_NAME).clear();
        driver.findElement(LAST_NAME).sendKeys("Quality");
        waitVisible(EMAIL).clear();
        driver.findElement(EMAIL).sendKeys("maria.quality@example.com");
        waitVisible(OPEN_TEXT).clear();
        driver.findElement(OPEN_TEXT).sendKeys("Mensagem válida para envio com sucesso.");

        // Exercise select and file input (optional)
        if (isPresent(PRODUCT_SELECT)) {
            Select s = new Select(driver.findElement(PRODUCT_SELECT));
            if (s.getOptions().size() > 1) s.selectByIndex(1);
        }
        if (isPresent(FILE_INPUT)) {
            // Cannot rely on local filesystem; skip actual upload while keeping locator coverage.
            driver.findElement(FILE_INPUT).isDisplayed();
        }

        waitClickable(SUBMIT).click();

        boolean successBlock = isPresent(SUCCESS);
        boolean successText = driver.getPageSource().toLowerCase().contains("sucesso") ||
                              driver.getPageSource().toLowerCase().contains("success");
        Assertions.assertTrue(successBlock || successText, "A success message/indicator should appear after valid submission");
    }

    @Test
    @Order(6)
    public void privacyPolicy_InternalOneLevel_NavigateAndBack() {
        openBase();
        Assumptions.assumeTrue(isPresent(PRIVACY_LINK), "Privacy/Política de Privacidade link not found on the base page");
        String origin = driver.getWindowHandle();
        Set<String> old = driver.getWindowHandles();

        WebElement link = waitClickable(PRIVACY_LINK);
        link.click();

        // Either new tab or same tab
        wait.until(d -> d.getWindowHandles().size() > old.size() || d.getCurrentUrl().contains("privacy"));
        if (driver.getWindowHandles().size() > old.size()) {
            Set<String> diff = new HashSet<>(driver.getWindowHandles());
            diff.removeAll(old);
            String newTab = diff.iterator().next();
            driver.switchTo().window(newTab);
            wait.until(ExpectedConditions.urlContains("privacy"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("privacy"), "Should navigate to privacy page (one level below)");
            driver.close();
            driver.switchTo().window(origin);
        } else {
            wait.until(ExpectedConditions.urlContains("privacy"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("privacy"), "Should navigate to privacy page (one level below)");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(FORM));
        }
    }

    @Test
    @Order(7)
    public void externalLinks_OpenInNewTab_AssertDomainAndClose() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href^='http']"));
        List<WebElement> externals = links.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    String host = hostOf(href);
                    return href != null && !host.isEmpty() && !host.contains(baseHost);
                })
                .collect(Collectors.toList());

        int max = Math.min(3, externals.size());
        for (int i = 0; i < max; i++) {
            WebElement a = externals.get(i);
            String host = hostOf(a.getAttribute("href"));
            if (!host.isEmpty()) {
                assertExternalLinkOpens(a, host);
            }
        }

        // If there are no external links, ensure page is still interactive
        if (max == 0) {
            Assertions.assertTrue(isPresent(SUBMIT), "No external links found; base page should remain interactive");
        }
    }

    @Test
    @Order(8)
    public void burgerMenuAndInventory_AreNotApplicable_ButGuarded() {
        openBase();
        Assertions.assertTrue(driver.findElements(BURGER).isEmpty(), "Burger/menu button should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU).isEmpty(), "Side menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_ALL_ITEMS).isEmpty(), "All Items menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_ABOUT).isEmpty(), "About menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_LOGOUT).isEmpty(), "Logout menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_RESET).isEmpty(), "Reset App State menu should not exist on this site");
    }
}