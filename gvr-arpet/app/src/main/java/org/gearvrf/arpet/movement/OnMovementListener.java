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

package org.gearvrf.arpet.movement;

/**
 * Interface called when movement occurs.
 *
 * @param <Movable>  The object to be moved.
 * @param <Target>   The target object.
 * @param <Position> The new position returned when movement occurs.
 */
public interface OnMovementListener<Movable extends MovableObject, Target extends TargetObject, Position> {

    void onStartMove();

    void onMove(Movable movable, Target target, Position position);

    void onStopMove();
}
