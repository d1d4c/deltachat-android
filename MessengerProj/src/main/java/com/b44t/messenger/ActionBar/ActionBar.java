/*******************************************************************************
 *
 *                              Delta Chat Android
 *                        (C) 2013-2016 Nikolai Kudashov
 *                           (C) 2017 Björn Petersen
 *                    Contact: r10s@b44t.com, http://b44t.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see http://www.gnu.org/licenses/ .
 *
 ******************************************************************************/


package com.b44t.messenger.ActionBar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.b44t.messenger.AndroidUtilities;
import com.b44t.messenger.AnimatorListenerAdapterProxy;
import com.b44t.messenger.ApplicationLoader;
import com.b44t.messenger.Components.LayoutHelper;

import java.util.ArrayList;

public class ActionBar extends FrameLayout {

    public static class ActionBarMenuOnItemClick {
        public void onItemClick(int id) {

        }

        public boolean canOpenMenu() {
            return true;
        }
    }

    public static final int TITLE_WITHOUT_BACK_X  = 16; /*original value: 18*/
    public static final int TITLE_AFTER_BACK_X    = 58; /*original value: 72*/
    public static final int AVATAR_AFTER_BACK_X   = 48; /*original value: 56, you have also to adapt the animation, if you change this value (see ProfileActivity.ANIM_OFF) */

    private ImageView backButtonImageView;
    private SimpleTextView titleTextView;
    private SimpleTextView subtitleTextView;
    //private View actionModeTop;
    private ActionBarMenu menu;
    private ActionBarMenu actionMode;
    private boolean occupyStatusBar = Build.VERSION.SDK_INT >= 21;
    private boolean actionModeVisible;
    private boolean addToContainer = true;
    private boolean interceptTouches = true;
    private AnimatorSet actionModeAnimation;

    private CharSequence lastTitle;
    private boolean castShadows = true;

    protected boolean isSearchFieldVisible;
    protected int itemsBackgroundColor;
    private boolean isBackOverlayVisible;
    protected BaseFragment parentFragment;
    public ActionBarMenuOnItemClick actionBarMenuOnItemClick;

    public ActionBar(Context context) {
        super(context);
    }

