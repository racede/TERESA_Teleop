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

import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.opentok.android.Session.SessionListener;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.RosActivity;
import org.ros.android.view.RosImageView;
import org.ros.android.view.VirtualJoystickView;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;
import java.util.HashMap;

import at.markushi.ui.CircleButton;

/**
 * An app that can be used to control and intereact with the TERESA robot.
 *
 * @author Ricardo Acedo de Talavera
 */
public class MainActivity extends RosActivity implements SessionListener {

    private VirtualJoystickView virtualJoystickView;
    private RosImageView<sensor_msgs.CompressedImage> image;
    private RosCameraPreviewView rosCameraPreviewView;
    private int cameraId;
    private Session mSession;
    private Publisher mPublisher;
    private ArrayList<Subscriber> mSubscribers = new ArrayList<Subscriber>();
    private HashMap<Stream, Subscriber> mSubscriberStream = new HashMap<Stream, Subscriber>();
    private CircleButton headUp;
    private CircleButton headDown;
    private CircleButton tiltUp;
    private CircleButton tiltDown;
    private StalkPublisher stalkPublisher;
    private RelativeLayout container;
    private RelativeLayout content;


    public MainActivity() {
        super("Teleop", "Teleop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.virtual_joystick_snap:
                joystickSnaping(item);
                break;
            case R.id.action_switch_camera:
                switchCamera();
                break;
            case R.id.action_keyboard:
                showKeyboard();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showKeyboard() {
        headUp = (CircleButton) findViewById(R.id.head_up);
        headDown = (CircleButton) findViewById(R.id.head_down);
        tiltUp = (CircleButton) findViewById(R.id.tilt_up);
        tiltDown = (CircleButton) findViewById(R.id.tilt_down);

        if (headUp.getVisibility() == View.VISIBLE) {
            headUp.setVisibility(View.INVISIBLE);
            headDown.setVisibility(View.INVISIBLE);
            tiltUp.setVisibility(View.INVISIBLE);
            tiltDown.setVisibility(View.INVISIBLE);
        } else {
            headUp.setVisibility(View.VISIBLE);
            headDown.setVisibility(View.VISIBLE);
            tiltUp.setVisibility(View.VISIBLE);
            tiltDown.setVisibility(View.VISIBLE);

            headUp.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            headUp.setPressed(true);
                            stalkPublisher.sendHeadUp();
                            break;
                        case MotionEvent.ACTION_UP:
                            stalkPublisher.sendFinish();
                            headUp.setPressed(false);
                            break;
                    }
                    return true;
                }
            });

            headDown.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            stalkPublisher.sendHeadDown();
                            headDown.setPressed(true);
                            break;
                        case MotionEvent.ACTION_UP:
                            stalkPublisher.sendFinish();
                            headDown.setPressed(false);
                            break;
                    }
                    return true;
                }
            });

            tiltUp.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            stalkPublisher.sendTitlUp();
                            tiltUp.setPressed(true);
                            break;
                        case MotionEvent.ACTION_UP:
                            stalkPublisher.sendFinish();
                            tiltUp.setPressed(false);
                            break;
                    }
                    return true;
                }
            });

            tiltDown.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            stalkPublisher.sendTiltDown();
                            tiltDown.setPressed(true);
                            break;
                        case MotionEvent.ACTION_UP:
                            stalkPublisher.sendFinish();
                            tiltDown.setPressed(false);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void switchCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras > 1) {
            cameraId = (cameraId + 1) % numberOfCameras;
            rosCameraPreviewView.releaseCamera();
            rosCameraPreviewView.setCamera(Camera.open(cameraId));
            Toast.makeText(this, "Switching cameras.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No alternative cameras to switch to.", Toast.LENGTH_SHORT).show();
        }

    }

    private void joystickSnaping(MenuItem item) {
        if (!item.isChecked()) {
            item.setChecked(true);
            virtualJoystickView.EnableSnapping();
        } else {
            item.setChecked(false);
            virtualJoystickView.DisableSnapping();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rosCameraPreviewView = (RosCameraPreviewView) findViewById(R.id.ros_camera_preview_view);

        virtualJoystickView = (VirtualJoystickView) findViewById(R.id.virtual_joystick);
        virtualJoystickView.setTopicName("cmd_vel");
        image = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.image);
        image.setTopicName("rgb/image/compressed");
        //image.setTopicName("camera/rgb/image_raw/compressed");
        image.setMessageType(sensor_msgs.CompressedImage._TYPE);
        image.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        container = (RelativeLayout) findViewById(R.id.container);
        content = (RelativeLayout) findViewById(R.id.content);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ScaleContents.scaleContents(content,container);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        rosCameraPreviewView.releaseCamera();

        if (mSession != null) {
            mSession.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSession != null) {
            mSession.onResume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (isFinishing()) {
            if (mSession != null) {
                mSession.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {

        if (mSession != null) {
            mSession.disconnect();
        }
        super.onDestroy();
        finish();
    }


    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        if (Camera.getNumberOfCameras() > 0) {
            cameraId = 1;
        } else {
            cameraId = 0;
        }
        rosCameraPreviewView.setCamera(Camera.open(cameraId));

        stalkPublisher = new StalkPublisher();

        sessionConnect();

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(rosCameraPreviewView, nodeConfiguration.setNodeName("camera_android"));
        nodeMainExecutor.execute(virtualJoystickView, nodeConfiguration.setNodeName("virtual_joystick"));
        nodeMainExecutor.execute(image, nodeConfiguration.setNodeName("android/video_view"));
        nodeMainExecutor.execute(stalkPublisher, nodeConfiguration.setNodeName("stalk"));

    }

    private void sessionConnect() {
        if (mSession == null) {
            mSession = new Session(this, OpenTokConfig.API_KEY,
                    OpenTokConfig.SESSION_ID);
            mSession.setSessionListener(this);
            mSession.connect(OpenTokConfig.TOKEN);
        }
    }


    @Override
    public void onConnected(Session session) {
        mPublisher = new Publisher(this, "Publisher");
        // Publish audio only
        mPublisher.setPublishVideo(false);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Subscriber subscriber = new Subscriber(this, stream);

        // Subscribe audio only
        subscriber.setSubscribeToVideo(false);

        mSession.subscribe(subscriber);
        mSubscribers.add(subscriber);
        mSubscriberStream.put(stream, subscriber);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Subscriber subscriber = mSubscriberStream.get(stream);
        if (subscriber != null) {
            mSession.unsubscribe(subscriber);
            mSubscribers.remove(subscriber);
            mSubscriberStream.remove(stream);
        }
    }

    @Override
    public void onError(Session session, OpentokError error) {
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
    }

}
