package com.tong.tvplay;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.GSYBaseADActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer;
import com.shuyu.gsyvideoplayer.video.GSYSampleADVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

/**
 * 带广告播放，支持中间插入广告模式
 */
public class DetailADPlayer2 extends GSYBaseADActivityDetail<GSYSampleADVideoPlayer, GSYADVideoPlayer> {

    private GSYSampleADVideoPlayer normalPlayer;

    private GSYADVideoPlayer adPlayer;

    private String urlAd = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";

    private String urlAd2 = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";

    private String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ad_player2);

        normalPlayer = (GSYSampleADVideoPlayer) findViewById(R.id.detail_player);
        adPlayer = (GSYADVideoPlayer) findViewById(R.id.ad_player);

        // 普通视频播放器
        resolveNormalVideoUI();

        // init adVideo and normalPlayer
        initVideoBuilderMode();

        normalPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });

        normalPlayer.setGSYVideoProgressListener(new GSYVideoProgressListener() {
            private int preSecond = 0;

            @Override
            public void onProgress(int progress, int secProgress, int currentPosition, int duration) {
                //在5秒的时候弹出中间广告
                int currentSecond = currentPosition / 1000;
                if (currentSecond == 5 && currentSecond != preSecond) {
                    normalPlayer.getCurrentPlayer().onVideoPause();
                    getGSYADVideoOptionBuilder().setUrl(urlAd2).build(adPlayer);
                    startAdPlay();
                }
                preSecond = currentSecond;
            }
        });

        normalPlayer.setStartAfterPrepared(false);
        normalPlayer.setReleaseWhenLossAudio(false);
    }

    @Override
    public GSYSampleADVideoPlayer getGSYVideoPlayer() {
        return normalPlayer;
    }

    @Override
    public GSYADVideoPlayer getGSYADVideoPlayer() {
        return adPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //不需要builder的
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        return getCommonBuilder()
                .setUrl(url)
                .setThumbImageView(imageView);
    }

    @Override
    public GSYVideoOptionBuilder getGSYADVideoOptionBuilder() {
        return getCommonBuilder()
                .setUrl(urlAd);
    }

    @Override
    public void clickForFullScreen() {

    }

    /**
     * 需要片头广告
     * 如果返回 false ，setStartAfterPrepared 需要设置为 ture
     */
    @Override
    public boolean isNeedAdOnStart() {
        return true;
    }

    /**
     * 是否启动旋转横屏，true表示启动
     *
     * @return true
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

    @Override
    public void onEnterFullscreen(String url, Object... objects) {
        super.onEnterFullscreen(url, objects);
        //隐藏调全屏对象的返回按键
        GSYVideoPlayer gsyVideoPlayer = (GSYVideoPlayer) objects[1];
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
    }

    /**
     * 公用的视频配置
     */
    private GSYVideoOptionBuilder getCommonBuilder() {
        return new GSYVideoOptionBuilder()
                .setCacheWithPlay(true)
                .setVideoTitle(" ")
                .setFullHideActionBar(true)
                .setFullHideStatusBar(true)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)//打开动画
                .setNeedLockFull(true)
                .setSeekRatio(1);
    }

    private void resolveNormalVideoUI() {
        //增加title
        normalPlayer.getTitleTextView().setVisibility(View.VISIBLE);

        //设置返回键
        normalPlayer.getBackButton().setVisibility(View.VISIBLE);

        //是否可以滑动调整
        normalPlayer.setIsTouchWiget(true);

        //设置返回按键功能
        normalPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}

