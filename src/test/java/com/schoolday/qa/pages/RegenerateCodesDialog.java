package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
        return driver.findElement(dialogContainer).getText();
    }

    public String getPasswordInputType() {
        return driver.findElement(passwordInput).getAttribute("type");
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
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            WebElement action = driver.findElement(
                    By.cssSelector("simple-snack-bar button, .mat-simple-snackbar-action button, .mat-mdc-snack-bar-action button"));
            action.click();
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.invisibilityOfElementLocated(
                            By.cssSelector("simple-snack-bar, .mat-mdc-snack-bar-container")));
        } catch (Exception e) {
            // Snackbar may auto-dismiss
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        }
    }
}
