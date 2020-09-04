package com.goomeim.widget.chatrow;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coomix.app.all.R;

import net.goome.im.GMCallBack;
import net.goome.im.chat.GMConstant;
import net.goome.im.chat.GMFileMessageBody;
import net.goome.im.chat.GMMessage;
import net.goome.im.util.TextFormater;

import java.io.File;

public class GMChatRowFile extends GMChatRow
{

    protected TextView fileNameView;
    protected TextView fileSizeView;
    protected TextView fileStateView;

    protected GMCallBack sendfileCallBack;

	protected boolean isNotifyProcessed;
	private GMFileMessageBody fileMessageBody;

	public GMChatRowFile(Context context, GMMessage message, int position, BaseAdapter adapter)
	{
		super(context, message, position, adapter);
	}

	@Override
	protected View onInflatView()
	{
		return inflater.inflate(message.getDirection() == GMConstant.MsgDirection.RECEIVE ? R.layout.ease_row_received_file
				: R.layout.ease_row_sent_file, this);
	}

	@Override
	protected void onFindViewById()
	{
		fileNameView = (TextView) mainView.findViewById(R.id.tv_file_name);
		fileSizeView = (TextView) mainView.findViewById(R.id.tv_file_size);
		fileStateView = (TextView) mainView.findViewById(R.id.tv_file_state);
		percentageView = (TextView) mainView.findViewById(R.id.percentage);
	}

    @Override
    protected void onSetUpView()
    {
        fileMessageBody = (GMFileMessageBody) message.getMsgBody();
        String filePath = fileMessageBody.getLocalPath();
        fileNameView.setText(fileMessageBody.getDisplayName());
        fileSizeView.setText(TextFormater.getDataSize(fileMessageBody.getFileLength()));
        if (message.getDirection() == GMConstant.MsgDirection.RECEIVE)
        {
            File file = new File(filePath);
            if (file != null && file.exists())
            {
                fileStateView.setText(R.string.Have_downloaded);
            }
            else
            {
                fileStateView.setText(R.string.Did_not_download);
            }
            return;
        }

        // until here, to sending message
        handleSendMessage();
    }

	/**
	 * handle sending message
	 */
	protected void handleSendMessage()
	{
		//setMessageSendCallback();
		switch (message.getStatus())
		{
			case SUCCESSED:
				if(progressBar != null)
				{
					progressBar.setVisibility(View.INVISIBLE);
				}
				if(statusView != null)
				{
					statusView.setVisibility(View.INVISIBLE);
				}
				if (percentageView != null)
				{
					percentageView.setVisibility(View.INVISIBLE);
				}
				break;

			case FAILED:
				if(progressBar != null)
				{
					progressBar.setVisibility(View.INVISIBLE);
				}
				if(statusView != null)
				{
					statusView.setVisibility(View.VISIBLE);
				}
				if (percentageView != null)
				{
					percentageView.setVisibility(View.INVISIBLE);
				}
				break;

			case DELIVERING://发送中
			case PENDING://未发送
				if(progressBar != null)
				{
					progressBar.setVisibility(View.VISIBLE);
				}
				if(statusView != null)
				{
					statusView.setVisibility(View.INVISIBLE);
				}
				if (percentageView != null)
				{
					percentageView.setVisibility(View.INVISIBLE);
					//percentageView.setVisibility(View.VISIBLE);
					//percentageView.setText((GMAFileMessageBody)message.getMsgBody().getProgress() + "%");
				}
				break;

			default:
				if(progressBar != null)
				{
					progressBar.setVisibility(View.INVISIBLE);
				}
				if(statusView != null)
				{
					statusView.setVisibility(View.VISIBLE);
				}
				if (percentageView != null)
				{
					percentageView.setVisibility(View.INVISIBLE);
				}
				break;
		}
	}

    @Override
    protected void onUpdateView()
    {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick()
    {
        // String filePath = fileMessageBody.getLocalUrl();
        // File file = new File(filePath);
        // if (file != null && file.exists()) {
        // // open files if it exist
        // FileUtils.openFile(file, (Activity) context);
        // } else {
        // // download the file
        // context.startActivity(new Intent(context,
        // GMShowNormalFileActivity.class).putExtra("msgbody",
        // message.getMsgBody()));
        // }
        // if (message.direct() == EMMessage.Direct.RECEIVE &&
        // !message.isAcked() && message.getChatType() == ChatType.Chat) {
        // try {
        // EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(),
        // message.getMsgId());
        // } catch (HyphenateException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        Toast.makeText(context, "该版本暂不支持查看文件，请更新到最新版本!", Toast.LENGTH_SHORT).show();
    }
}
