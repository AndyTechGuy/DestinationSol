/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.destinationsol.game.screens;

import org.destinationsol.GameOptions;
import org.destinationsol.SolApplication;
import org.destinationsol.game.MapDrawer;
import org.destinationsol.game.SolGame;
import org.destinationsol.ui.FontSize;
import org.destinationsol.ui.SolUiBaseScreen;
import org.destinationsol.ui.responsiveUi.UiRelativeLayout;
import org.destinationsol.ui.responsiveUi.UiVerticalListLayout;
import org.destinationsol.ui.responsiveUi.UiActionButton;
import org.destinationsol.ui.responsiveUi.UiTextBox;

import static org.destinationsol.ui.UiDrawer.UI_POSITION_TOP_RIGHT;
import static org.destinationsol.ui.responsiveUi.UiTextButton.DEFAULT_BUTTON_PADDING;

public class MapScreen extends SolUiBaseScreen {

    private static final int MAP_CONTROL_TOP_OFFSET = 300;

    private final UiVerticalListLayout buttonList;
    private final UiActionButton zoomOutButton;
    private final UiActionButton closeButton;
    private final UiActionButton zoomInButton;

    MapScreen(GameOptions gameOptions) {
        closeButton = new UiActionButton().addElement(new UiTextBox().setText("Close").setFontSize(FontSize.MENU))
                .setSoundEnabled(true)
                .setKeyCode(gameOptions.getKeyClose())
                .setAction(uiElement -> {
                    SolApplication.getInstance().getGame().getMapDrawer().setToggled(false);
                    SolApplication.getInstance().getGame().setPaused(false);
                    SolApplication.changeScreen(SolApplication.getInstance().getGame().getScreens().mainGameScreen);
                });

        zoomInButton = new UiActionButton().addElement(new UiTextBox().setText("Zoom In").setFontSize(FontSize.MENU))
                .setSoundEnabled(true)
                .setKeyCode(gameOptions.getKeyZoomIn())
                .setAction(uiElement -> {
                    MapDrawer mapDrawer = SolApplication.getInstance().getGame().getMapDrawer();
                    mapDrawer.changeZoom(true);
                    checkZoom(mapDrawer);
                });

        zoomOutButton = new UiActionButton().addElement(new UiTextBox().setText("Zoom Out").setFontSize(FontSize.MENU))
                .setSoundEnabled(true)
                .setKeyCode(gameOptions.getKeyZoomOut())
                .setAction(uiElement -> {
                    MapDrawer mapDrawer = SolApplication.getInstance().getGame().getMapDrawer();
                    mapDrawer.changeZoom(false);
                    checkZoom(mapDrawer);
                });

        buttonList = new UiVerticalListLayout()
                .addElement(closeButton)
                .addElement(zoomInButton)
                .addElement(zoomOutButton);

        rootUiElement = new UiRelativeLayout().addElement(buttonList, UI_POSITION_TOP_RIGHT, -buttonList.getWidth() / 2 - DEFAULT_BUTTON_PADDING, MAP_CONTROL_TOP_OFFSET);
    }

    @Override
    public void onAdd(SolApplication solApplication) {
        super.onAdd(solApplication);

        MapDrawer mapDrawer = SolApplication.getInstance().getGame().getMapDrawer();
        mapDrawer.setToggled(true);
    }

    private void checkZoom(MapDrawer mapDrawer) {
        zoomInButton.setEnabled(mapDrawer.getZoom() != MapDrawer.MIN_ZOOM);
        zoomOutButton.setEnabled(mapDrawer.getZoom() != MapDrawer.MAX_ZOOM);
    }
}
