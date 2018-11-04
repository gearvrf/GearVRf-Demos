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

public class SimpleMessageReceiver implements MessageReceiver {

    private String mName;

    public SimpleMessageReceiver(@NonNull String name) {
        this.mName = name;
    }

    @Override
    public void onReceivePetAnchor(CloudAnchor petAnchor) throws MessageException {
    }

    @Override
    public void onReceiveViewCommand(ViewCommand command) throws MessageException {
    }

    @Override
    public void onReceivePetActionCommand(PetActionCommand command) throws MessageException {
    }

    @Override
    public void onReceiveBallCommand(BallCommand command) {
    }

    @Override
    public void onReceiveUpdatePoses(SharedObjectPose[] poses) throws MessageException {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMessageReceiver that = (SimpleMessageReceiver) o;
        return mName != null ? mName.equals(that.mName) : that.mName == null;
    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SimpleMessageReceiver{" +
                "mName='" + mName + '\'' +
                '}';
    }
}