    private void createBackButtonImage() {
        if (backButtonImageView != null) {
            return;
        }
        backButtonImageView = new ImageView(getContext());
        backButtonImageView.setScaleType(ImageView.ScaleType.CENTER);
        backButtonImageView.setBackgroundDrawable(Theme.createBarSelectorDrawable(itemsBackgroundColor));
        backButtonImageView.setPadding(AndroidUtilities.dp(1), 0, 0, 0);
        addView(backButtonImageView, LayoutHelper.createFrame(54, 54, Gravity.START | Gravity.TOP));

        backButtonImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearchFieldVisible) {
                    closeSearchField();
                    return;
                }
                if (actionBarMenuOnItemClick != null) {
                    actionBarMenuOnItemClick.onItemClick(-1);
                }
            }
        });
    }

    public void setBackButtonDrawable(Drawable drawable) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        backButtonImageView.setVisibility(drawable == null ? GONE : VISIBLE);
        backButtonImageView.setImageDrawable(drawable);
        if (drawable instanceof BackDrawable) {
            ((BackDrawable) drawable).setRotation(isActionModeShowed() ? 1 : 0, false);
        }
    }

    public void setBackButtonImage(int resource) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        backButtonImageView.setVisibility(resource == 0 ? GONE : VISIBLE);
        backButtonImageView.setImageResource(resource);
    }

    private void createsubtitleTextView() {
        if (subtitleTextView != null) {
            return;
        }
        subtitleTextView = new SimpleTextView(getContext());
        subtitleTextView.setGravity(Gravity.START);
        subtitleTextView.setTextColor(Theme.ACTION_BAR_SUBTITLE_COLOR);
        subtitleTextView.setTextSize(Theme.ACTION_BAR_SUBTITLE_TEXT_SIZE);
        addView(subtitleTextView, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.TOP));
    }

    public void setAddToContainer(boolean value) {
        addToContainer = value;
    }

    public boolean getAddToContainer() {
        return addToContainer;
    }

    public void setSubtitle(CharSequence value) {
        if (value != null && subtitleTextView == null) {
            createsubtitleTextView();
        }
        if (subtitleTextView != null) {
            subtitleTextView.setVisibility(value != null && !isSearchFieldVisible ? VISIBLE : INVISIBLE);
            subtitleTextView.setText(value);
        }
    }

    private void createTitleTextView() {
        if (titleTextView != null) {
            return;
        }
        titleTextView = new SimpleTextView(getContext());
        titleTextView.setGravity(Gravity.START);
        titleTextView.setTextColor(0xffffffff);
        addView(titleTextView, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.TOP));
    }

    public void setTitle(CharSequence value) {
        if (value != null && titleTextView == null) {
            createTitleTextView();
        }
        if (titleTextView != null) {
            lastTitle = value;
            titleTextView.setVisibility(value != null && !isSearchFieldVisible ? VISIBLE : INVISIBLE);
            titleTextView.setText(value);
        }
    }

    public SimpleTextView getSubtitleTextView() {
        return subtitleTextView;
    }

    public SimpleTextView getTitleTextView() {
        return titleTextView;
    }

    public String getTitle() {
        if (titleTextView == null) {
            return null;
        }
        return titleTextView.getText().toString();
    }

    public ActionBarMenu createMenu() {
        if (menu != null) {
            return menu;
        }
        menu = new ActionBarMenu(getContext(), this);
        addView(menu, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.END));
        return menu;
    }

    public void setActionBarMenuOnItemClick(ActionBarMenuOnItemClick listener) {
        actionBarMenuOnItemClick = listener;
    }

    public ActionBarMenu createActionMode() {
        if (actionMode != null) {
            return actionMode;
        }
        actionMode = new ActionBarMenu(getContext(), this);
        actionMode.setBackgroundColor(0xfff8f8f8);
        addView(actionMode, indexOfChild(backButtonImageView));
        actionMode.setPadding(0, occupyStatusBar ? AndroidUtilities.statusBarHeight : 0, 0, 0);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)actionMode.getLayoutParams();
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.gravity = Gravity.END;
        actionMode.setLayoutParams(layoutParams);
        actionMode.setVisibility(INVISIBLE);

        /*
        if (occupyStatusBar && actionModeTop == null) {
            actionModeTop = new View(getContext());
            actionModeTop.setBackgroundColor(0x99000000);
            addView(actionModeTop);
            layoutParams = (FrameLayout.LayoutParams)actionModeTop.getLayoutParams();
            layoutParams.height = AndroidUtilities.statusBarHeight;
            layoutParams.width = LayoutHelper.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP | Gravity.START;
            actionModeTop.setLayoutParams(layoutParams);
            actionModeTop.setVisibility(INVISIBLE);
        }
        */

        return actionMode;
    }

    public void showActionMode() {
        if (actionMode == null || actionModeVisible) {
            return;
        }
        actionModeVisible = true;
        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofFloat(actionMode, "alpha", 0.0f, 1.0f));
        /*if (occupyStatusBar && actionModeTop != null) {
            animators.add(ObjectAnimator.ofFloat(actionModeTop, "alpha", 0.0f, 1.0f));
        }*/
        if (actionModeAnimation != null) {
            actionModeAnimation.cancel();
        }
        actionModeAnimation = new AnimatorSet();
        actionModeAnimation.playTogether(animators);
        actionModeAnimation.setDuration(200);
        actionModeAnimation.addListener(new AnimatorListenerAdapterProxy() {
            @Override
            public void onAnimationStart(Animator animation) {
                actionMode.setVisibility(VISIBLE);
                /*if (occupyStatusBar && actionModeTop != null) {
                    actionModeTop.setVisibility(VISIBLE);
                }*/
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                    actionModeAnimation = null;
                    if (titleTextView != null) {
                        titleTextView.setVisibility(INVISIBLE);
                    }
                    if (subtitleTextView != null) {
                        subtitleTextView.setVisibility(INVISIBLE);
                    }
                    if (menu != null) {
                        menu.setVisibility(INVISIBLE);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                    actionModeAnimation = null;
                }
            }
        });
        actionModeAnimation.start();
        if (backButtonImageView != null) {
            Drawable drawable = backButtonImageView.getDrawable();
            if (drawable instanceof BackDrawable) {
                ((BackDrawable) drawable).setRotation(1, true);
            }
            backButtonImageView.setBackgroundDrawable(Theme.createBarSelectorDrawable(Theme.ACTION_BAR_MODE_SELECTOR_COLOR));
        }
    }

    public void hideActionMode() {
        if (actionMode == null || !actionModeVisible) {
            return;
        }
        actionModeVisible = false;
        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofFloat(actionMode, "alpha", 0.0f));
        /*if (occupyStatusBar && actionModeTop != null) {
            animators.add(ObjectAnimator.ofFloat(actionModeTop, "alpha", 0.0f));
        }*/
        if (actionModeAnimation != null) {
            actionModeAnimation.cancel();
        }
        actionModeAnimation = new AnimatorSet();
        actionModeAnimation.playTogether(animators);
        actionModeAnimation.setDuration(200);
        actionModeAnimation.addListener(new AnimatorListenerAdapterProxy() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                    actionModeAnimation = null;
                    actionMode.setVisibility(INVISIBLE);
                    /*if (occupyStatusBar && actionModeTop != null) {
                        actionModeTop.setVisibility(INVISIBLE);
                    }*/
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (actionModeAnimation != null && actionModeAnimation.equals(animation)) {
                    actionModeAnimation = null;
                }
            }
        });
        actionModeAnimation.start();
        if (titleTextView != null) {
            titleTextView.setVisibility(VISIBLE);
        }
        if (subtitleTextView != null) {
            subtitleTextView.setVisibility(VISIBLE);
        }
        if (menu != null) {
            menu.setVisibility(VISIBLE);
        }
        if (backButtonImageView != null) {
            Drawable drawable = backButtonImageView.getDrawable();
            if (drawable instanceof BackDrawable) {
                ((BackDrawable) drawable).setRotation(0, true);
            }
            backButtonImageView.setBackgroundDrawable(Theme.createBarSelectorDrawable(itemsBackgroundColor));
        }
    }

    /*
    public void showActionModeTop() {
        if (occupyStatusBar && actionModeTop == null) {
            actionModeTop = new View(getContext());
            actionModeTop.setBackgroundColor(0x99000000);
            addView(actionModeTop);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) actionModeTop.getLayoutParams();
            layoutParams.height = AndroidUtilities.statusBarHeight;
            layoutParams.width = LayoutHelper.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP | Gravity.START;
            actionModeTop.setLayoutParams(layoutParams);
        }
    }
    */

    public boolean isActionModeShowed() {
        return actionMode != null && actionModeVisible;
    }

    protected void onSearchFieldVisibilityChanged(boolean visible) {
        isSearchFieldVisible = visible;
        if (titleTextView != null) {
            titleTextView.setVisibility(visible ? INVISIBLE : VISIBLE);
        }
        if (subtitleTextView != null) {
            subtitleTextView.setVisibility(visible ? INVISIBLE : VISIBLE);
        }
    }

    public void setInterceptTouches(boolean value) {
        interceptTouches = value;
    }

    public void closeSearchField() {
        if (!isSearchFieldVisible || menu == null) {
            return;
        }
        menu.closeSearchField();
    }

    public void openSearchField(String text) {
        if (menu == null || text == null) {
            return;
        }
        menu.openSearchField(!isSearchFieldVisible, text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int actionBarHeight = getCurrentActionBarHeight();
        int actionBarHeightSpec = MeasureSpec.makeMeasureSpec(actionBarHeight, MeasureSpec.EXACTLY);

        setMeasuredDimension(width, actionBarHeight + (occupyStatusBar ? AndroidUtilities.statusBarHeight : 0));

        int textLeft;
        if (backButtonImageView != null && backButtonImageView.getVisibility() != GONE) {
            backButtonImageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54), MeasureSpec.EXACTLY), actionBarHeightSpec);
            textLeft = AndroidUtilities.dp(TITLE_AFTER_BACK_X);
        } else {
            textLeft = AndroidUtilities.dp(TITLE_WITHOUT_BACK_X);
        }

        if (menu != null && menu.getVisibility() != GONE) {
            int menuWidth;
            if (isSearchFieldVisible) {
                menuWidth = MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(TITLE_AFTER_BACK_X), MeasureSpec.EXACTLY);
            } else {
                menuWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
            }
            menu.measure(menuWidth, actionBarHeightSpec);
        }

        if (titleTextView != null && titleTextView.getVisibility() != GONE || subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
            int availableWidth = width - (menu != null ? menu.getMeasuredWidth() : 0) - AndroidUtilities.dp(16) - textLeft;

            if (titleTextView != null && titleTextView.getVisibility() != GONE) {
                titleTextView.setTextSize(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 18 : 20);
                titleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24), MeasureSpec.AT_MOST));

            }
            if (subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
                subtitleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(20), MeasureSpec.AT_MOST));
            }
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || child == titleTextView || child == subtitleTextView || child == menu || child == backButtonImageView) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY), 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int additionalTop = occupyStatusBar ? AndroidUtilities.statusBarHeight : 0;

        int textLeft;
        if (backButtonImageView != null && backButtonImageView.getVisibility() != GONE) {
            backButtonImageView.layout(0, additionalTop, backButtonImageView.getMeasuredWidth(), additionalTop + backButtonImageView.getMeasuredHeight());
            textLeft = AndroidUtilities.dp(TITLE_AFTER_BACK_X);
        } else {
            textLeft = AndroidUtilities.dp(TITLE_WITHOUT_BACK_X);
        }

        if (menu != null && menu.getVisibility() != GONE) {
            int menuLeft = isSearchFieldVisible ? AndroidUtilities.dp(TITLE_AFTER_BACK_X) : (right - left) - menu.getMeasuredWidth();
            menu.layout(menuLeft, additionalTop, menuLeft + menu.getMeasuredWidth(), additionalTop + menu.getMeasuredHeight());
        }

        if (titleTextView != null && titleTextView.getVisibility() != GONE) {
            int textTop;
            if (subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
                textTop = (getCurrentActionBarHeight() / 2 - titleTextView.getTextHeight()) / 2 + AndroidUtilities.dp(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 3);
            } else {
                textTop = (getCurrentActionBarHeight() - titleTextView.getTextHeight()) / 2;
            }
            titleTextView.layout(textLeft, additionalTop + textTop, textLeft + titleTextView.getMeasuredWidth(), additionalTop + textTop + titleTextView.getTextHeight());
        }
        if (subtitleTextView != null && subtitleTextView.getVisibility() != GONE) {
            int textTop = getCurrentActionBarHeight() / 2 + (getCurrentActionBarHeight() / 2 - subtitleTextView.getTextHeight()) / 2 - AndroidUtilities.dp(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 1 : 1);
            subtitleTextView.layout(textLeft, additionalTop + textTop, textLeft + subtitleTextView.getMeasuredWidth(), additionalTop + textTop + subtitleTextView.getTextHeight());
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || child == titleTextView || child == subtitleTextView || child == menu || child == backButtonImageView) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int childLeft;
            int childTop;

            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = Gravity.TOP | Gravity.START;
            }

            final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = (right - left - width) / 2 + lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    childLeft = right - width - lp.rightMargin;
                    break;
                case Gravity.LEFT:
                default:
                    childLeft = lp.leftMargin;
            }

            switch (verticalGravity) {
                case Gravity.TOP:
                    childTop = lp.topMargin;
                    break;
                case Gravity.CENTER_VERTICAL:
                    childTop = (bottom - top - height) / 2 + lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = (bottom - top) - height - lp.bottomMargin;
                    break;
                default:
                    childTop = lp.topMargin;
            }
            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }

    public void onMenuButtonPressed() {
        if (menu != null) {
            menu.onMenuButtonPressed();
        }
    }

    protected void onPause() {
        if (menu != null) {
            menu.hideAllPopupMenus();
        }
    }

    public boolean isSearchFieldVisible() {
        return isSearchFieldVisible;
    }

    public void setOccupyStatusBar(boolean value) {
        occupyStatusBar = value;
        if (actionMode != null) {
            actionMode.setPadding(0, occupyStatusBar ? AndroidUtilities.statusBarHeight : 0, 0, 0);
        }
    }

    public boolean getOccupyStatusBar() {
        return occupyStatusBar;
    }

    public void setItemsBackgroundColor(int color) {
        itemsBackgroundColor = color;
        if (backButtonImageView != null) {
            backButtonImageView.setBackgroundDrawable(Theme.createBarSelectorDrawable(itemsBackgroundColor));
        }
    }

    public void setCastShadows(boolean value) {
        castShadows = value;
    }

    public boolean getCastShadows() {
        return castShadows;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event) || interceptTouches;
    }

    public static int getCurrentActionBarHeight() {
        if (ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return AndroidUtilities.dp(48);
        } else {
            return AndroidUtilities.dp(56);
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}