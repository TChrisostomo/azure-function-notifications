package org.leonardo.models.enums;

public enum AzureFunctionResponsesEnum {

    ERROR_CONVERTING_BODY("Error Converting Request Body"),
    OK("Message Created Successfuly");


    private String response;

    AzureFunctionResponsesEnum(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
