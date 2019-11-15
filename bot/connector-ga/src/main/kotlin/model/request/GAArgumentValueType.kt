package ai.tock.bot.connector.ga.model.request

import com.fasterxml.jackson.annotation.JsonValue

enum class GAArgumentValueType(@get:JsonValue val value: String){
    transactionRequirementsCheckResult("type.googleapis.com/google.actions.v2.TransactionRequirementsCheckResult"),
    transactionRequirementsCheckResultV3("type.googleapis.com/google.actions.transactions.v3.TransactionRequirementsCheckResult"),
    transactionDecisionValue("type.googleapis.com/google.actions.v2.TransactionDecisionValue"),
    holdValue("type.googleapis.com/google.actions.v2.HoldValue"),
    signInValue("type.googleapis.com/google.actions.v2.SignInValue"),
    newSurfaceValue("type.googleapis.com/google.actions.v2.NewSurfaceValue")
}