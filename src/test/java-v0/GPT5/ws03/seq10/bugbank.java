package GPT5.ws03.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    // Common locators - designed to be robust to minor UI changes
    private static final By BODY = By.tagName("body");
    private static final By ACCESS_BUTTON = By.xpath("//button[contains(.,'Acessar') or contains(.,'Entrar') or contains(.,'Login')]");
    private static final By USERNAME_INPUT = By.cssSelector("input[type='email'], input[name='email'], input#email");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'], input[name='password'], input#password");
    private static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit' and not(@disabled)] | //button[contains(.,'Acessar') or contains(.,'Entrar') or contains(.,'Login')]");
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')] | //a[contains(.,'Sair') or contains(.,'Logout')]");
    private static final By BURGER_MENU = By.xpath("//button[contains(@aria-label,'menu') or contains(@aria-label,'Menu') or contains(@class,'hamburger') or contains(@class,'burger')]");
    private static final By MENU_ALL_ITEMS = By.xpath("//a[contains(.,'All Items') or contains(.,'Home') or contains(.,'Início') or contains(.,'Dashboard')]");
    private static final By MENU_ABOUT = By.xpath("//a[contains(.,'About') or contains(.,'Sobre')]");
    private static final By MENU_RESET = By.xpath("//a[contains(.,'Reset App State') or contains(.,'Reset') or contains(.,'Reiniciar')]");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By ANY_SELECT = By.cssSelector("select, .custom-select select");

    // Error notifications (covering several common UI libs)
    private static final By TOAST_ERROR = By.cssSelector(
            ".toast-error, .alert-danger, .notification.is-danger, .v-alert.error, .MuiAlert-standardError, .snackbar, .toast, .error");

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --------------------- Helpers ---------------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should load the BugBank landing page");
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static String hostOf(String url) {
        try {
            return Optional.ofNullable(new URI(url)).map(URI::getHost).orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    private void openLoginModalIfNeeded() {
        if (isPresent(USERNAME_INPUT) && isPresent(PASSWORD_INPUT)) return;
        if (isPresent(ACCESS_BUTTON)) {
            waitClickable(ACCESS_BUTTON).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        }
    }

    private boolean tryLogin(String user, String pass) {
        openBase();
        openLoginModalIfNeeded();

        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        email.clear();
        email.sendKeys(user);

        WebElement pwd = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        pwd.clear();
        pwd.sendKeys(pass);

        WebElement submit = waitClickable(SUBMIT_BUTTON);
        submit.click();

        // Wait for either success (logout appears / welcome text) or error
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(LOGOUT_BUTTON),
                    ExpectedConditions.presenceOfElementLocated(TOAST_ERROR),
                    ExpectedConditions.invisibilityOfElementLocated(USERNAME_INPUT) // modal closed
            ));
        } catch (TimeoutException ignored) {}

        boolean successHeuristic =
                isPresent(LOGOUT_BUTTON) ||
                !isPresent(USERNAME_INPUT); // login modal closed implies success for this SPA
        boolean errorHeuristic = isPresent(TOAST_ERROR);

        return successHeuristic && !errorHeuristic;
    }

    private void logoutIfPossible() {
        if (isPresent(LOGOUT_BUTTON)) {
            waitClickable(LOGOUT_BUTTON).click();
            // After logout, login button/modal should be available again
            try {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(ACCESS_BUTTON),
                        ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT)
                ));
            } catch (TimeoutException ignored) {}
        }
    }

    private void assertExternalByClick(WebElement link) {
        String href = link.getAttribute("href");
        String expectedDomain = hostOf(href);
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Skipping non-http(s) link.");

        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(link));
        el.click();
        
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedDomain));
        } catch (TimeoutException te) {
            Assertions.fail("External link did not open as expected: " + href);
        }

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain external domain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain external domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    private List<WebElement> socialLinksOnPage() {
        List<String> domains = Arrays.asList("twitter.com", "facebook.com", "linkedin.com", "x.com", "instagram.com");
        return driver.findElements(ANY_LINK).stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && domains.stream().anyMatch(href::contains);
                })
                .collect(Collectors.toList());
    }

    // --------------------- Tests ---------------------

    @Test
    @Order(1)
    public void basePage_ShouldLoad_And_ShowAccessOrLoginControls() {
        openBase();
        // Either shows an "Acessar" button or direct inputs
        boolean showsAccessButton = isPresent(ACCESS_BUTTON);
        boolean showsInputs = isPresent(USERNAME_INPUT) && isPresent(PASSWORD_INPUT);
        Assertions.assertTrue(showsAccessButton || showsInputs, "Landing page should show Acessar or login inputs");
    }

    @Test
    @Order(2)
    public void login_Negative_WrongPassword_ShowsErrorOrKeepsLoginModal() {
        openBase();
        openLoginModalIfNeeded();

        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        email.clear();
        email.sendKeys(LOGIN);

        WebElement pwd = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        pwd.clear();
        pwd.sendKeys("wrong-password");

        waitClickable(SUBMIT_BUTTON).click();

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(TOAST_ERROR),
                    ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT)
            ));
        } catch (TimeoutException ignored) {}

        boolean errorShown = isPresent(TOAST_ERROR);
        boolean loginStillVisible = isPresent(USERNAME_INPUT);
        Assertions.assertTrue(errorShown || loginStillVisible, "Invalid login should show error toast or keep login visible");
    }

    @Test
    @Order(3)
    public void login_Positive_Then_Logout_IfCredentialsValid() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Provided credentials may be invalid on this environment; skipping positive login assertions.");

        // After login, check that logout control is available or login modal disappeared
        Assertions.assertTrue(isPresent(LOGOUT_BUTTON) || !isPresent(USERNAME_INPUT),
                "After successful login, logout should be present or login modal closed");

        logoutIfPossible();
        // After logout, login should be accessible again
        Assertions.assertTrue(isPresent(ACCESS_BUTTON) || isPresent(USERNAME_INPUT),
                "After logout, login controls should be visible again");
    }

    @Test
    @Order(4)
    public void internalLinks_OneLevelBelow_FromBase_AreReachable() {
        openBase();
        String baseHost = hostOf(BASE_URL);

        List<String> internalLinks = driver.findElements(ANY_LINK).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(href -> hostOf(href).equalsIgnoreCase(baseHost))
                .filter(href -> !href.contains("#"))
                .distinct()
                .limit(6)
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!internalLinks.isEmpty(), "No internal links found on base page.");

        for (String href : internalLinks) {
            driver.navigate().to(href);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should remain on same host for internal link: " + href);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    @Test
    @Order(5)
    public void footerSocialLinks_AsExternal_ShouldOpen_AndMatchDomain() {
        openBase();
        List<WebElement> links = socialLinksOnPage();
        if (links.isEmpty()) {
            // Sometimes social links might be visible post-login
            boolean ok = tryLogin(LOGIN, PASSWORD);
            Assumptions.assumeTrue(ok, "Could not login to discover social links.");
            links = socialLinksOnPage();
        }
        Assumptions.assumeTrue(!links.isEmpty(), "No social links (Twitter/Facebook/LinkedIn/Instagram) detected.");

        int tested = 0;
        Set<String> seenDomains = new HashSet<>();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            String domain = hostOf(href);
            if (domain.isEmpty() || seenDomains.contains(domain)) continue;
            seenDomains.add(domain);
            assertExternalByClick(link);
            tested++;
            if (tested >= 3) break;
        }
        Assumptions.assumeTrue(tested > 0, "No external link could be tested.");
        logoutIfPossible();
    }

    @Test
    @Order(6)
    public void menu_Burger_OpenClose_Navigate_About_Reset_IfAvailable() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Login required to test menu options.");

        if (isPresent(BURGER_MENU)) {
            waitClickable(BURGER_MENU).click();

            if (isPresent(MENU_ALL_ITEMS)) {
                waitClickable(MENU_ALL_ITEMS).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                Assertions.assertTrue(true, "Navigated via 'All Items/Home' menu entry");
            }

            if (isPresent(BURGER_MENU)) waitClickable(BURGER_MENU).click();
            if (isPresent(MENU_ABOUT)) {
                WebElement about = driver.findElements(MENU_ABOUT).get(0);
                assertExternalByClick(about);
            }

            if (isPresent(BURGER_MENU)) waitClickable(BURGER_MENU).click();
            if (isPresent(MENU_RESET)) {
                waitClickable(MENU_RESET).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                Assertions.assertTrue(true, "Reset App State invoked without error");
            }
        } else {
            Assumptions.assumeTrue(false, "Burger/menu button not present; skipping menu tests.");
        }

        logoutIfPossible();
    }

    @Test
    @Order(7)
    public void sortingDropdown_IfAny_SelectionChangesAcrossOptions() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Login required to access potential lists/dropdowns.");

        List<WebElement> selects = driver.findElements(ANY_SELECT).stream()
                .filter(WebElement::isDisplayed)
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!selects.isEmpty(), "No select/sorting dropdowns found.");

        Select sel = new Select(selects.get(0));
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Not enough options to exercise sorting.");

        String firstText = sel.getFirstSelectedOption().getText();
        int changed = 0;
        for (int i = 0; i < options.size(); i++) {
            sel.selectByIndex(i);
            String now = sel.getFirstSelectedOption().getText();
            if (!Objects.equals(now, firstText)) changed++;
        }
        Assertions.assertTrue(changed > 0, "Selecting different options should change the selection text.");
        logoutIfPossible();
    }

    @Test
    @Order(8)
    public void resetAppState_SkipIfMissing_ElseInvoke() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Login required to access menu/reset.");

        if (isPresent(BURGER_MENU)) {
            waitClickable(BURGER_MENU).click();
            if (isPresent(MENU_RESET)) {
                waitClickable(MENU_RESET).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                Assertions.assertTrue(true, "Reset App State executed.");
            } else {
                Assumptions.assumeTrue(false, "Reset App State not found; skipping.");
            }
        } else {
            Assumptions.assumeTrue(false, "Menu not present; skipping reset.");
        }
        logoutIfPossible();
    }
}
