package view.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.Contact;
import model.SMSTemplate;
import model.group.Group;
import model.group.GroupModel;
import model.task.Task;
import model.task.TaskModel;
import view.ConfirmDialogFragment;
import view.task.DatePickerFragment.KulOnDateChangeListener;
import view.task.TimePickerFragment.KulOnTimeChangeListener;
import vn.edu.rmit.assignment1.R;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class TaskView extends Activity implements KulOnDateChangeListener,
		KulOnTimeChangeListener {

	private EditText name;
	private EditText desc;
	private TextView dateText;
	private Spinner groupSpinner;
	private Spinner prioSpinner;
	private CheckBox allday;
	private Date date;
	private GroupModel gModel;
	private TaskModel tModel;
	private TextView timeText;
	private ImageButton addContact;
	private Group[] groups;
	private Task task;
	private ArrayList<Contact> contacts;
	private FragmentManager ft;
	private ConfirmDialogFragment backConfirm;
	private ConfirmDialogFragment sendSMSConfirm;
	private ListView contactList;
	private int initialPrio;// to detect there is change
	private int initialGroup;// to detect there is change
	private boolean needToSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_view);
		gModel = new GroupModel(this);
		tModel = new TaskModel(this);
		needToSave = false;

		ft = getFragmentManager();

		name = (EditText) findViewById(R.id.taskViewName);
		desc = (EditText) findViewById(R.id.taskViewDesc);
		dateText = (TextView) findViewById(R.id.taskViewDate);
		allday = (CheckBox) findViewById(R.id.taskViewAllDay);
		timeText = (TextView) findViewById(R.id.taskViewTime);
		groupSpinner = (Spinner) findViewById(R.id.taskViewGroup);
		contactList = (ListView) findViewById(R.id.taskViewContact);
		addContact = (ImageButton) findViewById(R.id.taskViewAddContact);
		prioSpinner = (Spinner) findViewById(R.id.taskViewPriority);

		String groupId = "";
		Intent intent = getIntent();
		int selectedGroupIndex = 0;
		task = (Task) intent.getSerializableExtra("task");
		if (task == null) {
			groupId = intent.getStringExtra("groupId");
			setTitle(getString(R.string.newTask));
			date = new Date();
			allday.setSelected(false);
			initialGroup = 10000;
			initialPrio = 10000;
			contacts = new ArrayList<Contact>();
		} else {
			setTitle(getString(R.string.editTask));
			date = task.getDate();
			groupId = task.getGroupId();
			name.setText(task.getTitle());
			desc.setText(task.getNote());
			allday.setChecked(task.isAllDay());
			contacts = task.getContacts();
			prioSpinner.setSelection(task.getPriority() - 1);
		}
		toggleAllDay();

		// initialize group spinner
		gModel.openDB();
		groups = gModel.getAllGroups();
		ArrayList<String> listGroupName = new ArrayList<String>(groups.length);
		for (int i = 0; i < groups.length; i++) {

			listGroupName.add(groups[i].getName());
			if (groupId.equals(groups[i].getId())) {
				selectedGroupIndex = i;
			}
		}
		ArrayAdapter<String> groupArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, listGroupName);
		groupArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(groupArrayAdapter);
		groupSpinner.setSelection(selectedGroupIndex);
		// done initialize group spinner

		initialGroup = selectedGroupIndex;
		initialPrio = prioSpinner.getSelectedItemPosition();

		// initialize contact list
		ContactListAdapter contactAdapter = new ContactListAdapter(contacts,
				this);
		contactList.setAdapter(contactAdapter);
		// done initialize contact list
		gModel.closeDB();

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		dateText.setText(sdf.format(date));
		sdf = new SimpleDateFormat("HH:mm");
		timeText.setText(sdf.format(date));

		setListener();
		createDialogFragment();
	}

	@Override
	protected void onResume() {
		super.onResume();
		tModel.openDB();
		gModel.openDB();
	}

	@Override
	protected void onPause() {
		super.onPause();
		tModel.closeDB();
		gModel.closeDB();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tModel.closeDB();
		gModel.closeDB();
	}

	@Override
	protected void onStop() {
		super.onStop();
		tModel.closeDB();
		gModel.closeDB();
	}

	@Override
	public void onBackPressed() {
		if (groupSpinner.getSelectedItemPosition() != initialGroup
				|| prioSpinner.getSelectedItemPosition() != initialPrio) {
			needToSave = true;
		}

		if (needToSave) {
			backConfirm.show(ft, "confirm exit");
		} else {
			super.onBackPressed();
		}
	}

	private void createDialogFragment() {
		backConfirm = ConfirmDialogFragment.newInstance(
				new ConfirmDialogFragment() {
					@Override
					public void confirm() {
						TaskView.super.onBackPressed();
					}

					@Override
					public void cancel() {
					}
				}, getString(R.string.exit),
				getString(R.string.exitNoSaveQuestion));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task_view_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.taskViewAdd) {
			// add/update task
			String title = name.getText().toString();
			String note = desc.getText().toString();
			String groupId = groups[groupSpinner.getSelectedItemPosition()]
					.getId();
			int priority = prioSpinner.getSelectedItemPosition() + 1;
			boolean allDay = allday.isChecked();

			if (task == null) {
				final Task t = tModel.addTask(title, note, groupId, date,
						allDay, contacts, priority);

				sendSMSConfirm = ConfirmDialogFragment.newInstance(
						new ConfirmDialogFragment() {
							@Override
							public void confirm() {
								sendSMS(t);
								finish();
							}

							@Override
							public void cancel() {
								finish();
							}
						}, getString(R.string.sendSMS),
						getString(R.string.sendSMSQuestion));

				sendSMSConfirm.show(getFragmentManager(), "send SMS");

			} else {
				String toSync = Task.UPDATE_TASK;
				tModel.editTask(task.getId(), title, note, groupId, date,
						task.isCompleted(), task.isSelected(), allDay,
						contacts, priority, toSync);
				finish();
			}
		}
		return true;
	}

	private void sendSMS(final Task t) {
		for (int i = 0; i < contacts.size(); i++) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("sms:"));
			intent.setType("vnd.android-dir/mms-sms");
			intent.putExtra("address", contacts.get(i).getPhone());
			Log.i("TODO", new SMSTemplate(t).getSMS());
			intent.putExtra("sms_body", new SMSTemplate(t).getSMS());
			startActivity(intent);
		}
	}

	private void toggleAllDay() {
		if (this.allday.isChecked()) {
			timeText.setVisibility(View.INVISIBLE);
		} else {
			timeText.setVisibility(View.VISIBLE);
		}
	}

	private void setListener() {
		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				needToSave = true;
			}
		};

		name.addTextChangedListener(textWatcher);
		desc.addTextChangedListener(textWatcher);

		dateText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment dpf = DatePickerFragment.newInstance(date);
				dpf.show(getFragmentManager(), "datePicker");
			}
		});

		timeText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerFragment tpf = TimePickerFragment.newInstance(date);
				tpf.show(getFragmentManager(), "timePicker");
			}
		});

		allday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				toggleAllDay();
				needToSave = true;
			}
		});

		addContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent contactPicker = new Intent(Intent.ACTION_PICK,
						Contacts.CONTENT_URI);
				startActivityForResult(contactPicker, 1);
			}
		});
	}

	private void onReceiveContact(Uri uri) {
		String name = "";
		String id = "";
		String phone = "";
		String email = "";
		Cursor cur = getContentResolver().query(uri, null, null, null, null);
		if (cur.moveToFirst()) {
			int hasNumber = Integer
					.parseInt(cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

			name = cur.getString(cur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			id = cur.getString(cur
					.getColumnIndex(ContactsContract.Contacts._ID));

			Cursor mailCursor = getContentResolver().query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = '"
							+ id + "'", null, null);

			if (mailCursor.moveToFirst()) {
				email = mailCursor
						.getString(mailCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				mailCursor.close();
			}

			if (hasNumber > 0) {
				Cursor pCur = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = '" + id + "'", null, null);

				pCur.moveToFirst();
				phone = pCur
						.getString(pCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				pCur.close();
			}
		}
		cur.close();

		Contact contact = new Contact(name, email, phone);
		contacts.add(contact);
		contactList.setAdapter(new ContactListAdapter(contacts, this));
		needToSave = true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				onReceiveContact(data.getData());
			}
		}
	}

	@Override
	public void onDateChange(Date date) {
		this.date = date;
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
		String s = sdf.format(date);
		this.dateText.setText(s);
		needToSave = true;
	}

	@Override
	public void onTimeChange(Date time) {
		this.date = time;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String s = sdf.format(time);
		this.timeText.setText(s);
		needToSave = true;
	}
}
