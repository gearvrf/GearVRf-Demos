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

import android.support.annotation.NonNull;

import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.service.data.BallCommand;
import org.gearvrf.arpet.service.data.PetActionCommand;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.arpet.service.share.SharedObjectPose;

public interface IMessageService {

    void sharePetAnchor(@NonNull CloudAnchor petAnchor, @NonNull MessageCallback<Void> callback);

    void sendViewCommand(@NonNull ViewCommand command, @NonNull MessageCallback<Void> callback);

    void sendBallCommand(@NonNull BallCommand command, @NonNull MessageCallback<Void> callback);

    void sendPetActionCommand(@NonNull PetActionCommand command, @NonNull MessageCallback<Void> callback);

    void updatePoses(@NonNull SharedObjectPose[] poses, @NonNull MessageCallback<Void> callback);

    void addMessageReceiver(MessageReceiver receiver);

    void removeMessageReceiver(MessageReceiver receiver);
}