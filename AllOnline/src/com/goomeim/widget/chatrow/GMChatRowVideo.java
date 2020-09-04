package com.goomeim.widget.chatrow;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.R;
import com.goomeim.model.GMImageCache;
import com.goomeim.utils.GMCommonUtils;

import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMMessage;
import net.goome.im.chat.GMVideoMessageBody;
import net.goome.im.util.ImageUtils;
import net.goome.im.util.TextFormater;

import java.io.File;

public class GMChatRowVideo extends GMChatRowFile
{

	private ImageView imageView;
	private TextView sizeView;
	private TextView timeLengthView;
	private ImageView playView;

	public GMChatRowVideo(Context context, GMMessage message, int position, BaseAdapter adapter)
	{
		super(context, message, position, adapter);
	}

	@Override
	protected View onInflatView()
	{
		return inflater.inflate(message.getDirection() == GMConstant.MsgDirection.RECEIVE ? R.layout.ease_row_received_video
				: R.layout.ease_row_sent_video, this);
	}

	@Override
	protected void onFindViewById()
	{
		imageView = ((ImageView) mainView.findViewById(R.id.chatting_content_iv));
		sizeView = (TextView) mainView.findViewById(R.id.chatting_size_iv);
		timeLengthView = (TextView) mainView.findViewById(R.id.chatting_length_iv);
		playView = (ImageView) mainView.findViewById(R.id.chatting_status_btn);
		percentageView = (TextView) mainView.findViewById(R.id.percentage);
	}

    @Override
    protected void onSetUpView()
    {
        GMVideoMessageBody videoBody = (GMVideoMessageBody) message.getMsgBody();
        String localThumb = videoBody.getLocalPath();

		if (localThumb != null)
		{
			showVideoThumbView(localThumb, imageView, videoBody.getLocalPath(), message);
		}
//		if (videoBody.getDuration() > 0)
//		{
//			String time = DateUtils.toTime(videoBody.getDuration());
//			timeLengthView.setText(time);
//		}

        if (message.getDirection() == GMConstant.MsgDirection.RECEIVE)
        {
            if (videoBody.getFileLength() > 0)
            {
                String size = TextFormater.getDataSize(videoBody.getFileLength());
                sizeView.setText(size);
            }
        }
        else
        {
            if (videoBody.getLocalPath() != null && new File(videoBody.getLocalPath()).exists())
            {
                String size = TextFormater.getDataSize(new File(videoBody.getLocalPath()).length());
                sizeView.setText(size);
            }
        }

        Log.d(TAG, "video thumbnailStatus:" + videoBody.getDownloadStatus());
        if (message.getDirection() == GMConstant.MsgDirection.RECEIVE)
        {
            //			if (videoBody.getDownloadStatus() == GMConstant.MsgDownloadStatus.DOWNLOADING
            //					|| videoBody.thumbnailDownloadStatus() == GMFileMessageBody.EMDownloadStatus.PENDING)
            //			{
            //				imageView.setImageResource(R.drawable.ease_default_image);
            //				//setMessageReceiveCallback();
            //			}
            //			else
            {
                // System.err.println("!!!! not back receive, show image
                // directly");
                imageView.setImageResource(R.drawable.ease_default_image);
                if (localThumb != null)
                {
                    showVideoThumbView(localThumb, imageView, videoBody.getLocalPath(), message);
                }

            }

            return;
        }
        // handle sending message
        handleSendMessage();
    }

    @Override
    protected void onBubbleClick()
    {
        // EMVideoMessageBody videoBody = (EMVideoMessageBody)
        // message.getMsgBody();
        // EMLog.d(TAG, "video rootView is on click");
        // Intent intent = new Intent(context, GMShowVideoActivity.class);
        // intent.putExtra("localpath", videoBody.getLocalUrl());
        // intent.putExtra("secret", videoBody.getSecret());
        // intent.putExtra("remotepath", videoBody.getRemoteUrl());
        // if (message != null && message.direct() == GMMessage.Direct.RECEIVE
        // && !message.isAcked()
        // && message.getChatType() == ChatType.Chat) {
        // try {
        // EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(),
        // message.getMsgId());
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // activity.startActivity(intent);
        Toast.makeText(context, "该版本暂不支持视频播放，请更新到最新版本!", Toast.LENGTH_SHORT).show();
    }

    /**
     * show video thumbnails
     *
     * @param localThumb   local path for thumbnail
     * @param iv
     * @param thumbnailUrl Url on server for thumbnails
     * @param message
     */
    private void showVideoThumbView(final String localThumb, final ImageView iv, String thumbnailUrl,
                                    final GMMessage message)
    {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = GMImageCache.getInstance().get(localThumb);
        if (bitmap != null)
        {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);

        }
        else
        {
            new AsyncTask<Void, Void, Bitmap>()
            {
                @Override
                protected Bitmap doInBackground(Void... params)
                {
                    if (new File(localThumb).exists())
                    {
                        return ImageUtils.decodeScaleImage(localThumb, 160, 160);
                    }
                    else
                    {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Bitmap result)
                {
                    super.onPostExecute(result);
                    if (result != null)
                    {
                        GMImageCache.getInstance().put(localThumb, result);
                        iv.setImageBitmap(result);

                    }
                    else
                    {
                        if (message.getStatus() == GMConstant.MsgStatus.FAILED)
                        {
                            if (GMCommonUtils.isNetWorkConnected(activity))
                            {
                                //GMClient.getInstance().chatManager().downloadThumbnail(message);
                            }
                        }
                    }
                }
            }.execute();
        }

    }

}
