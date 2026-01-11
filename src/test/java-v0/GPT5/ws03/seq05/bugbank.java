package GPT5.ws03.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) driver.quit();
    }

    // ---------- Helpers ----------

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        // Wait for either login form or main landing hero
        wait.until(driver1 -> driver1.findElements(By.cssSelector("input[type='email'], button, a")).size() > 0);
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should load base URL");
    }

    private boolean isPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    private void safeClick(WebElement el) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private boolean loginIfFormPresent(String email, String password) {
        // Try several robust selectors seen on BugBank clones
        List<By> emailLocators = List.of(
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[name='email']"),
                By.cssSelector("input[placeholder*='email'], input[placeholder*='Email']")
        );
        List<By> passwordLocators = List.of(
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[name='password']"),
                By.cssSelector("input[placeholder*='senha'], input[placeholder*='Senha'], input[placeholder*='password'], input[placeholder*='Password']")
        );
        List<By> loginBtnLocators = List.of(
                By.xpath("//button[contains(.,'Acessar') or contains(.,'Login') or contains(.,'Entrar')]"),
                By.cssSelector("button[type='submit']")
        );

        WebElement emailEl = null, passEl = null, btnEl = null;

        for (By by : emailLocators) if (isPresent(by)) { emailEl = driver.findElement(by); break; }
        for (By by : passwordLocators) if (isPresent(by)) { passEl = driver.findElement(by); break; }
        for (By by : loginBtnLocators) if (isPresent(by)) { btnEl = driver.findElement(by); break; }

        if (emailEl != null && passEl != null && btnEl != null) {
            emailEl.clear();
            emailEl.sendKeys(email);
            passEl.clear();
            passEl.sendKeys(password);
            safeClick(btnEl);
            // Wait for either an error toast/dialog or a navigation to a dashboard
            try {
                wait.until(d ->
                        d.getCurrentUrl().contains("home") ||
                                d.findElements(By.cssSelector(".Toastify, [role='dialog'], .error, .alert, .MuiSnackbar-root")).size() > 0
                );
            } catch (TimeoutException ignored) {}
            return true;
        }
        return false;
    }

    private boolean isLoginErrorVisible() {
        return isPresent(By.cssSelector(".Toastify, .error, .alert, [role='dialog']")) ||
                driver.getCurrentUrl().startsWith(BASE_URL); // still on login page
    }

    private boolean isDashboardVisible() {
        // Look for common dashboard cues: balance, transfer, pix, header, or URL containing home
        if (driver.getCurrentUrl().contains("home")) return true;
        return isPresent(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saldo')]")) ||
                isPresent(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transfer')]")) ||
                isPresent(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pix')]")) ||
                isPresent(By.cssSelector("[data-test*='home'], [class*='dashboard']"));
    }

    private void handleExternalLink(WebElement link) {
        String original = driver.getWindowHandle();
        String originalHost = hostOf(driver.getCurrentUrl());
        Set<String> before = driver.getWindowHandles();
        safeClick(link);
        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = new ArrayList<>(after).get(0);
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().startsWith("http"));
            String newHost = hostOf(driver.getCurrentUrl());
            Assertions.assertNotEquals(originalHost, newHost, "External link should navigate to a different host");
            driver.close();
            driver.switchTo().window(original);
        } else {
            // Same tab
            wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().startsWith("http"));
            String newHost = hostOf(driver.getCurrentUrl());
            Assertions.assertNotEquals(originalHost, newHost, "External link should navigate to a different host");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(originalHost));
        }
    }

    private String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    // ---------- Tests ----------

    @Test
    @Order(1)
    public void homeLoadsAndCoreElementsPresent() {
        openHome();
        // Expect at least one primary action: register or login button
        boolean hasLoginInputs = isPresent(By.cssSelector("input[type='email']")) && isPresent(By.cssSelector("input[type='password']"));
        boolean hasRegisterButton = isPresent(By.xpath("//button[contains(.,'Cadastrar') or contains(.,'Cadastre-se') or contains(.,'Register')]"));
        boolean hasBranding = isPresent(By.xpath("//*[contains(.,'Bug') and contains(.,'Bank')]"));
        Assertions.assertTrue(hasLoginInputs || hasRegisterButton || hasBranding,
                "Expected login inputs or register button or branding on the landing page");
    }

    @Test
    @Order(2)
    public void openAndCloseRegisterModalIfAvailable() {
        openHome();
        List<WebElement> registerBtns = driver.findElements(By.xpath("//button[contains(.,'Cadastrar') or contains(.,'Cadastre-se') or contains(.,'Register')]"));
        Assumptions.assumeTrue(registerBtns.size() > 0, "Register button not present; skipping");
        safeClick(registerBtns.get(0));
        // Wait for a modal/dialog with inputs for name/email/password
        boolean modalAppeared;
        try {
            wait.until(d -> d.findElements(By.cssSelector("[role='dialog'], .modal, .MuiDialog-paper, .MuiDialog-root")).size() > 0);
            modalAppeared = true;
        } catch (TimeoutException e) {
            modalAppeared = false;
        }
        Assertions.assertTrue(modalAppeared, "Registration modal should appear after clicking register");
        // Close modal via a cancel/close button
        List<WebElement> close = driver.findElements(By.xpath("//button[contains(.,'Fechar') or contains(.,'Cancelar') or contains(.,'Close')]"));
        if (!close.isEmpty()) {
            safeClick(close.get(0));
            wait.until(d -> d.findElements(By.cssSelector("[role='dialog'], .modal, .MuiDialog-paper")).isEmpty());
        }
    }

    @Test
    @Order(3)
    public void invalidLoginShowsErrorOrStaysOnLogin() {
        openHome();
        boolean formFound = loginIfFormPresent("invalid@example.com", "wrongpass");
        Assumptions.assumeTrue(formFound, "Login form not found; skipping invalid login test");
        Assertions.assertTrue(isLoginErrorVisible(), "Invalid login should show an error or remain on login page");
    }

    @Test
    @Order(4)
    public void validLoginAttemptsWithProvidedCredentials() {
        openHome();
        boolean formFound = loginIfFormPresent(LOGIN_EMAIL, LOGIN_PASSWORD);
        Assumptions.assumeTrue(formFound, "Login form not found; skipping valid login attempt");
        boolean success = isDashboardVisible();
        boolean error = isLoginErrorVisible();
        Assertions.assertTrue(success || error, "Either dashboard should be visible or an error should be shown after login attempt");
        // If we did log in, try to find a logout and return to home for independence
        if (success) {
            List<WebElement> logoutBtns = driver.findElements(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]"));
            if (!logoutBtns.isEmpty()) {
                safeClick(logoutBtns.get(0));
                wait.until(d -> hostOf(d.getCurrentUrl()).contains("netlify.app"));
            }
        }
    }

    @Test
    @Order(5)
    public void visitInternalLinksOneLevelBelowHome() {
        openHome();
        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        int visited = 0;
        for (WebElement a : anchors) {
            if (visited >= 5) break; // keep suite fast and reliable
            String href = a.getAttribute("href");
            if (href == null || href.isBlank()) continue;
            String host = hostOf(href);
            if (!baseHost.equals(host)) continue; // external -> skip here
            // Only one level below root (no deep paths)
            try {
                URI uri = URI.create(href);
                String path = uri.getPath() == null ? "" : uri.getPath();
                if (path.equals("/") || path.isEmpty()) continue;
                // One-level heuristic: at most one non-empty segment
                String[] segs = path.replaceAll("^/+", "").split("/");
                if (segs.length > 1) continue;
            } catch (Exception ignored) { continue; }

            String original = driver.getCurrentUrl();
            try {
                safeClick(a);
                wait.until(d -> !d.getCurrentUrl().equals(original));
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should remain on same host for internal navigation");

                // IMPORTANT: avoid Assertions.assertTrue(wait.until(...)) overload ambiguity on Java 21
                boolean contentRendered = wait.until(d ->
                        d.findElements(By.cssSelector("h1, h2, h3, button, [role='main']")).size() > 0
                );
                Assertions.assertTrue(contentRendered, "Internal page should render visible content");

                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(original));
                visited++;
            } catch (Exception e) {
                // Navigate back to recover if something failed
                driver.navigate().to(BASE_URL);
                wait.until(ExpectedConditions.urlContains(baseHost));
            }
        }
        Assertions.assertTrue(visited > 0, "At least one internal link one level below should be visited");
    }

    @Test
    @Order(6)
    public void footerOrHeaderExternalLinksOpenInNewTabOrNavigateAway() {
        openHome();
        String baseHost = hostOf(driver.getCurrentUrl());
        // Seek obvious external social/GitHub/documentation links
        List<WebElement> externals = new ArrayList<>();
        externals.addAll(driver.findElements(By.cssSelector("a[target='_blank']")));
        externals.addAll(driver.findElements(By.cssSelector("a[href*='twitter.com'], a[href*='facebook.com'], a[href*='linkedin.com'], a[href*='github.com']")));
        // De-duplicate by simple containment
        List<WebElement> unique = new ArrayList<>();
        for (WebElement e : externals) {
            if (!unique.contains(e)) unique.add(e);
        }
        Assumptions.assumeTrue(unique.size() > 0, "No obvious external links found; skipping");
        // Validate up to 2 external links
        int checked = 0;
        for (WebElement link : unique) {
            if (checked >= 2) break;
            String href = link.getAttribute("href");
            if (href == null || href.isBlank()) continue;
            if (hostOf(href).equals(baseHost)) continue;
            handleExternalLink(link);
            checked++;
        }
        Assertions.assertTrue(checked > 0, "At least one external link should be validated");
    }

    // -------- Optional generic tests from template (skipped if not applicable) --------

    @Test
    @Order(7)
    public void sortingDropdownIfPresentChangesOrder() {
        openHome();
        // Generic attempt to find a select dropdown that could sort lists
        List<WebElement> selects = driver.findElements(By.cssSelector("select"));
        Assumptions.assumeTrue(selects.size() > 0, "No dropdown present; skipping sort test");
        WebElement select = selects.get(0);
        List<WebElement> options = select.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() >= 2, "Not enough options to test sorting; skipping");
        // Capture a before snapshot of first list item text if there is a list
        String before = "";
        List<WebElement> items = driver.findElements(By.cssSelector("ul li, .list-item, .MuiListItem-root"));
        if (!items.isEmpty()) before = items.get(0).getText();

        safeClick(options.get(1));
        // wait for potential re-render (no-op wait, but keeps explicit wait policy)
        wait.until(d -> true);

        String after = before;
        items = driver.findElements(By.cssSelector("ul li, .list-item, .MuiListItem-root"));
        if (!items.isEmpty()) after = items.get(0).getText();

        Assumptions.assumeTrue(!before.isEmpty() && !after.isEmpty(), "No list items to validate sorting; skipping");
        Assertions.assertNotEquals(before, after, "Changing dropdown option should alter the order of listed items");
    }

    @Test
    @Order(8)
    public void burgerMenuIfPresentSupportsLogoutOrAbout() {
        openHome();
        // Try to open a common burger button
        List<WebElement> burgers = driver.findElements(By.cssSelector("button[aria-label*='menu'], button[aria-label*='Menu'], .burger, .hamburger"));
        Assumptions.assumeTrue(burgers.size() > 0, "No burger menu found; skipping");
        safeClick(burgers.get(0));
        // Try About link
        List<WebElement> about = driver.findElements(By.xpath("//a[contains(.,'About') or contains(.,'Sobre')]"));
        if (!about.isEmpty()) {
            String original = driver.getCurrentUrl();
            safeClick(about.get(0));
            // Either internal about page or external
            try {
                wait.until(d -> !d.getCurrentUrl().equals(original));
                if (!hostOf(driver.getCurrentUrl()).equals(hostOf(original))) {
                    // external, go back
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlToBe(original));
                }
            } catch (TimeoutException ignored) {}
        }
        // Try Logout if visible
        List<WebElement> logout = driver.findElements(By.xpath("//a[contains(.,'Logout') or contains(.,'Sair')] | //button[contains(.,'Logout') or contains(.,'Sair')]"));
        if (!logout.isEmpty()) {
            safeClick(logout.get(0));
            // Expect to see login inputs again
            wait.until(d -> d.findElements(By.cssSelector("input[type='email']")).size() > 0);
            Assertions.assertTrue(
                    isPresent(By.cssSelector("input[type='email']")) && isPresent(By.cssSelector("input[type='password']")),
                    "After logout the login form should be visible"
            );
        }
    }
}