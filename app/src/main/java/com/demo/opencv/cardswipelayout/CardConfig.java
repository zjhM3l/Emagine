package com.demo.opencv.cardswipelayout;

public final class CardConfig {
//    Displays the number of cards visible
    public static final int DEFAULT_SHOW_ITEM = 100;
//    The scale of the default scale
    public static final float DEFAULT_SCALE = 0.1f;
//    The offset of the Y-axis of the card is calculated as 14 aliquots
    public static final int DEFAULT_TRANSLATE_Y = 14;
//     The angle at which the card is tilted by default when it slides
    public static final float DEFAULT_ROTATE_DEGREE = 15f;

//     The card slides without tilting left or right

    public static final int SWIPING_NONE = 1;

//     When the card is swiped to the left

    public static final int SWIPING_LEFT = 1 << 2;

//     When the card slides to the right

    public static final int SWIPING_RIGHT = 1 << 3;

//     The card slides out from the left

    public static final int SWIPED_LEFT = 1;

//     The card slides out from the right

    public static final int SWIPED_RIGHT = 1 << 2;

}
