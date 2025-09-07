package GPT5.ws03.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankHeadlessE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    public static void setUpClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) driver.quit();
    }

    // ========= Helpers =========

    private void goHome() {
        driver.get(BASE_URL);
        // Wait for either login panel or any main container
        wait.until(d -> d.findElements(By.cssSelector("input[type='email']")).size() > 0
                || d.findElements(By.xpath("//*[contains(translate(.,'LOGINACESSAR','loginacessar'),'acessar')]")).size() > 0
                || d.findElements(By.xpath("//*[contains(.,'BugBank')]")).size() > 0);
    }

    private WebElement safeFind(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private boolean isLoggedIn() {
        // Heuristic: dashboard typically shows balance or a header and a logout/exit button.
        boolean hasBalance = driver.findElements(By.xpath("//*[contains(translate(.,'SALDO','saldo'),'saldo')]")).size() > 0;
        boolean hasExit = driver.findElements(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]")).size() > 0;
        boolean hasConta = driver.findElements(By.xpath("//*[contains(translate(.,'CONTA','conta'),'conta')]")).size() > 0;
        return hasBalance || hasExit || hasConta;
    }

    private void openRegisterModal() {
        WebElement registerBtn = safeFind(By.xpath("//button[contains(.,'Registrar') or contains(.,'Register')]"));
        if (registerBtn != null) {
            wait.until(ExpectedConditions.elementToBeClickable(registerBtn)).click();
            // Wait modal open: look for name/email inputs inside modal
            wait.until(d -> d.findElements(By.xpath("//div[contains(@class,'modal') or contains(@role,'dialog')]//input")).size() > 0
                    || d.findElements(By.xpath("//input[contains(@placeholder,'Nome') or contains(@placeholder,'name') or contains(@id,'name')]")).size() > 0);
        }
    }

    private void closeModalIfOpen() {
        List<By> closers = Arrays.asList(
                By.xpath("//button[contains(.,'Fechar') or contains(.,'OK') or contains(.,'Ok') or contains(.,'Feito') or contains(.,'Close')]"),
                By.cssSelector("button[aria-label='Close'], .modal button")
        );
        for (By by : closers) {
            List<WebElement> candidates = driver.findElements(by);
            for (WebElement b : candidates) {
                try {
                    if (b.isDisplayed()) {
                        wait.until(ExpectedConditions.elementToBeClickable(b)).click();
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        // As a fallback, press ESC to close modals
        try {
            new org.openqa.selenium.interactions.Actions(driver).sendKeys(Keys.ESCAPE).perform();
        } catch (Exception ignored) {
        }
    }

    private void ensureRegisteredUser() {
        goHome();
        // Attempt to register the user if login fails
        if (!tryLogin(LOGIN_EMAIL, LOGIN_PASSWORD)) {
            // Open registration and create account
            openRegisterModal();

            // Fill registration fields (robust selectors by placeholder/name/id)
            WebElement name = safeFind(By.xpath("//input[contains(@placeholder,'Nome') or contains(@placeholder,'name') or @name='name' or contains(@id,'name')]"));
            if (name != null) { name.clear(); name.sendKeys("Caio QA"); }

            WebElement email = safeFind(By.xpath("//input[@type='email' or contains(@placeholder,'mail') or contains(@placeholder,'E-mail') or contains(@id,'email')]"));
            if (email != null) { email.clear(); email.sendKeys(LOGIN_EMAIL); }

            WebElement password = safeFind(By.xpath("//input[@type='password' and not(@id='confirmation') and not(contains(@id,'confirm')) and not(@name='confirm')]"));
            if (password == null) password = safeFind(By.xpath("(//input[@type='password'])[1]"));
            if (password != null) { password.clear(); password.sendKeys(LOGIN_PASSWORD); }

            WebElement confirm = safeFind(By.xpath("//input[@type='password' and (contains(@id,'confirm') or contains(@name,'confirm') or contains(@placeholder,'onf'))]"));
            if (confirm == null) confirm = safeFind(By.xpath("(//input[@type='password'])[2]"));
            if (confirm != null) { confirm.clear(); confirm.sendKeys(LOGIN_PASSWORD); }

            // Toggle "Criar conta com saldo?" if present
            WebElement toggleSaldo = safeFind(By.xpath("//*[self::label or self::span or self::div][contains(.,'saldo') or contains(.,'Saldo')] | //input[@type='checkbox']"));
            if (toggleSaldo != null) {
                try {
                    if ("input".equalsIgnoreCase(toggleSaldo.getTagName())) {
                        if (!toggleSaldo.isSelected()) toggleSaldo.click();
                    } else {
                        toggleSaldo.click();
                    }
                } catch (Exception ignored) {}
            }

            // Submit register
            WebElement registerConfirm = safeFind(By.xpath("//button[contains(.,'Cadastrar') or contains(.,'Criar') or contains(.,'Register')]"));
            if (registerConfirm == null) registerConfirm = safeFind(By.cssSelector("button[type='submit']"));
            if (registerConfirm != null) {
                wait.until(ExpectedConditions.elementToBeClickable(registerConfirm)).click();
            }

            // Expect some success modal
            wait.until(d ->
                    d.findElements(By.xpath("//*[contains(translate(.,'SUCESSO','sucesso'),'sucesso') or contains(.,'success')]")).size() > 0
                            || d.findElements(By.xpath("//*[contains(.,'Conta') and contains(.,'criada')]")).size() > 0
                            || d.findElements(By.xpath("//*[contains(.,'Conta') and contains(.,'sucesso')]")).size() > 0
                            || d.findElements(By.xpath("//div[contains(@class,'modal')]")).size() > 0
            );

            closeModalIfOpen();

            // Now login with the same credentials
            boolean loggedIn = tryLogin(LOGIN_EMAIL, LOGIN_PASSWORD);
            Assertions.assertTrue(loggedIn, "Expected to log in after registration.");
        }
        // We should be logged in now
        if (!isLoggedIn()) {
            boolean loggedIn = tryLogin(LOGIN_EMAIL, LOGIN_PASSWORD);
            Assertions.assertTrue(loggedIn, "Expected to be logged in with existing credentials.");
        }
    }

    private boolean tryLogin(String email, String pwd) {
        goHome();
        WebElement emailInput = safeFind(By.xpath("//input[@type='email' or contains(@placeholder,'E-mail') or contains(@placeholder,'mail') or contains(@id,'email')]"));
        WebElement passInput = safeFind(By.xpath("(//input[@type='password'])[1]"));
        WebElement accessBtn = safeFind(By.xpath("//button[contains(.,'Acessar') or contains(.,'Login') or contains(.,'Entrar')]"));
        if (emailInput == null || passInput == null || accessBtn == null) return false;

        emailInput.clear(); emailInput.sendKeys(email);
        passInput.clear(); passInput.sendKeys(pwd);
        wait.until(ExpectedConditions.elementToBeClickable(accessBtn)).click();

        // Heuristic: on success, a dashboard should be visible; on failure, an error modal/toast appears
        try {
            wait.until(d -> isLoggedIn() ||
                    d.findElements(By.cssSelector(".Toastify, [role='alert'], .error")).size() > 0 ||
                    d.findElements(By.xpath("//div[contains(@class,'modal')]")).size() > 0
            );
        } catch (TimeoutException ignored) {}

        return isLoggedIn();
    }

    private void logoutIfPossible() {
        List<WebElement> logoutBtns = driver.findElements(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]"));
        if (!logoutBtns.isEmpty()) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(logoutBtns.get(0))).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email']")));
            } catch (Exception ignored) {}
        }
    }

    private void verifyAndCloseExternalLink(WebElement link) {
        String href = link.getAttribute("href");
        if (href == null || href.trim().isEmpty()) return;
        String originHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        } catch (Exception e) {
            // fallback to window.open
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank');", href);
        }

        // Determine if a new window opened
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().startsWith(BASE_URL));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        String expectedDomain = deriveDomain(href);

        if (after.size() > before.size()) {
            for (String h : after) {
                if (!before.contains(h)) {
                    driver.switchTo().window(h);
                    wait.until(ExpectedConditions.urlContains(expectedDomain));
                    Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomain.toLowerCase()),
                            "External URL should contain expected domain: " + expectedDomain);
                    driver.close();
                    driver.switchTo().window(originHandle);
                    break;
                }
            }
        } else {
            // Same tab navigation
            if (!driver.getCurrentUrl().startsWith(BASE_URL)) {
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomain.toLowerCase()),
                        "External URL should contain expected domain: " + expectedDomain);
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains(BASE_URL));
            }
        }
    }

    private String deriveDomain(String href) {
        try {
            URI uri = URI.create(href);
            String host = uri.getHost();
            if (host == null) return href;
            // Reduce to eTLD+1-ish heuristic
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                return parts[parts.length - 2] + "." + parts[parts.length - 1];
            }
            return host;
        } catch (Exception e) {
            return href;
        }
    }

    // ========= Tests =========

    @Test
    @Order(1)
    public void testHomeLoadsAndHasLoginFormAndExternalLinks() {
        goHome();
        // Assert visible login inputs
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email' or contains(@placeholder,'mail')]")));
        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@type='password'])[1]")));
        Assertions.assertAll("Login form visibility",
                () -> Assertions.assertTrue(email.isDisplayed(), "Email input should be visible."),
                () -> Assertions.assertTrue(password.isDisplayed(), "Password input should be visible.")
        );

        // External links (footer or header): test common social if present
        List<WebElement> externals = new ArrayList<>();
        externals.addAll(driver.findElements(By.cssSelector("a[href*='twitter.com']")));
        externals.addAll(driver.findElements(By.cssSelector("a[href*='facebook.com']")));
        externals.addAll(driver.findElements(By.cssSelector("a[href*='linkedin.com']")));
        externals.addAll(driver.findElements(By.cssSelector("a[href*='github.com']")));

        // Deduplicate
        externals = externals.stream().distinct().collect(Collectors.toList());

        for (WebElement a : externals) {
            verifyAndCloseExternalLink(a);
        }

        // Basic sanity of URL
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should remain on the base URL after external link checks.");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsFeedback() {
        goHome();
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email' or contains(@placeholder,'mail')]")));
        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@type='password'])[1]")));
        WebElement accessBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Acessar') or contains(.,'Login') or contains(.,'Entrar')]")));

        email.clear(); email.sendKeys("nobody@example.com");
        password.clear(); password.sendKeys("wrongpass");
        accessBtn.click();

        // Expect feedback: toast, modal, or inline error
        WebElement feedback = wait.until(d -> {
            List<By> candidates = Arrays.asList(
                    By.cssSelector(".Toastify"),
                    By.cssSelector("[role='alert']"),
                    By.xpath("//div[contains(@class,'modal')]"),
                    By.xpath("//*[contains(translate(@class,'ERROR','error'),'error')]"),
                    By.xpath("//*[contains(translate(.,'INV','inv'), 'inv')]")
            );
            for (By by : candidates) {
                List<WebElement> els = d.findElements(by);
                if (!els.isEmpty()) return els.get(0);
            }
            return null;
        });
        Assertions.assertNotNull(feedback, "Expected an error/feedback element after invalid login.");
    }

    @Test
    @Order(3)
    public void testRegisterIfNeededAndLoginSuccess() {
        ensureRegisteredUser();
        // Assert dashboard: look for welcome/saldo/conta
        WebElement dashboardProof = wait.until(d -> {
            List<By> proofs = Arrays.asList(
                    By.xpath("//*[contains(translate(.,'BEM VINDO','bem vindo'),'bem vindo')]"),
                    By.xpath("//*[contains(translate(.,'SALDO','saldo'),'saldo')]"),
                    By.xpath("//*[contains(translate(.,'CONTA','conta'),'conta')]")
            );
            for (By by : proofs) {
                List<WebElement> els = d.findElements(by);
                if (!els.isEmpty()) return els.get(0);
            }
            return null;
        });
        Assertions.assertNotNull(dashboardProof, "Expected a dashboard indicator (welcome/saldo/conta) after successful login.");
    }

    @Test
    @Order(4)
    public void testNavigateOneLevelPagesIfPresent() {
        ensureRegisteredUser();

        // Common one-level pages in BugBank: "Extrato", "Transferência", "Pagamentos" (names may vary)
        List<String> possibleLinks = Arrays.asList("Extrato", "Transfer", "Transferência", "Pagamento", "Pagamentos", "Depositar", "Saque");
        for (String label : possibleLinks) {
            List<WebElement> links = driver.findElements(By.xpath("//a[contains(.,'" + label + "')] | //button[contains(.,'" + label + "')]"));
            if (!links.isEmpty()) {
                WebElement target = links.get(0);
                wait.until(ExpectedConditions.elementToBeClickable(target)).click();
                // Assert the page changed: header or URL contains hint
                boolean changed = false;
                try {
                    wait.until(d -> d.getCurrentUrl().toLowerCase().contains(label.toLowerCase())
                            || d.findElements(By.xpath("//*[self::h1 or self::h2][contains(.,'" + label + "')]")).size() > 0);
                    changed = true;
                } catch (TimeoutException ignored) {}
                Assertions.assertTrue(changed, "Expected navigation feedback after clicking: " + label);
                // Return to dashboard/home if there is a Home/Voltar link
                List<WebElement> homeLinks = driver.findElements(By.xpath("//a[contains(.,'Home') or contains(.,'Início') or contains(.,'Voltar')]"));
                if (!homeLinks.isEmpty()) {
                    wait.until(ExpectedConditions.elementToBeClickable(homeLinks.get(0))).click();
                } else {
                    driver.navigate().back();
                }
                wait.until(d -> isLoggedIn() || d.getCurrentUrl().startsWith(BASE_URL));
            }
        }
    }

    @Test
    @Order(5)
    public void testMenuBurgerIfPresent() {
        ensureRegisteredUser();

        // Some apps expose a burger menu; if present, exercise it
        WebElement burger = safeFind(By.xpath("//button[contains(@aria-label,'menu') or contains(@class,'menu') or contains(.,'☰')]"));
        Assumptions.assumeTrue(burger != null, "Burger menu not present; skipping.");

        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();
        // Try common items: About (external), All Items/Home, Reset, Logout/Sair
        WebElement about = safeFind(By.xpath("//a[contains(.,'About')]"));
        if (about != null) {
            verifyAndCloseExternalLink(about);
        }

        WebElement allItems = safeFind(By.xpath("//a[contains(.,'Home') or contains(.,'Início') or contains(.,'All Items')]"));
        if (allItems != null) {
            wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();
            Assertions.assertTrue(isLoggedIn() || driver.getCurrentUrl().startsWith(BASE_URL), "Expected to return to main page after 'All Items/Home'.");
        }

        WebElement reset = safeFind(By.xpath("//a[contains(.,'Reset') or contains(.,'Reiniciar')] | //button[contains(.,'Reset') or contains(.,'Reiniciar')]"));
        if (reset != null) {
            wait.until(ExpectedConditions.elementToBeClickable(reset)).click();
            // No hard assertion; just ensure UI remains responsive
            Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "UI should remain responsive after Reset.");
        }

        WebElement logout = safeFind(By.xpath("//a[contains(.,'Logout')] | //button[contains(.,'Logout') or contains(.,'Sair')]"));
        if (logout != null) {
            wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email']")));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL) || driver.findElements(By.xpath("//input[@type='email']")).size() > 0,
                    "Expected to be back at login after Logout.");
            // Log back in to keep following tests independent
            tryLogin(LOGIN_EMAIL, LOGIN_PASSWORD);
            Assertions.assertTrue(isLoggedIn(), "Should be logged in again after burger-menu logout re-login.");
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinksIfPresent() {
        goHome();
        // Look for footer on home (pre-login) and verify external links if present
        List<WebElement> social = new ArrayList<>();
        social.addAll(driver.findElements(By.cssSelector("a[href*='twitter.com']")));
        social.addAll(driver.findElements(By.cssSelector("a[href*='facebook.com']")));
        social.addAll(driver.findElements(By.cssSelector("a[href*='linkedin.com']")));
        social.addAll(driver.findElements(By.cssSelector("a[href*='github.com']")));
        social = social.stream().distinct().collect(Collectors.toList());

        for (WebElement a : social) {
            verifyAndCloseExternalLink(a);
        }
        // Nothing to assert strictly if none found; ensure still on home
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should remain on base URL after checking social links.");
    }

    @Test
    @Order(7)
    public void testLogoutFlow() {
        ensureRegisteredUser();
        logoutIfPossible();
        // If no explicit "Sair" button exists, we still expect to be able to go home and see login
        goHome();
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email' or contains(@placeholder,'mail')]")));
        Assertions.assertTrue(emailInput.isDisplayed(), "Email input should be visible on login after logout.");
    }

    @Test
    @Order(8)
    public void testSortingDropdownIfPresent() {
        ensureRegisteredUser();
        // If a sorting dropdown exists in any listing, exercise it.
        WebElement select = safeFind(By.tagName("select"));
        Assumptions.assumeTrue(select != null, "No sorting dropdown present; skipping.");

        // Capture first option text and switch to another, then assert selection changed
        List<WebElement> options = select.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Not enough options to test sorting; skipping.");
        String initialSelected = options.stream().filter(WebElement::isSelected).map(WebElement::getText).findFirst().orElse(options.get(0).getText());

        // Choose a different option
        WebElement alternative = options.stream().filter(o -> !o.getText().equals(initialSelected)).findFirst().orElse(options.get(1));
        wait.until(ExpectedConditions.elementToBeClickable(select)).click();
        wait.until(ExpectedConditions.elementToBeClickable(alternative)).click();

        // Assert the selected option has changed
        String nowSelected = select.findElements(By.tagName("option")).stream().filter(WebElement::isSelected).map(WebElement::getText).findFirst().orElse("");
        Assertions.assertNotEquals(initialSelected, nowSelected, "Selected sorting option should change.");
    }
}
s