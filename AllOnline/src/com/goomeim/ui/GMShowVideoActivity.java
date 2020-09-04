package com.goomeim.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.coomix.app.all.R;
import java.io.File;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMVideoMessageBody;

/**
 * show the video
 */
public class GMShowVideoActivity extends GMBaseActivity
{
    private static final String TAG = "ShowVideoActivity";

    private RelativeLayout loadingLayout;
    private ProgressBar progressBar;
    private String localFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ease_showvideo_activity);
        loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final GMMessage message = getIntent().getParcelableExtra("msg");
        if (!(message.getMsgBody() instanceof GMVideoMessageBody))
        {
            Toast.makeText(GMShowVideoActivity.this, "Unsupported message body", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        GMVideoMessageBody messageBody = (GMVideoMessageBody) message.getMsgBody();

        localFilePath = messageBody.getLocalPath();

        if (localFilePath != null && new File(localFilePath).exists())
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(localFilePath)), "video/mp4");
            startActivity(intent);
            finish();
        }
        else
        {
            Log.d(TAG, "download remote video file");
            downloadVideo(message);
        }
    }

    /**
     * show local video
     *
     * @param localPath -- local path of the video file
     */
    private void showLocalVideo(String localPath)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(localPath)), "video/mp4");
        startActivity(intent);
        finish();
    }

    /**
     * download video file
     */
    private void downloadVideo(GMMessage message)
    {
        //        message.setMessageStatusCallback(new EMCallBack()
        //        {
        //            @Override
        //            public void onSuccess()
        //            {
        //                runOnUiThread(new Runnable()
        //                {
        //
        //                    @Override
        //                    public void run()
        //                    {
        //                        loadingLayout.setVisibility(View.GONE);
        //                        progressBar.setProgress(0);
        //                        showLocalVideo(localFilePath);
        //                    }
        //                });
        //            }
        //
        //            @Override
        //            public void onProgress(final int progress, String status)
        //            {
        //                Log.d("ease", "video progress:" + progress);
        //                runOnUiThread(new Runnable()
        //                {
        //
        //                    @Override
        //                    public void run()
        //                    {
        //                        progressBar.setProgress(progress);
        //                    }
        //                });
        //
        //            }
        //
        //            @Override
        //            public void onError(int error, String msg)
        //            {
        //                Log.e("###", "offline file transfer error:" + msg);
        //                File file = new File(localFilePath);
        //                if (file.exists())
        //                {
        //                    file.delete();
        //                }
        //            }
        //        });
        //        GMClient.getInstance().chatManager().downloadAttachment(message);
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

}
