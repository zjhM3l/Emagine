package com.demo.opencv.cardswipelayout;

import androidx.recyclerview.widget.RecyclerView;

public interface OnSwipeListener<T> {

//
//    The card also pulls back when swiped
//
//    @param viewHolder The slide card viewHolder
//    @param ratio The proportion of the sliding progress
//    @param direction  The direction in which the card slides, CardConfig.SWIPING_LEFT is to slide left, CardConfig.SWIPING_RIGHT is to slide right,
//                       CardConfig.SWIPING_NONE is neither left nor right
//
    void onSwiping(RecyclerView.ViewHolder viewHolder, float ratio, int direction);

//     Callback when the card slides out completely
//
//     @param viewHolder The viewHolder slides out of the card
//     @param t          The data slides out of the card
//     @param direction  The direction in which the card slides out, CardConfig.SWIPED_LEFT is the left slide-out; CardConfig.SWIPED_RIGHT is the right slide out
    void onSwiped(RecyclerView.ViewHolder viewHolder, T t, int direction);

//    Callback when all cards slide out
    void onSwipedClear();

}
