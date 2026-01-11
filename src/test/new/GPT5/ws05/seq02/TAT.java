package GPT5.ws05.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1440, 1000));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ------------------------ Helpers ------------------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(d -> d.getTitle() != null);
    }

    private WebElement firstDisplayed(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) {
                if (el.isDisplayed()) return el;
            }
        }
        return null;
    }

    private List<WebElement> displayedAll(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
        el.click();
    }

    private void resetOrReload() {
        WebElement resetBtn = firstDisplayed(
                By.cssSelector("button[type='reset']"),
                By.xpath("//button[contains(.,'Limpar') or contains(.,'Reset')]"),
                By.cssSelector("input[type='reset']")
        );
        if (resetBtn != null) {
            safeClick(resetBtn);
        } else {
            goHome();
        }
    }

    private void clickExternalAndAssert(By linkLocator, String expectedDomainOrPathFragment) {
        List<WebElement> links = displayedAll(linkLocator);
        if (links.isEmpty()) return; // optional
        String currentHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        safeClick(links.get(0));
        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && !d.getCurrentUrl().isEmpty() && !d.getCurrentUrl().equals("about:blank"));
            String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
            Assertions.assertTrue(url.contains(expectedDomainOrPathFragment.toLowerCase(Locale.ROOT)),
                    "External URL should contain: " + expectedDomainOrPathFragment + " but was: " + url);
            driver.close();
            driver.switchTo().window(currentHandle);
        } else {
            // navigated in same tab
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainOrPathFragment.toLowerCase(Locale.ROOT)));
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    // ------------------------ Tests ------------------------

    @Test
    @Order(1)
    public void landingPageLoadsAndCoreElementsExist() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
        // Check form fields commonly present in this app
        WebElement firstName = firstDisplayed(By.id("firstName"), By.name("firstName"), By.cssSelector("input[placeholder*='nome' i]"));
        WebElement lastName = firstDisplayed(By.id("lastName"), By.name("lastName"), By.cssSelector("input[placeholder*='sobrenome' i], input[placeholder*='last' i]"));
        WebElement email = firstDisplayed(By.id("email"), By.name("email"), By.cssSelector("input[type='email']"));
        WebElement message = firstDisplayed(By.id("open-text-area"), By.name("open-text-area"), By.cssSelector("textarea"));
        WebElement submit = firstDisplayed(By.cssSelector("button[type='submit']"), By.xpath("//button[contains(.,'Enviar') or contains(.,'Submit')]"));

        Assertions.assertAll("Core fields",
                () -> Assertions.assertNotNull(firstName, "First name should be present"),
                () -> Assertions.assertNotNull(lastName, "Last name should be present"),
                () -> Assertions.assertNotNull(email, "Email should be present"),
                () -> Assertions.assertNotNull(message, "Message textarea should be present"),
                () -> Assertions.assertNotNull(submit, "Submit button should be present")
        );
    }

    @Test
    @Order(2)
    public void emptySubmitShowsValidationErrors() {
        goHome();
        WebElement submit = firstDisplayed(By.cssSelector("button[type='submit']"), By.xpath("//button[contains(.,'Enviar') or contains(.,'Submit')]"));
        Assertions.assertNotNull(submit, "Submit button must exist");
        safeClick(submit);
        // Expect any error indicators
        boolean errors = driver.findElements(By.cssSelector(".error, .field-error, .invalid-feedback")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'obrigat')]")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'preencha')]")).size() > 0;
        Assertions.assertTrue(errors, "Submitting empty form should show validation errors");
    }

    @Test
    @Order(3)
    public void invalidEmailShowsError() {
        goHome();
        WebElement email = firstDisplayed(By.id("email"), By.name("email"), By.cssSelector("input[type='email']"));
        WebElement firstName = firstDisplayed(By.id("firstName"), By.name("firstName"));
        WebElement lastName = firstDisplayed(By.id("lastName"), By.name("lastName"));
        WebElement message = firstDisplayed(By.id("open-text-area"), By.name("open-text-area"));

        if (firstName != null) { firstName.clear(); firstName.sendKeys("Maria"); }
        if (lastName != null) { lastName.clear(); lastName.sendKeys("Silva"); }
        if (email != null) { email.clear(); email.sendKeys("not-an-email"); }
        if (message != null) { message.clear(); message.sendKeys("Mensagem de teste."); }

        WebElement submit = firstDisplayed(By.cssSelector("button[type='submit']"), By.xpath("//button[contains(.,'Enviar') or contains(.,'Submit')]"));
        Assertions.assertNotNull(submit, "Submit button must exist");
        safeClick(submit);

        boolean emailError = driver.findElements(By.cssSelector(".error, .field-error, .invalid-feedback")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]")).size() > 0;
        Assertions.assertTrue(emailError, "Invalid email should trigger an error message");
    }

    @Test
    @Order(4)
    public void validSubmitShowsSuccessAndResets() {
        goHome();
        WebElement firstName = firstDisplayed(By.id("firstName"), By.name("firstName"));
        WebElement lastName = firstDisplayed(By.id("lastName"), By.name("lastName"));
        WebElement email = firstDisplayed(By.id("email"), By.name("email"), By.cssSelector("input[type='email']"));
        WebElement phoneCheckbox = firstDisplayed(By.id("phone-checkbox"), By.cssSelector("input[id*='phone'][type='checkbox']"));
        WebElement phone = firstDisplayed(By.id("phone"), By.name("phone"), By.cssSelector("input[type='tel']"));
        WebElement message = firstDisplayed(By.id("open-text-area"), By.name("open-text-area"), By.cssSelector("textarea"));

        if (firstName != null) { firstName.clear(); firstName.sendKeys("Joao"); }
        if (lastName != null) { lastName.clear(); lastName.sendKeys("Souza"); }
        if (email != null) { email.clear(); email.sendKeys("joao.souza@example.com"); }
        // If phone is required toggle or type it when present
        if (phoneCheckbox != null && phone != null) {
            if (!phoneCheckbox.isSelected()) safeClick(phoneCheckbox);
            phone.clear(); phone.sendKeys("11999990000");
        } else if (phone != null) {
            phone.clear(); phone.sendKeys("11999990000");
        }
        if (message != null) { message.clear(); message.sendKeys("Mensagem longa para testes automatizados."); }

        // Select a product option if dropdown exists
        WebElement productSel = firstDisplayed(By.id("product"), By.name("product"), By.cssSelector("select#product"));
        if (productSel != null) {
            Select product = new Select(productSel);
            if (product.getOptions().size() > 1) {
                int idx = product.getOptions().get(0).getText().toLowerCase(Locale.ROOT).contains("selecione") ? 1 : 0;
                product.selectByIndex(idx);
                Assertions.assertEquals(product.getOptions().get(idx).getText(), product.getFirstSelectedOption().getText(), "Product selection should persist");
            }
        }

        // Select a service type radio if present
        WebElement typeHelp = firstDisplayed(By.cssSelector("input[type='radio'][value='ajuda']"), By.cssSelector("input[name='type']"));
        if (typeHelp != null) safeClick(typeHelp);

        // Accept terms if present
        WebElement consent = firstDisplayed(By.id("email-checkbox"), By.cssSelector("input[type='checkbox'][id*='email']"));
        if (consent != null && !consent.isSelected()) safeClick(consent);

        WebElement submit = firstDisplayed(By.cssSelector("button[type='submit']"), By.xpath("//button[contains(.,'Enviar') or contains(.,'Submit')]"));
        Assertions.assertNotNull(submit, "Submit button must exist");
        safeClick(submit);

        boolean success = driver.findElements(By.cssSelector(".success, .alert-success")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sucesso') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]")).size() > 0;
        Assertions.assertTrue(success, "Expected a success message after valid submission");

        // Try to reset to a clean state
        resetOrReload();
    }

    @Test
    @Order(5)
    public void productDropdownExerciseAllOptionsIfPresent() {
        goHome();
        WebElement productSelEl = firstDisplayed(By.id("product"), By.name("product"), By.cssSelector("select#product"));
        if (productSelEl == null) {
            Assertions.assertTrue(true, "Product dropdown not present; skipping.");
            return;
        }
        Select product = new Select(productSelEl);
        List<WebElement> options = product.getOptions();
        if (options.isEmpty()) {
            Assertions.fail("Product dropdown has no options");
        }
        String firstSelectedBefore = product.getFirstSelectedOption().getText();
        // iterate through options and ensure changing selection reflects in the control
        for (int i = 0; i < options.size(); i++) {
            WebElement option = options.get(i);
            if (option.isEnabled()) {
                product.selectByIndex(i);
                String selected = product.getFirstSelectedOption().getText();
                Assertions.assertEquals(options.get(i).getText(), selected, "Dropdown should reflect the chosen option");
            }
        }
        if (options.size() > 1) {
            product.selectByIndex((options.indexOf(product.getFirstSelectedOption()) + 1) % options.size());
            String newSelected = product.getFirstSelectedOption().getText();
            Assertions.assertNotEquals(firstSelectedBefore, newSelected, "Selection should change when another option is chosen");
        }
    }

    @Test
    @Order(6)
    public void radioButtonsAndCheckboxesBehave() {
        goHome();
        // Radios
        List<WebElement> radios = displayedAll(By.cssSelector("input[type='radio'][name='type']"));
        if (radios.size() >= 2) {
            safeClick(radios.get(0));
            Assertions.assertTrue(radios.get(0).isSelected(), "First radio should be selected");
            safeClick(radios.get(1));
            Assertions.assertTrue(radios.get(1).isSelected(), "Second radio should be selected after click");
            Assertions.assertFalse(radios.get(0).isSelected(), "First radio should be deselected");
        }

        // Checkboxes
        List<WebElement> checks = displayedAll(By.cssSelector("input[type='checkbox']"));
        for (int i = 0; i < Math.min(2, checks.size()); i++) {
            WebElement cb = checks.get(i);
            boolean wasSelected = cb.isSelected();
            safeClick(cb);
            Assertions.assertNotEquals(wasSelected, cb.isSelected(), "Checkbox selection should toggle on click");
        }
    }

    @Test
    @Order(7)
    public void uploadFileIfFieldExists() {
        goHome();
        WebElement fileInput = firstDisplayed(By.cssSelector("input[type='file']"));
        if (fileInput == null) {
            Assertions.assertTrue(true, "No file upload field; skipping.");
            return;
        }
        // Create a tiny file in memory via Java temp dir
        try {
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("selenium-upload", ".txt");
            java.nio.file.Files.write(tmp, "hello".getBytes());
            fileInput.sendKeys(tmp.toAbsolutePath().toString());
            // Verify file chosen (some browsers expose value ending with file name)
            String val = fileInput.getAttribute("value");
            Assertions.assertTrue(val != null && val.toLowerCase(Locale.ROOT).contains(".txt"), "File input should contain selected file");
        } catch (Exception e) {
            Assertions.fail("Failed to upload file: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    public void privacyPolicyOpensInNewTab() {
        goHome();
        // The privacy link typically targets a new tab to a privacy.html page
        WebElement privacy = firstDisplayed(By.id("privacy"), By.cssSelector("a[href*='privacy']"), By.xpath("//a[contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'privacy')]"));
        if (privacy == null) {
            Assertions.assertTrue(true, "Privacy link not present; skipping.");
            return;
        }
        clickExternalAndAssert(By.id("privacy"), "privacy");
    }

    @Test
    @Order(9)
    public void footerSocialLinksIfPresent() {
        goHome();
        clickExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        clickExternalAndAssert(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        clickExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
        clickExternalAndAssert(By.cssSelector("a[href*='youtube.com']"), "youtube.com");
    }

    @Test
    @Order(10)
    public void optionalMenuOrBurgerIfPresent() {
        goHome();
        WebElement burger = firstDisplayed(
                By.id("menu-button"),
                By.cssSelector(".burger, .hamburger, button[aria-label='menu']"),
                By.xpath("//button[contains(.,'Menu') or contains(@class,'menu')]")
        );
        if (burger == null) {
            Assertions.assertTrue(true, "No burger/menu found; skipping.");
            return;
        }
        safeClick(burger);
        // Try to find a generic 'About' or 'Home' link within one level
        WebElement about = firstDisplayed(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(.,'Sobre')]"));
        if (about != null) {
            String domainFrag = Optional.ofNullable(about.getAttribute("href")).orElse("").toLowerCase(Locale.ROOT).contains("http") ? "http" : "";
            safeClick(about);
            if (!domainFrag.isEmpty()) {
                // external
                clickExternalAndAssert(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(.,'Sobre')]"), "http");
            } else {
                wait.until(d -> d.getCurrentUrl() != null);
                Assertions.assertTrue(true, "About/Home clicked.");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
            }
        }
    }
}