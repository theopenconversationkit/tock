package fr.vsct.tock.bot.admin.test

/**
 * Status available for a test plan execution.
 * PENDING - the test plan is currently executed.
 * SUCCESS - the test plan has been executed and ended without any errors.
 * FAILED - the test plan has been executed but some errors occurred.
 * COMPLETE - the test plan has been executed but there is no more information about test success.
 */
enum class TestPlanExecutionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    COMPLETE,
    UNKNOWN
}