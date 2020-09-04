package com.goomeim.ui;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.coomix.app.all.R;

import net.goome.im.chat.GMFileMessageBody;
import net.goome.im.chat.GMMessage;

import java.io.File;

public class GMShowNormalFileActivity extends GMBaseActivity
{
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ease_activity_show_file);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final GMMessage message = getIntent().getParcelableExtra("msg");
        if (!(message.getMsgBody() instanceof GMFileMessageBody))
        {
            Toast.makeText(GMShowNormalFileActivity.this, "Unsupported message body", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final File file = new File(((GMFileMessageBody) message.getMsgBody()).getLocalPath());

        //        message.setMessageStatusCallback(new EMCallBack()
        //        {
        //            @Override
        //            public void onSuccess()
        //            {
        //                runOnUiThread(new Runnable()
        //                {
        //                    public void run()
        //                    {
        //                        FileUtils.openFile(file, GMShowNormalFileActivity.this);
        //                        finish();
        //                    }
        //                });
        //
        //            }
        //
        //            @Override
        //            public void onError(int code, String error)
        //            {
        //                runOnUiThread(new Runnable()
        //                {
        //                    public void run()
        //                    {
        //                        if (file != null && file.exists() && file.isFile())
        //                        {
        //                            file.delete();
        //                        }
        //                        String str4 = getResources().getString(R.string.Failed_to_download_file);
        //                        Toast.makeText(GMShowNormalFileActivity.this, str4 + message, Toast.LENGTH_SHORT)
        // .show();
        //                        finish();
        //                    }
        //                });
        //            }
        //
        //            @Override
        //            public void onProgress(final int progress, String status)
        //            {
        //                runOnUiThread(new Runnable()
        //                {
        //                    public void run()
        //                    {
        //                        progressBar.setProgress(progress);
        //                    }
        //                });
        //            }
        //        });
        //        GMClient.getInstance().chatManager().downloadAttachment(message);
    }
}
