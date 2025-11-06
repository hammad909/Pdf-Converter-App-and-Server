package com.example.pdfconverter.helper


fun parseErrorMessage(response: retrofit2.Response<*>): String {
    return try {
        val errorJson = response.errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            val json = org.json.JSONObject(errorJson)
            json.optString("message", json.optString("error", "Unknown server error"))
        } else {
            "Unknown server error"
        }
    } catch (e: Exception) {
        "Error parsing server response"
    }
}

