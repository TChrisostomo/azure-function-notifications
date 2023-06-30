package org.leonardo.functions;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.leonardo.models.dto.MoodleNotificationDto;
import org.leonardo.models.enums.AzureFunctionResponsesEnum;
import org.leonardo.utils.Constants;

import java.util.List;

public class NotificationFunction {


    /**
     * This function listens at endpoint "/api/sendNotification".
     * By adding to the host.json file extensions -> http -> routePrefix = "",
     * we remove "api" from the endpoint, and it becomes "/sendNotification"
     */
    @FunctionName("sendNotification")
    public HttpResponseMessage send (
            @HttpTrigger(name = "req", methods = {HttpMethod.POST},
            authLevel = AuthorizationLevel.FUNCTION)HttpRequestMessage<List<MoodleNotificationDto>> request,
            final ExecutionContext context){


        context.getLogger().info("Notification Request Triggered");

        List<MoodleNotificationDto> notificationList = request.getBody();

        /**
         * Use ObjectWritter to transform our Notification Object into a json string
         */
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();


        /**
         * create ServiceBusSenderClient (azure service bus dependency) with topic name
         * in order to avoid using Bindings, since they only accept
         * sending messages to specific subscriptions and not to the topic itself
         */
        ServiceBusSenderClient serviceBusSenderClient = new ServiceBusClientBuilder()
                .sender()
                .topicName("topic-notifications").buildClient();

        for(MoodleNotificationDto moodleNotificationDto : notificationList) {

            try {
                String json = objectWriter.writeValueAsString(moodleNotificationDto);


                ServiceBusMessage message = new ServiceBusMessage(json);

                /**
                 * Adding Custom Property "notificationtype" so the topic can filter it in the right
                 * subscription
                 */
                message.getApplicationProperties().put(Constants.NOTIFICATION_TYPE, moodleNotificationDto.getNotificationtype());

                //message.setSubject(moodleNotificationDto.getNotificationtype());

                serviceBusSenderClient.sendMessage(message);
            } catch (Exception e) {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(AzureFunctionResponsesEnum.ERROR_CONVERTING_BODY).build();
            }

        }

        return request.createResponseBuilder(HttpStatus.OK).body(AzureFunctionResponsesEnum.OK).build();
    }
}
