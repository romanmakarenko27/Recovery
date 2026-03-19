package com.schoolday.qa.tests;

import com.schoolday.qa.base.BaseTest;
import com.schoolday.qa.pages.LoginPage;
import com.schoolday.qa.pages.MfaPage;
import com.schoolday.qa.pages.RecoveryCodePage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecoveryCodeTest extends BaseTest {

    private LoginPage loginPage;
    private MfaPage mfaPage;

    @BeforeEach
    void navigateToMfaPage() {
        loginPage = new LoginPage(driver);
        mfaPage = new MfaPage(driver);

        loginPage.open(getBaseUrl());
        loginPage.login(getEmail(), getPassword());
        mfaPage.waitForPage();
    }

    @Test
    @Order(1)
    void testMfaPageElementsDisplayed() {
        assertTrue(mfaPage.isDisplayed(), "MFA page should be displayed");
        assertEquals("Enter code", mfaPage.getHeadingText(), "Heading should be 'Enter code'");
        assertTrue(mfaPage.getSmsInfoText().contains("SMS"), "SMS info text should mention SMS");
        assertTrue(mfaPage.getSmsInfoText().contains("111"), "SMS info text should contain masked phone ending in 111");
        assertTrue(mfaPage.isCodeInputDisplayed(), "Code input should be visible");
        assertTrue(mfaPage.isConfirmButtonDisplayed(), "Confirm button should be visible");
        assertTrue(mfaPage.isSendNewCodeButtonDisplayed(), "Send New Code button should be visible");
        assertTrue(mfaPage.getSendNewCodeButtonText().contains("Send New Code"),
                "Send New Code button should contain timer text");
        assertTrue(mfaPage.isHavingProblemsTextDisplayed(), "'Having problems?' text should be visible");
        assertTrue(mfaPage.isRecoveryCodeLinkDisplayed(), "Recovery code link should be visible");
    }

    @Test
    @Order(2)
    void testNavigateToRecoveryCodePage() {
        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();

        assertTrue(recoveryCodePage.isDisplayed(), "Recovery Code page should be displayed");
        assertEquals("Enter Recovery code", recoveryCodePage.getHeadingText(),
                "Heading should be 'Enter Recovery code'");
        assertTrue(recoveryCodePage.getDescriptionText().contains("recovery codes"),
                "Description should mention recovery codes");
        assertTrue(recoveryCodePage.isRecoveryCodeInputDisplayed(), "Recovery code input should be visible");
        assertTrue(recoveryCodePage.isConfirmButtonDisplayed(), "Confirm button should be visible");
        assertTrue(recoveryCodePage.isContactSupportLinkDisplayed(), "Contact Support link should be visible");
    }

    @Test
    @Order(3)
    void testNavigateBackToMfaFromRecovery() {
        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();
        recoveryCodePage.clickBackToMfa();

        mfaPage.waitForPage();
        assertTrue(mfaPage.isDisplayed(), "Should return to MFA page");
        assertEquals("Enter code", mfaPage.getHeadingText(), "MFA heading should be 'Enter code'");
    }

    @Test
    @Order(4)
    void testContactSupportLink() {
        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();

        String href = recoveryCodePage.getContactSupportHref();
        assertTrue(href.startsWith("https://share.hsforms.com/"),
                "Contact Support href should point to hsforms.com, but was: " + href);

        recoveryCodePage.clickContactSupport();

        wait.until(d -> d.getCurrentUrl().startsWith("https://share.hsforms.com/"));
        assertTrue(driver.getCurrentUrl().startsWith("https://share.hsforms.com/"),
                "Should navigate to hsforms.com, but URL was: " + driver.getCurrentUrl());
    }

    @Test
    @Order(5)
    void testRecoveryCodeEmptySubmission() {
        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();

        String urlBefore = driver.getCurrentUrl();
        recoveryCodePage.clickConfirm();

        assertTrue(recoveryCodePage.hasValidationError(),
                "Validation error should be shown for empty submission");
        assertTrue(recoveryCodePage.isDisplayed(),
                "Should remain on Recovery Code page after empty submission");
        assertEquals(urlBefore, driver.getCurrentUrl(),
                "URL should not change after empty submission");
    }

    @Test
    @Order(6)
    void testRecoveryCodeShortCode() {
        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();

        String urlBefore = driver.getCurrentUrl();
        recoveryCodePage.enterRecoveryCode("ABC123");
        recoveryCodePage.clickConfirm();

        assertTrue(recoveryCodePage.hasValidationError(),
                "Validation error should be shown for short code");
        assertTrue(recoveryCodePage.isDisplayed(),
                "Should remain on Recovery Code page after short code submission");
        assertEquals(urlBefore, driver.getCurrentUrl(),
                "URL should not change after short code submission");
    }

    @Test
    @Order(7)
    void testRecoveryCodeInvalidCode() {
        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();
        recoveryCodePage.enterRecoveryCode("PK9P%vuMbv");
        recoveryCodePage.clickConfirm();

        assertTrue(recoveryCodePage.hasSnackbarError(),
                "Snackbar error should be shown for invalid recovery code");

        String errorMessage = recoveryCodePage.getSnackbarErrorMessage();
        assertFalse(errorMessage.isBlank(), "Error message should not be blank");

        assertTrue(hasHttpStatus(403),
                "Server should respond with 403 for invalid recovery code");
    }

    @Test
    @Order(8)
    void testSuccessfulRecoveryCodeLogin() throws Exception {
        String recoveryCode = consumeRecoveryCode();

        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();
        recoveryCodePage.enterRecoveryCode(recoveryCode);
        recoveryCodePage.clickConfirm();

        wait.until(d -> d.getCurrentUrl().contains("/admin/institutions"));

        assertEquals("https://connect-qa.gg4l.com/admin/institutions", driver.getCurrentUrl(),
                "Should redirect to /admin/institutions after successful recovery code login");
    }

    @Test
    @Order(9)
    void testAlreadyUsedRecoveryCode() throws Exception {
        String usedCode = getUsedRecoveryCode();

        mfaPage.clickRecoveryCodeLink();

        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);
        recoveryCodePage.waitForPage();
        recoveryCodePage.enterRecoveryCode(usedCode);
        recoveryCodePage.clickConfirm();

        assertTrue(recoveryCodePage.hasSnackbarError(),
                "Snackbar error should be shown for already-used recovery code");

        String errorMessage = recoveryCodePage.getSnackbarErrorMessage();
        assertFalse(errorMessage.isBlank(), "Error message should not be blank");

        assertTrue(hasHttpStatus(403),
                "Server should respond with 403 for already-used recovery code");
    }
}
