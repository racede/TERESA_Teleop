

/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.android_teresa_teleop;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import giraff_driver.Stalk;

/**
 * A publisher for Stalk messages.
 *
 * @author Ricardo Acedo de Talavera
 */
public class StalkPublisher extends AbstractNodeMain {
    private String topic_name;
    private Publisher<Stalk> publisher;

    public StalkPublisher() {
        topic_name = "stalk";
    }

    public StalkPublisher(String topic)
    {
        topic_name = topic;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_pubsub/talker");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
         publisher = connectedNode.newPublisher(topic_name, Stalk._TYPE);
    }

    public void sendHeadUp(){
        Stalk stalk = publisher.newMessage();
        stalk.setHeadDown(false);
        stalk.setHeadUp(true);
        stalk.setTiltDown(false);
        stalk.setTiltUp(false);
        publisher.publish(stalk);
    }

    public void sendHeadDown(){
        Stalk stalk = publisher.newMessage();
        stalk.setHeadDown(true);
        stalk.setHeadUp(false);
        stalk.setTiltDown(false);
        stalk.setTiltUp(false);
        publisher.publish(stalk);
    }

    public void sendTiltDown(){
        Stalk stalk = publisher.newMessage();
        stalk.setHeadDown(false);
        stalk.setHeadUp(false);
        stalk.setTiltDown(true);
        stalk.setTiltUp(false);
        publisher.publish(stalk);
    }

    public void sendTitlUp(){
        Stalk stalk = publisher.newMessage();
        stalk.setHeadDown(false);
        stalk.setHeadUp(false);
        stalk.setTiltDown(false);
        stalk.setTiltUp(true);
        publisher.publish(stalk);
    }

    public void sendFinish(){
        Stalk stalk = publisher.newMessage();
        stalk.setHeadDown(false);
        stalk.setHeadUp(false);
        stalk.setTiltDown(false);
        stalk.setTiltUp(false);
        publisher.publish(stalk);
    }
}