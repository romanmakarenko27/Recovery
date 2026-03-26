package com.schoolday.qa.pages;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegenerateCodesDialog {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locator — "Regenerate Recovery Codes" button on the Security Settings tab
    private final By regeneratePageButton = By.cssSelector(
            "[data-testid='user-security-settings-regenerate-recovery-codes-btn']");

    // Locators — password confirmation dialog
    private final By dialogContainer = By.cssSelector("mat-dialog-container");
    private final By passwordInput = By.cssSelector(
            "[data-testid='verify-password-password-input']");
    private final By regenerateDialogButton = By.cssSelector(
            "[data-testid='verify-password-regenerate-btn']");
    private final By cancelButton = By.cssSelector(
            "[data-testid='verify-password-cancel-btn']");
    private final By passwordToggleButton = By.cssSelector(
            "[data-testid='verify-password-toggle-password-visibility-icon']");

    // Locators — new recovery codes display dialog
    private final By codesList = By.cssSelector(
            "[data-testid='recovery-codes-codes-list']");
    private final By codeItems = By.cssSelector(
            "mat-list-item[data-testid^='recovery-codes-code']");
    private final By downloadButton = By.cssSelector(
            "[data-testid='recovery-codes-download-btn']");
    private final By printButton = By.cssSelector(
            "[data-testid='recovery-codes-print-btn']");
    private final By copyButton = By.cssSelector(
            "[data-testid='recovery-codes-copy-btn']");
    private final By closeButton = By.cssSelector(
            "[data-testid='recovery-codes-close-btn']");

    // Locators — codes dialog text elements
    private final By codesDialogTitle = By.cssSelector("mat-dialog-container h2.verify-title");
    private final By codesDialogDescription = By.cssSelector("mat-dialog-container h3");
    private final By codesDialogWarning = By.cssSelector("mat-dialog-container p.warning-msg");
    private final By codesDialogWarningIcon = By.cssSelector("mat-dialog-container p.warning-msg mat-icon");
    private final By codesDialogInstruction = By.cssSelector(
            "mat-dialog-container div.mt-12.mb-24");
    private final By backdrop = By.cssSelector(".cdk-overlay-backdrop");
    private final By snackbarContainer = By.cssSelector(
            "simple-snack-bar, .mat-mdc-snack-bar-container, .mat-snack-bar-container");

    public RegenerateCodesDialog(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void clickRegenerateButton() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(regeneratePageButton));
        button.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialogContainer));
    }

    public boolean isDialogDisplayed() {
        try {
            return driver.findElement(dialogContainer).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPasswordInputDisplayed() {
        try {
            return driver.findElement(passwordInput).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRegenerateButtonDisplayed() {
        try {
            return driver.findElement(regenerateDialogButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCancelButtonDisplayed() {
        try {
            return driver.findElement(cancelButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public RegenerateCodesDialog enterPassword(String password) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(passwordInput));
        input.clear();
        input.sendKeys(password);
        return this;
    }

    public void clickRegenerate() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(regenerateDialogButton));
        button.click();
    }

    public void clickCancel() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        button.click();
    }

    /**
     * Waits for the dialog to transition from the password step to the codes display step.
     */
    public void waitForCodesDialog() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(codesList));
    }

    public boolean isCodesDialogDisplayed() {
        try {
            return driver.findElement(codesList).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCloseButtonDisplayed() {
        try {
            return driver.findElement(closeButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDownloadButtonDisplayed() {
        try {
            return driver.findElement(downloadButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts recovery codes from the codes display dialog.
     * Codes are rendered as mat-list-item elements with data-testid starting with "recovery-codes-code".
     */
    public List<String> extractNewCodes() {
        waitForCodesDialog();

        List<WebElement> items = driver.findElements(codeItems);
        List<String> codes = new ArrayList<>();
        for (WebElement item : items) {
            String text = item.getText().trim();
            if (!text.isBlank()) {
                codes.add(text);
            }
        }
        return codes;
    }

    /**
     * The Close button is disabled until the user downloads, prints, or copies the codes.
     * Click Download first to enable Close, then click Close.
     */
    public void downloadAndClose() {
        WebElement download = wait.until(ExpectedConditions.elementToBeClickable(downloadButton));
        download.click();

        WebElement close = wait.until(ExpectedConditions.elementToBeClickable(closeButton));
        close.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogContainer));
    }

    public String getDialogHtml() {
        try {
            return driver.findElement(dialogContainer).getAttribute("innerHTML");
        } catch (Exception e) {
            return "";
        }
    }

    public String getDialogText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dialogContainer)).getText();
    }

    public String getPasswordInputType() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).getAttribute("type");
    }

    public void clickPasswordToggle() {
        WebElement toggle = wait.until(ExpectedConditions.elementToBeClickable(passwordToggleButton));
        toggle.click();
    }

    public boolean isRegenerateButtonEnabled() {
        try {
            return driver.findElement(regenerateDialogButton).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForDialogClosed() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogContainer));
    }

    public boolean hasSnackbarError() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("simple-snack-bar, .mat-mdc-snack-bar-container, .mat-snack-bar-container")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSnackbarAtBottom() {
        WebElement snackbar = driver.findElement(
                By.cssSelector("simple-snack-bar, .mat-mdc-snack-bar-container, .mat-snack-bar-container"));
        int snackbarY = snackbar.getLocation().getY() + snackbar.getSize().getHeight();
        int viewportHeight = driver.manage().window().getSize().getHeight();
        return snackbarY > viewportHeight / 2;
    }

    public String getSnackbarErrorMessage() {
        try {
            WebElement snackbar = driver.findElement(
                    By.cssSelector("simple-snack-bar span, .mat-mdc-snack-bar-label, .mat-simple-snackbar"));
            return snackbar.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void dismissSnackbar() {
        try {
            WebElement action = driver.findElement(
                    By.cssSelector("simple-snack-bar button, .mat-simple-snackbar-action button, .mat-mdc-snack-bar-action button"));
            action.click();
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.invisibilityOfElementLocated(
                            By.cssSelector("simple-snack-bar, .mat-mdc-snack-bar-container")));
        } catch (Exception e) {
            // Snackbar may auto-dismiss
        }
    }

    // --- Codes dialog text verification methods ---

    public String getCodesDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(codesDialogTitle)).getText();
    }

    public String getCodesDialogDescription() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(codesDialogDescription)).getText();
    }

    public String getCodesDialogWarningText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(codesDialogWarning)).getText();
    }

    public boolean isWarningIconDisplayed() {
        try {
            return driver.findElement(codesDialogWarningIcon).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCodesDialogInstructionText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(codesDialogInstruction)).getText();
    }

    // --- Action button verification methods ---

    public boolean isPrintButtonDisplayed() {
        try {
            return driver.findElement(printButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCopyButtonDisplayed() {
        try {
            return driver.findElement(copyButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getDownloadButtonAriaLabel() {
        return driver.findElement(downloadButton).getAttribute("aria-label");
    }

    public String getPrintButtonAriaLabel() {
        return driver.findElement(printButton).getAttribute("aria-label");
    }

    public String getCopyButtonAriaLabel() {
        return driver.findElement(copyButton).getAttribute("aria-label");
    }

    public String getDownloadButtonIconText() {
        return driver.findElement(downloadButton).findElement(By.cssSelector("mat-icon")).getText();
    }

    public String getPrintButtonIconText() {
        return driver.findElement(printButton).findElement(By.cssSelector("mat-icon")).getText();
    }

    public String getCopyButtonIconText() {
        return driver.findElement(copyButton).findElement(By.cssSelector("mat-icon")).getText();
    }

    // --- Close button state ---

    public boolean isCloseButtonEnabled() {
        try {
            return driver.findElement(closeButton).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // --- Click outside dialog (backdrop) ---

    public void clickBackdrop() {
        WebElement overlay = wait.until(ExpectedConditions.presenceOfElementLocated(backdrop));
        // Click at coordinates offset from center to ensure we hit the backdrop, not the dialog
        new org.openqa.selenium.interactions.Actions(driver)
                .moveToElement(overlay, -overlay.getSize().getWidth() / 2 + 5, 0)
                .click()
                .perform();
    }

    // --- Copy button clipboard content ---

    public void clickCopyButton() {
        wait.until(ExpectedConditions.elementToBeClickable(copyButton)).click();
    }

    /**
     * Reads the clipboard content via CDP after the Copy button has been clicked.
     * Grants clipboardReadWrite permission first, then reads via the Clipboard API.
     */
    public List<String> readClipboardCodes() {
        if (driver instanceof ChromeDriver chromeDriver) {
            chromeDriver.executeCdpCommand("Browser.grantPermissions",
                    Map.of("permissions", List.of("clipboardReadWrite")));
        }

        String clipboard = (String) ((JavascriptExecutor) driver).executeAsyncScript(
                "var cb = arguments[arguments.length - 1];" +
                "navigator.clipboard.readText()" +
                ".then(function(text) { cb(text); })" +
                ".catch(function(e) { cb(''); });");

        if (clipboard == null || clipboard.isBlank()) return List.of();
        return Arrays.asList(clipboard.split(","));
    }

    public boolean hasCopySnackbar() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(snackbarContainer));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCopySnackbarMessage() {
        try {
            WebElement snackbar = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(snackbarContainer));
            return snackbar.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    // --- Download button ---

    public void clickDownloadButton() {
        wait.until(ExpectedConditions.elementToBeClickable(downloadButton)).click();
    }

    public void clickCloseButton() {
        wait.until(ExpectedConditions.elementToBeClickable(closeButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogContainer));
    }

    // --- Download file methods ---

    /**
     * Waits for a file to appear in the download directory (up to 10 seconds).
     * Returns the path to the most recently modified file.
     */
    public Path findDownloadedFile(Path downloadDir) {
        WebDriverWait fileWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return fileWait.until(d -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadDir)) {
                Path newest = null;
                long newestTime = 0;
                for (Path file : stream) {
                    // Skip Chrome's .crdownload partial files
                    if (file.toString().endsWith(".crdownload")) continue;
                    long modified = Files.getLastModifiedTime(file).toMillis();
                    if (modified > newestTime) {
                        newestTime = modified;
                        newest = file;
                    }
                }
                return newest;
            } catch (IOException e) {
                return null;
            }
        });
    }

    /**
     * Reads the downloaded text file and extracts recovery codes (one per line, non-blank).
     */
    public List<String> extractCodesFromDownloadedFile(Path filePath) throws IOException {
        List<String> codes = new ArrayList<>();
        for (String line : Files.readAllLines(filePath, java.nio.charset.StandardCharsets.ISO_8859_1)) {
            String trimmed = line.trim();
            if (!trimmed.isBlank()) {
                codes.add(trimmed);
            }
        }
        return codes;
    }

    // --- Print / PDF methods ---

    /**
     * Captures the current page (with codes dialog visible) as a PDF using CDP Page.printToPDF.
     * This avoids the Print button's window.print() which blocks the renderer in non-headless mode.
     */
    public void captureCurrentPageAsPdf(Path pdfPath) throws IOException {
        if (driver instanceof ChromeDriver chromeDriver) {
            Map<String, Object> result = chromeDriver.executeCdpCommand("Page.printToPDF",
                    Map.of("printBackground", true));
            String base64Pdf = (String) result.get("data");
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);
            Files.createDirectories(pdfPath.getParent());
            Files.write(pdfPath, pdfBytes);
        }
    }

    /**
     * Extracts recovery codes from a PDF file using PDFBox.
     * Codes are 10-character strings matching the pattern used by the app.
     */
    public List<String> extractCodesFromPdf(Path pdfPath) throws IOException {
        List<String> codes = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
            String text = new PDFTextStripper().getText(doc);
            // Split by whitespace and match 10-char code-like tokens
            // (PDF text may have codes mixed with labels, numbering, etc.)
            Pattern codePattern = Pattern.compile("[A-Za-z0-9%!@#$^&*]{10}");
            for (String token : text.split("\\s+")) {
                if (codePattern.matcher(token).matches()) {
                    codes.add(token);
                }
            }
        }
        return codes;
    }

    // --- Print button: suppress window.print() to prevent native dialog ---

    /**
     * Suppresses window.print() and window.open(), clicks Print, then restores both.
     * The Print button calls window.print() on the current page — the same content
     * that CDP Page.printToPDF captures. This method verifies the button is functional
     * without triggering the native print dialog that would block the renderer.
     */
    public void clickPrintSuppressed() {
        ((JavascriptExecutor) driver).executeScript(
                "window.__origPrint = window.print;" +
                "window.__origOpen = window.open;" +
                "window.__printCalled = false;" +
                "window.__openCalled = false;" +
                "window.print = function() { window.__printCalled = true; };" +
                "window.open = function() {" +
                "  window.__openCalled = true;" +
                "  return { document: { write:function(){}, writeln:function(){}, close:function(){} }," +
                "           print:function(){}, close:function(){}, focus:function(){}," +
                "           addEventListener:function(){} };" +
                "};");

        wait.until(ExpectedConditions.elementToBeClickable(printButton)).click();

        // Wait until the click handler invokes print() or open()
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> {
                    Boolean printed = (Boolean) ((JavascriptExecutor) d)
                            .executeScript("return window.__printCalled || window.__openCalled;");
                    return Boolean.TRUE.equals(printed);
                });

        // Restore originals
        ((JavascriptExecutor) driver).executeScript(
                "window.print = window.__origPrint;" +
                "window.open = window.__origOpen;");
    }

    public boolean wasPrintTriggered() {
        Boolean printCalled = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return window.__printCalled === true;");
        Boolean openCalled = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return window.__openCalled === true;");
        return Boolean.TRUE.equals(printCalled) || Boolean.TRUE.equals(openCalled);
    }
}
