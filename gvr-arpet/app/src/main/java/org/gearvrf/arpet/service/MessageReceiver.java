/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.service;

import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.service.data.BallCommand;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.arpet.service.share.SharedObjectPose;

public interface MessageReceiver {
    /**
     * Resolve end load the objects represented by given shared scene.
     *
     * @param cloudAnchors Anchors to resolve.
     * @throws MessageException If any exception occurs.
     */
    void onReceiveShareCloudAnchors(CloudAnchor[] cloudAnchors) throws MessageException;

    /**
     * Execute the given command on this device
     *
     * @param command View command to execute.
     * @throws MessageException If any exception occurs
     */
    void onReceiveViewCommand(ViewCommand command) throws MessageException;

    /**
     * Execute the given command on this device
     *
     * @param command Ball command to execute.
     * @throws MessageException If any exception occurs
     */
    void onReceiveBallCommand(BallCommand command) throws MessageException;

    /**
     * @param poses Objects to update
     * @throws MessageException If an exception occurs
     */
    void onReceiveUpdatePoses(SharedObjectPose[] poses) throws MessageException;
}
