package view.task;

import java.util.ArrayList;

import model.Contact;
import vn.edu.rmit.assignment1.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContactListAdapter extends BaseAdapter {
	private ArrayList<Contact> contacts;
	private Context context;

	public ContactListAdapter(ArrayList<Contact> contacts, Context context) {
		this.contacts = contacts;
		this.context = context;
	}

	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public Object getItem(int position) {
		return contacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.contact_line, parent, false);
		TextView name = (TextView) v.findViewById(R.id.contactLinecontactName);
		TextView phone = (TextView) v
				.findViewById(R.id.contactLinecontactPhone);
		TextView email = (TextView) v
				.findViewById(R.id.contactLinecontactEmail);

		name.setText(contacts.get(position).getName());
		phone.setText("Phone number: " + contacts.get(position).getPhone());
		email.setText("Email: " + contacts.get(position).getEmail());

		v.setClickable(true);

		name.setClickable(false);
		email.setClickable(false);
		phone.setClickable(false);

		v.setOnTouchListener(new OnTouchListener() {
			private float downX;
			private float MIN_DISTANCE = 120;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downX = event.getX();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					float upX = event.getX();
					float distance = upX - downX;

					if (Math.abs(distance) > MIN_DISTANCE) {
						if (distance > 0) {// swipe Left-Right
							contacts.remove(position);
							notifyDataSetInvalidated();
							notifyDataSetChanged();
							return true;
						}
					}
				}
				return false;
			}
		});
		return v;
	}
}
