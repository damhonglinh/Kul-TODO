package view.task;

import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		android.app.TimePickerDialog.OnTimeSetListener {

	private Date time;
	private KulOnTimeChangeListener listener;

	static TimePickerFragment newInstance(Date time) {
		Bundle args = new Bundle();
		args.putSerializable("time", time);
		TimePickerFragment tp = new TimePickerFragment();
		tp.setArguments(args);
		return tp;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		try {
			listener = (KulOnTimeChangeListener) getActivity();
		} catch (ClassCastException e) {
			Log.i("TODO", "Bad KulOnDataDialogListener");
		}

		Bundle args = getArguments();
		this.time = (Date) args.getSerializable("time");
		final Calendar c = Calendar.getInstance();
		c.setTime(time);

		return new TimePickerDialog(getActivity(), this,
				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
	}

	@Override
	public void onTimeSet(TimePicker view, int hour, int min) {
		Calendar c = Calendar.getInstance();
		final int year = c.get(Calendar.YEAR);
		final int mon = c.get(Calendar.MONTH);
		final int day = c.get(Calendar.DAY_OF_MONTH);
		c.setTime(time);
		c.set(year, mon, day, hour, min);

		time = c.getTime();
		listener.onTimeChange(time);
	}

	interface KulOnTimeChangeListener {
		void onTimeChange(Date time);
	}
}
