package view;

import model.SMSTemplate;
import model.task.Task;
import model.task.TaskModel;
import vn.edu.rmit.assignment1.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		String strMessage = "";

		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			for (int i = 0; i < pdus.length; i++) {
				SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) pdus[i]);
				String strMsgBody = smsmsg.getMessageBody().toString();
				strMessage += strMsgBody;
				Log.i("TODO", strMessage);
			}
		}

		TaskModel tModel = new TaskModel(context);
		tModel.openDB();

		SMSTemplate temp = new SMSTemplate(strMessage);
		Task t = temp.getTask();

		Log.i("TODO", "Receive SMS which contents: " + strMessage);
		if (t != null) {
			Log.i("TODO", "Finish decoded task: SMSReceiver:  task.getGId() "
					+ t.getGroupId() + " t.id " + t.getId());
			Toast.makeText(context,
					context.getString(R.string.newTaskReceived),
					Toast.LENGTH_LONG).show();
			tModel.addSharedTask(t);
		}
	}
}
