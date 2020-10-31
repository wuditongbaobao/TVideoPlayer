package com.tong.tvplay;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.squareup.leakcanary.LeakCanary;
import com.tong.tvplay.exosource.GSYExoHttpDataSourceFactory;

import java.io.File;

import tv.danmaku.ijk.media.exo2.ExoMediaSourceInterceptListener;
import tv.danmaku.ijk.media.exo2.ExoSourceManager;

public class MainApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        // LeakCanary.install(this);

        // GSYVideoType.enableMediaCodec();
        // GSYVideoType.enableMediaCodecTexture();

        // 切换内核，默认 IJKPlayer
        // PlayerFactory.setPlayManager(Exo2PlayerManager.class);//EXO模式
        // ExoSourceManager.setSkipSSLChain(true);
        // PlayerFactory.setPlayManager(SystemPlayerManager.class);//系统模式
        PlayerFactory.setPlayManager(IjkPlayerManager.class);//ijk模式

        // CacheFactory 更方便自定义，默认 ProxyCacheManager
        // CacheFactory.setCacheManager(ExoPlayerCacheManager.class);//exo缓存模式，支持m3u8，只支持exo
        CacheFactory.setCacheManager(ProxyCacheManager.class);//代理缓存模式，支持所有模式，不支持m3u8等

        // 默认显示比例
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
        // 全屏裁减显示，为了显示正常 CoverImageView 建议使用FrameLayout作为父布局
        // GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        // 全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
        // GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        // 16:9
        // GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
        // 4:3
        // GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
        // 自定义比例
        // GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_CUSTOM);
        // GSYVideoType.setScreenScaleRatio(9.0f/16);

        // 默认TextureView
         GSYVideoType.setRenderType(GSYVideoType.TEXTURE);
        // SurfaceView，动画切换等时候效果比较差
        // GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
        // GLSurfaceView、支持滤镜
        // GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);

        // IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

        // 增加 ExoMediaSourceInterceptListener，方便 Exo 模式下使用自定义的 MediaSource。
        ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
                //如果返回 null，就使用默认的
                return null;
            }

            /**
             * 通过自定义的 HttpDataSource ，可以设置自签证书或者忽略证书
             * demo 里的 GSYExoHttpDataSourceFactory 使用的是忽略证书
             * */
            @Override
            public HttpDataSource.BaseFactory getHttpDataSourceFactory(String userAgent, @Nullable TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis, boolean allowCrossProtocolRedirects) {
                //如果返回 null，就使用默认的
                return new GSYExoHttpDataSourceFactory(userAgent, listener,
                        connectTimeoutMillis,
                        readTimeoutMillis, allowCrossProtocolRedirects);
            }
        });

        /*GSYVideoManager.instance().setPlayerInitSuccessListener(new IPlayerInitSuccessListener() {
            ///播放器初始化成果回调
            @Override
            public void onPlayerInitSuccess(IMediaPlayer player, GSYModel model) {
                if (player instanceof IjkExo2MediaPlayer) {
                    ((IjkExo2MediaPlayer) player).setTrackSelector(new DefaultTrackSelector());
                    ((IjkExo2MediaPlayer) player).setLoadControl(new DefaultLoadControl());
                }
            }
        });*/


    }
}
