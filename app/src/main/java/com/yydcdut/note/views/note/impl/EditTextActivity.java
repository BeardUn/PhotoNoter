package com.yydcdut.note.views.note.impl;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.yydcdut.note.R;
import com.yydcdut.note.presenters.note.impl.EditTextPresenterImpl;
import com.yydcdut.note.utils.AppCompat;
import com.yydcdut.note.utils.Const;
import com.yydcdut.note.utils.Utils;
import com.yydcdut.note.views.BaseActivity;
import com.yydcdut.note.views.note.IEditTextView;
import com.yydcdut.note.widget.CircleProgressBarLayout;
import com.yydcdut.note.widget.HorizontalEditScrollView;
import com.yydcdut.note.widget.KeyBoardResizeFrameLayout;
import com.yydcdut.note.widget.RevealView;
import com.yydcdut.note.widget.VoiceRippleView;
import com.yydcdut.note.widget.fab.FloatingActionsMenu;
import com.yydcdut.note.widget.fab.OnSnackBarActionListener;
import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMDEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yyd on 15-4-8.
 */
public class EditTextActivity extends BaseActivity implements IEditTextView,
        KeyBoardResizeFrameLayout.OnKeyBoardShowListener, FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
    private static final String TAG = EditTextActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.appbar)
    View mAppBarLayout;
    @BindView(R.id.et_edit_title)
    EditText mTitleEdit;
    @BindView(R.id.et_edit_content)
    RxMDEditText mContentEdit;
    @BindView(R.id.layout_fab_edittext)
    FloatingActionsMenu mFabMenuLayout;
    @BindView(R.id.img_ripple_fab)
    VoiceRippleView mVoiceRippleView;
    @BindView(R.id.layout_fab_voice_start)
    View mVoiceFabLayout;
    @BindView(R.id.txt_voice)
    View mVoiceTextView;
    @BindView(R.id.layout_voice)
    View mVoiceLayout;
    @BindView(R.id.layout_progress)
    CircleProgressBarLayout mProgressLayout;
    @BindView(R.id.reveal_fab)
    RevealView mFabRevealView;
    @BindView(R.id.reveal_voice)
    RevealView mVoiceRevealView;
    @BindView(R.id.view_fab_location)
    View mFabPositionView;
    @BindView(R.id.til_edit_title)
    View mTitleTextInputLayout;
    @BindView(R.id.scroll_edit_markdown)
    HorizontalEditScrollView mHorizontalEditScrollView;

    @Inject
    EditTextPresenterImpl mEditTextPresenter;

    @Override
    public boolean setStatusBar() {
        return false;
    }

    @Override
    public int setContentView() {
        return R.layout.activity_edit;
    }

    @Override
    public void initInjector() {
        mActivityComponent.inject(this);
        mIPresenter = mEditTextPresenter;
    }

    @Override
    public void initUiAndListener() {
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        mEditTextPresenter.bindData(bundle.getInt(Const.CATEGORY_ID_4_PHOTNOTES),
                bundle.getInt(Const.PHOTO_POSITION), bundle.getInt(Const.COMPARATOR_FACTORY));
        mEditTextPresenter.attachView(this);
        initToolBar();
        initFloating();
        initOtherUI();
        AppCompat.setElevation(mTitleTextInputLayout, getResources().getDimension(R.dimen.checkbox_photo_padding_right));
        AppCompat.setElevation(mHorizontalEditScrollView, getResources().getDimension(R.dimen.checkbox_photo_padding_right));
    }

    void initOtherUI() {
        ((KeyBoardResizeFrameLayout) findViewById(R.id.layout_root)).setOnKeyboardShowListener(this);
        mFabRevealView.setOnTouchListener((v, event) -> {
            mFabMenuLayout.collapse();
            return true;
        });
        mVoiceLayout.setVisibility(View.INVISIBLE);
    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_check_white_24dp);
        AppCompat.setElevation(mToolbar, getResources().getDimension(R.dimen.ui_elevation));
        mToolbar.setTitle(" ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_text, menu);
        return true;
    }

    private void initFloating() {
        mFabMenuLayout.setOnFloatingActionsMenuUpdateListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mEditTextPresenter.saveText();
                mEditTextPresenter.finishActivity(true);
                break;
            case R.id.menu_markdown:
                mEditTextPresenter.doMarkdownPreview();
                break;
        }
        return true;
    }

    @Override
    public void startActivityAnimation() {
        int actionBarHeight = getActionBarSize();
        int screenHeight = Utils.sScreenHeight;
        int contentEditHeight = screenHeight - actionBarHeight * 2;
        AnimatorSet animation = new AnimatorSet();
        animation.setDuration(Const.DURATION_ACTIVITY);
        animation.playTogether(
                ObjectAnimator.ofFloat(mAppBarLayout, "translationY", -actionBarHeight, 0),
                ObjectAnimator.ofFloat(mTitleTextInputLayout, "translationY", -actionBarHeight * 3, 0),
                ObjectAnimator.ofFloat(mContentEdit, "translationY", contentEditHeight, 0),
                ObjectAnimator.ofFloat(mFabMenuLayout, "translationY", contentEditHeight, 0),
                ObjectAnimator.ofFloat(mHorizontalEditScrollView, "translationY", contentEditHeight, 0)
        );
        animation.start();
    }

    @OnClick(R.id.fab_voice)
    public void clickFabVoice(View v) {
        mFabMenuLayout.collapse();
        revealVoiceAndStart();
    }

    @OnClick(R.id.fab_evernote_update)
    public void clickEvernoteUpdate(View v) {
        mFabMenuLayout.collapse();
        mEditTextPresenter.update2Evernote();
    }

    @OnClick(R.id.fab_voice_stop)
    public void clickVoiceStart(View v) {
        mEditTextPresenter.stopVoice();
    }

    @OnClick(R.id.layout_voice)
    public void clickVoiceLayout(View view) {
        //do nothing
    }

    @Override
    public void onBackPressed() {
        mEditTextPresenter.onBackPressEvent();
    }

    @Override
    public boolean isFabMenuLayoutOpen() {
        return mFabMenuLayout.isExpanded();
    }

    @Override
    public void closeFabMenuLayout() {
        mFabMenuLayout.collapse();
    }

    @Override
    public void setFabMenuLayoutClickable(boolean clickable) {
//        mFabMenuLayout.setMenuClickable(clickable);
    }

    @Override
    public void finishActivityWithAnimation(final boolean saved, final int categoryId, final int position, final int comparator) {
        int actionBarHeight = getActionBarSize();
        int screenHeight = Utils.sScreenHeight;
        int contentEditHeight = screenHeight - actionBarHeight;
        AnimatorSet animation = new AnimatorSet();
        animation.setDuration(Const.DURATION_ACTIVITY);
        animation.playTogether(
                ObjectAnimator.ofFloat(mAppBarLayout, "translationY", 0, -actionBarHeight),
                ObjectAnimator.ofFloat(mTitleTextInputLayout, "translationY", 0, -actionBarHeight * 3),
                ObjectAnimator.ofFloat(mContentEdit, "translationY", 0, contentEditHeight),
                ObjectAnimator.ofFloat(mFabMenuLayout, "translationY", 0, contentEditHeight),
                ObjectAnimator.ofFloat(mHorizontalEditScrollView, "translationY", 0, contentEditHeight)

        );
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!saved) {
                    setResult(RESULT_NOTHING);
                    EditTextActivity.this.finish();
                    overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_no_animation);
                } else {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(Const.PHOTO_POSITION, position);
                    bundle.putInt(Const.CATEGORY_ID_4_PHOTNOTES, categoryId);
                    bundle.putInt(Const.COMPARATOR_FACTORY, comparator);
                    intent.putExtras(bundle);
                    setResult(RESULT_DATA, intent);
                    EditTextActivity.this.finish();
                    overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_no_animation);
                }
            }
        });
        animation.start();
    }

    @Override
    public void clearMarkdownPreview() {
        mContentEdit.clear();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyboardShow() {
        mFabMenuLayout.collapse();
        mFabMenuLayout.setVisibility(View.GONE);
    }

    @Override
    public void onKeyboardHide() {
        mFabMenuLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMenuExpanded() {
        Point p = getLocationInView(mFabRevealView, mFabPositionView);
        mFabRevealView.reveal(p.x, p.y, getResources().getColor(R.color.fab_reveal_black), Const.RADIUS, Const.DURATION, null);
    }

    @Override
    public void onMenuCollapsed() {
        Point p = getLocationInView(mFabRevealView, mFabPositionView);
        mFabRevealView.hide(p.x, p.y, Color.TRANSPARENT, 0, Const.DURATION, null);
    }

    private void revealVoiceAndStart() {
        mVoiceLayout.setVisibility(View.VISIBLE);
        Point p = getLocationInView(mVoiceRevealView, mFabPositionView);
        mVoiceRevealView.reveal(p.x, p.y, getResources().getColor(R.color.bg_background),
                1, Const.DURATION, () -> {
                    mVoiceFabLayout.setVisibility(View.VISIBLE);
                    mVoiceTextView.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_scale_small_2_big);
                    animation.setDuration(300l);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mVoiceRippleView.startAnimation();
                            mEditTextPresenter.startVoice();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    mVoiceFabLayout.startAnimation(animation);
                    mVoiceTextView.setAnimation(AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_alpha_in));
                });
    }

    @Override
    public void hideVoiceAnimation() {
        Animation alphaAnimation = AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_alpha_out);
        mVoiceTextView.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mVoiceRippleView.pauseAnimation();
                mVoiceFabLayout.startAnimation(AnimationUtils.loadAnimation(EditTextActivity.this, R.anim.anim_scale_big_2_small));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mVoiceFabLayout.setVisibility(View.GONE);
                mVoiceTextView.setVisibility(View.GONE);
                Point p = getLocationInView(mVoiceRevealView, mFabPositionView);
                mVoiceRevealView.hide(p.x, p.y, Color.TRANSPARENT, 0, Const.DURATION, () -> {
                    mVoiceLayout.setOnClickListener(null);
                    mVoiceLayout.setVisibility(View.INVISIBLE);
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @Override
    public void setRippleVoice(float volume) {
        mVoiceRippleView.setVoice((volume / 50f) / 1.0f);
    }

    @Override
    public void initEditView(RxMDConfiguration rxMDConfiguration) {
        mHorizontalEditScrollView.setEditTextAndConfig(mContentEdit, rxMDConfiguration);
    }

    @Override
    public String getNoteTitle() {
        return mTitleEdit.getText().toString();
    }

    @Override
    public String getNoteContent() {
        return mContentEdit.getText().toString();
    }

    @Override
    public void setEditNoteTitle(String title) {
        mTitleEdit.setText(title);
    }

    @Override
    public void setNoteContent(String content) {
        mContentEdit.setText(content);
    }

    @Override
    public RxMDEditText getRxMDEditText() {
        return mContentEdit;
    }

    @Override
    public void showProgressBar() {
        mProgressLayout.show();
    }

    @Override
    public void hideProgressBar() {
        mProgressLayout.hide();
    }

    @Override
    public void showSnakeBar(String message) {
        Snackbar.make(mFabMenuLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showSnakeBarWithAction(String message, String action, final OnSnackBarActionListener listener) {
        Snackbar.make(mFabMenuLayout, message, Snackbar.LENGTH_SHORT)
                .setAction(action, v -> {
                    if (listener != null) {
                        listener.onClick();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditTextPresenter.detachView();
        mVoiceRippleView.stopAnimation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mHorizontalEditScrollView.handleResult(requestCode, resultCode, data);
    }
}
