package org.leonardo.functions;

import java.util.*;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import org.leonardo.models.dto.MoodleNotificationDto;
import org.leonardo.models.enums.AzureFunctionResponsesEnum;
import org.leonardo.utils.Constants;

/**
 * Azure Functions with HTTP Trigger.
 */
public class HttpTriggerJava {
    /**
     * This function listens at endpoint "/api/HttpTriggerJava". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTriggerJava
     * 2. curl {your host}/api/HttpTriggerJava?name=HTTP%20Query
     */
    @FunctionName("HttpTriggerJava")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<List<MoodleNotificationDto>> request,
//            @ServiceBusTopicOutput(name = "message", topicName = "topic-notifications",subscriptionName = "FE-notifications", connection = "ServiceBusConnection") OutputBinding<String> messageFE,
//            @ServiceBusTopicOutput(name = "message", topicName = "topic-notifications",subscriptionName = "EMAIL-notifications", connection = "ServiceBusConnection") OutputBinding<String> messageEmail,
//            @ServiceBusTopicOutput(name = "message", topicName = "topic-notifications",subscriptionName = "Technical-subscription", connection = "ServiceBusConnection") OutputBinding<String> messageTechnical,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");


        List<MoodleNotificationDto> notificationList = request.getBody();

        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();


        ServiceBusSenderClient serviceBusSenderClient = new ServiceBusClientBuilder()
                .sender()
                .topicName("topic-notifications").buildClient();



        for(MoodleNotificationDto moodleNotificationDto : notificationList) {
            String json = "";
            try {
                json = objectWriter.writeValueAsString(moodleNotificationDto);


                ServiceBusMessage message = new ServiceBusMessage(json);

                message.getApplicationProperties().put(Constants.NOTIFICATION_TYPE, moodleNotificationDto.getNotificationtype());

                //                message.setSubject(moodleNotificationDto.getNotificationtype());

                serviceBusSenderClient.sendMessage(message);
            } catch (Exception e) {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(AzureFunctionResponsesEnum.ERROR_CONVERTING_BODY).build();
            }

        }

        return request.createResponseBuilder(HttpStatus.OK ).body(AzureFunctionResponsesEnum.OK).build();


//        if (name == null) {
//            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
//        } else {
//            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
//        }
    }


}
