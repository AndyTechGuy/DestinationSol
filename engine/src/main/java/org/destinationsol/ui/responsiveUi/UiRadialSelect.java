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
package org.destinationsol.ui.responsiveUi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import org.destinationsol.SolApplication;
import org.destinationsol.common.SolColor;
import org.destinationsol.common.SolMath;
import org.destinationsol.ui.DisplayDimensions;
import org.destinationsol.ui.FontSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UiRadialSelect extends AbstractUiElement{

    /**
     * The rotation of the entire menu from the right-side reference point in degrees.
     */
    private static final int GENERAL_ROTATION = 0;

    /**
     * The additional shift of each segment away from the center in pixels. Used to create gaps.
     */
    private static final int SEGMENT_SHIFT = 20;

    /**
     * The ratio of the inner circle radius over the radius of the entire circle.
     */
    private static final float INNER_CIRCLE_RADIUS_RATIO = 0.3f;

    /**
     * The ratio of the offset of the text from the center of the circle over the radius of the entire circle.
     */
    private static final float TEXT_RADIUS_RATIO = 0.5f;

    /**
     * The texture quality of all pixmaps, by a ratio of width/height of the pixmap to width/height of the circle.
     * Higher values will make a smoother circle.
     */
    private static final float PIXMAP_QUALITY = 4f;

    /**
     * The size of the font for every element in the circle
     */
    private float fontSize = FontSize.HUD;

    /**
     * The list of {@link RadialSegment}s in this UiRadialSelect
     */
    private List<RadialSegment> radialSegments = new ArrayList<>();

    /**
     * The radius of the entire circle.
     */
    private int radius;

    /**
     * The TextureRegion containing the segment shape and color for a radial element while not hovered or selected.
     */
    private TextureRegion segmentRegion;

    @Override
    public UiRadialSelect setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
        recalculate();
        return null;
    }

    /**
     * Sets the radius of the entire circle.
     *
     * @param newRadius The new radius of this circle.
     * @return This element
     */
    public UiRadialSelect setRadius(int newRadius) {
        this.radius = newRadius;
        recalculate();
        return this;
    }

    @Override
    public int getWidth() {
        return radius * 2;
    }

    @Override
    public int getHeight() {
        return radius * 2;
    }

    @Override
    public UiElement recalculate() {
        Pixmap segmentMap = createSegmentPixmap(radialSegments.size());
        segmentRegion = new TextureRegion(new Texture(segmentMap));
        segmentMap.dispose();
        positionSegments();

        return null;
    }

    @Override
    public UiElement setParent(UiContainerElement parent) {
        this.parent = Optional.of(parent);
        return this;
    }

    @Override
    public void draw() {
        Color col = SolColor.UI_DARK;
        DisplayDimensions displayDimensions = SolApplication.displayDimensions;

        float unitRadius = displayDimensions.getFloatHeightForPixelHeight(radius);
        float unitDiameter = unitRadius * 2;
        float unitX = displayDimensions.getFloatWidthForPixelWidth(x);
        float unitY = displayDimensions.getFloatHeightForPixelHeight(y);

        for (RadialSegment segment : radialSegments) {
            SolApplication.getUiDrawer().draw(segmentRegion,
                    unitDiameter, unitDiameter,
                    unitRadius, unitRadius,
                    unitX + displayDimensions.getFloatWidthForPixelWidth((int) (segment.xRatio * SEGMENT_SHIFT)), unitY + displayDimensions.getFloatHeightForPixelHeight((int) (segment.yRatio * SEGMENT_SHIFT)),
                    segment.angle + (360 / (float) radialSegments.size()) / 2, col);
            SolApplication.getUiDrawer().drawString(segment.label,
                    unitX + ((displayDimensions.getFloatWidthForPixelWidth(SEGMENT_SHIFT) + (unitRadius*INNER_CIRCLE_RADIUS_RATIO) + (unitRadius*(TEXT_RADIUS_RATIO*(1-INNER_CIRCLE_RADIUS_RATIO)))) * segment.xRatio),
                    unitY + ((displayDimensions.getFloatHeightForPixelHeight(SEGMENT_SHIFT) + (unitRadius*INNER_CIRCLE_RADIUS_RATIO) + (unitRadius*(TEXT_RADIUS_RATIO*(1-INNER_CIRCLE_RADIUS_RATIO)))) * segment.yRatio),
                    fontSize, true, SolColor.WHITE);
        }
    }

    /**
     * Creates the base circle pixmap with a clear inner circle. Used to create all segments.
     *
     * @return The new circle pixmap.
     */
    private Pixmap createCirclePixmap() {
        Color col = Color.WHITE;

        int pixmapDiameter = (int) (2 * radius * PIXMAP_QUALITY);
        int pixmapRadius = pixmapDiameter / 2;

        Pixmap circle = new Pixmap(pixmapDiameter, pixmapDiameter, Pixmap.Format.RGBA8888);
        Pixmap.setBlending(Pixmap.Blending.None);

        // colors for pixmaps are inverted from dest-sol colors
        circle.setColor(new Color(col).add(1 - col.r, 1 - col.g, 1 - col.b, 1 - col.a));

        circle.fillCircle(pixmapRadius, pixmapRadius, pixmapRadius);
        circle.setColor(Color.CLEAR);
        circle.fillCircle(pixmapRadius, pixmapRadius, (int) (pixmapRadius * INNER_CIRCLE_RADIUS_RATIO));

        return circle;
    }

    private Pixmap createSegmentPixmap(int segmentCount) {
        Pixmap segment = createCirclePixmap();
        segment.setColor(Color.CLEAR);

        // diameter and radius for the pixmap are not the same as the general radius, ex. if PIXMAP_QUALITY is 2.0f
        int segDiameter = segment.getWidth();
        int segRadius = segDiameter / 2;

        double angle;
        if (segmentCount == 0) {
            angle = 360;
        } else {
            angle =  360 / (double) segmentCount;
        }

        // Bottom half of the circle
        if (angle < 360) {
            segment.fillRectangle(0, 0, segment.getWidth(), segment.getHeight() / 2);
        }

        // Top-left quarter
        if (angle < 180) {
            float quarterAngle = (float) Math.min(180 - angle, 90);

            Vector2 circlePosition = SolMath.getVec(segRadius, 0);
            SolMath.rotate(circlePosition, quarterAngle);

            // flipped since the highest point in the circle requires the longest xLength and vice-versa.
            int xLength = (int) circlePosition.y;
            int yLength = (int) circlePosition.x;
            SolMath.free(circlePosition);

            segment.fillRectangle(
                    0, segRadius, // left side of the circle
                    xLength, segRadius); // width set to the calculated width, and height set to the radius of the circle.

            segment.fillTriangle(
                    xLength, segRadius, // x set to the calculated width, and y set to the middle of the circle
                    xLength, yLength + segRadius, // x set to the calculated width, and y set to the middle of the circle offset by the calculated height
                    segRadius, segRadius); // middle of the circle
        }

        // Top-Right quarter
        if (angle < 90) {
            Vector2 circlePosition = SolMath.getVec(segRadius, 0);
            SolMath.rotate(circlePosition, (float) angle);

            int xLength = (int) circlePosition.x;
            int yLength = (int) circlePosition.y;
            SolMath.free(circlePosition);

            segment.fillRectangle(segRadius, segDiameter - (segRadius - yLength),
                    segRadius, segRadius - yLength);

            segment.fillTriangle(segRadius, segRadius + yLength,
                    xLength + segRadius, segRadius + yLength,
                    segRadius, segRadius);
        }


        return segment;
    }

    private void positionSegments() {
        for (RadialSegment segment : radialSegments) {
            float angle = ((360f / radialSegments.size()) * radialSegments.indexOf(segment)) + GENERAL_ROTATION;
            Vector2 elementRatio = SolMath.getVec(1, 0);
            SolMath.rotate(elementRatio, angle);

            segment.angle = angle;
            segment.xRatio = elementRatio.x;
            segment.yRatio = elementRatio.y;
            SolMath.free(elementRatio);
        }
    }

    public UiRadialSelect addSegment(String text, UiCallback action, int keyCode) {
        radialSegments.add(new RadialSegment(text, action, keyCode));
        recalculate();

        return this;
    }

    public class RadialSegment {
        private String label;
        private float angle;
        private float xRatio;
        private float yRatio;
        private UiCallback action;
        private boolean wasPressed;
        private boolean isAreaPressed;
        private boolean isKeyPressed;
        private boolean isMouseOver;
        private int keyCode;
        private boolean isSoundEnabled = false;

        RadialSegment(String label, UiCallback action, int keyCode) {
            this.angle = 0; // default, set later by recalculate()
            this.label = label;
            this.action = action;
            this.keyCode = keyCode;
        }
    }
}
