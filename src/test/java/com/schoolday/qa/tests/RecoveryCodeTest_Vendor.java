package com.schoolday.qa.tests;

import com.schoolday.qa.base.BaseTest;
import com.schoolday.qa.pages.LoginPage;
import com.schoolday.qa.pages.MfaPage;
import com.schoolday.qa.pages.RecoveryCodePage;
import com.schoolday.qa.pages.RegenerateCodesDialog;
import com.schoolday.qa.pages.UserProfilePage;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class RecoveryCodeTest_Vendor extends BaseTest {

    @Override
    @BeforeEach
    protected void setUp() {
        // no-op — driver lifecycle managed by nested classes
    }

    @Override
    @AfterEach
    protected void tearDown() {
        // no-op — driver lifecycle managed by nested classes
    }

    protected void initializeDriver() throws Exception {
        super.setUp();
    }

    protected void destroyDriver() {
        super.tearDown();
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("UI Tests (1-4)")
    class UiTests {

        private MfaPage mfaPage;
        private RecoveryCodePage recoveryCodePage;

        @BeforeAll
        void initDriver() throws Exception {
            initializeDriver();
            LoginPage loginPage = new LoginPage(driver);
            loginPage.open(getBaseUrl());
            loginPage.login(getEmail(), getPassword());
            mfaPage = new MfaPage(driver);
            mfaPage.waitForPage();
            recoveryCodePage = new RecoveryCodePage(driver);
        }

        @AfterAll
        void closeDriver() {
            destroyDriver();
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
            recoveryCodePage.clickBackToMfa();

            mfaPage.waitForPage();
            assertTrue(mfaPage.isDisplayed(), "Should return to MFA page");
            assertEquals("Enter code", mfaPage.getHeadingText(), "MFA heading should be 'Enter code'");
        }

        @Test
        @Order(4)
        void testContactSupportLink() {
            mfaPage.clickRecoveryCodeLink();
            recoveryCodePage.waitForPage();

            String href = recoveryCodePage.getContactSupportHref();
            assertTrue(href.startsWith("https://share.hsforms.com/"),
                    "Contact Support href should point to hsforms.com, but was: " + href);

            recoveryCodePage.clickContactSupport();

            wait.until(d -> d.getCurrentUrl().startsWith("https://share.hsforms.com/"));
            assertTrue(driver.getCurrentUrl().startsWith("https://share.hsforms.com/"),
                    "Should navigate to hsforms.com, but URL was: " + driver.getCurrentUrl());
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Validation, Login & Regenerate Tests (5-15)")
    class ValidationLoginAndRegenerateTests {

        private RecoveryCodePage recoveryCodePage;
        private UserProfilePage userProfilePage;
        private RegenerateCodesDialog regenerateCodesDialog;

        @BeforeAll
        void initDriver() throws Exception {
            initializeDriver();
            LoginPage loginPage = new LoginPage(driver);
            loginPage.open(getBaseUrl());
            loginPage.login(getEmail(), getPassword());
            MfaPage mfaPage = new MfaPage(driver);
            mfaPage.waitForPage();
            mfaPage.clickRecoveryCodeLink();
            recoveryCodePage = new RecoveryCodePage(driver);
            recoveryCodePage.waitForPage();

            userProfilePage = new UserProfilePage(driver);
            regenerateCodesDialog = new RegenerateCodesDialog(driver);
        }

        @AfterAll
        void closeDriver() {
            destroyDriver();
        }

        @Test
        @Order(5)
        void testRecoveryCodeEmptySubmission() {
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
        void testAlreadyUsedRecoveryCode() throws Exception {
            assumeTrue(hasUsedRecoveryCodes(),
                    "No USED recovery codes in file — skipping");

            recoveryCodePage.dismissSnackbar();

            String usedCode = getUsedRecoveryCode();
            recoveryCodePage.enterRecoveryCode(usedCode);
            recoveryCodePage.clickConfirm();

            assertTrue(recoveryCodePage.hasSnackbarError(),
                    "Snackbar error should be shown for already-used recovery code");

            String errorMessage = recoveryCodePage.getSnackbarErrorMessage();
            assertFalse(errorMessage.isBlank(), "Error message should not be blank");

            assertTrue(hasHttpStatus(403),
                    "Server should respond with 403 for already-used recovery code");
        }

        @Test
        @Order(9)
        void testSuccessfulRecoveryCodeLogin() throws Exception {
            recoveryCodePage.dismissSnackbar();

            String recoveryCode = consumeRecoveryCode();

            recoveryCodePage.enterRecoveryCode(recoveryCode);
            recoveryCodePage.clickConfirm();

            wait.until(d -> d.getCurrentUrl().contains("/admin/institutions"));

            assertEquals("https://connect-qa.gg4l.com/admin/institutions", driver.getCurrentUrl(),
                    "Should redirect to /admin/institutions after successful recovery code login");
        }

        @Test
        @Order(10)
        void testNavigateToMyProfile() {
            userProfilePage.clickUserMenu();
            userProfilePage.clickMyProfile();

            assertTrue(driver.getCurrentUrl().contains("/user/profile"),
                    "Should navigate to user profile page");
            assertTrue(userProfilePage.isTabGroupDisplayed(), "Tab group should be visible");
            assertTrue(userProfilePage.isProfileTabDisplayed(), "Profile tab should be visible");
            assertTrue(userProfilePage.isSecuritySettingsTabDisplayed(), "Security Settings tab should be visible");
            assertTrue(userProfilePage.isEmailNotificationsTabDisplayed(), "Email Notifications tab should be visible");
            assertTrue(userProfilePage.isEditOrganizationTabDisplayed(), "Edit Organization tab should be visible");
        }

        @Test
        @Order(11)
        void testNavigateToSecuritySettingsAndOpenDialog() {
            userProfilePage.clickSecuritySettingsTab();

            assertTrue(userProfilePage.isSecuritySettingsContentLoaded(),
                    "Security settings content should be loaded");

            regenerateCodesDialog.clickRegenerateButton();

            assertTrue(regenerateCodesDialog.isDialogDisplayed(),
                    "Regenerate Recovery Codes dialog should be displayed");
        }

        @Test
        @Order(12)
        void testRegenerateDialogElements() {
            assertTrue(regenerateCodesDialog.isDialogDisplayed(), "Dialog should be displayed");
            assertTrue(regenerateCodesDialog.isPasswordInputDisplayed(), "Password input should be visible");
            assertTrue(regenerateCodesDialog.isRegenerateButtonDisplayed(), "Regenerate button should be visible");
            assertTrue(regenerateCodesDialog.isCancelButtonDisplayed(), "Cancel button should be visible");

            String dialogText = regenerateCodesDialog.getDialogText();
            assertTrue(dialogText.contains("Regenerate Recovery Codes"),
                    "Dialog should contain title 'Regenerate Recovery Codes'");
            assertTrue(dialogText.contains("enter your password"),
                    "Dialog should contain password instruction text");
            assertTrue(dialogText.contains("Old codes won't work after generating new ones"),
                    "Dialog should contain warning about old codes");
        }

        @Test
        @Order(13)
        void testRegenerateButtonDisabledWithEmptyPassword() {
            assertFalse(regenerateCodesDialog.isRegenerateButtonEnabled(),
                    "Regenerate button should be disabled when password is empty");
        }

        @Test
        @Order(14)
        void testPasswordVisibilityToggle() {
            regenerateCodesDialog.enterPassword("TestPassword");

            assertEquals("password", regenerateCodesDialog.getPasswordInputType(),
                    "Password input should be masked by default");

            regenerateCodesDialog.clickPasswordToggle();

            assertEquals("text", regenerateCodesDialog.getPasswordInputType(),
                    "Password should be visible after clicking toggle");

            regenerateCodesDialog.clickPasswordToggle();

            assertEquals("password", regenerateCodesDialog.getPasswordInputType(),
                    "Password should be masked again after clicking toggle");
        }

        @Test
        @Order(15)
        void testInvalidPasswordThenCancelOrRegenerate() throws Exception {
            // Dialog is already open from test 14
            regenerateCodesDialog.enterPassword("WrongPassword123!");
            regenerateCodesDialog.clickRegenerate();

            assertTrue(regenerateCodesDialog.hasSnackbarError(),
                    "Error should be shown for invalid password");
            assertTrue(regenerateCodesDialog.isSnackbarAtBottom(),
                    "Snackbar should appear at the bottom of the page");

            String errorMessage = regenerateCodesDialog.getSnackbarErrorMessage();
            assertFalse(errorMessage.isBlank(), "Error message should not be blank");

            regenerateCodesDialog.dismissSnackbar();

            if (countUnusedCodes() == 0) {
                // No codes left — regenerate using the already-open dialog
                regenerateCodesDialog.enterPassword(getPassword());
                regenerateCodesDialog.clickRegenerate();
                regenerateCodesDialog.waitForCodesDialog();

                List<String> newCodes = regenerateCodesDialog.extractNewCodes();

                // Save codes BEFORE asserting — regeneration already invalidated all previous codes
                if (!newCodes.isEmpty()) {
                    saveNewRecoveryCodes(newCodes);
                }

                regenerateCodesDialog.downloadAndClose();

                assertFalse(newCodes.isEmpty(), "New recovery codes should be generated");
                assertEquals(6, newCodes.size(),
                        "Should generate exactly 6 recovery codes, but got " + newCodes.size()
                                + ": " + newCodes);
            } else {
                // Codes still available — cancel and close dialog
                regenerateCodesDialog.clickCancel();
                regenerateCodesDialog.waitForDialogClosed();

                assertFalse(regenerateCodesDialog.isDialogDisplayed(),
                        "Dialog should be closed after clicking Cancel");
            }
        }
    }
}
