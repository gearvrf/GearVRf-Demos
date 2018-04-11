/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.immersivepedia.util;

import org.gearvrf.GVRRenderData;

public class RenderingOrderApplication {

    public static final int BUTTON_BOARD = GVRRenderData.GVRRenderingOrder.OVERLAY;
    public static final int VIDEO = GVRRenderData.GVRRenderingOrder.OVERLAY + 1;
    public static int LOADING_COMPONENT = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
    public static int GALLERY = GVRRenderData.GVRRenderingOrder.GEOMETRY;
    public static int GALLERY_PHOTO = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
    public static int TEXT = GVRRenderData.GVRRenderingOrder.TRANSPARENT + 2;
    public static int TEXT_BACKGROUND = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
    public static int DINOSAUR = GVRRenderData.GVRRenderingOrder.GEOMETRY;
    public static int GALLERY_SCROLLBAR = GVRRenderData.GVRRenderingOrder.GEOMETRY;
    public static int TOTEM = GVRRenderData.GVRRenderingOrder.GEOMETRY;

    public static final int BACKGROUND_IMAGE = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
    public static final int MAIN_IMAGE = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
    public static final int IMAGE_TEXT_BACKGROUND = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
    public static final int IMAGE_TEXT = GVRRenderData.GVRRenderingOrder.TRANSPARENT + 1;
}
