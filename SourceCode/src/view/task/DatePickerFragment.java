package view.task;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	private Date date;
	private KulOnDateChangeListener listener;

	static DatePickerFragment newInstance(Date date) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("date", date);
		DatePickerFragment fragment = new DatePickerFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		try {
			listener = (KulOnDateChangeListener) getActivity();
		} catch (ClassCastException e) {
			Log.i("TODO", "Bad KulOnDataDialogListener");
		}

		Bundle args = getArguments();
		date = (Date) args.getSerializable("date");
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(year, monthOfYear, dayOfMonth);
		date = cal.getTime();
		listener.onDateChange(date);
	}

	interface KulOnDateChangeListener {
		void onDateChange(Date date);
	}
}
