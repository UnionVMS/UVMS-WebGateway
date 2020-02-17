/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.streamcollector;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.json.bind.Jsonb;
import java.util.Collections;
import java.util.List;

@MessageDriven(mappedName = "jms/topic/EventStream", activationConfig = {
    @ActivationConfigProperty(propertyName = "messagingType", propertyValue = "javax.jms.MessageListener"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = Constants.TOPIC_NAME)
})
public class StreamCollectorTopicListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(StreamCollectorTopicListener.class);

    @Inject
    private SSEResource sseResource;

    private Jsonb jsonb;

    @PostConstruct
    public void init() {
        jsonb = new JsonBConfigurator().getContext(null);
    }

    @Override
    public void onMessage(Message inMessage) {
        try {
            TextMessage textMessage = (TextMessage) inMessage;
            String eventName = textMessage.getStringProperty(Constants.EVENT);
            String subscriberJson = textMessage.getStringProperty(Constants.SUBSCRIBERLIST);
            List<String> subscriberList = (subscriberJson == null ? Collections.singletonList(Constants.ALL) : jsonb.fromJson(subscriberJson, List.class));
            String movementSourceString = textMessage.getStringProperty(Constants.MOVEMENT_SOURCE);
            MovementSourceType sourceType = (movementSourceString == null ? null : MovementSourceType.fromValue(movementSourceString));
            String data = textMessage.getText();

            sseResource.sendSSEEvent(data, eventName,subscriberList, sourceType);

        }catch (Exception e){
            LOG.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}