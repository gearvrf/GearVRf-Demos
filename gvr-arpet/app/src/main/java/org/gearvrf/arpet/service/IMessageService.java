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
import android.support.annotation.StringDef;

import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.service.data.BallCommand;
import org.gearvrf.arpet.service.data.PetActionCommand;
import org.gearvrf.arpet.service.data.RequestStatus;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.arpet.service.share.SharedObjectPose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IMessageService {

    String MESSAGE_TYPE_PET_ANCHOR = "MESSAGE_TYPE_PET_ANCHOR";
    String MESSAGE_TYPE_VIEW_COMMAND = "MESSAGE_TYPE_VIEW_COMMAND";
    String MESSAGE_TYPE_BALL_COMMAND = "MESSAGE_TYPE_BALL_COMMAND";
    String MESSAGE_TYPE_PET_ACTION_COMMAND = "MESSAGE_TYPE_PET_ACTION_COMMAND";
    String MESSAGE_TYPE_UPDATE_POSES = "MESSAGE_TYPE_UPDATE_POSES";
    String MESSAGE_TYPE_REQUEST_STATUS = "MESSAGE_TYPE_REQUEST_STATUS";


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            MESSAGE_TYPE_PET_ANCHOR,
            MESSAGE_TYPE_VIEW_COMMAND,
            MESSAGE_TYPE_BALL_COMMAND,
            MESSAGE_TYPE_PET_ACTION_COMMAND,
            MESSAGE_TYPE_UPDATE_POSES,
            MESSAGE_TYPE_REQUEST_STATUS
    })
    @interface MessageType {
    }

    int sharePetAnchor(@NonNull CloudAnchor petAnchor);

    void sendViewCommand(@NonNull ViewCommand command);

    void sendBallCommand(@NonNull BallCommand command);

    void sendPetActionCommand(@NonNull PetActionCommand command);

    void updatePoses(@NonNull SharedObjectPose[] poses);

    void sendRequestStatus(RequestStatus status);
}