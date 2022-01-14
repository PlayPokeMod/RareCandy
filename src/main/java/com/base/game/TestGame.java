/*
 * Copyright (C) 2014 Benny Bobaganoosh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.base.game;

import com.base.engine.components.*;
import com.base.engine.core.*;
import com.base.engine.rendering.*;

import java.io.File;

public class TestGame extends Game {
    public void Init() {
        AddObject(
                //AddObject(
                new GameObject().AddComponent(new FreeLook(0.3f)).AddComponent(new FreeMove(10.0f))
                        .AddComponent(new Camera(new Matrix4f().InitPerspective((float) Math.toRadians(90.0f),
                                (float) Window.GetWidth() / (float) Window.GetHeight(), 0.01f, 1000.0f))));

        GameObject animatedObject = new GameObject().AddComponent(AnimationUtil.loadAnimatedFile(new File("/venusaur.glb")));
        animatedObject.GetTransform().SetScale(new Vector3f(0.4f, 0.4f, 0.4f));
        animatedObject.GetTransform().SetRot(new Quaternion(new Vector3f(1, 0, 0), (float) Math.toRadians(-90)));
        AddObject(animatedObject);
    }
}